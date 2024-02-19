package eZMIRunecrafting;

import BotUtils.eActions;
import BotUtils.eBanking;
import BotUtils.eData;
import BotUtils.eLogGenius;
import Utility.Trivia.eTriviaInfo;
import eApiAccess.eAutoResponderGui;
import eApiAccess.eAutoResponser;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimplePrayers;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Game;
import simple.hooks.simplebot.Magic;
import simple.hooks.simplebot.Pathing;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.utils.WorldArea;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static BotUtils.eActions.random;
import static eApiAccess.eAutoResponser.*;

@ScriptManifest(
        author = "Esmaabi",
        category = Category.RUNECRAFTING,
        description = "<html>"
                + "<p>Enhance your runecrafting experience with the Ourania ZMI Bot, the most efficient runecrafting bot!</p>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "<ul>"
                + "<li>Make sure to <b>start the bot</b> while at the Ourania Cave \"bank\" or near the Chaos Altar.</li>"
                + "<li>Handles energy management by consuming stamina potions. You must include <b>stamina potion (1)</b> in your \"Last Preset\" for this feature.</li>"
                + "<li>For health management, include your choice of food in the \"Last Preset\". The bot automatically consumes food when health falls below 40%.</li>"
                + "<li>Bot will efficiently use \"Protect from Missiles\" and \"Rapid Healing\" for protection inside the cave (Prayer lvl 40 requirement).</li>"
                + "<li>Seamless banking experience with the use of \"Last Preset\". You must set up your \"Last Preset\" with a full inventory of essence and \"empty pouches\".</li>"
                + "<li>Supported to use <strong>all pouches</strong> & recommended order w/o Colossal: Giant, Small, Large, Medium. You can use only few if you want!</li>"
                + "<li>Provides real-time status updates, keeping you informed of each action and any issues encountered.</li>"
                + "<li>Automates the entire process of runecrafting, including pouch filling and emptying, rune crafting, and teleporting.</li>"
                + "<li>It is required to use \"Lunar spellbook\" to teleport to \"Ourania\", also recommended to wield any earth staff.</li>"
                + "<li>If there are not enough runes to teleport, bot will run back.</li>"
                + "<li>Integrated chat GPT answering for interactive responses.</li>"
                + "</ul>"
                + "</html>",
        discord = "Esmaabi#5752",
        name = "eEffortlessZMI",
        servers = {"Zenyte"},
        version = "0.2"
)

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private static final String eBotName = "eEffortlessZMI";
    private static final String ePaintText = "Runs made";
    private static final String eVersion = "v 0.2";
    private static final SimpleSkills.Skills CHOSEN_SKILL = SimpleSkills.Skills.RUNECRAFT;
    private static final Logger logger = Logger.getLogger(eAnglerFisherBot.eMain.class.getName());

    public static final WorldArea OURANIA_CAVE_AREA = new WorldArea(new WorldPoint(2999, 5635, 0), new WorldPoint(3074, 5565, 0));
    public static final WorldArea OURANIA_RC_ALTAR_AREA = new WorldArea(new WorldPoint(3072, 5591, 0), new WorldPoint(3051, 5571, 0));
    public static final WorldArea OURANIA_BANKING_AREA = new WorldArea(new WorldPoint(3005, 5632, 0), new WorldPoint(3028, 5620, 0));

    private final WorldPoint[] WALKING_TO_ALTAR = {
            new WorldPoint(3015, 5620, 0),
            new WorldPoint(3017, 5613, 0),
            new WorldPoint(3016, 5604, 0),
            new WorldPoint(3018, 5597, 0),
            new WorldPoint(3017, 5590, 0),
            new WorldPoint(3019, 5584, 0),
            new WorldPoint(3021, 5579, 0),
            new WorldPoint(3029, 5578, 0),
            new WorldPoint(3034, 5583, 0),
            new WorldPoint(3040, 5582, 0),
            new WorldPoint(3047, 5579, 0),
            new WorldPoint(3053, 5579, 0),
            new WorldPoint(3058, 5579, 0)
    };

    private final WorldPoint[] WALKING_TO_ALTAR2 = {
            new WorldPoint(3014, 5618, 0),
            new WorldPoint(3014, 5611, 0),
            new WorldPoint(3014, 5605, 0),
            new WorldPoint(3014, 5599, 0),
            new WorldPoint(3014, 5591, 0),
            new WorldPoint(3015, 5583, 0),
            new WorldPoint(3021, 5578, 0),
            new WorldPoint(3031, 5577, 0),
            new WorldPoint(3037, 5582, 0),
            new WorldPoint(3043, 5580, 0),
            new WorldPoint(3051, 5578, 0),
            new WorldPoint(3057, 5578, 0)
    };

    // Variables
    private int count;
    private static eAutoResponderGui guiGpt;
    public static boolean hidePaint = false;
    private long startTime = 0L;
    private long startingSkillExp;
    private long startingSkillLevel;
    private boolean bankingCompleted = false;
    private boolean runecraftingCompleted = false;
    private boolean prayingAtAltar = false;
    private boolean noRunesToTele = false;
    private boolean fillingPouches = false;
    private boolean emptyingPouches = false;
    private boolean prayerIsOn;

    eLogGenius elog = new eLogGenius(ctx);


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
        eAutoResponser.scriptPurpose = "you're just doing Runecrafting for faster xp. ";
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
        ctx.viewport.angle(180);
        ctx.viewport.pitch(true);
        eActions.zoomOutViewport();
        setBooleans(false);
        prayerIsOn = true;
    }

    @Override
    public void onProcess() {
        super.onProcess();

        if (!botStarted) {
            eActions.status = "Please start the bot!";
            return;
        }

        if (ctx.magic.spellBook() != Magic.SpellBook.LUNAR) {
            switchTabs(Game.Tab.MAGIC, Game.Tab.INVENTORY, 4);
            eActions.status = "Lunar spellbook required!";
            ctx.log("Stopping script");
            ctx.log("Please change spellbook to Lunar");
            ctx.sleep(5000);
            ctx.stopScript();
            return;
        }

        handleEnergy();

        if (isOutsideOuraniaCave()) {
            handleOutsideCave();
        } else {
            handleInsideCave();
        }
    }

    private boolean isOutsideOuraniaCave() {
        return !ctx.pathing.inArea(OURANIA_CAVE_AREA);
    }

    private void handleOutsideCave() {
        if (!prayingAtAltar && ctx.skills.level(SimpleSkills.Skills.PRAYER) != ctx.skills.realLevel(SimpleSkills.Skills.PRAYER)) {
            prayAtChaosAltar();
        } else {
            climbDownToCave();
        }
    }

    private void prayAtChaosAltar() {
                SimpleObject chaosAltar = ctx.objects.populate().filter(411).next();
        if (elog.isValid(chaosAltar)) {
            eActions.status = "Praying at " + chaosAltar.getName().toLowerCase();
            eActions.interactWith(chaosAltar, "Pray-at");
        } else {
            eActions.status = "Chaos Altar not found";
            teleportToOurania();
        }
    }

    private void climbDownToCave() {
        SimpleObject ladderDown = ctx.objects.populate().filter(29635).next();
        if (elog.isValid(ladderDown)) {
            eActions.status = "Climbing down the ladder";
            eActions.interactWith(ladderDown, "Climb");
            setBooleans(false);
            eActions.openTab(Game.Tab.INVENTORY);
            prayingAtAltar = true;
        } else {
            eActions.status = "Ladder not found";
        }
    }

    private void handleInsideCave() {
        if (noRunesToTele && !ctx.pathing.inArea(OURANIA_BANKING_AREA)) {
            outOfTeleportRunes();
        } else {
            if (!bankingCompleted) {
                performBankingTasks();
            } else {
                runecraftAtAltar();
            }
        }

        if (ctx.combat.healthPercent() < 40) {
            if (!eatFood()) {
                ctx.log("Out of food, teleporting home");
                teleportToHome();
                ctx.sendLogout();
                ctx.stopScript();
            }
        }
    }

    private void performBankingTasks() {
        SimpleNpc banker = ctx.npcs.populate().filter("Eniola").next();
        boolean essenceInInv = !ctx.inventory.populate().filterContains("essence").isEmpty();

        if (!elog.isValid(banker)) {
            eActions.status = "Banker not found";
            return;
        }

        if (!essenceInInv || !ctx.inventory.inventoryFull()) {
            eActions.status = "Loading preset from bank";
            BotUtils.eActions.interactWith(banker, "Last Preset");
            return;
        }

        if (!fillingPouches) {
            fillPouches(banker);
            return;
        }

/*        if (eActions.hasItemsInInventory(eActions.StackableType.NON_STACKABLE, ItemID.PURE_ESSENCE, ItemID.RUNE_ESSENCE) && fillingPouches) {
            eActions.status = "Banking completed";
            setBooleans(false);
            bankingCompleted = true;
        }*/


        eActions.status = "Banking completed";
        setBooleans(false);
        bankingCompleted = true;
    }

    private void fillPouches(SimpleNpc banker) {
        if (!eBanking.bankIsOpen()) {
            eActions.interactWith(banker, "Last Preset");

            if (eActions.hasItemsInInventory(eActions.StackableType.NON_STACKABLE, "pure essence", "rune essence")) {
                eActions.interactWith(banker, "Bank");
            }
        }

        if (eBanking.bankIsOpen()) {
            eActions.status = "Starting to fill pouches";
            List<SimpleItem> pouches = ctx.inventory.populate().filterContains("pouch").toStream()
                    .filter(elog::isValid)
                    .collect(Collectors.toList());

            for (SimpleItem pouch : pouches) {
                String pouchName = pouch.getName().toLowerCase();
                eActions.status = "Filling " + pouchName;

                if (pouchName.contains("colossal")) {
                    while (!fillingPouches) {
                        eActions.status = "Filling " + pouchName;
                        pouch.click("Fill");
                        ctx.sleep(50);
                        if (fillingPouches) {
                            ctx.bank.closeBank();
                            break;
                        }
                    }
                } else {
                    eActions.status = "Filling " + pouchName;
                    pouch.click("Fill");
                    ctx.sleep(50);
                }
            }
            ctx.bank.closeBank();
            fillingPouches = true;
        }
    }

    private void runecraftAtAltar() {
        if (!ctx.pathing.inArea(OURANIA_RC_ALTAR_AREA)) {
            setPrayers(prayerIsOn);
            eActions.status = "Walking to altar";

            // Randomly choose between the two paths for next step
            if (random.nextDouble() < 0.75) {
                BotUtils.eActions.walkPath(OURANIA_RC_ALTAR_AREA, WALKING_TO_ALTAR2, false);
            } else {
                BotUtils.eActions.walkPath(OURANIA_RC_ALTAR_AREA, WALKING_TO_ALTAR, false);
            }

            useItemOnItemFletch();

        } else {
            craftingRunes();
        }
    }

