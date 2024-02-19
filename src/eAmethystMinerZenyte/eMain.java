package eAmethystMinerZenyte;

import BotUtils.eActions;
import BotUtils.eBanking;
import BotUtils.eData;
import Utility.Trivia.eTriviaInfo;
import eApiAccess.eAutoResponderGui;
import eApiAccess.eAutoResponser;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleObjects;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.*;
import simple.robot.utils.WorldArea;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static eApiAccess.eAutoResponser.*;

@ScriptManifest(
        author = "Esmaabi",
        category = Category.MINING,
        description = "<html>"
                + "<p>The most effective Volcanic Ash miner Bot!</p>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "<ul>"
                + "<li>Start near 3 ash piles (in same area).</li>"
                + "<li><strong>Have any pickaxe equipped or in inventory.</strong>.</li>"
                + "<li>Dragon pickaxe special attack is supported.</li>"
                + "<li>Chat GPT answering is integrated.</li>"
                + "</ul>"
                + "</html>",
        discord = "Esmaabi#5752",
        name = "eAmethystMinerBot",
        servers = {"Zenyte"},
        version = "1"
)

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private static final WorldArea BANK_AREA = new WorldArea (new WorldPoint(3011, 9720, 0), new WorldPoint(3015, 9716, 0));
    private static final SimpleSkills.Skills CHOSEN_SKILL = SimpleSkills.Skills.MINING;
    private static final String eBotName = "eAmethystMinerBot";
    private static final String ePaintText = "Crystals mined";
    private static final Logger logger = Logger.getLogger(eAnglerFisherBot.eMain.class.getName());
    private static final WorldArea MINING_AREA = new WorldArea (new WorldPoint(3043, 9695, 0), new WorldPoint(2993, 9729, 0));

    // Variables
    private int count;
    private int currentExp;
    private long lastAnimation = -1;
    private int[] lastCoordinates = null;
    private boolean maximumXpReached;
    private static eAutoResponderGui guiGpt;
    public static boolean hidePaint = false;
    private final int[] inventoryPickaxes = {30742, 20014, 13243, 12797, 12297, 11920, 1275, 1273, 1271, 1269, 1267, 1265};
    private long startTime = 0L;
    private long startingSkillExp;
    private long startingSkillLevel;


    // Gui GPT
    private void initializeGptGui() {
        guiGpt = new eAutoResponderGui();
        guiGpt.setVisible(true);
        guiGpt.setLocale(ctx.getClient().getCanvas().getLocale());
    }

    private void initializeMethods() {
        BotUtils.eBanking bankingUtils = new eBanking(ctx);
        BotUtils.eActions actionUtils = new eActions(ctx);
        BotUtils.eData dataUtils = new eData(ctx);
        Utility.Trivia.eTriviaInfo triviaInfo = new eTriviaInfo(ctx);
    }

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

        tasks.addAll(Arrays.asList(new eAutoResponser(ctx)));
        initializeMethods(); // BotUtils
        initializeGptGui(); // GPT
        eAutoResponser.scriptPurpose = "you're mining some amethyst. ";
        botStarted = false;
        gptStarted = false;

        // Other vars
        ctx.log("--------------- " + BotUtils.eActions.getCurrentTimeFormatted() + " ---------------");
        ctx.log("-------------------------------------");
        ctx.log("            " + eBotName + "         ");
        ctx.log("-------------------------------------");

        // Vars
        BotUtils.eActions.updateStatus("Setting up bot");
        this.startTime = System.currentTimeMillis();
        this.startingSkillLevel = this.ctx.skills.realLevel(CHOSEN_SKILL);
        this.startingSkillExp = this.ctx.skills.experience(CHOSEN_SKILL);
        this.currentExp = this.ctx.skills.experience(CHOSEN_SKILL);
        maximumXpReached = ctx.skills.experience(CHOSEN_SKILL) == 200000000;
        count = 0;
        ctx.viewport.angle(180);
        ctx.viewport.pitch(true);
        lastAnimation = System.currentTimeMillis();
        BotUtils.eActions.specialAttackTool = true;
    }

    @Override
    public void onProcess() {
        super.onProcess();

        final SimplePlayer localPlayer = ctx.players.getLocal();

        if (!botStarted) {
            BotUtils.eActions.status = "Please start the bot!";
            return;
        }

        BotUtils.eActions.handleRunning();

        handleCount();

        if (!BotUtils.eActions.inArea(MINING_AREA)) {
            BotUtils.eActions.status = "Searching for mining area...";
            takingStepsRMining();
            return;
        }

        if (ctx.inventory.inventoryFull()) {
            BotUtils.eBanking.bankTask(true, 8, 1, false, -1, -1, inventoryPickaxes);
        } else {
            if (!localPlayer.isAnimating() && (System.currentTimeMillis() > (lastAnimation + BotUtils.eActions.getRandomInt(1200, 12000)))) {
                miningTask();
            } else if (localPlayer.isAnimating()) {
                lastAnimation = System.currentTimeMillis();
            }
        }

        if (localPlayer.isAnimating()) {
            if (BotUtils.eActions.specialAttackTool) {
                BotUtils.eActions.specialAttack(ItemID.DRAGON_PICKAXE);
            }
        }

        int[] combinedItems = IntStream.concat(Arrays.stream(eActions.randomEventItems), IntStream.of(21347))
                .toArray();
        BotUtils.eActions.handleGroundItem("Take", combinedItems);
    }

    private void miningTask() {
        final SimplePlayer localPlayer = ctx.players.getLocal();

        if (BANK_AREA.containsPoint(ctx.players.getLocal().getLocation()) && !ctx.pathing.inMotion()) {
            BotUtils.eActions.updateStatus("Heading to mining area");
            takingStepsRMining();
        }

        String objectName = "Crystals";
        SimpleObjects nearestSpot = (SimpleObjects) ctx.objects.populate().filter(objectName);
        BotUtils.eActions.updateStatus("Looking for nearest mining spot...");

        while (!nearestSpot.isEmpty()) {
            String actionName = "Mine";
            SimpleObject nearestObject = nearestSpot.filterHasAction(actionName).nearest().next();
            WorldPoint theTreeLocation = nearestObject.getLocation();
            boolean isOtherPlayerMining = !ctx.players.populate().filterWithin(theTreeLocation, 2).filter(otherPlayer -> !otherPlayer.getName().equals(ctx.players.getLocal().getName())).isEmpty();

            if (nearestSpot.size() >= 2 && isOtherPlayerMining) {
                BotUtils.eActions.updateStatus("Spot take... Looking for another one...");
                nearestSpot = (SimpleObjects) nearestSpot.filter(other -> !other.equals(nearestObject));
                continue;
            } else {
                if (nearestObject.validateInteractable()) {
                    WorldPoint objectLocation = nearestObject.getLocation();
                    boolean objectReachable = isObjectReachable(objectLocation);

                    if (objectReachable) {
                        int distance = objectLocation.distanceTo(ctx.players.getLocal().getLocation());
                        ctx.log(objectName + " found " + distance + " " + BotUtils.eActions.pluralize(distance, "tile", "tiles") + " away");
                        BotUtils.eActions.interactWith(nearestObject, actionName);
                        BotUtils.eActions.updateStatus("Mining " + objectName.toLowerCase());
                        ctx.onCondition(localPlayer::isAnimating, 250, 10);
                        return;
                    } else {
                        BotUtils.eActions.updateStatus("Next " + objectName.toLowerCase() + " spot is not reachable.");
                        nearestSpot = (SimpleObjects) nearestSpot.filter(otherTree -> !otherTree.equals(nearestObject));
                    }
                } else {
                    BotUtils.eActions.updateStatus("No " + objectName.toLowerCase() + " found in the vicinity.");
                    return;
                }
            }
        }
        BotUtils.eActions.updateStatus("No suitable " + objectName.toLowerCase() + " spot found nearby.");
    }

    private boolean isObjectReachable(WorldPoint objectLocation) {
        int[] offsets = { 0, 1, -1}; // Adjust these offsets as needed
        for (int offsetX : offsets) {
            for (int offsetY : offsets) {
                WorldPoint offsetLocation = new WorldPoint(objectLocation.getX() + offsetX, objectLocation.getY() + offsetY, objectLocation.getPlane());
                if (ctx.pathing.reachable(offsetLocation)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void takingStepsRMining() {
        int max = 6;
        int min = 1;
        int[][] coordinates = {{3024, 9708}, {3018, 9704}, {3022, 9707}, {3028, 9704}, {3019, 9706}, {3027, 9705}};

        if (lastCoordinates == null) {
            int randomNum = ThreadLocalRandom.current().nextInt(min, max + min);
            lastCoordinates = coordinates[randomNum - 1];
        }
        ctx.pathing.step(lastCoordinates[0], lastCoordinates[1]);
    }

    private void handleCount() {
        if (currentExp != this.ctx.skills.experience(CHOSEN_SKILL)) {
            count++;
            currentExp = this.ctx.skills.experience(CHOSEN_SKILL);
        }
    }

    @Override
    public void onTerminate() {

        // Termination message
        ctx.log("-------------- " + BotUtils.eActions.getCurrentTimeFormatted() + " --------------");
        ctx.log(ePaintText + ": " + count);
        ctx.log("-----------------------------------");
        ctx.log("----- Thank You & Good Luck! ------");
        ctx.log("-----------------------------------");

        // Other variables
        this.startingSkillLevel = 0L;
        this.startingSkillExp = 0L;
        this.count = 0;
        guiGpt.setVisible(false);
        gptStarted = false;
        messageSaved = null;
        BotUtils.eActions.specialAttackTool = false;
    }

    @Override
    public void onChatMessage(ChatMessage m) {
        String formattedMessage = m.getFormattedMessage();
        ChatMessageType getType = m.getType();
        net.runelite.api.events.ChatMessage getEvent = m.getChatEvent();
        String senderName = getEvent.getName();
        String gameMessage = getEvent.getMessage();

        if (m.getMessage() == null) {
            return;
        }

        eApiAccess.eAutoResponser.handleGptMessages(getType, senderName, formattedMessage);
        Utility.Trivia.eTriviaInfo.handleBroadcastMessage(getType, gameMessage);
    }

    @Override
    public int loopDuration() {
        return 600;
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
            g.drawString("Status: " + BotUtils.eActions.status, 15, 225);
        }
    }
}