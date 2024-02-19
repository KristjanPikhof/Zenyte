package eIronMinerBotZenyte;

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
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimplePlayer;

import java.awt.*;
import java.util.*;
import java.util.List;

import static eApiAccess.eAutoResponser.*;

@ScriptManifest(
        author = "Esmaabi",
        category = Category.MINING,
        description = "<html>"
                + "<p>The most effective Iron Miner Bot on Zenyte!</p>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "<ul>"
                + "<li>Start near Mining Guild banking chest;</li>"
                + "<li>Recommended <b>to zoom out</b>.</li>"
                + "<li><strong>Have any pickaxe equipped or in inventory.</strong>.</li>"
                + "<li>Dragon pickaxe special attack is supported.</li>"
                + "<li>Chat GPT answering is integrated.</li>"
                + "</ul>"
                + "</html>",
        discord = "Esmaabi#5752",
        name = "eIronMinerBot",
        servers = {"Zenyte"},
        version = "1"
)

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private static final String eBotName = "IronMinerBot";
    private static final String ePaintText = "Ores mined";
    private static eAutoResponderGui guiGpt;
    private static final SimpleSkills.Skills CHOSEN_SKILL = SimpleSkills.Skills.MINING;
    private static final WorldPoint MINING_POSITION1 = new WorldPoint(3021, 9721, 0);
    private static final WorldPoint MINING_POSITION2 = new WorldPoint(3029, 9720, 0);
    private static final WorldPoint MINING_POSITION3 = new WorldPoint(3024, 9725, 0);
    private static final WorldPoint rockLocation1 = new WorldPoint(3021, 9720, 0);
    private static final WorldPoint rockLocation2 = new WorldPoint(3020, 9721, 0);
    private static final WorldPoint rockLocation3 = new WorldPoint(3021, 9722, 0);
    private static final WorldPoint rockLocation4 = new WorldPoint(3028, 9720, 0);
    private static final WorldPoint rockLocation5 = new WorldPoint(3029, 9721, 0);
    private static final WorldPoint rockLocation6 = new WorldPoint(3030, 9720, 0);
    private static final WorldPoint rockLocation7 = new WorldPoint(3025, 9725, 0);
    private static final WorldPoint rockLocation8 = new WorldPoint(3024, 9726, 0);
    private static final int[] IRON = {7488, 7455};
    private static final int[] INVENTORY_PICKAXE = {20014, 13243, 12797, 12297, 11920, 1275, 1273, 1271, 1269, 1267, 1265, 30742};

    // Variables
    private long startTime = 0L;
    private long startingSkillLevel;
    private long startingSkillExp;
    private int count;
    public static boolean hidePaint = false;
    private int currentExp;

    // Gui GPT
    private void initializeGptGui() {
        guiGpt = new eAutoResponderGui();
        guiGpt.setVisible(true);
        guiGpt.setLocale(ctx.getClient().getCanvas().getLocale());
    }

    private void initializeMethods() {
        eBanking bankingUtils = new eBanking(ctx);
        eActions actionUtils = new eActions(ctx);
        eData dataUtils = new eData(ctx);
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

        tasks.addAll(Arrays.asList(new eAutoResponser(ctx)));
        initializeMethods(); // BotUtils
        initializeGptGui(); // GPT
        eAutoResponser.scriptPurpose = "you're mining iron for faster xp. ";
        gptDeactivation();

        // Other vars
        ctx.log("--------------- " + eActions.getCurrentTimeFormatted() + " ---------------");
        ctx.log("-------------------------------------");
        ctx.log("            " + eBotName + "         ");
        ctx.log("-------------------------------------");

        // Vars
        eActions.updateStatus("Setting up bot");
        this.startTime = System.currentTimeMillis();
        this.startingSkillLevel = this.ctx.skills.realLevel(CHOSEN_SKILL);
        this.startingSkillExp = this.ctx.skills.experience(CHOSEN_SKILL);
        count = 0;
        ctx.viewport.angle(0);
        ctx.viewport.pitch(true);
        eActions.specialAttackTool = true;
    }

    @Override
    public void onProcess() {
        super.onProcess();

        final SimplePlayer localPlayer = ctx.players.getLocal();

        if (!botStarted) {
            BotUtils.eActions.status = "Please start the bot!";
            return;
        }

        updateExperienceAndCount();

        BotUtils.eActions.handleRunning();

        if (ctx.dialogue.dialogueOpen()) {
            ctx.dialogue.clickContinue();
        }

        SimpleObjects miningRocks = (SimpleObjects) ctx.objects.populate().filter(IRON).filterHasAction("Mine");
        if (ctx.inventory.inventoryFull()) {
            bankTask();
        } else {
            miningTask(miningRocks);
        }

        if (localPlayer.isAnimating()) {
            if (eActions.specialAttackTool) {
                eActions.specialAttack(ItemID.DRAGON_PICKAXE);
            }
        }
    }

    public void bankTask() {
        SimpleObject bankChest = ctx.objects.populate().filter("Bank chest").nearest().next();
        if (bankChest == null || !bankChest.validateInteractable()) {
            return;
        }

        BotUtils.eActions.status = "Banking";
        if (BotUtils.eBanking.bankIsOpen()) {
            ctx.bank.depositAllExcept(INVENTORY_PICKAXE);
            ctx.bank.closeBank();
        } else {
            if (!bankChest.visibleOnScreen()) ctx.pathing.step(bankChest.getLocation());
            BotUtils.eActions.interactWith(bankChest,"Use");
            ctx.onCondition(() -> ctx.bank.bankOpen());
        }
    }

    public void miningTask(SimpleObjects ironObjects) {
        SimpleObject miningRock = ironObjects.nearest().next();

        if (ctx.bank.bankOpen()) ctx.bank.closeBank();

        if (miningRock == null || !miningRock.validateInteractable()) {
            return;
        }

        WorldPoint currentLocation = ctx.players.getLocal().getLocation();
        boolean movedToPosition = false;
        for (WorldPoint position : MINING_POSITION_TO_ORE_MAP.keySet()) {

            if (!miningSpotIsOccupied(position) && !currentLocation.equals(position)) {
                BotUtils.eActions.status = "Getting to mining spot";
                moveToPositionAndMine(miningRock, position);
                movedToPosition = true;
                break;
            }

            if (currentLocation.equals(position)) {
                BotUtils.eActions.status = "Mining...";
                startMining(miningRock);
                movedToPosition = true;
                break;
            }

        }
        if (!movedToPosition) {
            BotUtils.eActions.status = "Waiting for a mining position to free up";
        }
    }


    private void startMining(SimpleObject objectName) {
        if (!ctx.players.getLocal().isAnimating() && !ctx.pathing.inMotion()) BotUtils.eActions.interactWith(objectName, "Mine");
    }
    private static final Map<WorldPoint, WorldPoint[]> MINING_POSITION_TO_ORE_MAP = new LinkedHashMap<WorldPoint, WorldPoint[]>() {{
        put(MINING_POSITION1, new WorldPoint[]{rockLocation1, rockLocation2, rockLocation3});
        put(MINING_POSITION2, new WorldPoint[]{rockLocation4, rockLocation5, rockLocation6});
        put(MINING_POSITION3, new WorldPoint[]{rockLocation7, rockLocation8});
    }};


    private boolean miningSpotIsOccupied(WorldPoint position) {
        return !ctx.players.populate().filterWithin(position, 1)
                .filter(otherPlayer -> !otherPlayer.getName().equals(ctx.players.getLocal().getName())).isEmpty();
    }

    private void moveToPositionAndMine(SimpleObject miningRock, WorldPoint miningPosition) {
        if (miningRock == null) {
            return;
        }

        if (miningPosition == null) {
            return;
        }

        if (ctx.players.getLocal().getLocation() != miningPosition) {
            if (ctx.players.getLocal().getLocation().distanceTo(miningPosition) > 5) {
                ctx.pathing.step(miningPosition);
            }
            ctx.pathing.clickSceneTile(miningPosition, false, true);
            ctx.onCondition(() -> ctx.players.getLocal().getLocation().equals(miningPosition), 400, 4);
        } else {
            BotUtils.eActions.status = "Mining...";
            startMining(miningRock);
        }
    }

    private void updateExperienceAndCount() {
        int newExp = this.ctx.skills.experience(CHOSEN_SKILL);
        if (currentExp != newExp) {
            count++;
            currentExp = newExp;
        }
    }

    private boolean ironMineObjectAppeared(WorldPoint location) {
        return !ctx.objects.populate()
                .filter(o -> o.getLocation().equals(location))
                .filter(IRON)
                .filterHasAction("Mine").isEmpty();
    }

    @Override
    public void onTerminate() {

        // Termination message
        ctx.log("-------------- " + eActions.getCurrentTimeFormatted() + " --------------");
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
        eActions.specialAttackTool = false;
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
    }

    @Override
    public int loopDuration() {
        return 150;
    }

    @Override
    public void paint(Graphics g) {

        // Highlights availabe iron rocks
        if (gptStarted) {
            for (WorldPoint[] oreLocations : MINING_POSITION_TO_ORE_MAP.values()) {
                for (WorldPoint oreTile : oreLocations) {
                    if (oreTile != null) {
                        if (ironMineObjectAppeared(oreTile)) {
                            eActions.drawTileMatrix(ctx, (Graphics2D) g, oreTile, Color.GREEN);
                        } else {
                            eActions.drawTileMatrix(ctx, (Graphics2D) g, oreTile, Color.YELLOW);
                        }
                    }
                }
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
            g.drawString("Status: " + eActions.status, 15, 225);
        }
    }
}


