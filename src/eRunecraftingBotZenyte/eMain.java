package eRunecraftingBotZenyte;

import eRandomEventSolver.eRandomEventForester;
import eRunecraftingBotZenyte.listeners.SkillListener;
import eRunecraftingBotZenyte.listeners.SkillObserver;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleBank;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Game;
import simple.hooks.simplebot.Pathing;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.utils.WorldArea;

import java.awt.*;
import java.awt.Point;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

import static simple.hooks.filters.SimpleSkills.Skills.*;


@ScriptManifest(author = "Esmaabi", category = Category.RUNECRAFTING, description = " "
        + "Please read <b>eRunecraftingBot</b> description first!</b><br>"
        + "<br><b>Description</b>:<br>"
        + "It is required to have chisel in inventory for <b>Mining</b> and <b>Running Bloods</b> tasks<br>"
        + "Start near dense runestone for <b>Mining</b> task while <b>Zenyte deposit chest</b> is activated<br>"
        + "Start at Crafting Guild with <b>Max cape</b> for other tasks<br><br> "
        + "For more information check out Esmaabi on SimpleBot!", discord = "Esmaabi#5752",
        name = "eRunecraftingBotZenyte", servers = { "Zenyte" }, version = "2.1")

public class eMain extends TaskScript implements SkillListener, LoopingScript {


    // Vars
    public static boolean started;
    private long startTime = 0L;
    public static String status = null;
    public static State playerState;
    private long lastAnimation = -1;
    private int darkBlocks;
    private int bloodRunes;
    private int denseEssence;
    private boolean chiselTask = false;
    boolean runningSkillListener = true;
    private boolean scriptStopped = false;
    private static String playerGameName;
    private static boolean hidePaint;


    // Vars for Paint / Stats
    private static final Color PhilippineRed = new Color(196, 18, 48);
    private static final Color RaisinBlack = new Color(35, 31, 32, 127);
    private static int runecraftingXp, miningXp, craftingXp = 0;
    private static int runecraftingLvl, miningLvl, craftingLvl = 0;
    private static int runecraftingLvlGained, miningLvlGained, craftingLvlGained = 0;
    private static int startingSkillLevelMining, startingSkillLevelCrafting, startingSkillLevelRunecrafting;
    private static int startingSkillExpMining, startingSkillExpCrafting, startingSkillExpRunecrafting;

    // Areas
    private final WorldArea craftingGuild = new WorldArea (new WorldPoint(2938, 3288, 0), new WorldPoint(2929, 3279, 0));
    private final WorldArea arecuusAltarArea = new WorldArea (new WorldPoint(1683, 3893, 0), new WorldPoint(1726, 3872, 0));
    private final WorldArea bloodAltarArea = new WorldArea(new WorldPoint(1710,3836, 0), new WorldPoint(1722,3822, 0));
    private final WorldArea arecuusWholeArea = new WorldArea(new WorldPoint(1745,3898, 0), new WorldPoint(1652,3821, 0));
    private final WorldPoint northDenseObjectLocation = new WorldPoint(1764, 3858, 0);
    private final WorldPoint southDenseObjectLocation = new WorldPoint(1764, 3846, 0);

    private static final WorldArea miningArea = new WorldArea (
            new WorldPoint(1769, 3849, 0),
            new WorldPoint(1770, 3854, 0),
            new WorldPoint(1771, 3860, 0),
            new WorldPoint(1770, 3865, 0),
            new WorldPoint(1762, 3866, 0),
            new WorldPoint(1757, 3862, 0),
            new WorldPoint(1757, 3854, 0),
            new WorldPoint(1758, 3842, 0),
            new WorldPoint(1766, 3840, 0),
            new WorldPoint(1769, 3846, 0));

