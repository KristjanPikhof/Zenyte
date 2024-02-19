package eVolcanicAshMinerBot;

import BotUtils.eActions;
import BotUtils.eBanking;
import BotUtils.eData;
import Utility.Trivia.eTriviaInfo;
import eApiAccess.eAutoResponderGui;
import eApiAccess.eAutoResponser;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;
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
        category = Category.FARMING,
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
        name = "eVolcanicAshMinerBot",
        servers = {"Zenyte"},
        version = "0.1"
)

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private static final String eBotName = "eVolcanicAshMinerBot";
    private static final String ePaintText = "Ash mined";
    private static eAutoResponderGui guiGpt;
    private static final Logger logger = Logger.getLogger(eAnglerFisherBot.eMain.class.getName());
    private static final SimpleSkills.Skills CHOSEN_SKILL = SimpleSkills.Skills.MINING;
    private final WorldPoint rockLocation1 = new WorldPoint(3800, 3767, 0);
    private final WorldPoint rockLocation2 = new WorldPoint(3794, 3773, 0);
    private final WorldPoint rockLocation3 = new WorldPoint(3789, 3769, 0);

    private static final WorldArea MINING_AREA = new WorldArea(3779, 3754, 40, 26, 0);

    // Variables
    private long startTime = 0L;
    private long startingSkillLevel;
    private long startingSkillExp;
    private int count;
    public static boolean hidePaint = false;
    private long lastAnimation = -1;
    private boolean countActive = false;
    private int cachedCount;

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
        eAutoResponser.scriptPurpose = "you're collecting volcanic ash for farming. ";
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

        if (!botStarted) {
            BotUtils.eActions.status = "Please start the bot!";
            return;
        }

        BotUtils.eActions.handleRunning();

        int currentCount = BotUtils.eActions.getNotedItemCount(ItemID.VOLCANIC_ASH);
        handleCount(currentCount);

        if (!BotUtils.eActions.inArea(MINING_AREA)) {
            BotUtils.eActions.status = "Searching for mining area...";
            ctx.pathing.step(new WorldPoint(3794, 3769, 0));
            return;
        }

        if (!localPlayer.isAnimating() && (System.currentTimeMillis() > (lastAnimation + BotUtils.eActions.getRandomInt(1000, 5000)))) {
            miningTask();
        } else if (localPlayer.isAnimating()) {
            lastAnimation = System.currentTimeMillis();
        }

        if (localPlayer.isAnimating()) {
            if (BotUtils.eActions.specialAttackTool) {
                BotUtils.eActions.specialAttack(ItemID.DRAGON_PICKAXE);
            }
        }
    }

    private void miningTask() {
        final SimplePlayer localPlayer = ctx.players.getLocal();
        SimpleObject volcanicAshMine = ctx.objects.populate()
                .omit(o -> !o.getLocation().equals(rockLocation1) &&
                        !o.getLocation().equals(rockLocation2) &&
                        !o.getLocation().equals(rockLocation3))
                .filter(ObjectID.ASH_PILE)
                .filterHasAction("Mine")
                .nearest()
                .next();

        if (volcanicAshMine == null) {
            BotUtils.eActions.status = "Waiting...";
            return;
        }
        BotUtils.eActions.status = "Mining...";
        BotUtils.eActions.interactWith(volcanicAshMine, "Mine");
        ctx.onCondition(localPlayer::isAnimating, 250, 10);
    }

    private boolean ashMineObjectAppeared(WorldPoint location) {
        return !ctx.objects.populate()
                .filter(o -> o.getLocation().equals(location))
                .filter(ObjectID.ASH_PILE)
                .filterHasAction("Mine").isEmpty();
    }

    private void handleCount(int currentCount) {
        if (!countActive) {
            if (count == 0) {
                cachedCount = currentCount;
                countActive = true;
            }
        } else {
            count += (currentCount - cachedCount);
            cachedCount = currentCount;
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
        gptDeactivation();
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

            if (spamMessage.contains("manage to mine some")) {
                count++;
            }
        }
    }

    @Override
    public int loopDuration() {
        return 150;
    }

    @Override
    public void paint(Graphics g) {

        // Check if ash pile is ready to be mined
        if (gptStarted) {
            if (BotUtils.eActions.inArea(MINING_AREA)) {
                WorldPoint[] ashLocations = new WorldPoint[]{rockLocation1, rockLocation2, rockLocation3};
                for (WorldPoint ashTile : ashLocations) {
                    if (ashTile != null) {
                        if (ashMineObjectAppeared(ashTile)) {
                            BotUtils.eActions.drawTileMatrix(ctx, (Graphics2D) g, ashTile, Color.GREEN);
                        } else {
                            BotUtils.eActions.drawTileMatrix(ctx, (Graphics2D) g, ashTile, Color.YELLOW);
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
            g.drawString("Status: " + BotUtils.eActions.status, 15, 225);
        }
    }
}


