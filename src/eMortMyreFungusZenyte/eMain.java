package eMortMyreFungusZenyte;

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
    private static final String BOX_OF_RESTORATION = "Box of Restoration"; // Restore
    private static String playerGameName;
    public static final WorldArea EDGE_HOME_AREA = new WorldArea(new WorldPoint(3110, 3474, 0), new WorldPoint(3074, 3516, 0));


    // Coordinates
    private final WorldArea homeArea = new WorldArea(new WorldPoint(3101, 3181, 0), new WorldPoint(3074, 3504, 0));
    private final WorldArea swampArea = new WorldArea(new WorldPoint(3480,3449, 0), new WorldPoint(3417,3424, 0));
    private final WorldArea altarArea = new WorldArea(new WorldPoint(2584, 3228, 0), new WorldPoint(2629, 3199, 0));
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

        tasks.addAll(Arrays.asList());// Adds tasks to our {task} list for execution

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
            teleportToAltar();
        }

        if (pathing.inArea(homeArea)) {

            if (playerReady()) {
                teleportToSwamp();
            } else {
                openingBank();
            }
        }

        if (pathing.inArea(altarArea)) {
            if (ctx.prayers.points() < prayerLevel) {
                restorePrayer();
            } else {
                teleportHomeSpellbook();
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
                        groundFungus.menuAction("Take");
                    }
                }

            } else {
                teleportToAltar();
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
            handleSickleBank();
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
        SimpleItem silverSicle = ctx.inventory.populate().filter(SILVER_SICKLE_ID).next();
        return silverSicle != null;
    }

    private void handleSickleBank() {
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
        SimpleObject restorBox = ctx.objects.populate().filter("Altar").nearest().next();
        if (restorBox != null && restorBox.validateInteractable()) {
            ctx.pathing.step(2606, 3210);
            int prayerPoints = getPrayPoints.points();
            restorBox.menuAction("Pray-at");
            ctx.sleepCondition(() -> prayerPoints == prayerLevel, randomSleeping(6000, 8000));
        }
    }

    // Mort Myre Swamp tasks
    private void castBloom() {
        if (ctx.prayers.points() == 0) return;
        SimpleItem silverSicle = ctx.inventory.populate().filter(SILVER_SICKLE_ID).next();
        if (silverSicle != null && silverSicle.validateInteractable()) {
            updateStatus("Casting bloom");
            silverSicle.click(2);
            ctx.onCondition(() -> ctx.objects.populate().filter(FUNGI_ON_LOG_ID).population() > 0, randomSleeping(1000, 2000));
        }
    }

    private void pickingFungi() {
        SimpleObject fungiOnLog = ctx.objects.populate().filter(FUNGI_ON_LOG_ID).next();
        updateStatus("Picking up fungi");
        if (fungiOnLog != null && fungiOnLog.validateInteractable()) {
            int funi = ctx.inventory.getFreeSlots();
            fungiOnLog.menuAction("Pick");
            //ctx.sleepCondition(() -> ctx.players.getLocal().getAnimation() != 827 && !ctx.pathing.inMotion(), randomSleeping(2000, 4000));
            ctx.onCondition(() -> ctx.inventory.getFreeSlots() < funi, 200, 20);
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
    public void teleportToAltar() {
        updateStatus("Teleporting to Home");
        ctx.game.tab(Game.Tab.EQUIPMENT);
        SimpleItem cape = ctx.equipment.populate().filter(n -> n.getItemDefinitions().getName().toLowerCase().contains("ardougne cloak")).next();
        if (cape != null) {
            cape.click("Kandarin Monastery");
            ctx.onCondition(() -> ctx.pathing.inArea(altarArea), 500, 5);
        }
    }

    public void teleportToSwamp() {
        updateStatus("Teleporting to Mort Myre Swamp");

        if (ctx.bank.bankOpen()) {
            ctx.bank.closeBank();
        }

        SimpleObject spiritualTree = ctx.objects.populate().filter(SPIRITUAL_FAIRY_THEE_ID).next();
        if (spiritualTree != null && spiritualTree.validateInteractable()) {
            updateStatus("Clicking the Spiritual tree");
            spiritualTree.menuAction("Ring-last-destination");
            ctx.sleepCondition(() -> swampArea.containsPoint(ctx.players.getLocal().getLocation()), randomSleeping(9000, 12000));
        }
        ctx.game.tab(Game.Tab.INVENTORY);
    }

    public void teleportHomeSpellbook() {
        if (ctx.pathing.inArea(EDGE_HOME_AREA)) return;

        BotUtils.eActions.status = "Teleporting to home";
        BotUtils.eActions.openTab(Game.Tab.MAGIC);

        int widgetNumber;
        switch (ctx.magic.spellBook()) {
            case MODERN:
                widgetNumber = 4;
                ctx.log("Normal magic book");
                break;
            case LUNAR:
                widgetNumber = 99;
                ctx.log("Lunar magic book");
                break;
            case ANCIENT:
                widgetNumber = 98;
                ctx.log("Ancient magic book");
                break;
            case ARCEUUS:
                widgetNumber = 143;
                ctx.log("Areceuus magic book");
                break;
            default:
                widgetNumber = -1;
        }

        if (widgetNumber != -1) {
            ctx.log("Widget: 218, " + widgetNumber);
            clickWidget(widgetNumber);
        }

        //ctx.onCondition(() -> ctx.players.getLocal().getGraphic() != -1 || ctx.pathing.inArea(EDGE_HOME_AREA), 500, 20);
        ctx.sleepCondition(() -> !ctx.pathing.inArea(EDGE_HOME_AREA), randomSleeping(9000, 12000));
        ctx.game.tab(Game.Tab.INVENTORY);
    }

    private void clickWidget(int childId) {
        SimpleWidget widgetToClick = ctx.widgets.getWidget(218, childId);
        if (widgetToClick == null) return;
        widgetToClick.click(0);
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