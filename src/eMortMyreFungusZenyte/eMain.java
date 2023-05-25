package eMortMyreFungusZenyte;

import eRandomEventSolver.eRandomEventForester;
import net.runelite.api.ChatMessageType;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleBank;
import simple.hooks.filters.SimplePrayers;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Game;
import simple.hooks.simplebot.Pathing;
import simple.hooks.wrappers.*;
import simple.robot.utils.WorldArea;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ScriptManifest(
        author = "Esmaabi",
        category = Category.MONEYMAKING,
        description = "<html>"
                + "<p>The most effective mort myre fungus collector bot on Zenyte!</p>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "<ul>"
                + "<li>Start with a <strong>Silver Sickle (b)</strong> in your inventory.</li>"
                + "<li>Start with <strong>Dramen staff equipped</strong>.</li>"
                + "<li>Set spiritual tree last location to <strong>BKR</strong></li>"
                + "<li>Zoom out to <strong>center</strong> for best performance.</li>"
                + "<li>Bot will collect Mort Myre Fungus for you!</li>"
                + "</ul>"
                + "</html>",
        discord = "Esmaabi#5752",
        name = "eMortMyreFungusZenyte",
        servers = {"Zenyte"},
        version = "0.2"
)

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private static final int SILVER_SICKLE_ID = 2963;
    private static final int FUNGI_ON_LOG_ID = 3509; //action "Pick" "Fungi on log"
    private static final int MORT_MYRE_FUNGUS_ID = 2970;
    private static final int SPIRITUAL_FAIRY_THEE_ID = 35003; //Ring-last-destination
    private static final int BANKER_ID = 10029;
    private static final int BOX_OF_RESTORATION_ID = 35021; // Restore
    private static String playerGameName;


    // Coordinates
    private final WorldArea homeArea = new WorldArea(new WorldPoint(3101, 3181, 0), new WorldPoint(3074, 3504, 0));
    private final WorldArea swampArea = new WorldArea(new WorldPoint(3480,3449, 0), new WorldPoint(3417,3424, 0));
    private final WorldPoint bloomLocation = new WorldPoint(3431, 3433, 0);
    private final WorldPoint bankerLocation = new WorldPoint(3089, 3495, 0);

    private final WorldPoint[] pathToBloomLocation = {
            new WorldPoint(3463, 3432, 0),
            new WorldPoint(3455, 3433, 0),
            new WorldPoint(3447, 3434, 0),
            new WorldPoint(3438, 3435, 0),
            new WorldPoint(3431, 3434, 0)
    };


    // Variables
    private long startTime = 0L;
    private long prayerLevel;
    private int count;

    public static String status = null;
    private static boolean hidePaint = false;
    private boolean inHome;

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

        tasks.addAll(Arrays.asList(new eRandomEventForester(ctx)));// Adds tasks to our {task} list for execution

        // Other vars
        System.out.println("Started eMortMyreFungus!");
        this.ctx.updateStatus("--------------- " + currentTime() + " ---------------");
        this.ctx.updateStatus("-------------------------------");
        this.ctx.updateStatus("         eMortMyreFungus       ");
        this.ctx.updateStatus("-------------------------------");

        // Vars
        updateStatus("Setting up bot");
        this.startTime = System.currentTimeMillis();
        this.prayerLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.PRAYER);
        count = 0;
        ctx.viewport.pitch(true);

    }

    @Override
    public void onProcess() {
        super.onProcess();

        SimplePlayer localPlayer = ctx.players.getLocal();
        Pathing pathing = ctx.pathing;

        if (pathing.energyLevel() > 30 && !pathing.running()) {
            pathing.running(true);
        }

        if (ctx.prayers.points() == 0) {
            if (!ctx.pathing.inArea(homeArea)) {
                teleportToHome();
            } else {
                inHome = true;
            }
        }

        if (pathing.inArea(homeArea) || inHome) {

            if (playerReady()) {
                teleportToSwamp();
            } else {
                if (ctx.prayers.points() < prayerLevel) {
                    restorePrayer();
                }

                if (!playerReady()) {
                    openingBank();
                }
            }
        }

        if (pathing.inArea(swampArea)) {
            inHome = false;

            if (!ctx.inventory.inventoryFull()) {
                if (checkFungiOnLog()) {
                    pickingFungi();
                } else {
                    if (pathing.onTile(bloomLocation)) {
                        ctx.viewport.angle(0);
                        ctx.viewport.pitch(true);
                        castBloom();
                    } else {
                        if (localPlayer.getLocation().distanceTo(bloomLocation) > 5) {
                            status = "Getting to blooming location";
                            pathing.walkPath(pathToBloomLocation);
                        } else {
                            pathing.clickSceneTile(bloomLocation, false, true);
                        }
                    }
                }

                if (!ctx.groundItems.populate().filter(MORT_MYRE_FUNGUS_ID).isEmpty()) {
                    SimpleGroundItem groundFungus = ctx.groundItems.populate().filter(MORT_MYRE_FUNGUS_ID).next();

                    if (groundFungus != null && groundFungus.validateInteractable()) {
                        updateStatus("Picking up fungus");
                        groundFungus.click("Take");
                    }
                }

            } else {
                teleportToHome();
            }
        }
    }

    // Banking
    private void openingBank() {
        if (ctx.bank.bankOpen()) {
            updateStatus("Depositing items");
            int fungiAmount = ctx.inventory.populate().filter(MORT_MYRE_FUNGUS_ID).population();
            count += fungiAmount;
            ctx.bank.depositAllExcept(SILVER_SICKLE_ID);
            handleSickle();
            updateStatus("Closing bank");
            ctx.bank.closeBank();
            return;
        }

        SimpleNpc bankerHome = ctx.npcs.populate().filter(BANKER_ID).nearest(bankerLocation).next();
        if (!ctx.bank.bankOpen() && !ctx.pathing.inMotion()) {
            if (bankerHome != null && bankerHome.validateInteractable()) {
                updateStatus("Opening bank");
                bankerHome.click("Bank");
                ctx.onCondition(() -> ctx.bank.bankOpen(), 250, 10);
            }
        }
    }

    private boolean getSickleInventory() {
        SimpleItem hammer = ctx.inventory.populate().filter(SILVER_SICKLE_ID).next();
        return hammer != null;
    }

    private void handleSickle() {
        boolean sicleInInv = getSickleInventory();
        if (sicleInInv) {
            return;
        }

        SimpleItem sickleInBank = ctx.bank.populate().filter(SILVER_SICKLE_ID).next();
        if (sickleInBank == null) {
            updateStatus("No Silver Sickle (b) in bank");
            ctx.updateStatus("Stopping script");
            ctx.sleep(10000);
            ctx.stopScript();
        } else {
            withdrawSickle();
            clearBankSearch();
        }
    }

    private void withdrawSickle() {
        updateStatus("Withdrawing Silver Sickle");
        SimpleWidget quantityOne = ctx.widgets.getWidget(12, 29);
        if (quantityOne != null && !quantityOne.isHidden()) {
            quantityOne.click(0);
        }
        ctx.bank.withdraw(SILVER_SICKLE_ID, SimpleBank.Amount.ONE);
    }

    private void clearBankSearch() {
        SimpleWidget searchButton = ctx.widgets.getWidget(12, 40);
        if (searchButton != null && !searchButton.isHidden()) {
            searchButton.click(0);
        }
    }

    private void restorePrayer() {
        SimplePrayers getPrayPoints = ctx.prayers;
        updateStatus("Restoring prayer points");
        SimpleObject restorBox = ctx.objects.populate().filter(BOX_OF_RESTORATION_ID).nearest().next();
        if (restorBox != null && restorBox.validateInteractable()) {
            restorBox.click("Restore");
            int prayerPoints = getPrayPoints.points();
            ctx.sleepCondition(() -> prayerPoints == prayerLevel, randomSleeping(2000, 4000));
        }
    }

    // Mort Myre Swamp tasks
    private void castBloom() {
        SimpleItem silverSicle = ctx.inventory.populate().filter(SILVER_SICKLE_ID).next();
        if (silverSicle != null && silverSicle.validateInteractable()) {
            updateStatus("Casting bloom");
            silverSicle.click(2);
            ctx.onCondition(() -> !ctx.objects.populate().filter(FUNGI_ON_LOG_ID).isEmpty(), randomSleeping(1000, 3000));
        }
    }

    private void pickingFungi() {
        SimpleObject fungiOnLog = ctx.objects.populate().filter(FUNGI_ON_LOG_ID).next();
        updateStatus("Picking up fungi");
        if (fungiOnLog != null && fungiOnLog.validateInteractable()) {
            fungiOnLog.click(1);
            ctx.sleepCondition(() -> ctx.players.getLocal().getAnimation() != 827 && !ctx.pathing.inMotion(), randomSleeping(2000, 4000));
        }
    }

    private boolean checkFungiOnLog() {
        return !ctx.objects.populate().filter(FUNGI_ON_LOG_ID).isEmpty();
    }

    private boolean playerReady() {
        boolean dramenStaff = !ctx.equipment.populate().filter(772).isEmpty();
        if (!dramenStaff) {
            updateStatus("No Dramen staff equipped");
        }
        boolean silverSickle = getSickleInventory();
        if (!silverSickle) {
            updateStatus("No Silver Sickle (b) found");
        }
        boolean prayerPoints = ctx.prayers.points() > 50;
        boolean inventoryPopulation = ctx.inventory.populate().population() <= 2;
        return dramenStaff && silverSickle && prayerPoints && inventoryPopulation;
    }


    // Teleproting
    public void teleportToHome() {
        updateStatus("Teleporting to Home");
        ctx.game.tab(Game.Tab.MAGIC);
        SimpleWidget homeTeleport = ctx.widgets.getWidget(218, 4); //home teleport normal spellbook
        if (homeTeleport != null & !homeTeleport.isHidden()) {
            homeTeleport.click(1); //Home, Zenyte Home Teleport
            ctx.sleepCondition(() -> homeArea.containsPoint(ctx.players.getLocal().getLocation()), randomSleeping(9000, 12000));
        }
        ctx.game.tab(Game.Tab.INVENTORY);
    }

    public void teleportToSwamp() {
        updateStatus("Teleporting to Mort Myre Swamp");

        if (ctx.bank.bankOpen()) {
            ctx.bank.closeBank();
        }

        SimpleObject spiritualTree = ctx.objects.populate().filter(SPIRITUAL_FAIRY_THEE_ID).next();
        if (spiritualTree != null && spiritualTree.validateInteractable()) {
            updateStatus("Clicking the Spiritual tree");
            spiritualTree.click("Ring-last-destination", "Spiritual Fairy Tree");
            ctx.sleepCondition(() -> swampArea.containsPoint(ctx.players.getLocal().getLocation()), randomSleeping(9000, 12000));
        }
        ctx.game.tab(Game.Tab.INVENTORY);
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

        if (m.getMessage() == null) {
            return;
        }

        if (m.getMessage() != null) {
            String message = m.getMessage().toLowerCase();
            if (message.contains("you need to wait")) {
                updateStatus("Cannot restore prayer yet");
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

        // Calculate experience and actions per hour
        long actionsPerHour = count * 3600000L / (System.currentTimeMillis() - this.startTime);

        // Set up colors
        Color philippineRed = new Color(196, 18, 48);
        Color raisinBlack = new Color(35, 31, 32, 127);

        // Draw paint if not hidden
        if (!hidePaint) {
            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gradientPaint = new GradientPaint(5, 120, raisinBlack, 220, 230, philippineRed, true);
            g2d.setPaint(gradientPaint);
            g.fillRoundRect(5, 120, 200, 85, 20, 20);

            g.setColor(philippineRed);
            g.drawRoundRect(5, 120, 200, 85, 20, 20);

            Font title = new Font("Arial", Font.BOLD, 12);
            Font text = new Font("Arial", Font.PLAIN, 11);

            g.setFont(title);
            g.setColor(Color.WHITE);
            g.drawString("eMortMyreFungus by Esmaabi", 15, 140);

            g.setFont(text);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + formatTime(runTime), 15, 160);
            g.drawString("Fungi picked: " + count + " (" + actionsPerHour + " per/h)", 15, 175);
            g.drawString("Status: " + status, 15, 190);
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