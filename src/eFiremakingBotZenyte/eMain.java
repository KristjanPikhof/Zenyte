package eFiremakingBotZenyte;

import net.runelite.api.ChatMessageType;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleBank;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.*;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@ScriptManifest(author = "Esmaabi", category = Category.FIREMAKING, description =
        "<br>Most effective firemaking bot on Zenyte! <br><br><b>Features & recommendations:</b><br><br>" +
        "<ul>" +
        "<li>You must start near chosen bank!;</li>" +
        "<li>Supported locations: Falador East, Varrock East, Grand Exchange</li>" +
        "<li>Supported trees: all normal trees from redwood to logs.</li></ul>", discord = "Esmaabi#5752",
        name = "eFiremakingBotZenyte", servers = { "Zenyte" }, version = "3")

public class eMain extends TaskScript implements LoopingScript {

    // Constants - Names
    private static final String eBotName = "eFiremakingBot";
    private static final String ePaintText = "Logs used";
    static String bankName = "Banker";
    static String bankOpen = "Bank";
    static String tinderBox = "Tinderbox";
    private static String playerGameName;

    // Constants - Locations
    private static final SimpleSkills.Skills CHOSEN_SKILL = SimpleSkills.Skills.FIREMAKING;
    private WorldPoint START_TILE;
    private final static int INVENTORY_BAG_WIDGET_ID = 548;
    private final static int INVENTORY_BAG_CHILD_ID = 58;
    private final WorldPoint NEAR_BANK_LOC_VARROCK = new WorldPoint(3254, 3426, 0);
    private final WorldPoint NEAR_BANK_LOC_FALADOR = new WorldPoint(3012, 3360, 0);
    private final WorldPoint NEAR_BANK_LOC_GE = new WorldPoint(3164, 3487, 0);
    private static final WorldPoint[] PATH_FALADOR_EAST = new WorldPoint[] {
            new WorldPoint(3025, 3361, 0),
            new WorldPoint(3025, 3362, 0),
            new WorldPoint(3025, 3363, 0)
    };
    private static final WorldPoint[] PATH_VARROCK_EAST = new WorldPoint[] {
            new WorldPoint(3266, 3428, 0),
            new WorldPoint(3266, 3429, 0),
            new WorldPoint(3266, 3430, 0)
    };
    private static final WorldPoint[] PATH_GRAND_EXCHANGE = new WorldPoint[] {
            new WorldPoint(3177, 3478, 0),
            new WorldPoint(3177, 3477, 0),
            new WorldPoint(3177, 3476, 0)
    };

    // Variables
    public static boolean botStarted = false;
    private int count;
    private int counter;
    private int currentExp;
    private boolean firemakingStarted;
    private static eGui gui;
    public static boolean hidePaint = false;
    public static boolean firstInventory;
    private int locationsTried = 0;
    private long startTime = 0L;
    private long startingSkillExp;
    private long startingSkillLevel;
    private static String status;

    @Override
    public int loopDuration() {
        return 150;
    }

    public enum FiremakingLocations {
        FALADOR_EAST(PATH_FALADOR_EAST),
        VARROCK_EAST(PATH_VARROCK_EAST),
        GRAND_EXCHANGE(PATH_GRAND_EXCHANGE);

        private final WorldPoint[] path;

        FiremakingLocations(WorldPoint[] path) {
            this.path = path;
        }

        public WorldPoint[] getPath() {
            return path;
        }
    }

    private void initializeGUI() {
        gui = new eGui();
        gui.setVisible(true);
        gui.setLocale(ctx.getClient().getCanvas().getLocale());
    }

    //Tasks
    List<Task> tasks = new ArrayList<>();

    @Override
    public boolean prioritizeTasks() {
        return true;
    }

    @Override
    public List<Task> tasks() {
        return tasks;
    }

    @Override
    public void onExecute() {
        tasks.addAll(Arrays.asList());
        adjustViewportForFiremaking();
        initializeGUI();

        ctx.log("-------------------------------------");
        ctx.log("            " + eBotName + "         ");
        ctx.log("-------------------------------------");

        status = "Setting up bot";
        this.startTime = System.currentTimeMillis(); //paint
        this.startingSkillLevel = this.ctx.skills.realLevel(CHOSEN_SKILL);
        this.startingSkillExp = this.ctx.skills.experience(CHOSEN_SKILL);
        currentExp = this.ctx.skills.experience(CHOSEN_SKILL);// for actions counter by xp drop
        count = 0;
        counter = 0;
        firemakingStarted = false;
        botStarted = false;
        hidePaint = false;
        firstInventory = true;

        // Getting FM starting tile from GUI selection
        WorldPoint[] locationPaths = getSelectedLocationPaths(gui);
        START_TILE = locationPaths[0];
    }