    private final WorldPoint[] darkAltarToBloodAltar = {
            new WorldPoint(1697, 3880, 0),
            new WorldPoint(1690, 3880, 0),
            new WorldPoint(1684, 3881, 0),
            new WorldPoint(1678, 3881, 0),
            new WorldPoint(1672, 3881, 0),
            new WorldPoint(1667, 3881, 0),
            new WorldPoint(1662, 3881, 0),
            new WorldPoint(1658, 3879, 0),
            new WorldPoint(1658, 3874, 0),
            new WorldPoint(1659, 3870, 0),
            new WorldPoint(1661, 3866, 0),
            new WorldPoint(1665, 3863, 0),
            new WorldPoint(1670, 3862, 0),
            new WorldPoint(1674, 3860, 0),
            new WorldPoint(1679, 3859, 0),
            new WorldPoint(1686, 3858, 0),
            new WorldPoint(1691, 3857, 0),
            new WorldPoint(1696, 3857, 0),
            new WorldPoint(1702, 3857, 0),
            new WorldPoint(1707, 3857, 0),
            new WorldPoint(1713, 3857, 0),
            new WorldPoint(1718, 3856, 0),
            new WorldPoint(1723, 3856, 0),
            new WorldPoint(1728, 3854, 0),
            new WorldPoint(1734, 3851, 0),
            new WorldPoint(1735, 3848, 0),
            new WorldPoint(1735, 3843, 0),
            new WorldPoint(1734, 3837, 0),
            new WorldPoint(1735, 3833, 0),
            new WorldPoint(1733, 3828, 0),
            new WorldPoint(1728, 3826, 0),
            new WorldPoint(1721, 3826, 0),
            new WorldPoint(1719, 3828, 0)
    };

    public static int randomSleeping(int minimum, int maximum) {
        return (int)(Math.random() * (maximum - minimum)) + minimum;
    }

    // Tasks
    private final java.util.List<Task> tasks = new ArrayList<>();

    @Override
    public boolean prioritizeTasks() {
        return true;
    }

    @Override
    public List<Task> tasks() {
        return tasks;
    }

    @Override
    public int loopDuration() {
        return 150;
    }

    enum State {
        MINING,
        CRAFTING,
        BLOODCRAFTING,
        WAITING,
    }

    @Override
    public void onExecute() {
        tasks.addAll(Arrays.asList());

        System.out.println("Started eRunecraftingBot Zenyte!");
        started = false;
        status = "Setting up config";
        startTime = System.currentTimeMillis(); //paint
        ctx.viewport.angle(90);
        ctx.viewport.pitch(true);
        darkBlocks = 0;
        bloodRunes = 0;
        denseEssence = 0;

        this.ctx.updateStatus("----------------------------------");
        this.ctx.updateStatus("      eRunecraftingBotZenyte      ");
        this.ctx.updateStatus("----------------------------------");

        //Mining
        startingSkillLevelMining = this.ctx.skills.realLevel(SimpleSkills.Skills.MINING);
        startingSkillExpMining = this.ctx.skills.experience(SimpleSkills.Skills.MINING);

        //Crafting
        startingSkillLevelCrafting = this.ctx.skills.realLevel(SimpleSkills.Skills.CRAFTING);
        startingSkillExpCrafting = this.ctx.skills.experience(SimpleSkills.Skills.CRAFTING);

        //Runecrafting
        startingSkillLevelRunecrafting = this.ctx.skills.realLevel(RUNECRAFT);
        startingSkillExpRunecrafting = this.ctx.skills.experience(RUNECRAFT);

        //GUI
        eGui gui = new eGui();
        gui.setVisible(true);

        //skill listener
        runningSkillListener = true;
        SkillObserver skillObserver = new SkillObserver(ctx, new BooleanSupplier() {

            public boolean getAsBoolean() {
                return runningSkillListener;
            }
        });
        skillObserver.addListener(this);
        skillObserver.start();

    }

    @Override
    public void onProcess() {
        super.onProcess();
        if (!started) {
            playerState = State.WAITING;
            return;
        }

        // Running
        if (!ctx.pathing.running()) {
            handleEnergy();
        }

        switch (playerState) {
            case MINING:
                onMiningState();
                break;

            case CRAFTING:
                onCraftingState();
                break;

            case BLOODCRAFTING:
                onBloodCraftingState();
                break;

            default:
                break;
        }
    }

