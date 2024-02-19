package eBlastFurnaceZenyte;

import BotUtils.eActions;
import BotUtils.eBanking;
import BotUtils.eData;
import Utility.Trivia.eTriviaInfo;
import eApiAccess.eAutoResponderGui;
import eApiAccess.eAutoResponser;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;
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
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static eApiAccess.eAutoResponser.*;

@ScriptManifest(
        author = "Esmaabi",
        category = Category.MINIGAMES,
        description = "<html>"
                + "<br>The most effective Blast Furnace bot on Zenyte!<br>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "Start <b>at Blast Furnace with chosen bank preset</b>.<br>"
                + "Make sure that preset has been loaded.<br> "
                + "Goldsmithing Gauntlets and Enhanced Ice Gloves supported.<br> "
                + "Start with Goldsmithing Gauntlets in inventory and and Enhanced Ice gloves equipped.<br> "
                + "Stamina potion drinking support added.<br> "
                + "Recommended to have Stamina potion (1) in inventory as preset.<br>"
                + "Chat GPT answering is integrated."
                + "</html>",
        discord = "Esmaabi#5752",
        name = "eBlastFurnaceBot",
        servers = {"Zenyte"},
        version = "1.5"
)

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private static final String eBotName = "eBlastFurnaceBot";
    private static final String ePaintText = "Bars made";
    private static eAutoResponderGui guiGpt;
    private static final Logger logger = Logger.getLogger(eAnglerFisherBot.eMain.class.getName());
    private static final SimpleSkills.Skills CHOSEN_SKILL = SimpleSkills.Skills.SMITHING;
    private final WorldPoint BAR_DISPENSER_LOCATION = new WorldPoint(1940, 4963, 0);
    private final WorldPoint CONVAYOR_BELT_LOCATION = new WorldPoint(1943, 4967, 0);
    private final WorldPoint NEAR_CONVAYOR_BELT_LOCATION = new WorldPoint(1942, 4967, 0);
    private final WorldPoint TAKE_BARS_FROM_DISPENSER_LOCATION = new WorldPoint(1939, 4963, 0);
    private static final int ENHANCED_ICE_GLOVES = 30030;
    private static final int GOLDSMITH_GAUNTLETS = 776;
    private static final int BAR_DISPENSER = 9092;
    private static final List<String> BARS_LIST = Arrays.asList(
            "Bronze bar",
            "Iron bar",
            "Steel bar",
            "Silver bar",
            "Gold bar",
            "Mithril bar",
            "Adamantite bar",
            "Runite bar"
    );
    private static final List<String> ORES_LIST = Arrays.asList(
            "Copper ore",
            "Tin ore",
            "Iron ore",
            "Coal",
            "Silver ore",
            "Gold ore",
            "Mithril ore",
            "Adamantite ore",
            "Runite ore"
    );

    private static int countInv;

    // Variables
    private long startTime = 0L;
    private long startingSkillLevel;
    private long startingSkillExp;
    private int count;
    public static boolean hidePaint = false;
    private boolean oresOnConveyor;

