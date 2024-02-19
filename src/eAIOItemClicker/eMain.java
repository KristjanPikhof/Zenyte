package eAIOItemClicker;

import BotUtils.eActions;
import BotUtils.eBanking;
import BotUtils.eData;
import Utility.Trivia.eTriviaInfo;
import eApiAccess.eAutoResponderGui;
import eApiAccess.eAutoResponser;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;
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

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static eApiAccess.eAutoResponser.botStarted;
import static eApiAccess.eAutoResponser.gptDeactivation;

@ScriptManifest(
        author = "Esmaabi",
        category = Category.OTHER,
        description = "<html>"
                + "<p>The most effective item clicker on Zenyte!</p>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "<ul>"
                + "<li></li>"
                + "<li></li>"
                + "<li>Chat GPT answering is integrated.</li>"
                + "</ul>"
                + "</html>",
        discord = "Esmaabi#5752",
        name = "eAIOItemClicker",
        servers = {"Zenyte"},
        version = "0.1"
)

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private static final String eBotName = "eAIOItemClicker";
    private static final String ePaintText = "Products made: ";
    private static SimpleSkills.Skills CHOSEN_SKILL = null;
    private static final Logger logger = Logger.getLogger(eAnglerFisherBot.eMain.class.getName());
    private static final String[] TOOLS_NAME = {"Knife", "Chisel"};

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
        eAutoResponser.scriptPurpose = "you're amethyst darts. ";
        gptDeactivation();
        checkInvForTool();

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
        ctx.viewport.angle(180);
        ctx.viewport.pitch(true);
        lastAnimation = System.currentTimeMillis();
    }

    @Override
    public void onProcess() {
        super.onProcess();

        final SimplePlayer localPlayer = ctx.players.getLocal();

        if (!botStarted) {
            eActions.status = "Please start the bot!";
            return;
        }

        eActions.handleRunning();

        if (!eActions.hasItemsInInventory(eActions.StackableType.NON_STACKABLE, ItemID.CHISEL)) eBanking.bankTask(false, -1, -1, true, ItemID.CHISEL, 1, -1);

        if (eActions.hasItemsInInventory(eActions.StackableType.NON_STACKABLE, ItemID.AMETHYST)) {
            if (localPlayer.getAnimation() == -1 && (eActions.getCurrentTimeMilli() > (lastAnimation + eActions.getRandomInt(3000, 4000)))) {
                useItemOnItem();
            } else if (localPlayer.getAnimation() != -1) {
                lastAnimation = eActions.getCurrentTimeMilli();
            }
        } else {
            eBanking.bankTask(false, -1, 1, true, ItemID.AMETHYST, 27, ItemID.CHISEL);
        }
    }

    // Making cannonballs
    private void useItemOnItem() {
        SimpleItem itemInv = ctx.inventory.populate().filter(ItemID.AMETHYST).reverse().next();
        SimpleItem toolInv = ctx.inventory.populate().filter(TOOLS_NAME).next();

        if (eBanking.bankIsOpen()) ctx.bank.closeBank();

        if (itemInv == null || toolInv == null) {
            return;
        }

        BotUtils.eActions.updateStatus("Using " + itemInv.getName().toLowerCase() + " on " + toolInv.getName().toLowerCase());
        toolInv.click("Use");
        ctx.sleep(50, 100);
        itemInv.click(0);
        ctx.onCondition(this::widgetVisible, 250, 12);

        if (widgetVisible()) {
            int itemsCached = eActions.getItemCountInventory(ItemID.AMETHYST);
            eActions.clickWidget(270, 17); // 270, 17 for darts, 14 for bolts
            ctx.onCondition(() -> itemsCached > eActions.getItemCountInventory(ItemID.AMETHYST), 50, 20);
            BotUtils.eActions.updateStatus("Cutting " + itemInv.getName().toLowerCase());
        }
    }

    private void checkInvForTool() {
        SimpleItem toolInv = ctx.inventory.populate().filter(TOOLS_NAME).next();
        if (CHOSEN_SKILL != null) {
            return;
        }

        if (toolInv.getName().equals("Knife")) {
            CHOSEN_SKILL = SimpleSkills.Skills.FLETCHING;
        } else if (toolInv.getName().equals("Chisel")) {
            CHOSEN_SKILL = SimpleSkills.Skills.CRAFTING;
        } else {
            CHOSEN_SKILL = SimpleSkills.Skills.OVERALL;
            eActions.updateStatus("Please start with knife / chisel");
            eActions.updateStatus("No tool in inventory");
            ctx.stopScript();
        }
    }

    private boolean widgetVisible() {
        SimpleWidget widgetToClick = ctx.widgets.getWidget(270, 17); // 270, 17 for darts, 14 for bolts

        if (widgetToClick == null) {
            return false;
        }
        ctx.keyboard.pressKey(KeyEvent.VK_4);
        return true;
    }

    @Override
    public void onTerminate() {

        // Termination message
        ctx.log("-------------- " + eActions.getCurrentTimeFormatted() + " --------------");
        ctx.log(ePaintText + count);
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
            if (gameMessageTrimmed.contains("cut the")) {
                count += 8;
            }
        }

        eAutoResponser.handleGptMessages(getType, senderName, formattedMessage);
        eTriviaInfo.handleBroadcastMessage(getType, gameMessage);
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
            g.drawString(ePaintText + count + " (" + actionsPerHour + " per/h)", 15, 210);
            g.drawString("Status: " + eActions.status, 15, 225);

        }
    }
}