    private void onMiningState() {
        if (!miningArea.containsPoint(ctx.players.getLocal().getLocation())) {
            return;
        }

        if (ctx.inventory.inventoryFull() || ctx.bank.depositBoxOpen()) {
            depositTask();
        } else {
            if (!ctx.players.getLocal().isAnimating() && (System.currentTimeMillis() > (lastAnimation + randomSleeping(600, 8000)))) {
                miningTask();
            } else if (ctx.players.getLocal().isAnimating()) {
                lastAnimation = System.currentTimeMillis();
            }
        }
    }

    private void onCraftingState() {
        if (ctx.inventory.populate().filter(13445).isEmpty()) {
            if (!craftingGuild.containsPoint(ctx.players.getLocal().getLocation())) {
                teleportToBankTask();
            } else {
                bankTask();
            }
        }
        if (!ctx.inventory.populate().filter(13445).isEmpty()) {
            if (craftingGuild.containsPoint(ctx.players.getLocal().getLocation())) {
                teleportToArceuusTask();
            }

            if (arecuusAltarArea.containsPoint(ctx.players.getLocal().getLocation())) {
                venerateTask();
            }
        }
    }

    private void onBloodCraftingState() {
        if (chiselTask) {
            chiselTaskStart();
            return;
        }

        if (!ctx.inventory.populate().filter(13446, 1755).isEmpty()
                && ctx.inventory.populate().filter(7938).isEmpty()
                && (craftingGuild.containsPoint(ctx.players.getLocal().getLocation()) || ctx.pathing.inArea(bloodAltarArea))
                && !chiselTask) {
            status = "Making fragments";
            chiselTask = true;
            chiselTaskStart();
        }

        if (!ctx.inventory.populate().filter(13446).isEmpty() &&
                !ctx.inventory.populate().filter(7938).isEmpty() &&
                craftingGuild.containsPoint(ctx.players.getLocal().getLocation())) {
            teleportToArceuusTask();
            return;
        }

        if (!ctx.inventory.populate().filter(13446).isEmpty() &&
                !ctx.inventory.populate().filter(7938).isEmpty() &&
                arecuusWholeArea.containsPoint(ctx.players.getLocal().getLocation()) &&
                !bloodAltarArea.containsPoint(ctx.players.getLocal().getLocation())) {
            status = "Getting to Blood Altar";
            walkPath(bloodAltarArea, darkAltarToBloodAltar, false);
            return;
        }

        if (ctx.pathing.inArea(bloodAltarArea) && !chiselTask) {
            if (!ctx.inventory.populate().filter(13446).isEmpty() || !ctx.inventory.populate().filter(7938).isEmpty()) {
                status = "Making Blood runes";
                SimpleObject bloodAltar = ctx.objects.populate().filter(27978).nearest().next();
                bloodAltar.click("Bind", "Blood Altar");
                ctx.sleep(2000);
            } else if (ctx.inventory.populate().filter(7938).isEmpty() && ctx.inventory.populate().filter(13446).isEmpty()) {
                bloodRunes += ctx.inventory.populate().filter(565).population(true);
                teleportToBankTask();
            }
        } else if (craftingGuild.containsPoint(ctx.players.getLocal().getLocation()) && !chiselTask &&
                (ctx.inventory.populate().filter(13446).isEmpty() || ctx.inventory.populate().filter(7938).isEmpty())) {
            bankTask();
        }
    }


    // Mining
    private void depositTask() {
        final int BANK_CHEST_ID = 29090;
        SimpleObject depositBox = ctx.objects.populate().filter(BANK_CHEST_ID).filterHasAction("Deposit").nearest().next();

        if (depositBox == null || !depositBox.validateInteractable()) {
            return;
        }

        if (!ctx.bank.depositBoxOpen()) {
            status = "Opening deposit box";
            depositBox.click("Deposit");
            ctx.onCondition(() -> ctx.bank.depositBoxOpen());
        }

        if (ctx.bank.depositBoxOpen()) {
            status = "Depositing inventory";
            ctx.bank.depositAllExcept("Chisel");
            ctx.bank.closeBank();
            ctx.onCondition(() -> !ctx.bank.depositBoxOpen());
        }
    }

