package eFiremakingBotZenyte;

import eRandomEventSolver.eRandomEventForester;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleBank;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleWidget;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@ScriptManifest(author = "Esmaabi", category = Category.FIREMAKING, description =
        "<br>Most effective firemaking bot on Zenyte! <br><br><b>Features & recommendations:</b><br><br>" +
        "<ul>" +
        "<li>You must start at chosen bank without logs in inventory;</li>" +
        "<li>Supported locations: Falador East, Varrock East, Grand Exchange</li>" +
        "<li>Supported trees: all normal trees from redwood to logs.</li></ul>", discord = "Esmaabi#5752",
        name = "eFiremakingBotZenyte", servers = { "Zenyte" }, version = "2.2")

public class eMain extends TaskScript implements LoopingScript {
    private static eGui gui;
    private long startTime = 0L;
    private long startingSkillLevel;
    private long startingSkillExp;
    private int count;
    private int currentExp;
    static String status = null;
    public static boolean botStarted = false;
    public static boolean hidePaint = false;
    private boolean FMStarted;

    //Items
    public static String woodName;
    static String bankName = "Banker";
    static String bankOpen = "Bank";
    static String tinderBox = "Tinderbox";



    //Locations
    private WorldPoint START_TILE;
    private final WorldPoint NEAR_BANK_LOC_VARROCK = new WorldPoint(3254, 3426, 0);
    private final WorldPoint NEAR_BANK_LOC_FALADOR = new WorldPoint(3012, 3360, 0);
    private final WorldPoint NEAR_BANK_LOC_GE = new WorldPoint(3164, 3487, 0);

    //Paths
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
        tasks.addAll(Arrays.asList(new eRandomEventForester(ctx)));
        System.out.println("Started eFiremakingBot!");
        initializeGUI();