    @Override
    public void onProcess() {
        super.onProcess();

        if (botStarted) {

            if (ctx.pathing.energyLevel() > 30 && !ctx.pathing.running()) {
                ctx.pathing.running(true);
            }

            updateExperienceAndCount();

            WorldPoint[] availableLocations = getSelectedLocationPaths(gui);


            if (!firemakingStarted) {
                if (!logsInInventory() || firstInventory) {
                    bankTask();
                }
                adjustViewportForFiremaking();
                findAvailableFireSpot(availableLocations);
                handleFiremakingStart();
            } else {
                if (logsInInventory()) {
                    lightFire();
                } else if (playerIsIdle()) {
                    bankTask();
                }
            }
        }
    }

    //
    private void findAvailableFireSpot(WorldPoint[] locations) {
        SimpleObject fireOngoing = getFireAtLocation(START_TILE);

        while (fireOngoing != null && locationsTried < locations.length) {
            handleFireLightingLoc();
            fireOngoing = getFireAtLocation(START_TILE);
        }

        if (locationsTried >= locations.length && fireOngoing != null) {
            waitForAvailableSpot(locations);
        }
    }

    private SimpleObject getFireAtLocation(WorldPoint location) {
        return ctx.objects.populate()
                .filter(o -> o.getLocation().equals(location))
                .filter("Fire").next();
    }

    private void waitForAvailableSpot(WorldPoint[] locations) {
        ctx.onCondition(() -> {
            for (WorldPoint point : locations) {
                if (getFireAtLocation(point) == null) {
                    START_TILE = point;
                    locationsTried = 0;
                    return true;
                }
            }
            return false;
        });
    }

    private void handleFiremakingStart() {
        SimplePlayer localPlayer = ctx.players.getLocal();
        if (localPlayer.getLocation().equals(START_TILE)) {
            firemakingStarted = true;
        } else {
            moveToStartTile();
        }
    }

    private void moveToStartTile() {
        SimplePlayer localPlayer = ctx.players.getLocal();
        final int DISTANCE_THRESHOLD = 3;
        if (localPlayer.getLocation().distanceTo(START_TILE) > DISTANCE_THRESHOLD) {
            if (ctx.pathing.reachable(START_TILE)) {
                status = "Running to start location";
                ctx.pathing.step(START_TILE);
                ctx.onCondition(() -> true, 250, 10);
            }
        } else {
            status = "Clicking starting tile";
            ctx.pathing.clickSceneTile(START_TILE, false, true);
            ctx.onCondition(() -> localPlayer.getLocation().equals(START_TILE));
        }
    }

    private void adjustViewportForFiremaking() {
        ctx.viewport.angle(180);
        ctx.viewport.pitch(true);
    }

    private void lightFire() {
        SimplePlayer localPlayer = ctx.players.getLocal();
        final int IDLE_ANIMATION = -1;
        if (localPlayer.getAnimation() == IDLE_ANIMATION) {
            SimpleItem tinderbox = ctx.inventory.populate().filter(tinderBox).next();
            SimpleItem woodInventory = ctx.inventory.populate().filter(eGui.getLogsComboBox()).next();

            if (tinderbox != null && woodInventory != null) {
                status = "Burning " + eGui.getLogsComboBox().toLowerCase() + "...";
                clickOnBag();
                tinderbox.click("Use");
                ctx.sleep(50);
                woodInventory.click(0);
                WorldPoint cached = localPlayer.getLocation();
                ctx.onCondition(() -> !localPlayer.getLocation().equals(cached), 250, 8);
            }
        }
    }

    private boolean playerIsIdle() {
        SimplePlayer localPlayer = ctx.players.getLocal();
        final int IDLE_ANIMATION = -1;
        return !logsInInventory() && localPlayer.getAnimation() == IDLE_ANIMATION;
    }

    private void updateExperienceAndCount() {
        int newExp = this.ctx.skills.experience(CHOSEN_SKILL);
        if (currentExp != newExp) {
            count++;
            currentExp = newExp;
        }
    }

    private boolean logsInInventory() {
        return !ctx.inventory.populate().filter(eGui.getLogsComboBox()).isEmpty();
    }

    private void bankTask() {
        if (!ctx.bank.bankOpen()) {
            approachBank();
        } else {
            handleBanking();
        }
    }