    private void miningTask() {
        int southRunestoneImposter = ctx.getClient().getObjectDefinition(10796).getImpostor().getId();
        int northRunestoneImposter = ctx.getClient().getObjectDefinition(8981).getImpostor().getId();

        if (southRunestoneImposter == ObjectID.DEPLETED_RUNESTONE && northRunestoneImposter == ObjectID.DEPLETED_RUNESTONE) {
            status = "Both runestones are depleted";
            //ctx.log("Both runestones are depleted");
            ctx.sleep(3000);
            return;
        }

        if (ctx.combat.getSpecialAttackPercentage() == 100) {
            specialAttack();
        }

        WorldPoint playerLocation = ctx.players.getLocal().getLocation();

        if (southRunestoneImposter == ObjectID.DENSE_RUNESTONE
                && (northRunestoneImposter == ObjectID.DEPLETED_RUNESTONE || playerLocation.distanceTo(southDenseObjectLocation) <= playerLocation.distanceTo(northDenseObjectLocation))) {
            status = "Mining south runestone...";
            clickRunestone(southDenseObjectLocation);
            return;
        }

        if (northRunestoneImposter == ObjectID.DENSE_RUNESTONE) {
            status = "Mining north runestone...";
            clickRunestone(northDenseObjectLocation);
            return;
        }
    }

    private void clickRunestone(WorldPoint location) {
        SimpleObject runestone = ctx.objects.populate().filter("Dense runestone").filterHasAction("Chip").nearest(location).next();
        runestone.click("Chip");
        ctx.onCondition(() -> ctx.players.getLocal().isAnimating(), 250, 20);
    }

    private void specialAttack() { //special attack for mining state
        if (ctx.equipment.populate().filter("Dragon pickaxe").isEmpty()) {
            return;
        }

        if (ctx.pathing.inArea(miningArea)) {
            ctx.combat.toggleSpecialAttack(true);
            ctx.game.tab(Game.Tab.INVENTORY);
        }
    }

    // Banking task for crafting and bloodcrafting states
    private void bankTask() {
        final Pattern STAMINA_POTION_PATTERN = Pattern.compile("Stamina potion\\(\\d+\\)");
        int[] EXCLUDE_STAMINA_POTS = {12625, 12627, 12629, 12631}; // Stamina potions
        int[] EXCLUDE_DEPOSIT_ITEMS = {1755, 7938, 12625, 12627, 12629, 12631}; // Stamina potions and other items
        int ENERGY_THRESHOLD = 30;

        status = "Starting banking task";
        SimpleObject bankChest = ctx.objects.populate().filter(14886).filterHasAction("Use").nearest().next(); // bank chest
        if (bankChest == null || !bankChest.validateInteractable()) {
            return;
        }
        if (!ctx.bank.bankOpen()) {
            status = "Opening bank";
            bankChest.click("Use");
            ctx.onCondition(() -> ctx.bank.bankOpen(), 250, 8);
        } else {

            if (playerState == State.CRAFTING) {
                ctx.bank.depositAllExcept(EXCLUDE_STAMINA_POTS);
                if (ctx.pathing.energyLevel() <= ENERGY_THRESHOLD && ctx.inventory.populate().filter(STAMINA_POTION_PATTERN).isEmpty()) {
                    withdrawItem(12625); // Stamina potion(4)
                }
                if (!ctx.bank.populate().filter(13445).isEmpty()) { // check if there are dense essence blocks in the bank
                    ctx.bank.withdraw(13445, SimpleBank.Amount.ALL);
                } else {
                    ctx.log("No dense blocks in bank");
                    playerState = State.BLOODCRAFTING;
                }
            }
            if (playerState == State.BLOODCRAFTING) {
                ctx.bank.depositAllExcept(EXCLUDE_DEPOSIT_ITEMS); //chisel and dark fragments
                if (ctx.pathing.energyLevel() <= ENERGY_THRESHOLD && ctx.inventory.populate().filter(STAMINA_POTION_PATTERN).isEmpty()) {
                    withdrawItem(12625); // Stamina potion(4)
                }
                if (ctx.inventory.populate().filter(1755).isEmpty()) {
                    System.out.println("Chisel not found in inventory. Withdrawing it.");
                    SimpleWidget quantityOne = ctx.widgets.getWidget(12, 29);
                    if (quantityOne != null && !quantityOne.isHidden()) {
                        quantityOne.click(0);
                    }
                    ctx.bank.withdraw(1755, SimpleBank.Amount.ONE);
                    ctx.onCondition(() -> !ctx.inventory.populate().filter(1755).isEmpty(), randomSleeping(1000, 1500));
                }
                if (!ctx.bank.populate().filter(13446).isEmpty()) { // check if there are dense essence blocks in the bank
                    ctx.bank.withdraw(13446, SimpleBank.Amount.ALL);
                }
            }
            status = "Closing bank";
            ctx.bank.closeBank();
            ctx.onCondition(() -> !ctx.bank.bankOpen());
        }
    }

