package eAnglerFisherBot;

import BotUtils.eActions;
import BotUtils.eBanking;
import BotUtils.eData;
import Utility.Trivia.eTriviaInfo;
import eApiAccess.eAutoResponderGui;
import eApiAccess.eAutoResponser;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleInventory;
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
import java.util.logging.Logger;

import static eApiAccess.eAutoResponser.*;

@ScriptManifest(
        author = "Esmaabi",
        category = Category.FISHING,
        description = "<html>"
                + "<p>The most effective Anglers Fishing bot!</p>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "<ul>"
                + "<li>Start anywhere near trees and bank.</li>"
                + "<li><strong>Examine the tree you want to cut to select it</strong>.</li>"
                + "<li>Bot will bank at any nearby location.</li>"
                + "<li>Bot will chop any tree <strong>you examine</strong>.</li>"
                + "<li>Axe in inventory is supported.</li>"
                + "<li>Dragon harpoon special attack is supported.</li>"
                + "<li>Picking up anglers is supported.</li>"
                + "<li>Auto wearing Anglers outfit pieces is supported.</li>"
                + "<li>Chat GPT answering is integrated.</li>"
                + "</ul>"
                + "</html>",
        discord = "Esmaabi#5752",
        name = "eAnglerFisherBot with GPT",
        servers = {"Zenyte"},
        version = "0.1"
)

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private static final String eBotName = "eAnglerFisherBot";
    private static final String ePaintText = "Fish caught";
    private static eAutoResponderGui guiGpt;
    private static final Logger logger = Logger.getLogger(eMain.class.getName());
    private static final SimpleSkills.Skills CHOSEN_SKILL = SimpleSkills.Skills.FISHING;
    private final WorldArea ANGLERS_AREA = new WorldArea(new WorldPoint(1841, 3799, 0), new WorldPoint(1792, 3767, 0));

    private static final WorldArea BANK_AREA = new WorldArea(
            new WorldPoint(1815, 3784, 0),
            new WorldPoint(1815, 3779, 0),
            new WorldPoint(1792, 3779, 0),
            new WorldPoint(1792, 3798, 0),
            new WorldPoint(1815, 3798, 0));

    private static final WorldArea FISHING_AREA = new WorldArea(
            new WorldPoint(1815, 3796, 0),
            new WorldPoint(1815, 3778, 0),
            new WorldPoint(1805, 3778, 0),
            new WorldPoint(1805, 3767, 0),
            new WorldPoint(1835, 3767, 0),
            new WorldPoint(1844, 3782, 0),
            new WorldPoint(1837, 3796, 0));

    private final WorldPoint[] WALKING_TO_ANGLER_SPOT = {
            new WorldPoint(1810, 3781, 0),
            new WorldPoint(1818, 3779, 0),
            new WorldPoint(1824, 3775, 0),
    };

    // Variables
    private long startTime = 0L;
    private long startingSkillLevel;
    private long startingSkillExp;
    private int count;
    public static boolean hidePaint = false;
    private long lastAnimation = -1;

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
        eAutoResponser.scriptPurpose = "you're grinding anglers. ";
        gptDeactivation();

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
        count = 0;
        ctx.viewport.angle(0);
        ctx.viewport.pitch(true);
        lastAnimation = System.currentTimeMillis();
        BotUtils.eActions.specialAttackTool = true;
    }

    @Override
    public void onProcess() {
        super.onProcess();

        final SimplePlayer localPlayer = ctx.players.getLocal();
        final SimpleInventory myInventory = ctx.inventory;

        if (!botStarted) {
            BotUtils.eActions.status = "Please start the bot!";
            return;
        }

        if (!ctx.pathing.inArea(ANGLERS_AREA)) return;

        if (!myInventory.inventoryFull() && !BotUtils.eBanking.bankIsOpen()) {

            if (!localPlayer.isAnimating() && (System.currentTimeMillis() > (lastAnimation + BotUtils.eActions.getRandomInt(1000, 5000)))) {
                fishingTask();
                BotUtils.eActions.status = "Fishing...";
            } else if (localPlayer.isAnimating()) {
                lastAnimation = System.currentTimeMillis();
            }

        } else {
            bankingTask();
        }

        if (BotUtils.eActions.hasItemsInInventory(null, ItemID.ANGLER_HAT, ItemID.ANGLER_TOP, ItemID.ANGLER_WADERS, ItemID.ANGLER_BOOTS)) {
            BotUtils.eActions.updateStatus("Wearing Anglers piece");
            BotUtils.eActions.handleInventoryItem("Wear", ItemID.ANGLER_HAT, ItemID.ANGLER_TOP, ItemID.ANGLER_WADERS, ItemID.ANGLER_BOOTS);
        }

        BotUtils.eActions.handleGroundItem("Take", ItemID.RAW_ANGLERFISH, ItemID.ANTIQUE_LAMP_7498);

        BotUtils.eActions.handleRunning();

        if (localPlayer.isAnimating()) {
            if (BotUtils.eActions.specialAttackTool) {
                BotUtils.eActions.specialAttack(BotUtils.eData.Fishing.SPECIAL_ATTACK_TOOL);
            }
        }
    }

    // Fishing
    private void fishingTask() {
        final SimplePlayer localPlayer = ctx.players.getLocal();
        SimpleNpc fishingSpot = ctx.npcs.populate().filter(6825).filterHasAction("Bait").nearest().next();

        BotUtils.eActions.walkPath(FISHING_AREA, WALKING_TO_ANGLER_SPOT, false);

        if (fishingSpot == null) return;

        if (!fishingSpot.visibleOnScreen()) {
            ctx.pathing.step(fishingSpot.getLocation());
        }
        BotUtils.eActions.interactWith(fishingSpot, "Bait");
        ctx.onCondition(localPlayer::isAnimating, 250, 10);
    }

    private void bankingTask() {
        BotUtils.eActions.walkPath(BANK_AREA, WALKING_TO_ANGLER_SPOT, true);
        BotUtils.eBanking.bankTask(true, 8, 1, false, -1, -1, ItemID.FISHING_ROD, ItemID.SANDWORMS);
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
        gptDeactivation();
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

        if (gptStarted && botStarted) eAutoResponser.handleGptMessages(getType, senderName, formattedMessage);
        Utility.Trivia.eTriviaInfo.handleBroadcastMessage(getType, gameMessage);

        if (getType == ChatMessageType.SPAM) {
            String spamMessage = getEvent.getMessage().toLowerCase();

            if (spamMessage.contains("you catch an")) {
                count++;
            }
        }
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