    private void approachBank() {
        SimpleNpc banker = ctx.npcs.populate().filter(bankName).nearest().next();
        WorldPoint nearBankTile = nearBankLocation(gui);
        //System.out.println("Near bank tile: " + nearBankTile);

        if (ctx.players.getLocal().getLocation().distanceTo(nearBankTile) > 10) {
            status = "Running to bank";
            ctx.pathing.step(nearBankTile);
            ctx.onCondition(() -> ctx.pathing.inMotion(), 250, 10);
        } else {
            if (banker != null && banker.validateInteractable()) {
                status = "Opening bank";
                banker.click(bankOpen, bankName);
                ctx.onCondition(() -> ctx.bank.bankOpen(), 250, 12);
            }
        }
    }

    private void handleBanking() {
        if (!ctx.bank.bankOpen()) return;
        if (ctx.bank.bankOpen()) {
            status = "Banking";
            ctx.bank.depositAllExcept(tinderBox);
            ctx.sleep(200);
            handleTinderbox();
            handleWoodWithdrawal();
        }
        setStartingTileAndCloseBank();
    }

    private void handleTinderbox() {
        SimpleItem tinderBoxInv = ctx.inventory.populate().filter(tinderBox).next();
        if (tinderBoxInv != null) {
            return;
        }

        SimpleItem tinderBoxBank = ctx.bank.populate().filter(tinderBox).next();

        if (tinderBoxBank == null) {
            status = "No tinderbox in bank";
            ctx.updateStatus("Stopping script");
            ctx.updateStatus("No tinderbox in bank");
            ctx.sleep(10000);
            ctx.stopScript();
        } else {
            withdrawTinderbox();
            clearBankSearch();
        }
    }

    private void withdrawTinderbox() {
        ctx.log("Tinderbox not found in inventory. Withdrawing it.");
        SimpleWidget quantityOne = ctx.widgets.getWidget(12, 29);
        if (quantityOne != null && !quantityOne.isHidden()) {
            quantityOne.click(0);
        }
        ctx.bank.withdraw(tinderBox, SimpleBank.Amount.ONE);
    }

    private void clearBankSearch() {
        SimpleWidget searchButton = ctx.widgets.getWidget(12, 40);
        if (searchButton != null && !searchButton.isHidden()) {
            searchButton.click(0);
        }
    }

    private void handleWoodWithdrawal() {
        SimpleItem woodBank = ctx.bank.populate().filter(eGui.getLogsComboBox()).next();
        if (logsInInventory()) return;
        if (woodBank != null) {
            status = "Found " + eGui.getLogsComboBox() + " in bank";
            ctx.bank.withdraw(eGui.getLogsComboBox(), SimpleBank.Amount.ALL);
            ctx.onCondition(this::logsInInventory, 250, 10);
        } else {
            status = "Out of " + eGui.getLogsComboBox().toLowerCase();
            ctx.updateStatus("Stopping script");
            ctx.updateStatus("Out of " + eGui.getLogsComboBox().toLowerCase());
            ctx.sleep(10000);
            ctx.stopScript();
        }
    }

    private void setStartingTileAndCloseBank() {
        WorldPoint[] locationPaths = getSelectedLocationPaths(gui);
        status = "Closing bank";
        ctx.bank.closeBank();
        START_TILE = locationPaths[0];
        firstInventory = false;
        firemakingStarted = false;
    }

    public WorldPoint[] getSelectedLocationPaths(eGui gui) {
        String selectedItem = (String) gui.getLocationComboBox().getSelectedItem();
        Map<String, FiremakingLocations> locations = new HashMap<>();
        locations.put("Falador East", FiremakingLocations.FALADOR_EAST);
        locations.put("Varrock East", FiremakingLocations.VARROCK_EAST);
        locations.put("Grand Exchange", FiremakingLocations.GRAND_EXCHANGE);
        FiremakingLocations locationSelected = locations.get(selectedItem);

        return locationSelected.getPath();
    }

    private WorldPoint nearBankLocation(eGui gui) {
        String selectedItem = (String) gui.getLocationComboBox().getSelectedItem();
        Map<String, WorldPoint> locations = new HashMap<>();
        locations.put("Falador East", NEAR_BANK_LOC_FALADOR);
        locations.put("Varrock East", NEAR_BANK_LOC_VARROCK);
        locations.put("Grand Exchange", NEAR_BANK_LOC_GE);
        return locations.get(selectedItem);
    }

    private void handleFireLightingLoc() {
        WorldPoint[] locationPaths = getSelectedLocationPaths(gui);
        if (locationsTried >= locationPaths.length) {
            ctx.log("All paths have been tried. Pausing...");
            return;
        }

        for (int i = 0; i < locationPaths.length; i++) {
            if (START_TILE.equals(locationPaths[i])) {
                ctx.log("Firemaking path used, choosing next.");
                START_TILE = locationPaths[(i + 1) % locationPaths.length];
                locationsTried++;
                break;
            }
        }

        firemakingStarted = false;
    }