    // Making dark blocks using altar for crafting state
    private void venerateTask() {
        status = "Clicking Dark Altar";
        SimpleObject darkAltar = ctx.objects.populate().filter(27979).filterHasAction("Venerate").nearest().next();
        if (darkAltar != null && darkAltar.validateInteractable()) {
            darkAltar.click("Venerate");
            ctx.sleepCondition(() -> ctx.inventory.populate().filter(13445).isEmpty(), randomSleeping(2000, 4000));
        }
        darkBlocks += ctx.inventory.populate().filter(13446).population();
    }

    private void walkPath(WorldArea toArea, WorldPoint[] walkPath, boolean reverse) {
        while (!ctx.pathing.inArea(toArea)) {
            if (!ctx.pathing.running()) {
                handleEnergy();
            }

            if (playerState == State.WAITING || scriptStopped) {
                break;
            }

            if (!ctx.pathing.inArea(arecuusWholeArea)) {
                break;
            }

            ctx.pathing.walkPath(walkPath, reverse);
            ctx.sleep(1000);
        }
    }

    private void handleEnergy() {
        Pathing pathing = ctx.pathing;

        if (pathing.energyLevel() >= 30 && !pathing.running()) {
            pathing.running(true);
        }

        if (pathing.energyLevel() < 30) {
            final SimpleItem potion = ctx.inventory.populate().filter(Pattern.compile("Stamina potion\\(\\d+\\)")).filterHasAction("Drink").next();
            final int cached = pathing.energyLevel();
            if (potion == null) {
                return;
            }
            status = ("Drinking " + potion.getName().toLowerCase());
            if (potion != null && potion.click("Drink")) {
                ctx.onCondition(() -> pathing.energyLevel() > cached, 250, 8);
            }
        }
    }

    private void chiselTaskStart() { // Making dark fragments for bloodcrafting state (altar / bank)
        SimpleItem chiselTool = ctx.inventory.populate().filter(1755).next();
        SimpleItem darkEssence = ctx.inventory.populate().filter(13446).reverse().next();
        int sleepTime = randomSleeping(20, 75);
        if (chiselTool != null && chiselTool.validateInteractable()
                && darkEssence != null && darkEssence.validateInteractable()) {
            while (!ctx.inventory.populate().filter(13446).isEmpty()) {
                if (playerState == State.WAITING || scriptStopped) {
                    break;
                }
                chiselTool.click(0);
                ctx.sleep(sleepTime);
                darkEssence.click(0);
                ctx.sleep(sleepTime);
            }
        }
        chiselTask = false;
    }

