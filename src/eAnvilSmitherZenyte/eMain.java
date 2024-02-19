package eAnvilSmitherZenyte;

import eRandomEventSolver.eRandomEventForester;
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
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimplePlayer;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.Graphics;

@ScriptManifest(
        author = "Esmaabi",
        category = Category.SMITHING,
        description = "<html>"
                + "<p>The most effective anvil smithing bot on Zenyte!</p>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "<ul>"
                + "<li>Start with a <strong>hammer</strong> in your inventory.</li>"
                + "<li>Start <strong>with bars in your inventory</strong>.</li>"
                + "<li>Start at Varrock West Bank or Port Khazard Bank.</li>"
                + "<li>Zoom out to <strong>see both the anvil and the bank</strong>.</li>"
                + "<li>Incorporates random sleep times for a more natural behavior.</li>"
                + "</ul>"
                + "</html>",
        discord = "Esmaabi#5752",
        name = "eAnvilSmitherZenyte",
        servers = {"Zenyte"},
        version = "0.3"
)

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private static final int HAMMER_ID = 2347;
    private static final String[] BANK_NAME = {"Bank booth", "Bank chest"};

    // Coordinates
    private final WorldArea smithingAreaKhazard = new WorldArea(new WorldPoint(2664, 3157, 0), new WorldPoint(1371, 8982, 0));
    private final WorldArea smithingAreaVarrock = new WorldArea(new WorldPoint(3177, 3449, 0), new WorldPoint(3196, 3418, 0));
    private final WorldPoint anvilLocationKhazard = new WorldPoint(2652, 3164, 0);
    private final WorldPoint anvilLocationVarrock = new WorldPoint(3188, 3426, 0);
    private final WorldPoint bankBoothVarrock = new WorldPoint(3186, 3436, 0);
    private final WorldPoint bankChestKhazard = new WorldPoint(2661, 3163, 0);

    // Variables
    private long startTime = 0L;
    private long startingSkillLevel;
    private long startingSkillExp;
    private int count;
    private int smithingWidget;
    private int barsInInv;
    public static String status = null;
    private static String nameOfItem = null;
    public int minBarsRequired;
    private long lastAnimation = -1;
    private boolean botStarted = false;
    private static boolean hidePaint = false;
    private Runnable lastSmithingTask, lastBankingTask = null;
    private static String playerGameName;

    // Tasks
    private final List<Task> tasks = new ArrayList<>();

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

        tasks.addAll(Arrays.asList());// Adds tasks to our {task} list for execution

        // Other vars
        System.out.println("Started eAnvilSmither!");
        this.ctx.updateStatus("--------------- " + currentTime() + " ---------------");
        this.ctx.updateStatus("-------------------------------");
        this.ctx.updateStatus("       eAnvilSmitherZenyte     ");
        this.ctx.updateStatus("-------------------------------");

        // Vars
        updateStatus("Setting up bot");
        this.startTime = System.currentTimeMillis();
        this.startingSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.SMITHING);
        this.startingSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.SMITHING);
        lastAnimation = System.currentTimeMillis();
        botStarted = false;
        minBarsRequired = 0;
        count = 0;
        ctx.viewport.angle(270);
        ctx.viewport.pitch(true);

        // Choosing bars and items to smith
        barsInInv = getItemFunction(); //checking which bars to use
        if (barsInInv != -1) {
            eGui.eGuiDialogueTarget();
            String returnItem = eGui.returnItem;
            switch (returnItem) {
                case "Sword":
                    nameOfItem = "swords";
                    updateStatus("Smithing " + nameOfItem);
                    smithingWidget = 3;
                    minBarsRequired = 1;
                    botStarted = true;
                    break;
                case "Platebody":
                    nameOfItem = "platebodies";
                    updateStatus("Smithing " + nameOfItem);
                    smithingWidget = 15;
                    minBarsRequired = 5;
                    botStarted = true;
                    break;
                case "Dart tips":
                    nameOfItem = "dart tips";
                    updateStatus("Smithing " + nameOfItem);
                    smithingWidget = 23;
                    minBarsRequired = 1;
                    botStarted = true;
                    break;
                case "Bolts":
                    nameOfItem = "bolts";
                    updateStatus("Smithing " + nameOfItem);
                    smithingWidget = 28;
                    minBarsRequired = 1;
                    botStarted = true;
                    break;
                default:
                    updateStatus("Waiting for GUI options");
                    botStarted = false;
            }
        }
    }

    @Override
    public void onProcess() {
        super.onProcess();

        if (!botStarted) {
            if (!getHammer()) {
                updateStatus("Hammer not found");
                startBankingTask();
            }
            return;
        }

        if (ctx.pathing.energyLevel() > 30 && !ctx.pathing.running()) {
            ctx.pathing.running(true);
        }

        SimplePlayer localPlayer = ctx.players.getLocal();
        WorldPoint localPlayerLocation = localPlayer.getLocation();
        boolean playerIsAnimating = localPlayer.isAnimating();

        if (smithingAreaVarrock.containsPoint(localPlayerLocation) || smithingAreaKhazard.containsPoint(localPlayerLocation)) {

            int barsInInventoryCount = ctx.inventory.populate().filter(barsInInv).population();

            if (barsInInventoryCount < minBarsRequired) {
                startBankingTask();
            } else {
                if (ctx.bank.bankOpen()) {
                    updateStatus("Closing bank task");
                    ctx.bank.closeBank();
                }

                if (!playerIsAnimating && (System.currentTimeMillis() > (lastAnimation + 6000))) {
                    startingSmithingTask();
                } else if (playerIsAnimating) {
                    lastAnimation = System.currentTimeMillis();
                }
            }
        }
    }

    // Banking
    private void startBankingTask() {
        Runnable bankingTask = getBankingLocation();
        updateStatus("Banking task");
        if (bankingTask != null) {
            bankingTask.run();
        }
    }

    private void openingBank(WorldPoint bankLocation, String bankAction) {
        if (ctx.bank.bankOpen()) {
            updateStatus("Depositing items");
            ctx.bank.depositAllExcept(HAMMER_ID, barsInInv);
            handleHammer();
            updateStatus("Withdrawing bars");
            ctx.bank.withdraw(barsInInv, SimpleBank.Amount.ALL);
            ctx.onCondition(() -> ctx.inventory.populate().filter(barsInInv).population() >= minBarsRequired, 3000);
            updateStatus("Closing bank");
            ctx.bank.closeBank();
            return;
        }

        SimpleObject bankChest = ctx.objects.populate().filter(BANK_NAME).nearest(bankLocation).next();
        if (!ctx.bank.bankOpen() && !ctx.pathing.inMotion()) {
            if (bankChest != null && bankChest.validateInteractable()) {
                int sleepTime = randomSleeping(0, 6000);
                updateStatus("Sleeping to bank (" + sleepTime + "ms)");
                ctx.sleep(sleepTime);
                updateStatus("Refilling supplies");
                bankChest.click(bankAction);
                ctx.onCondition(() -> ctx.bank.bankOpen(), 5000);
            }
        }
    }

    private void openingBankKhazard() {
        openingBank(bankChestKhazard, "Use");
    }

    private void openingBankVarrock() {
        openingBank(bankBoothVarrock, "Bank");
    }

    private Runnable getBankingLocation() {
        if (lastBankingTask != null) {
            return lastBankingTask;
        }

        if (smithingAreaVarrock.containsPoint(ctx.players.getLocal().getLocation())) {
            lastBankingTask = this::openingBankVarrock;
        } else if (smithingAreaKhazard.containsPoint(ctx.players.getLocal().getLocation())) {
            lastBankingTask = this::openingBankKhazard;
        }

        return lastBankingTask;
    }

    private boolean getHammer() {
        SimpleItem hammer = ctx.inventory.populate().filter(HAMMER_ID).next();
        return hammer != null;
    }

    private void handleHammer() {
        boolean hammerInInv = getHammer();
        if (hammerInInv) {
            return;
        }

        SimpleItem hammerInBank = ctx.bank.populate().filter(HAMMER_ID).next();
        if (hammerInBank == null) {
            updateStatus("No hammer in bank");
            ctx.updateStatus("Stopping script");
            ctx.sleep(10000);
            ctx.stopScript();
        } else {
            withdrawHammer();
            clearBankSearch();
        }
    }

    private void withdrawHammer() {
        updateStatus("Withdrawing hammer");
        SimpleWidget quantityOne = ctx.widgets.getWidget(12, 29);
        if (quantityOne != null && !quantityOne.isHidden()) {
            quantityOne.click(0);
        }
        ctx.bank.withdraw(HAMMER_ID, SimpleBank.Amount.ONE);
    }

    private void clearBankSearch() {
        SimpleWidget searchButton = ctx.widgets.getWidget(12, 40);
        if (searchButton != null && !searchButton.isHidden()) {
            searchButton.click(0);
        }
    }

    // Smithing
    private void startingSmithingTask() {
        Runnable smithingTask = getSmithingLocation();
        if (smithingTask != null) {
            smithingTask.run();
        }
    }

    private void smithingTask(WorldPoint anvilLocation) {
        SimpleObject anvil = ctx.objects.populate().filter("Anvil").nearest(anvilLocation).next();
        SimpleWidget smithingItem = ctx.widgets.getWidget(312, smithingWidget);
        boolean widgetScreenVisible = smithingItem != null && !smithingItem.isHidden();

        if (ctx.players.getLocal().isAnimating()) {
            return;
        }

        if (!widgetScreenVisible) {
            if (anvil != null && anvil.validateInteractable() && !ctx.pathing.inMotion()) {
                updateStatus("Clicking anvil");
                anvil.menuAction("Smith");
                ctx.sleepCondition(() -> smithingItem == null, 5000);
            }
        } else {
            updateStatus("Making " + nameOfItem);
            smithingItem.click(5); // menu element 5
            lastAnimation = System.currentTimeMillis();
            ctx.onCondition(smithingItem::isHidden, 250, 10);
            lastAnimation = System.currentTimeMillis();
        }
    }

    private void smithingTaskKhazard() {
        smithingTask(anvilLocationKhazard);
    }

    private void smithingTaskVarrock() {
        smithingTask(anvilLocationVarrock);
    }

    private Runnable getSmithingLocation() {
        if (lastSmithingTask != null) {
            return lastSmithingTask;
        }

        if (smithingAreaVarrock.containsPoint(ctx.players.getLocal().getLocation())) {
            lastSmithingTask = this::smithingTaskVarrock;
        } else if (smithingAreaKhazard.containsPoint(ctx.players.getLocal().getLocation())) {
            lastSmithingTask = this::smithingTaskKhazard;
        }

        return lastSmithingTask;
    }

    //Getting bars ID onExecute
    private static int getItemId(String... itemName) {
        HashSet<String> lowerCaseItemNames = Arrays.stream(itemName)
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(HashSet::new));

        SimpleItem optionalItem = ClientContext.instance().inventory.populate()
                .filter(p -> {
                    String lowerCaseItemName = p.getName().toLowerCase();
                    return lowerCaseItemNames.stream().anyMatch(lowerCaseItemName::contains);
                })
                .next();

        return optionalItem != null ? optionalItem.getId() : -1;
    }

    private int getItemFunction() {
        int barsInventory = getItemId("bar");
        if (barsInventory == -1) {
            updateStatus("No bars in inventory to learn!");
            ctx.updateStatus("Please start with bars in inventory");
            ctx.sleep(5000);
            updateStatus("Stopping script");
            ctx.stopScript();
        } else {
            updateStatus("Bars in inventory found");
        }
        return barsInventory;
    }

    //Utility
    public static int randomSleeping(int minimum, int maximum) {
        return (int)(Math.random() * (maximum - minimum)) + minimum;
    }

    public static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void updateStatus(String newStatus) {
        status = newStatus;
        ctx.updateStatus(status);
        System.out.println(status);
    }

    public String getPlayerName() {
        if (playerGameName == null) {
            playerGameName = ctx.players.getLocal().getName();
        }
        return playerGameName;
    }


    @Override
    public void onTerminate() {

        // Other vars
        this.startingSkillLevel = 0L;
        this.startingSkillExp = 0L;
        this.count = 0;

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
        String message = m.getMessage().toLowerCase();

        if (m.getMessage() == null) {
            return;
        }

        if (m.getMessage() != null) {
            if (message.contains("you hammer the")) {
                count++;
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
    public int loopDuration() {
        return 150;
    }

    @Override
    public void paint(Graphics g) {
        // Check if mouse is hovering over the paint
        Point mousePos = ctx.mouse.getPoint();
        if (mousePos != null) {
            Rectangle paintRect = new Rectangle(5, 120, 200, 110);
            hidePaint = paintRect.contains(mousePos.getLocation());
        }

        // Get runtime and skill information
        long runTime = System.currentTimeMillis() - this.startTime;
        long currentSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.SMITHING);
        long currentSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.SMITHING);
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
            g.drawString("eAnvilSmithingBot by Esmaabi", 15, 135);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + formatTime(runTime), 15, 150);
            g.drawString("Skill Level: " + currentSkillLevel + " (+" + skillLevelsGained + "), started at " + this.startingSkillLevel, 15, 165);
            g.drawString("Current Exp: " + currentSkillExp, 15, 180);
            g.drawString("Exp gained: " + skillExpGained + " (" + (skillExpPerHour / 1000L) + "k xp/h)", 15, 195);
            g.drawString("Items smithed: " + count + " (" + actionsPerHour + " per/h)", 15, 210);
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