    private void clickOnBag() {
        SimpleWidget inventoryBagWidget = ctx.widgets.getWidget(INVENTORY_BAG_WIDGET_ID, INVENTORY_BAG_CHILD_ID);
        if (inventoryBagWidget != null) {
            inventoryBagWidget.click(0);
        }
    }



    public String getPlayerName() {
        if (playerGameName == null) {
            playerGameName = ctx.players.getLocal().getName();
        }
        return playerGameName;
    }

    public static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void drawTileMatrix(Graphics2D g, WorldPoint startTile) {
        for (int i = 0; i < 27; i++) {
            WorldPoint tile = new WorldPoint(startTile.getX() - i, startTile.getY(), startTile.getPlane());
            ctx.paint.drawTileMatrix(g, tile, Color.GREEN);
        }
    }

    @Override
    public void onTerminate() {
        this.count = 0;
        this.counter = 0;
        this.startingSkillLevel = 0L;
        this.startingSkillExp = 0L;
        status = "Stopping bot";
        gui.setVisible(false);
        hidePaint = true;
        ctx.updateStatus("Logs burned: " + count);

        this.ctx.updateStatus("-------------- " + currentTime() + " --------------");
        this.ctx.updateStatus("----------------------");
        this.ctx.updateStatus("Thank You & Good Luck!");
        this.ctx.updateStatus("----------------------");
    }

    @Override
    public void onChatMessage(ChatMessage m) {
        ChatMessageType getType = m.getType();
        net.runelite.api.events.ChatMessage getEvent = m.getChatEvent();
        playerGameName = getPlayerName();
        String message = m.getMessage();

        if (message == null) {
            return;
        }

        if (message.toLowerCase().contains("light a fire here")) {
            counter++;
            if (counter >= 5) {
                firemakingStarted = false;
                counter = 0;
            }
        }

        if (getType == ChatMessageType.PUBLICCHAT) {
            String senderName = getEvent.getName();

            // Remove any text within angle brackets and trim
            senderName = senderName.replaceAll("<[^>]+>", "").trim();

            if (senderName.contains(playerGameName)) {
                ctx.updateStatus(currentTime() + " Someone asked for you");
                ctx.updateStatus(currentTime() + " Stopping script");
                ctx.stopScript();
            }

        }
    }

    @Override
    public void paint(Graphics g) {

        // Drawing a Firemaking path
        if (START_TILE != null) {
            WorldPoint[] tileToDrawFrom = new WorldPoint[]{START_TILE};
            for (WorldPoint startTile : tileToDrawFrom) {
                drawTileMatrix((Graphics2D) g, startTile);
            }
        }

        // Check if mouse is hovering over the paint
        Point mousePos = ctx.mouse.getPoint();
        if (mousePos != null) {
            Rectangle paintRect = new Rectangle(5, 120, 200, 110);
            hidePaint = paintRect.contains(mousePos.getLocation());
        }

        // Get runtime and skill information
        String runTime = ctx.paint.formatTime(System.currentTimeMillis() - startTime);
        long currentSkillLevel = this.ctx.skills.realLevel(CHOSEN_SKILL);
        long currentSkillExp = this.ctx.skills.experience(CHOSEN_SKILL);
        long skillLevelsGained = currentSkillLevel - this.startingSkillLevel;
        long skillExpGained = currentSkillExp - this.startingSkillExp;

        // Calculate experience and actions per hour
        long skillExpPerHour = ctx.paint.valuePerHour((int) skillExpGained, startTime);
        long actionsPerHour = ctx.paint.valuePerHour(count, startTime);

        // Set up colors
        Color philippineRed = new Color(196, 18, 48);
        Color raisinBlack = new Color(35, 31, 32, 127);

        // Draw paint if not hidden
        if (!hidePaint) {
            g.setColor(raisinBlack);
            g.fillRoundRect(5, 120, 200, 110, 20, 20);

            g.setColor(philippineRed);
            g.drawRoundRect(5, 120, 200, 110, 20, 20);

            g.setColor(philippineRed);
            g.drawString(eBotName + " by Esmaabi", 15, 135);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + runTime, 15, 150);
            g.drawString("Skill Level: " + currentSkillLevel + " (+" + skillLevelsGained + "), started at " + this.startingSkillLevel, 15, 165);
            g.drawString("Current Exp: " + currentSkillExp, 15, 180);
            g.drawString("Exp gained: " + skillExpGained + " (" + (skillExpPerHour / 1000L) + "k xp/h)", 15, 195);
            g.drawString(ePaintText + ": " + count + " (" + actionsPerHour + " per/h)", 15, 210);
            g.drawString("Status: " + status, 15, 225);
        }
    }

}