    //teleporting
    private void teleportToBankTask() { // teleporting for crafting or bloodcrafting state
        status = "Teleporting to Crafting Guild";
        ctx.game.tab(Game.Tab.EQUIPMENT);
        if (!ctx.equipment.populate().filter("Crafting cape(t)").isEmpty()) { // max cape 13342
            SimpleWidget maxCape = ctx.widgets.getWidget(387, 7);//cape slot
            if (maxCape != null && !maxCape.isHidden()) {
                //maxCape.click("Crafting Guild", "Max cape");
                maxCape.click("Teleport", "Crafting cape(t)");
                ctx.sleepCondition(() -> craftingGuild.containsPoint(ctx.players.getLocal().getLocation()), randomSleeping(1000, 3000));
            }
        }
        ctx.game.tab(Game.Tab.INVENTORY);
    }

    private void withdrawItem(int ID) {
        SimpleWidget quantityOne = ctx.widgets.getWidget(12, 29);
        if (quantityOne != null && !quantityOne.isHidden()) {
            quantityOne.click(0);
        }
        ctx.bank.withdraw(ID, SimpleBank.Amount.ONE);
        clearBankSearch();
    }

    private void clearBankSearch() {
        SimpleWidget searchButton = ctx.widgets.getWidget(12, 40);
        if (searchButton != null && !searchButton.isHidden()) {
            searchButton.click(0);
        }
    }

    private void teleportToArceuusTask() { // teleporting for crafting or bloodcrafting state
        status = "Teleporting to Arceuus";
        ctx.game.tab(Game.Tab.MAGIC);
        SimpleWidget homeTeleport = ctx.widgets.getWidget(218, 143);//home teleport
        if (homeTeleport != null & !homeTeleport.isHidden()) {
            homeTeleport.click("Arceuus", "Home Teleport");
            ctx.sleepCondition(() -> arecuusAltarArea.containsPoint(ctx.players.getLocal().getLocation()), randomSleeping(9000, 12000));
        }
        ctx.game.tab(Game.Tab.INVENTORY);
    }

    private static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String getPlayerName() {
        if (playerGameName == null) {
            playerGameName = ctx.players.getLocal().getName();
        }
        return playerGameName;
    }