        status = "Setting up bot";
        this.startTime = System.currentTimeMillis(); //paint
        this.startingSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.FIREMAKING);
        this.startingSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING);
        currentExp = this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING);// for actions counter by xp drop
        count = 0;
        FMStarted = false;
        botStarted = false;
        hidePaint = false;

        //Getting FM starting tile from GUI selection
        WorldPoint[] locationPaths = getSelectedLocationPaths(gui);
        START_TILE = locationPaths[0];
    }

    @Override
    public void onProcess() {
        super.onProcess();

        if (botStarted) {

            updateExperienceAndCount();

            if (!FMStarted) {
                if (logsInInventory()) {
                    if (!ctx.players.getLocal().getLocation().equals(START_TILE)) {
                        status = "Running to start location";
                        ctx.pathing.step(START_TILE);
                        ctx.sleepCondition(() -> ctx.players.getLocal().getLocation().equals(START_TILE), 2500);
                    } else {
                        FMStarted = true;
                        System.out.println("Starting FM task?: " + FMStarted);
                    }
                } else {
                    bankTask();
                }
            } else {
                ctx.viewport.angle(180);
                ctx.viewport.pitch(true);

                if (logsInInventory()) {
                    if (ctx.players.getLocal().getAnimation() == -1) {
                        SimpleItem tinderbox = ctx.inventory.populate().filter(tinderBox).next();
                        SimpleItem woodInventory = ctx.inventory.populate().filter(woodName).next();

                        if (tinderbox != null && woodInventory != null) {
                            status = "Burning " + woodName.toLowerCase() + "...";
                            tinderbox.click("Use");
                            ctx.sleep(100);
                            woodInventory.click(0);
                            WorldPoint cached = ctx.players.getLocal().getLocation();
                            ctx.sleepCondition(() -> ctx.players.getLocal().getLocation() != cached, 5000);
                        }
                    }

                } else if (!logsInInventory() && !ctx.players.getLocal().isAnimating()) {
                    bankTask();
                }
            }
        }
    }

    private void updateExperienceAndCount() {
        int newExp = this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING);
        if (currentExp != newExp) {
            count++;
            currentExp = newExp;
        }
    }

    private boolean logsInInventory() {
        return !ctx.inventory.populate().filter(woodName).isEmpty();
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
        System.out.println("Near bank tile: " + nearBankTile);

        if (ctx.players.getLocal().getLocation().distanceTo(nearBankTile) > 10) {
            status = "Running to bank";
            ctx.pathing.step(nearBankTile);
            ctx.sleepCondition(() -> !ctx.pathing.inMotion(), 1200);
        } else {
            if (banker != null && banker.validateInteractable()) {
                status = "Opening bank";
                banker.click(bankOpen, bankName);
                ctx.sleepCondition(() -> ctx.bank.bankOpen(), 3000);
            }
        }
    }

    private void handleBanking() {
        if (!logsInInventory()) {
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
        System.out.println("Tinderbox not found in inventory. Withdrawing it.");
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
        SimpleItem woodBank = ctx.bank.populate().filter(woodName).next();
        if (woodBank != null) {
            ctx.bank.withdraw(woodName, SimpleBank.Amount.ALL);
            ctx.onCondition(this::logsInInventory, 250, 10);
        } else {
            status = "Out of " + woodName.toLowerCase();
            ctx.updateStatus("Stopping script");
            ctx.updateStatus("Out of " + woodName.toLowerCase());
            ctx.sleep(10000);
            ctx.stopScript();
        }
    }

    private void setStartingTileAndCloseBank() {
        WorldPoint[] locationPaths = getSelectedLocationPaths(gui);
        status = "Closing bank";
        ctx.bank.closeBank();
        START_TILE = locationPaths[0];
        FMStarted = false;
        System.out.println("Bank closed");
        System.out.println("Starting FM task?: " + FMStarted);
        System.out.println("START_TILE has been set to: " + START_TILE);
        ctx.viewport.angle(270);
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
        String startingFmTask = "Starting FM task?: " + FMStarted;
        WorldPoint[] locationPaths = getSelectedLocationPaths(gui);

        for (int i = 0; i < locationPaths.length; i++) {
            if (START_TILE.equals(locationPaths[i])) {
                System.out.println("Can't light a fire here: " + START_TILE);
                START_TILE = locationPaths[(i + 1) % locationPaths.length];
                break;
            }
        }

        FMStarted = false;
        System.out.println(startingFmTask);
        System.out.println("Changing START_TILE to: " + START_TILE);
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
        if (m.getMessage() == null) {
            return;
        }

        String message = m.getMessage().toLowerCase();
        String playerName = ctx.players.getLocal().getName().toLowerCase();

        if (message.contains(playerName)) {
            ctx.updateStatus(currentTime() + " Someone asked for you");
            ctx.updateStatus(currentTime() + " Stopping script");
            ctx.stopScript();
        } else if (message.contains("light a fire here")) {
            handleFireLightingLoc();
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
        long runTime = System.currentTimeMillis() - this.startTime;
        long currentSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.FIREMAKING);
        long currentSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING);
        long skillLevelsGained = currentSkillLevel - this.startingSkillLevel;
        long skillExpGained = currentSkillExp - this.startingSkillExp;

        // Calculate experience and actions per hour
        long skillExpPerHour = skillExpGained * 3600000L / runTime;
        long actionsPerHour = count * 3600000L / (System.currentTimeMillis() - this.startTime);

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
            g.drawString("eFiremakingBot by Esmaabi", 15, 135);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + formatTime(runTime), 15, 150);
            g.drawString("Skill Level: " + this.startingSkillLevel + " (+" + skillLevelsGained + "), started at " + currentSkillLevel, 15, 165);
            g.drawString("Current Exp: " + currentSkillExp, 15, 180);
            g.drawString("Exp gained: " + skillExpGained + " (" + (skillExpPerHour / 1000L) + "k xp/h)", 15, 195);
            g.drawString("Logs used: " + count + " (" + actionsPerHour + " per/h)", 15, 210);
            g.drawString("Status: " + status, 15, 225);

        }
    }

    private String formatTime(long ms) {
        long s = ms / 1000L;
        long m = s / 60L;
        long h = m / 60L;
        s %= 60L;
        m %= 60L;
        h %= 24L;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

}
