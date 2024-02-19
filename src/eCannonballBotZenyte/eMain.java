package eCannonballBotZenyte;

import BotUtils.eActions;
import BotUtils.eBanking;
import BotUtils.eData;
import Utility.Trivia.eTriviaInfo;
import eApiAccess.eAutoResponderGui;
import eApiAccess.eAutoResponser;
import net.runelite.api.ChatMessageType;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

import static eApiAccess.eAutoResponser.*;

@ScriptManifest(
        author = "Esmaabi",
        category = Category.SMITHING,
        description = "<html>"
                + "<p>The most effective cannonball maker bot on Zenyte!</p>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "<ul>"
                + "<li>Start near any furnace & bank.</li>"
                + "<li>Make sure you have Ammo mould!</li>"
                + "<li>Chat GPT answering is integrated.</li>"
                + "</ul>"
                + "</html>",
        discord = "Esmaabi#5752",
        name = "eCannonballBotZenyte",
        servers = {"Zenyte"},
        version = "0.1"
)

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private static final String eBotName = "eCannonballBot";
    private static final String ePaintText = "Cannonballs made: ";
    private static final SimpleSkills.Skills CHOSEN_SKILL = SimpleSkills.Skills.SMITHING;
    private static final Logger logger = Logger.getLogger(eAnglerFisherBot.eMain.class.getName());
    private static final String[] FURNACE_NAME = {"Furnace", "Clay forge"};
    private static final int STEEL_BAR = 2353;
    private static final int AMMO_MOULD = 4;

    // Variables
    private int count;
    private long lastAnimation = -1;
    private static eAutoResponderGui guiGpt;
    public static boolean hidePaint = false;
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
        eAutoResponser.scriptPurpose = "you're making cannonballs. ";
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
        ctx.viewport.angle(180);
        ctx.viewport.pitch(true);
        lastAnimation = System.currentTimeMillis();
        BotUtils.eActions.zoomOutViewport();
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

        if (!BotUtils.eActions.hasItemsInInventory(eActions.StackableType.BOTH, AMMO_MOULD)) BotUtils.eBanking.bankTask(false, -1, -1, true, AMMO_MOULD, 1, -1);

        if (BotUtils.eActions.hasItemsInInventory(eActions.StackableType.BOTH, STEEL_BAR)) {
            if (localPlayer.getAnimation() == -1 && (BotUtils.eActions.getCurrentTimeMilli() > (lastAnimation + BotUtils.eActions.getRandomInt(3000, 4000)))) {
                useFurnace();
            } else if (localPlayer.getAnimation() != -1) {
                lastAnimation = BotUtils.eActions.getCurrentTimeMilli();
            }
        } else {
            BotUtils.eBanking.bankTask(false, -1, 1, true, STEEL_BAR, 27, AMMO_MOULD);
        }
    }

    // Making cannonballs
/*    private void useFurnace() {
        SimpleObject furnace = ctx.objects.populate().filter(FURNACE_NAME).next();
        SimpleWidget widget = ctx.widgets.getWidget(270, 14);

        if (BotUtils.eBanking.bankIsOpen()) ctx.bank.closeBank();

        if (widget == null) {
            if (furnace != null && furnace.validateInteractable()) {
                BotUtils.eActions.updateStatus("Clicking furnace");
                furnace.click("Smelt");
                WorldPoint playerLocation = ctx.players.getLocal().getLocation();
                WorldPoint furnaceLocation = furnace.getLocation();
                ctx.onCondition(() -> playerLocation.distanceTo(furnaceLocation) < 5, 250, 12);
            }
        } else {
            if (!widget.isHidden()) {
                BotUtils.eActions.updateStatus("Making cannonballs");
                widget.click(0);
                ctx.onCondition(() -> false, 250, 10);
                lastAnimation = BotUtils.eActions.getCurrentTimeMilli();
            }
        }
    }*/

    private void useFurnace() {
        SimpleObject furnace = ctx.objects.populate().filterHasAction("Smelt").nearest().next();

        if (BotUtils.eBanking.bankIsOpen()) ctx.bank.closeBank();

        if (furnace != null && furnace.validateInteractable()) {
            if (!furnace.visibleOnScreen()) {
                ctx.pathing.step(furnace.getLocation());
                ctx.onCondition(furnace::visibleOnScreen);
            }
            BotUtils.eActions.updateStatus("Clicking " + furnace.getName().toLowerCase());
            WorldPoint playerLocation = ctx.players.getLocal().getLocation();
            WorldPoint furnaceLocation = furnace.getLocation();
            furnace.click("Smelt", furnace.getName());
            ctx.onCondition(() -> playerLocation.distanceTo(furnaceLocation) < 5, 250, 12);
        }

        if (widgetVisible()) {
            int steelBarsCached = BotUtils.eActions.getItemCountInventory(STEEL_BAR);
            BotUtils.eActions.clickWidget(270, 14);
            ctx.onCondition(() -> steelBarsCached > BotUtils.eActions.getItemCountInventory(STEEL_BAR), 50, 20);
        }
    }


    private boolean widgetVisible() {
        SimpleWidget widgetToClick = ctx.widgets.getWidget(270, 14);

        if (widgetToClick == null) {
            return false;
        }
        ctx.keyboard.pressKey(KeyEvent.VK_SPACE);
        return true;
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

        if (getType == ChatMessageType.SPAM) {
            String gameMessageTrimmed = gameMessage.replaceAll("<[^>]+>", "").trim();
            if (gameMessageTrimmed.contains("into your cannonball mould")) {
                count += 8;
            }
        }

        eApiAccess.eAutoResponser.handleGptMessages(getType, senderName, formattedMessage);
        Utility.Trivia.eTriviaInfo.handleBroadcastMessage(getType, gameMessage);
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