    @Override
    public void onTerminate() {
        if (playerState == State.MINING) {
            this.ctx.updateStatus("Dense runestones mined: " + denseEssence);
        } else if (playerState == State.CRAFTING) {
            this.ctx.updateStatus("Dark blocks made: " + darkBlocks);
        } else if (playerState == State.BLOODCRAFTING) {
            this.ctx.updateStatus("Blood runes crafted: " + bloodRunes);
        }

        //listener
        runningSkillListener = false;

        ///vars
        denseEssence = 0;
        darkBlocks = 0;
        bloodRunes = 0;
        chiselTask = false;
        scriptStopped = true;

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

        if (getType == ChatMessageType.PUBLICCHAT) {
            String senderName = getEvent.getName();

            // Remove any text within angle brackets and trim
            senderName = senderName.replaceAll("<[^>]+>", "").trim();

            if (senderName.contains(playerGameName) && !getEvent.getMessage().toLowerCase().contains("smashing")) {
                ctx.updateStatus(currentTime() + " Someone asked from you");
                ctx.updateStatus(currentTime() + " Stopping script");
                ctx.stopScript();
            }

            if (!senderName.contains(playerGameName) && getEvent.getMessage().toLowerCase().contains(playerGameName.toLowerCase())) {
                ctx.updateStatus(currentTime() + " Someone asked for you");
                ctx.updateStatus(currentTime() + " Stopping script");
                ctx.stopScript();
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        Point mousePos = ctx.mouse.getPoint();

        if (mousePos != null) {
            Rectangle paintRect = new Rectangle(5, 120, 225, 110);
            hidePaint = paintRect.contains(mousePos.getLocation());
        }

        if (!hidePaint) {
            // Settings for Paint
            g.setColor(RaisinBlack);
            g.fillRoundRect(5, 120, 225, 110, 20, 20);
            g.setColor(PhilippineRed);
            g.drawRoundRect(5, 120, 225, 110, 20, 20);
            g.setColor(PhilippineRed);
            g.drawString("eRunecraftingBot by Esmaabi", 15, 135);
            g.setColor(Color.WHITE);
            String runTime = ctx.paint.formatTime(System.currentTimeMillis() - startTime);
            g.drawString("Runtime: " + runTime, 15, 150);
            g.drawString("Status: " + status, 15, 225);

            // Settings for playerState
            if (playerState == State.MINING) {
                long SkillExpGained = miningXp - startingSkillExpMining;
                long SkillExpGained2 = craftingXp - startingSkillExpCrafting;
                long expGainedTotal = SkillExpGained + SkillExpGained2;
                long totalExpPerHour = ctx.paint.valuePerHour((int) expGainedTotal, startTime);
                long itemsPerHour = ctx.paint.valuePerHour(denseEssence, startTime);
                g.drawString("Starting: Mining " + startingSkillLevelMining + " (+" + miningLvlGained + ")," + " Crafting " + startingSkillLevelCrafting + " (+" + craftingLvlGained + ")", 15, 165);
                g.drawString("Current: Mining " + miningLvl + ", Crafting " + craftingLvl, 15, 180);
                g.drawString("Exp gained: " + expGainedTotal + " (" + (totalExpPerHour / 1000L) + "k" + " xp/h)", 15, 195);
                g.drawString("Dense mined: " + denseEssence + " (" + itemsPerHour + " / h)", 15, 210);

            } else {
                long SkillExpGained = runecraftingXp - startingSkillExpRunecrafting;
                long SkillExpGained2 = craftingXp - startingSkillExpCrafting;
                long expGainedTotal = SkillExpGained + SkillExpGained2;
                long totalExpPerHour = ctx.paint.valuePerHour((int) expGainedTotal, startTime);
                long itemsPerHour1 = ctx.paint.valuePerHour(darkBlocks, startTime);
                long itemsPerHour2 = ctx.paint.valuePerHour(bloodRunes, startTime);
                g.drawString("Starting: RuneCraft " + startingSkillLevelRunecrafting + " (+" + runecraftingLvlGained + ")," + " Crafting " + startingSkillLevelCrafting + " (+" + craftingLvlGained + ")", 15, 165);
                g.drawString("Current: RuneCraft " + runecraftingLvl + ", Crafting " + craftingLvl, 15, 180);
                g.drawString("Exp gained: " + expGainedTotal + " (" + (totalExpPerHour / 1000L) + "k" + " xp/h)", 15, 195);
                g.drawString("Blocks: " + darkBlocks + " (" + itemsPerHour1 + " / h), Bloods: " + bloodRunes + " (" + itemsPerHour2 + " / h)", 15, 210);
            }
        }
    }

    @Override
    public void skillLevelAdded(SimpleSkills.Skills skill, int current, int previous, int gained) {
        //ctx.log("We gained %d levels in %s, we went from +%d to %d. ", gained, skill.toString(), previous, current);
        if (skill == CRAFTING) {
            craftingLvl = current;
            craftingLvlGained = gained;
        }

        if (skill == RUNECRAFT) {
            runecraftingLvl = current;
            runecraftingLvlGained = gained;
        }

        if (skill == MINING) {
            miningLvl = current;
            miningLvlGained = gained;
        }

    }

    @Override
    public void skillExperienceAdded(SimpleSkills.Skills skill, int current, int previous, int gained) {
        //ctx.log("We gained %d experience in %s, we went from +%d to %d. ", gained, skill.toString(), previous, current);

        if (skill == CRAFTING) {
            craftingXp = current;
        }

        if (skill == RUNECRAFT) {
            runecraftingXp = current;
        }

        if (skill == MINING) {
            miningXp = current;
        }

        if (playerState == State.MINING) {
            if (skill == CRAFTING) {
                denseEssence++;
            }
        }
    }

}
