package eWoodcuttingBotZenyte;

import BotUtils.eActions;
import BotUtils.eBanking;
import BotUtils.eData;
import BotUtils.eImpCatcher;
import Utility.Trivia.eTriviaInfo;
import eApiAccess.eAutoResponderGui;
import eApiAccess.eAutoResponser;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleInventory;
import simple.hooks.filters.SimpleObjects;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Pathing;
import simple.hooks.wrappers.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

import static eApiAccess.eAutoResponser.*;

@ScriptManifest(
        author = "Esmaabi",
        category = Category.WOODCUTTING,
        description = "<html>"
                + "<p>The most effective Woodcutting bot!</p>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "<ul>"
                + "<li>Start anywhere near trees and bank.</li>"
                + "<li><strong>Examine the tree you want to cut to select it</strong>.</li>"
                + "<li>Bot will bank at any nearby location.</li>"
                + "<li>Bot will chop any tree <strong>you examine</strong>.</li>"
                + "<li>Axe in inventory is supported.</li>"
                + "<li>Dragon axe special attack is supported.</li>"
                + "<li>Picking up bird nests is supported.</li>"
                + "<li>Auto wearing Lumberjack outfit pieces is supported.</li>"
                + "<li>Chat GPT answering is integrated.</li>"
                + "</ul>"
                + "</html>",
        discord = "Esmaabi#5752",
        name = "eWoodcuttingBot",
        servers = {"Zenyte"},
        version = "1"
)

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private static final String eBotName = "eWoodcuttingBot";
    private static final String ePlainText = "Logs count: ";
    private static final String[] BIRD_NEST = {"Bird nest", "Clue nest (beginner)", "Clue nest (easy)", "Clue nest (medium)", "Clue nest (hard)", "Clue nest (elite)"};
    private static final SimpleSkills.Skills CHOSEN_SKILL = SimpleSkills.Skills.WOODCUTTING;
    private static eAutoResponderGui guiGpt;
    private static final Logger logger = Logger.getLogger(eMain.class.getName());
    private static final String[] SPECIAL_ATTACK_TOOL = {
            "Dragon axe (or)",
            "Infernal axe",
            "Dragon axe"
    };

    // Variables
    private int count;
    public static boolean hidePaint = false;
    private long lastAnimation = -1;
    public static boolean redwoodMode;
    private long startTime = 0L;
    private long startingSkillExp;
    private long startingSkillLevel;
    private static String treeAction;
    private static String treeName;


    // Gui GPT
    private void initializeGptGUI() {
        guiGpt = new eAutoResponderGui();
        guiGpt.setVisible(true);
        guiGpt.setLocale(ctx.getClient().getCanvas().getLocale());
    }

    private void initializeMethods() {
        eBanking bankingUtils = new eBanking(ctx);
        eActions actionUtils = new eActions(ctx);
        eData dataUtils = new eData(ctx);
        eImpCatcher impCatcher = new eImpCatcher(ctx);
        eTriviaInfo triviaInfo = new eTriviaInfo(ctx);
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

        tasks.addAll(Arrays.asList(new eAutoResponser(ctx), new eImpCatcher(ctx)));

        initializeGptGUI();
        initializeMethods();
        eAutoResponser.scriptPurpose = "you're grinding woodcutting. ";
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
        ctx.viewport.angle(270);
        ctx.viewport.pitch(true);
        lastAnimation = System.currentTimeMillis();
        treeName = null;
        treeAction = null;
        redwoodMode = false;
        eActions.specialAttackTool = true;
    }

    @Override
    public void onProcess() {
        super.onProcess();

        final SimplePlayer localPlayer = ctx.players.getLocal();
        final Pathing pathing = ctx.pathing;
        final SimpleInventory myInventory = ctx.inventory;

        if (!botStarted || treeName == null) {
            BotUtils.eActions.status = "Press start & examine any tree";
            return;
        }

        if (redwoodMode) {
            if (!myInventory.inventoryFull() && !BotUtils.eBanking.bankIsOpen()) {

                if (localPlayer.getLocation().getPlane() != 1 && ctx.players.getLocal().getLocation().getRegionID() == 6198) {
                    handleRopeLadder(localPlayer, "Climb-up", 1);

                } else {

                    if (!localPlayer.isAnimating() && !pathing.inMotion() && (System.currentTimeMillis() > (lastAnimation + BotUtils.eActions.getRandomInt(1200, 3200)))) {
                        cuttingTrees(localPlayer, treeName);
                    } else if (localPlayer.isAnimating()) {
                        lastAnimation = System.currentTimeMillis();
                    }
                }

            } else {
                if (localPlayer.getLocation().getPlane() == 1 && ctx.players.getLocal().getLocation().getRegionID() == 6198) {
                    handleRopeLadder(localPlayer, "Climb-down", 0);
                } else {
                    BotUtils.eBanking.bankTask(true, 8, 1, false, (String) null, -1, eData.Woodcutting.WOODCUTTING_AXE);
                }
            }

        }

        if (!redwoodMode) {
            if (!myInventory.inventoryFull() && !BotUtils.eBanking.bankIsOpen()) {

                if (!localPlayer.isAnimating() && !pathing.inMotion() && (System.currentTimeMillis() > (lastAnimation + BotUtils.eActions.getRandomInt(1000, 5000)))) {
                    cuttingTrees(localPlayer, treeName);
                } else if (localPlayer.isAnimating()) {
                    lastAnimation = System.currentTimeMillis();
                }

            } else {
                BotUtils.eBanking.bankTask(true, 8, 1, false, (String) null, -1, eData.Woodcutting.WOODCUTTING_AXE);
            }
        }

        BotUtils.eActions.handleRunning();

        if (!myInventory.populate().filter(ItemID.LUMBERJACK_HAT, ItemID.LUMBERJACK_TOP, ItemID.LUMBERJACK_LEGS, ItemID.LUMBERJACK_BOOTS).isEmpty()) {
            BotUtils.eActions.updateStatus("Wearing Lumberjack piece");
            myInventory.populate().filter(ItemID.LUMBERJACK_HAT, ItemID.LUMBERJACK_TOP, ItemID.LUMBERJACK_LEGS, ItemID.LUMBERJACK_BOOTS).forEach((item) -> item.click("Wear"));
        }

        if (!ctx.inventory.inventoryFull()) {
            BotUtils.eActions.handleGroundItem("Take", BIRD_NEST);
        }

        if (localPlayer.isAnimating()) {
            if (BotUtils.eActions.specialAttackTool) {
                BotUtils.eActions.specialAttack(SPECIAL_ATTACK_TOOL);
            }
        }
    }

    // Woodcutting
    private void cuttingTrees(SimplePlayer localPlayer, String nameOfTree) {
        SimpleObjects treesNearby = (SimpleObjects) ctx.objects.populate().filter(nameOfTree);
        BotUtils.eActions.updateStatus("Looking for " + nameOfTree + " trees...");

        while (!treesNearby.isEmpty()) {
            SimpleObject nearestTree = treesNearby.filterHasAction(treeAction).nearest().next();
            WorldPoint theTreeLocation = nearestTree.getLocation();
            boolean isOtherPlayerChopping = !ctx.players.populate().filterWithin(theTreeLocation, 2).filter(otherPlayer -> !otherPlayer.getName().equals(ctx.players.getLocal().getName())).isEmpty();

            if (treesNearby.size() >= 2 && isOtherPlayerChopping) {
                BotUtils.eActions.updateStatus("Another player is chopping the nearest " + nameOfTree + ".");
                BotUtils.eActions.updateStatus("Looking for another tree...");
                treesNearby = (SimpleObjects) treesNearby.filter(otherTree -> !otherTree.equals(nearestTree));
                continue;
            } else {
                if (nearestTree.validateInteractable()) {
                    WorldPoint treeLocation = nearestTree.getLocation();
                    boolean reachable = isTreeReachable(treeLocation);

                    if (reachable) {
                        int distance = treeLocation.distanceTo(ctx.players.getLocal().getLocation()) - 1;
                        BotUtils.eActions.updateStatus(nameOfTree + " found " + (treeLocation.distanceTo(ctx.players.getLocal().getLocation()) - 1) + BotUtils.eActions.pluralize(distance, " tile", " tiles") + " away");
                        if (redwoodMode) {
                            ctx.viewport.pitch(true);
                        }
                        BotUtils.eActions.interactWith(nearestTree, treeAction);
                        BotUtils.eActions.status = "Chopping " + nameOfTree;
                        BotUtils.eActions.updateStatus(BotUtils.eActions.status);
                        ctx.onCondition(localPlayer::isAnimating, 250, 10);
                        return;
                    } else {
                        BotUtils.eActions.updateStatus("Next " + nameOfTree + " is not reachable.");
                        treesNearby = (SimpleObjects) treesNearby.filter(otherTree -> !otherTree.equals(nearestTree));
                    }
                } else {
                    BotUtils.eActions.updateStatus("No " + nameOfTree + " found in the vicinity.");
                    return;
                }
            }
        }
        BotUtils.eActions.updateStatus("No suitable " + nameOfTree + " trees found nearby.");
    }


    private boolean isTreeReachable(WorldPoint treeLocation) {
        int[] offsets = { 0, 1, -1, 2, -2}; // Adjust these offsets as needed
        for (int offsetX : offsets) {
            for (int offsetY : offsets) {
                WorldPoint offsetLocation = new WorldPoint(treeLocation.getX() + offsetX, treeLocation.getY() + offsetY, treeLocation.getPlane());
                if (ctx.pathing.reachable(offsetLocation)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void handleGroundItem() {
        SimpleGroundItem itemToPickup = ctx.groundItems.populate().filter(eMain.BIRD_NEST).nearest().next();

        if (itemToPickup != null && itemToPickup.validateInteractable()) {
            BotUtils.eActions.updateStatus(BotUtils.eActions.getCurrentTimeFormatted() + " Found " + itemToPickup.getName());
            if (itemToPickup.click("Take")) {
                ctx.onCondition(() -> ctx.groundItems.populate().filter(eMain.BIRD_NEST).isEmpty(), 250, 12);
            }
        }
    }

    private void handleRopeLadder(SimplePlayer localPlayer, String action, int expectedPlane) {
        SimpleObject ropeLadder = ctx.objects.populate().filter("Rope ladder").filterHasAction(action).nearest().next();
        if (!ropeLadder.visibleOnScreen()) {
            ctx.viewport.turnTo(ropeLadder);
            ctx.pathing.step(ropeLadder.getLocation());
        }
        BotUtils.eActions.interactWith(ropeLadder, action);
        ctx.onCondition(() -> localPlayer.getLocation().getPlane() == expectedPlane, 250, 4);
    }

    @Override
    public void onTerminate() {

        // Termination message
        ctx.log("-------------- " + BotUtils.eActions.getCurrentTimeFormatted() + " --------------");
        ctx.log(ePlainText + count);
        ctx.log("-----------------------------------");
        ctx.log("----- Thank You & Good Luck! ------");
        ctx.log("-----------------------------------");

        // Other variables
        this.startingSkillLevel = 0L;
        this.startingSkillExp = 0L;
        this.count = 0;
        guiGpt.setVisible(false);
        gptDeactivation();
        treeName = null;
        treeAction = null;
        redwoodMode = false;
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
        eTriviaInfo.handleBroadcastMessage(getType, gameMessage);

        if (getType == ChatMessageType.SPAM) {
            String spamMessage = trimGameMessage(gameMessage);

            if (spamMessage.contains("You get some")) {
                count++;
            }
        }

        if (getType == ChatMessageType.OBJECT_EXAMINE) {
            String examineMessage = getEvent.getMessage();
            if (examineMessage == null) {
                return;
            }

            for (eDataTrees treeData : eDataTrees.allTreesData) {
                if (examineMessage.contains(treeData.examineResult)) {
                    treeName = treeData.objectName;
                    treeAction = treeData.action;
                    redwoodMode = Objects.equals(treeName, "Redwood");
                    BotUtils.eActions.updateStatus("New tree selected: " + treeName);
                    break;
                }
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
            g.drawString(ePlainText + count + " (" + actionsPerHour + " per/h)", 15, 210);
            g.drawString("Status: " + BotUtils.eActions.status, 15, 225);

        }
    }
}