/*    private void useItemOnItemFletch() {
        SimpleItem itemInv = ctx.inventory.populate().filter(n -> n.getName().toLowerCase().contains("dart tip")).next();
        SimpleItem featherInv = ctx.inventory.populate().filter(ItemID.FEATHER).next();
        if (eBanking.bankIsOpen()) ctx.bank.closeBank();

        if (itemInv == null || featherInv == null) {
            return;
        }

        BotUtils.eActions.status = "Fletching";
        featherInv.click(0);
        itemInv.click(0);
    }*/

    private void useItemOnItemFletch() {
        Iterable<SimpleItem> inventoryItems = ctx.inventory.populate();

        SimpleItem itemInv = null;
        SimpleItem featherInv = null;

        for (SimpleItem item : inventoryItems) {
            String itemName = item.getName().toLowerCase();
            if (itemInv == null && itemName.contains("dart tip")) {
                itemInv = item;
            } else if (featherInv == null && item.getId() == ItemID.FEATHER) {
                featherInv = item;
            }

            if (itemInv != null && featherInv != null) {
                break;
            }
        }

        if (itemInv == null || featherInv == null) {
            return;
        }

        if (!eBanking.bankIsOpen()) {
            BotUtils.eActions.status = "Fletching " + itemInv.getName().toLowerCase();
            featherInv.click(0);
            itemInv.click(0);
            ctx.sleep(50);
        }
    }


    private void craftingRunes() {
        SimpleObject RC_ALTAR = ctx.objects.populate().filter(29631).next();
        boolean essenceInInv = !ctx.inventory.populate().filterContains("essence").isEmpty();

        if (!elog.isValid(RC_ALTAR)) {
            eActions.status = "Altar not found";
            return;
        }

        if (!runecraftingCompleted) {
            if (essenceInInv) {
                eActions.status = "Crafting runes";
                eActions.interactWith(RC_ALTAR, "Craft-rune");
            } else if (!emptyingPouches) {
                emptyPouches(RC_ALTAR);
                return;
            }
        }

        if (emptyingPouches && !essenceInInv && !noRunesToTele) {
            eActions.status = "Teleporting back";
            teleportToOurania();
            runecraftingCompleted = true;
        }
    }

    private void teleportToOurania() {
        if (ctx.magic.castSpellOnce("Ourania Teleport")) {
            ctx.onCondition(() -> ctx.players.getLocal().getLocation().getRegionID() == 9778, 250, 10);
        }
    }

    private void emptyPouches(SimpleObject RC_ALTAR) {
        eActions.status = "Starting to empty pouches";
        List<SimpleItem> pouches = ctx.inventory.populate().filterContains("pouch").toStream()
                .filter(elog::isValid)
                .collect(Collectors.toList());

        for (SimpleItem pouch : pouches) {
            String pouchName = pouch.getName().toLowerCase();

            if (pouchName.contains("colossal")) {
                while (!emptyingPouches) {
                    eActions.status = "Emptying " + pouchName;
                    pouch.click("Empty");
                    ctx.sleep(50);
                    eActions.status = "Clicking " + RC_ALTAR.getName().toLowerCase();
                    eActions.interactWith(RC_ALTAR, "Craft-rune");
                    ctx.sleep(50);
                    if (emptyingPouches) {
                        break;
                    }
                }
            } else {
                eActions.status = "Emptying " + pouchName;
                pouch.click("Empty");
                ctx.sleep(50);
                eActions.status = "Clicking " + RC_ALTAR.getName().toLowerCase();
                eActions.interactWith(RC_ALTAR, "Craft-rune");
                ctx.sleep(50);
            }
        }
        emptyingPouches = true;
    }

    private void outOfTeleportRunes() {
        setBooleans(false);
        noRunesToTele = true;
        if (!ctx.pathing.inArea(OURANIA_BANKING_AREA)) {
            eActions.status = "Running back to bank";
            setPrayers(prayerIsOn);
            BotUtils.eActions.walkPath(OURANIA_BANKING_AREA, WALKING_TO_ALTAR, true);
        } else {
            setBooleans(false);
        }
    }

    private void setBooleans(boolean set) {
        bankingCompleted = set;
        runecraftingCompleted = set;
        emptyingPouches = set;
        fillingPouches = set;
        prayingAtAltar = set;
        noRunesToTele = set;
    }

    private void setPrayers(boolean set) {
        if (ctx.prayers.points() > 0) {
            ctx.prayers.prayer(SimplePrayers.Prayers.PROTECT_FROM_MISSILES, set);
            ctx.prayers.prayer(SimplePrayers.Prayers.RAPID_HEAL,set);
        }
        ctx.combat.toggleAutoRetaliate(false);
    }

    public boolean eatFood() {
        SimpleItem food = ctx.inventory.populate().filterHasAction("Eat").next();

        if (food == null) return false;
        eActions.status = "Eating " + food.getName().toLowerCase();
        food.click(0);
        return true;
    }

    public void teleportToHome() {
        if (ctx.pathing.inArea(eActions.EDGE_HOME_AREA)) {
            return;
        }

        eActions.status = "Teleporting to Edgeville";
        SimpleItem tab = ctx.inventory.populate().filter("Zenyte home teleport").next();

        if (tab == null) {
            return;
        }

        tab.click(0);
        ctx.onCondition(() -> ctx.pathing.inArea(eActions.EDGE_HOME_AREA), 250, 2);
    }

    private void handleEnergy() {
        Pathing pathing = ctx.pathing;

        if (pathing.energyLevel() < 20) {
            final SimpleItem potion = ctx.inventory.populate()
                    .filter(Pattern.compile("Stamina potion\\(\\d+\\)"), Pattern.compile("Super energy potion\\(\\d+\\)"))
                    .filterHasAction("Drink").next();
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

    private void switchTabs(Game.Tab tabOne, Game.Tab tabTwo, int times) {
        for (int i = 0; i < times; i++) {
            ctx.game.tab(tabOne);
            ctx.game.tab(tabTwo);
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
        setBooleans(false);
        emptyingPouches = true;
        fillingPouches = true;
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

/*        if (elog.printChatContaining(m, "You pray to the gods...", false)) {
            prayingAtAltar = true;
            count++;
        }*/

        if (elog.printChatContaining(m, "has been loaded", false)) {
            count++;
        }

        if (elog.printChatContaining(m, "You already have full prayer", false)) {
            prayingAtAltar = true;
        }

        if (elog.printChatContaining(m, "You do not have enough", false)) {
            noRunesToTele = true;
        }

        if (elog.printChatContaining(m, "Your pouch is empty", false)) {
            emptyingPouches = true;
        }

        if (elog.printChatContaining(m, "Your pouch is already full", false)) {
            fillingPouches = true;
        }

        if (elog.printChatType(m, ChatMessageType.GAMEMESSAGE, false) &&
                (elog.printChatContaining(m, "track unlocks", false) || elog.printChatContaining(m, "track was unlocked", false))) {
            prayerIsOn = !prayerIsOn;
            ctx.log("Prayer has been toggled: " + (prayerIsOn ? "ON" : "OFF"));
        }

        elog.printChatContaining(m, "Inventory was loaded successfully", false);

        eAutoResponser.handleGptMessages(getType, senderName, formattedMessage);
        eTriviaInfo.handleBroadcastMessage(getType, gameMessage);
    }

    @Override
    public int loopDuration() {
        return 200;
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
            g.drawString(eVersion, 175, 135);
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