/*    // Define the data structure
    private static final HashMap<String, HashMap<String, Integer>> BAR_TO_ORE_MAP = new HashMap<>();

    // Static block to initialize the mapping
    static {
        // Define ores for each bar and add them to the main map
        BAR_TO_ORE_MAP.put("Bronze bar", createOreMap("Copper ore", 1, "Tin ore", 1));
        BAR_TO_ORE_MAP.put("Iron bar", createOreMap("Iron ore", 1));
        BAR_TO_ORE_MAP.put("Steel", createOreMap("Iron ore", 1, "Coal", 1));
        BAR_TO_ORE_MAP.put("Silver bar", createOreMap("Silver ore", 1));
        BAR_TO_ORE_MAP.put("Gold bar", createOreMap("Gold ore", 1));
        BAR_TO_ORE_MAP.put("Mithril bar", createOreMap("Mithril ore", 1, "Coal", 2));
        BAR_TO_ORE_MAP.put("Adamant bar", createOreMap("Adamantite ore", 1, "Coal", 3));
        BAR_TO_ORE_MAP.put("Rune bar", createOreMap("Runite ore", 1, "Coal", 4));
    }*/

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
        eAutoResponser.scriptPurpose = "you're just doing some bars for smithing. ";
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
        countInv = 0;
        ctx.viewport.angle(0);
        ctx.viewport.pitch(true);
        oresOnConveyor = false;
    }

    @Override
    public void onProcess() {
        super.onProcess();

        final SimplePlayer localPlayer = ctx.players.getLocal();
        Pathing pathing = ctx.pathing;

        if (!botStarted) {
            eActions.status = "Please start the bot!";
            return;
        }

        handleEnergy();

        if (oresOnConveyor) {

            if (BotUtils.eActions.hasItemsInInventory(null, BARS_LIST)) {
                oresOnConveyor = false;
                return;
            }

            if (localPlayer.getLocation().distanceTo(TAKE_BARS_FROM_DISPENSER_LOCATION) > 2) {
                BotUtils.eActions.status = "Walking to bar dispenser";
                pathing.clickSceneTile(TAKE_BARS_FROM_DISPENSER_LOCATION, false, true);
                ctx.onCondition(pathing::inMotion);
            }

            String[] actions = ctx.getClient().getObjectDefinition(BAR_DISPENSER).getImpostor().getActions();
            if (Arrays.asList(actions).contains("Take")) {
                wearGloves(ENHANCED_ICE_GLOVES);
                clickObject(BAR_DISPENSER, "Take", widgetVisible(270, 14));
            }

            if (widgetVisible(270, 14)) {
                BotUtils.eActions.clickWidget(270, 14);
                ctx.onCondition(() -> BotUtils.eActions.hasItemsInInventory(null, BARS_LIST), 50, 20);
                return;
            }
        }

        if (!oresOnConveyor) {

            if (BotUtils.eActions.hasItemsInInventory(null, ORES_LIST)) {
                count += countInv;
                countInv = 0;
                if (!pathing.inMotion()) pathing.step(NEAR_CONVAYOR_BELT_LOCATION);
                wearGloves(GOLDSMITH_GAUNTLETS);
                clickObject(ObjectID.CONVEYOR_BELT, "Put-ore-on", !BotUtils.eActions.hasItemsInInventory(null, ORES_LIST));
                return;
            }

            if (BotUtils.eActions.hasItemsInInventory(null, BARS_LIST)
                    || (!BotUtils.eActions.hasItemsInInventory(null, BARS_LIST) && !BotUtils.eActions.hasItemsInInventory(null, ORES_LIST))) {
                bankTask();
            }
        }
    }

    private void bankTask() {
        SimpleObject bankChest = ctx.objects.populate().filter("Bank chest").nearest().next();
        countInv = countBarsInInventory();
        BotUtils.eActions.status = "Banking";
        if (bankChest != null && bankChest.validateInteractable()) {
            BotUtils.eActions.interactWith(bankChest, "Last Preset");
            ctx.onCondition(() -> !BotUtils.eActions.hasItemsInInventory(null, ORES_LIST), 50, 20);
        }
    }

    private int countBarsInInventory() {
        int totalCount = 0;
        for (String barName : BARS_LIST) {
            totalCount += ctx.inventory.populate().filter(barName).population();
        }
        return totalCount;
    }

    private boolean widgetVisible(int wigetId, int childId) {
        SimpleWidget widgetToClick = ctx.widgets.getWidget(wigetId, childId);

        if (widgetToClick == null) {
            return false;
        }
        ctx.keyboard.pressKey(KeyEvent.VK_SPACE);
        return true;
    }

    private void handleEnergy() {
        Pathing pathing = ctx.pathing;

        if (pathing.energyLevel() < 20) {
            final SimpleItem potion = ctx.inventory.populate().filter(Pattern.compile("Stamina potion\\(\\d+\\)")).filterHasAction("Drink").next();
            final int cached = pathing.energyLevel();
            if (potion == null) {
                return;
            }
            BotUtils.eActions.status = ("Drinking " + potion.getName().toLowerCase());
            if (potion.click("Drink")) {
                ctx.onCondition(() -> pathing.energyLevel() > cached, 50, 20);
            }
        }

        if (pathing.energyLevel() >= 30 && !pathing.running()) {
            pathing.running(true);
        }
    }

    private void wearGloves(int glovesInt) {
        SimpleItem theGloves = ctx.inventory.populate().filter(glovesInt).next();

        if (theGloves != null) {
            BotUtils.eActions.status = "Wearing " + theGloves.getName().toLowerCase();
            if (theGloves.click(0)) {
                ctx.onCondition(() -> !BotUtils.eActions.hasItemsInInventory(null, glovesInt), 50, 20);
            }
        }
    }

    private void clickObject(int objetId, String actionName, boolean condition) {
        SimpleObject objectToClick = ctx.objects.populate().filter(objetId).nearest().next();

        if (objectToClick != null) {
            String objectName = "Conveyor belt".equals(objectToClick.getName()) ? "Conveyor belt" : "Bar dispenser";
            BotUtils.eActions.status = "Clicking " + objectName.toLowerCase();
            BotUtils.eActions.interactWith(objectToClick, actionName);
            ctx.onCondition(() -> condition, 50, 20);
        }
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


        if (getType == ChatMessageType.GAMEMESSAGE) {
            if (gameMessage.contains("your ore goes onto the conveyor belt")) {
                BotUtils.eActions.status = "Ores are on conveyor belt!";
                oresOnConveyor = true;
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


