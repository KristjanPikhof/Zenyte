package eAutoFighterZenyte;

import BotUtils.*;
import Utility.Trivia.eTriviaInfo;
import eApiAccess.eAutoResponser;
import eAutoFighterZenyte.data.eLoots;
import net.runelite.api.ChatMessageType;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.*;
import simple.hooks.queries.SimpleEntityQuery;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Game;
import simple.hooks.simplebot.Pathing;
import simple.hooks.wrappers.*;
import simple.robot.utils.WorldArea;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eApiAccess.eAutoResponser.*;

@ScriptManifest(
        author = "Reminisce | Esmaabi",
        category = Category.COMBAT,
        description = "<html>"
                + "<p><strong>eAIO Fighter with GPT</strong>: Revolutionizing combat with AI-driven intelligence!</p>"
                + "<p><strong>Guidelines & Features:</strong></p>"
                + "<ul>"
                + "<li>Always start near the NPCs you intend to target.</li>"
                + "<li><strong>Equip your primary weapon first!</strong> This ensures the bot can correctly identify and use it along with your special attack weapon.</li>"
                + "<li>Set your desired HP threshold and food type for the bot to manage your health.</li>"
                + "<li>Choose between quick prayers or the advanced prayer flick options.</li>"
                + "<li>Define which prayer restoration potions to use and the bot will drink them as needed.</li>"
                + "<li>Specify the items you want to loot and looting range. The bot can also eat to make space for loot.</li>"
                + "<li>If you want to bury bones or ashes, add them to the drop list for smooth execution.</li>"
                + "<li>Click the Slayer icon (top-right corner) whenever you want an update on remaining Slayer kills.</li>"
                + "<li>Personalize your experience: save and load bot settings at will.</li>"
                + "<li>Engage in lifelike interactions with integrated GPT chatbot, bringing smart AI to your gameplay.</li>"
                + "</ul>"
                + "</html>",
        discord = "Reminisce#1707 | Esmaabi#5752",
        name = "eAIO Fighter with GPT",
        servers = {"Zenyte"},
        version = "3.5"
)

public class AutoFighter extends TaskScript implements LoopingScript, MouseListener {

    // Constants
    public static final Rectangle slayerImageRect = new Rectangle(735, 10, 20, 20);
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private static final Logger logger = Logger.getLogger(AutoFighter.class.getName());
    private static final int ENHANCED_GEM_ID = 4155;
    private static final int[] SPEC_WEAPON_IDS = {1215, 1231, 5680, 5698, 20407, 1434, 1377, 13271};
    private static final java.util.regex.Pattern SLAYER_HELMET_PATTERN = java.util.regex.Pattern.compile("^(?i)(Black|Green|Red|Purple|Turquoise|Hydra|Twisted|Tztok|Vampyric|Tzkal)?\\s?Slayer helmet(\\s\\(i\\))?$");


    public static final WorldArea HOME_AREA = new WorldArea(new WorldPoint(3110, 3474, 0), new WorldPoint(3074, 3516, 0));
    long previous = System.currentTimeMillis();


    // Variables
    private BufferedImage slayerImage;
    private BufferedImage slayerImageClicked;
    private SimpleNpc currentNpc = null;
    private boolean attackingAllowed;
    private boolean healingDone;
    private boolean slayerTaskDone;
    private boolean alreadyInCombat;
    private long startTime = 0L;
    private long startingSkillExp;
    private static int countNpc;
    private static int monsterCount = 0;
    public AutoFighterUI fighterGui;
    public String MAIN_WEAPON_NAME;
    public String SPEC_WEAPON_NAME;
    public boolean buryBones;
    public boolean eatForSpace;
    public boolean enableStatBoosting;
    public boolean bypassNpcReachable;
    public boolean prayFlick;
    public boolean quickPrayers;
    public boolean teleOnTaskFinish;
    public int eatHealth;
    public int lootWithin;
    public int[] foodId;
    public int[] lootNames;
    public int[] npcIds;
    public static SimplePrayers.Prayers CHOSEN_PRAYER_FLICK;
    public static SimpleSkills.Skills chosenSkillIndicator;
    public static String prayPotionName;
    public static String statsBoostingPotionName;
    public static boolean autoRetaliate;
    public static boolean autoRetaliateDefault;
    public static boolean checkSlayerKillCount;
    public static boolean hidePaint;
    public static boolean specialAttackTool;
    public boolean useMainWeaponAsSpec;
    public static int drinkStatBoostAt;
    static String status = null;


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

        tasks.addAll(Arrays.asList(new eAutoResponser(ctx), new eWildyTeleport(ctx), new eImpCatcher(ctx)));
        previous = 0;

        // Initialize GptGUI();
        initializeMethods();
        gptDeactivation();

        // Other vars
        ctx.log("--------------- " + BotUtils.eActions.getCurrentTimeFormatted() + " ---------------");
        ctx.log("------------------------------------");
        ctx.log("             eAIO Fighter           ");
        ctx.log("------------------------------------");

        // Vars
        updateStatus("Setting up bot");
        this.startTime = System.currentTimeMillis();
        this.startingSkillExp = this.ctx.skills.totalExperience();
        ctx.viewport.pitch(true);
        slayerTaskDone = false;
        healingDone = false;
        countNpc = 0;
        attackingAllowed = true;

        // Auto Fighter Gui
        try {
            AutoFighter script = this;
            SwingUtilities.invokeLater(() -> fighterGui = new AutoFighterUI(script));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while initializing AutoFighter GUI", e);
        }

        // Initialize Slayer image for kill-count
        try {
            slayerImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("data/slayer.png")));
            slayerImageClicked = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("data/slayerClicked.png")));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An error occurred while initializing Slayer image", e);
        }

        // Setting up special attack weapon switch
        setupSpecialAttackWeapon();

        // Counting killed NPCs
        executorService.scheduleAtFixedRate(this::monitorNpc, 0, 200, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onProcess() {
        super.onProcess();

        final Pathing pathing = ctx.pathing;
        final SimplePlayer localPlayer = ctx.players.getLocal();

        try {

            if (!botStarted) {
                status = "Please start the bot!";
                return;
            }

            // Check if the player is in the home and healed
            if (pathing.inArea(HOME_AREA) && healingDone) {
                return;
            }

            // Handle eating
            handleEating();

            // Handle looting
            handleLooting();

            // Handle combat
            if (attackingAllowed) {
                if (!localPlayer.inCombat() || localPlayer.getInteracting() == null) { // Player not in combat

                    Optional<SimpleNpc> potentialNpc = findEngagedNpc();
                    if (!potentialNpc.isPresent()) {
                        potentialNpc = findNearestNpc();
                    }

                    if (!potentialNpc.isPresent()) {
                        return;
                    }

                    currentNpc = potentialNpc.get();
                    if (shouldSkipNpc(currentNpc)) {
                        status = currentNpc.getName() + " is not reachable.";
                        currentNpc = null;
                    }

                    // If NPC is not visible on the screen, move towards it
                    npcNotVisible(currentNpc);
                    healingDone = false;
                    status("Attacking " + currentNpc.getName());
                    BotUtils.eActions.interactWith(currentNpc, "Attack");
                    ctx.onCondition(() -> ctx.combat.inCombat() && localPlayer.getInteracting() == currentNpc.getNpc(), 250, 12);
                    return;

                } else if (alreadyInCombat) {
                    currentNpc = null;
                    ctx.combat.toggleAutoRetaliate(true);
                    alreadyInCombat = false;

                } else { // Player is in combat

                    // Quick prayers handling
                    if (quickPrayers) {
                        handleQuickPrayers();
                    }

                    // Prayer flick handling
                    if (prayFlick && !quickPrayers) {
                        handlePrayerFlick();
                    }

                    // Special attack handling
                    handleSpecialAttack();

                    // Handle bones in inventory
                    handleBuryingBones();

                    // Handle auto retaliate
                    handleAutoRetaliate();
                }
            }

            // Handle running
            BotUtils.eActions.handleRunning();

            if (checkSlayerKillCount) checkSlayerEquipment();

            // Handle stats boosting
            if (enableStatBoosting && ctx.skills.level(chosenSkillIndicator) < drinkStatBoostAt) drinkStatsBoostingPotion();

            // Handle teleporting to home after slayer task is finished
            handleTeleportAfterTask();

        } catch (
                Exception e) {
            logger.log(Level.SEVERE, "An error occurred during AutoFighter onProcess", e);
        }

    }

    private boolean teleportHome() {
        if (!playerAtHome()) {
            ctx.log("Not at home, attempting to teleport");
            status = "Teleporting to home";
            BotUtils.eActions.handleInventoryItem("Break", 22721);
            boolean teleported = ctx.onCondition(this::playerAtHome, 1000, 10);
            if (!teleported) {
                return false;
            }
        }
        status = "Restoring hitpoints";
        if (!ctx.prayers.quickPrayers()) {
            ctx.prayers.quickPrayers(true);
        }
        ctx.prayers.quickPrayers(false);

        if (slayerTaskDone) {
            updateStatus(BotUtils.eActions.getCurrentTimeFormatted() + " Slayer Task is Done");
            slayerTaskDone = false;
        }

        if (!healingDone) {
            SimpleObject healingBox = ctx.objects.populate().filter("Box of Restoration").nearest().next();

            if (healingBox != null && healingBox.validateInteractable()) {
                ctx.log("Found Box of Restoration, interacting with it");
                BotUtils.eActions.interactWith(healingBox, "Restore");
                ctx.onCondition(() -> ctx.players.getLocal().getGraphic() != 0, 250, 12);
            }
            healingDone = true;
            status = "Waiting at home...";
        }
        return true;
    }

    private boolean playerAtHome() {
        return ctx.pathing.inArea(HOME_AREA);
    }

    private void handleQuickPrayers() {
        if (ctx.prayers.points() > 20) {
            if (!ctx.prayers.quickPrayers()) {
                ctx.prayers.quickPrayers(true);
            }
        } else {
            drinkPrayerPotion();
        }

        if (prayFlick) {
            prayFlick = false;
        }
    }

    private void handlePrayerFlick() {
        if (CHOSEN_PRAYER_FLICK != null && ctx.prayers.points() > 0) {
            ctx.prayers.prayer(CHOSEN_PRAYER_FLICK, ctx.combat.inCombat() && !ctx.players.getLocal().isAnimating());
        }

        if (ctx.prayers.points() <= 20) {
            drinkPrayerPotion();
        }

        if (quickPrayers) {
            quickPrayers = false;
        }
    }

    private void drinkPrayerPotion() {
        final SimpleItem potionInventory = ctx.inventory.populate()
                .filter(item -> item != null && item.getName() != null && item.getName().startsWith(prayPotionName))
                .filterHasAction("Drink").next();

        final int cached = ctx.prayers.points();

        if (potionInventory == null) {
            ctx.log(prayPotionName + " not found.");
            ctx.log("Disabling prayers.");
            quickPrayers = false;
            prayFlick = false;
            return;
        }

        status = "Drinking " + prayPotionName;
        if (potionInventory.click("Drink")) {
            ctx.onCondition(() -> ctx.prayers.points() > cached, 250, 12);
        }
    }

    private void drinkStatsBoostingPotion() {

        final SimpleItem potionInventory = ctx.inventory.populate()
                .filter(item -> item != null && item.getName() != null && item.getName().startsWith(statsBoostingPotionName))
                .filterHasAction("Drink").next();

        final int cached = ctx.skills.level(chosenSkillIndicator);

        if (potionInventory == null) {
            ctx.log(statsBoostingPotionName + " not found.");
            ctx.log("Disabling stat-boosting.");
            enableStatBoosting = false;
            return;
        }

        status = "Drinking " + statsBoostingPotionName;
        if (potionInventory.click("Drink")) {
            ctx.onCondition(() -> ctx.skills.level(chosenSkillIndicator) > cached, 250, 12);
        }
    }

    private void eatFood() {
        if (foodId != null) {
            final SimpleItem food = ctx.inventory.populate().filter(foodId).next();
            if (food != null) {
                final int cached = ctx.inventory.getFreeSlots();
                status("Eating " + food.getName().toLowerCase());
                food.click("Eat");
                ctx.onCondition(() -> ctx.inventory.getFreeSlots() > cached, 250, 9);
            }
        }
    }

    private void handleEating() {
        if (ctx.combat.health() < eatHealth && foodId != null) {
            if (!ctx.inventory.populate().filter(foodId).isEmpty()) {
                eatFood();
            } else {
                ctx.log("Out of food!");
                teleportHome();
            }
        }
    }

    private void handleBuryingBones() {
        if (!buryBones) return;

        if (!BotUtils.eActions.hasItemsInInventory(eActions.StackableType.NON_STACKABLE, eLoots.BONES_TO_BURY)) return;
        updateStatus("Burying bones...");
        BotUtils.eActions.handleInventoryItem("Bury", eLoots.BONES_TO_BURY);
    }

    private void handleLooting() {
        if (lootNames == null || lootNames.length == 0) {
            attackingAllowed = true;
            return;
        }

        SimpleGroundItem item = ground().nearest().next();

        if (item == null) {
            return;
        }

        boolean smallCoinsStackFound = item.getId() == 995 && item.getQuantity() < 30000;
        if (smallCoinsStackFound) {
            return;
        }

        int freeSlots = ctx.inventory.getFreeSlots();

        if (freeSlots <= 0) {

            if (eatForSpace && foodId != null && !ctx.inventory.populate().filter(foodId).isEmpty()) {
                eatFood();
                freeSlots = ctx.inventory.getFreeSlots();

                if (freeSlots <= 0) {
                    return;
                }
            }
        }

        attackingAllowed = false;

        if (!item.visibleOnScreen()) {
            ctx.pathing.step(item.getLocation());
            ctx.onCondition(item::visibleOnScreen, 200, 6);
        }

        BotUtils.eActions.interactWith(item, "Take");
        ctx.onCondition(() -> ground().filter(item).isEmpty(), 200, 6);

        attackingAllowed = true;
    }

    public final SimpleEntityQuery<SimpleNpc> npcs() {
        return ctx.npcs.populate()
                .filter(npcIds)
                .filter(this::isValidNpc);
    }

    private boolean isValidNpc(SimpleNpc n) {
        if (n == null) return false;
        if (isUndesiredNpc(n)) return false;
        if (isTooFarFromPlayer(n)) return false;
        if (isEngagedWithOther(n)) return false;
        return !n.isDead();
    }

    private boolean isUndesiredNpc(SimpleNpc n) {
        return n.getId() == 10; // Death spawn
    }

    private boolean isTooFarFromPlayer(SimpleNpc n) {
        return n.getLocation().distanceTo(BotUtils.eActions.getPlayerLocation()) > 15;
    }

    private boolean isEngagedWithOther(SimpleNpc n) {
        return n.inCombat() && n.getInteracting() != null && !n.getInteracting().getName().equals(BotUtils.eActions.getPlayerName());
    }

    private Optional<SimpleNpc> findEngagedNpc() {
        return Optional.ofNullable(npcs().filter((n) ->
                n.getInteracting() != null &&
                        n.getInteracting().getName().equals(BotUtils.eActions.getPlayerName()) &&
                        n.inCombat()
        ).nearest().next());
    }

    private Optional<SimpleNpc> findNearestNpc() {
        return Optional.ofNullable(npcs().nearest().next());
    }

    private boolean shouldSkipNpc(SimpleNpc npc) {
        return !bypassNpcReachable && !ctx.pathing.reachable(npc.getLocation());
    }

    private void npcNotVisible(SimpleNpc npc) {
        if (!bypassNpcReachable && !npc.visibleOnScreen()) {
            ctx.pathing.step(npc.getLocation());
            ctx.onCondition(npc::visibleOnScreen, 250, 12);
        }
    }

    public final SimpleEntityQuery<SimpleGroundItem> ground() {
        return ctx.groundItems.populate()
                .filter(Objects::nonNull)
                .filter(t -> t.getLocation().distanceTo(BotUtils.eActions.getPlayerLocation()) <= lootWithin)
                .filter(lootNames)
                .filter(ctx.pathing::reachable);
    }

    private void setupSpecialAttackWeapon() {
        SimpleItem mainWeapon = ctx.equipment.getEquippedItem(SimpleEquipment.EquipmentSlot.WEAPON);
        SimpleItem specWeapon = ctx.inventory.populate().filter(SPEC_WEAPON_IDS).filterHasAction("Wield").next();

        MAIN_WEAPON_NAME = getNameSafe(mainWeapon);
        if (useMainWeaponAsSpec) {
            SPEC_WEAPON_NAME = MAIN_WEAPON_NAME;  // Use main weapon as special weapon
        } else {
            SPEC_WEAPON_NAME = getNameSafe(specWeapon);
        }

        if (!checkWeapon(MAIN_WEAPON_NAME, "Main")) {
            return;
        }

        checkWeapon(SPEC_WEAPON_NAME, "Special");
    }

    private String getNameSafe(SimpleItem item) {
        return item == null ? null : item.getName();
    }

    private boolean checkWeapon(String weaponName, String weaponType) {
        if (weaponName == null || weaponName.isEmpty()) {
            ctx.log(weaponType + " weapon is missing.");
            return false;
        }

        ctx.log(weaponType + " weapon: " + weaponName);
        return true;
    }

    private void wieldWeaponFromInventory(String weaponName) {
        SimpleItem weapon = ctx.inventory.populate().filter(weaponName).filterHasAction("Wield", "Wear").next();
        if (weapon != null && weapon.click(0)) {
            updateStatus("Wielding " + weapon.getName());
            ctx.onCondition(() -> false, 250, 12);
        }
    }

    private void useSpecialAttack() {
        status = "Using special attack";
        int specLeft = ctx.combat.getSpecialAttackPercentage();
        ctx.combat.toggleSpecialAttack(true);
        ctx.onCondition(() -> ctx.combat.getSpecialAttackPercentage() < specLeft, 250, 4);
    }

    public void handleSpecialAttack() {
        int specPercentage = ctx.combat.getSpecialAttackPercentage();
        boolean mainWeaponEquipped = isWeaponEquipped(MAIN_WEAPON_NAME);
        boolean specWeaponEquipped = isWeaponEquipped(SPEC_WEAPON_NAME);
        boolean shouldSwitchForSpecial = shouldSwitchForSpecialAttack();

        if (useMainWeaponAsSpec && mainWeaponEquipped && specPercentage == 100) {
            useSpecialAttack();
        } else {
            if (specPercentage == 100 && mainWeaponEquipped && shouldSwitchForSpecial) {
                wieldWeaponFromInventory(SPEC_WEAPON_NAME);
            }

            if (specPercentage >= 25 && specWeaponEquipped) {
                useSpecialAttack();
            }

            if (specPercentage < 25 && specWeaponEquipped) {
                wieldWeaponFromInventory(MAIN_WEAPON_NAME);
            }
        }
    }

    private boolean isWeaponEquipped(String weaponName) {
        return !ctx.equipment.populate().filter(weaponName).isEmpty();
    }

    private boolean shouldSwitchForSpecialAttack() {
        return !useMainWeaponAsSpec && !ctx.inventory.populate().filter(SPEC_WEAPON_NAME).isEmpty();
    }

    private void handleAutoRetaliate() {
        if (!autoRetaliateDefault) {
            ctx.combat.toggleAutoRetaliate(autoRetaliate);
        }
    }

    private void handleTeleportAfterTask() {
        if (teleOnTaskFinish && slayerTaskDone) {
            if (teleportHome()) {
                ctx.onCondition(() -> true, 250, 12);
            }
        }
    }

    public void setupEating(int[] foodId, int eatAt) {
        this.foodId = foodId;
        this.eatHealth = eatAt;
    }

    public void setupLooting(int[] lootNames, int lootWithin) {
        this.lootNames = lootNames;
        this.lootWithin = lootWithin;
    }

    public void setLootNames(int[] newLootNames) {
        this.lootNames = newLootNames;
    }

    public void setupAttacking(int[] npcIds) {
        this.npcIds = npcIds;
    }

    private void checkSlayerEquipment() {
        ctx.game.tab(Game.Tab.EQUIPMENT);

        SimpleItem slayerHelmet = ctx.equipment.populate().filter(SLAYER_HELMET_PATTERN).filterHasAction("Check").next();

        if (slayerHelmet != null) {
            status("Clicking " + slayerHelmet.getName().toLowerCase());
            slayerHelmet.click("Check");
        } else {
            SimpleItem slayerHelmetInv = ctx.inventory.populate().filter(SLAYER_HELMET_PATTERN).filterHasAction("Check").next();

            if (slayerHelmetInv != null) {
                status("Clicking " + slayerHelmetInv.getName().toLowerCase());
                slayerHelmetInv.click("Check");
            } else {
                SimpleItem enhancedGem = ctx.inventory.populate().filter(ENHANCED_GEM_ID).next();

                if (enhancedGem != null) {
                    status("Clicking " + enhancedGem.getName().toLowerCase());
                    enhancedGem.click("Check");
                } else {
                    checkSlayerKillCount = false;
                    updateStatus("Cannot check the slayer kill-count");
                    updateStatus("Slayer helmet / enchanted gem is missing!");
                }
            }
        }

        ctx.game.tab(Game.Tab.INVENTORY);
    }

    private void status(String status) {
        AutoFighter.status = status;
    }
    private void updateStatus(String newStatus) {
        status = newStatus;
        ctx.log(status);
    }

    private void monitorNpc() {
        if (currentNpc != null) {
            synchronized(this) {
                if (currentNpc.isDead() || currentNpc.getHealthRatio() == 0) {
                    countNpc++;
                    if (monsterCount != 0) monsterCount--;
                    currentNpc = null;
                }
            }
        }
    }


    @Override
    public void onTerminate() {

        // Termination message
        ctx.log("-------------- " + BotUtils.eActions.getCurrentTimeFormatted() + " --------------");
        ctx.log("-----------------------------------");
        ctx.log("----- Thank You & Good Luck! ------");
        ctx.log("-----------------------------------");
        ctx.log("Monsters killed: " + countNpc);

        // Other variables
        this.startingSkillExp = 0L;
        specialAttackTool = false;
        gptDeactivation();

        if (fighterGui != null) {
            fighterGui.closeGui();
        }
        executorService.shutdown();
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
            String gameMessageTrimmed = gameMessage.replaceAll("<[^>]+>", "").trim();
            if (gameMessageTrimmed.contains("return to a Slayer master")) {
                slayerTaskDone = true;
                monsterCount = 0;
            }

            if (gameMessageTrimmed.contains("are already in combat")) {
                ctx.log("In combat, attacking next NPC");
                alreadyInCombat = true;
            }

            Pattern pattern = Pattern.compile("You're assigned to kill ([^;]+); only (\\d+) more to go.");
            Matcher matcher = pattern.matcher(gameMessageTrimmed);
            if (matcher.find()) {
                String monsterName = matcher.group(1);
                monsterCount = Integer.parseInt(matcher.group(2));

                ctx.log("You still have to kill " + monsterCount + " " + monsterName);
                checkSlayerKillCount = false;
            }

            if (gameMessageTrimmed.contains("need something new to hunt")) {
                ctx.log("You need something new to hunt!");
                monsterCount = 0;
                checkSlayerKillCount = false;
            }
        }
    }

    @Override
    public void paint(Graphics g) {

        // Check if mouse is hovering over the paint
        Point mousePos = ctx.mouse.getPoint();

        // Set up colors
        Color philippineRed = new Color(196, 18, 48);
        Color raisinBlack = new Color(35, 31, 32, 127);

        // Get runtime and skill information
        String runTime = ctx.paint.formatTime(System.currentTimeMillis() - startTime);
        long currentSkillExp = this.ctx.skills.totalExperience();
        long skillExpGained = currentSkillExp - this.startingSkillExp;

        // Calculate experience and actions per hour
        long skillExpPerHour = ctx.paint.valuePerHour((int) skillExpGained, startTime);
        long killsPerHour = ctx.paint.valuePerHour(countNpc, startTime);

        // Draw paint if not hidden
        if (!hidePaint) {
            g.setColor(raisinBlack);
            g.fillRoundRect(5, 120, 205, 80, 20, 20);

            g.setColor(philippineRed);
            g.drawRoundRect(5, 120, 205, 80, 20, 20);

            g.setColor(philippineRed);
            g.drawString("eAIO Fighter with GPT", 15, 135);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + runTime, 15, 150);
            g.drawString("Exp gained: " + skillExpGained + " (" + (skillExpPerHour / 1000L) + "k xp/h)", 15, 165);
            g.drawString("Monsters killed: " + countNpc + " (" + killsPerHour + " per/h)", 15, 180);
            g.drawString("Status: " + status, 15, 195);

        }

        // Paint
        if (mousePos != null) {
            Rectangle paintRect = new Rectangle(5, 120, 200, 80);
            hidePaint = paintRect.contains(mousePos.getLocation());
        }

        // Slayer icon setup
        if (slayerImage != null) {
            BufferedImage imageToDraw = (checkSlayerKillCount && slayerImageClicked != null) ? slayerImageClicked : slayerImage;
            g.drawImage(imageToDraw, 735, 10, 18, 20, null);
        }

        if (monsterCount != 0) {
            g.setColor(Color.white);
            g.setFont(new Font("Verdana", Font.BOLD, 8));
            int computedX = computeXForMonsterCount(monsterCount);
            g.drawString(String.valueOf(monsterCount), computedX, 39);
        }
    }

    private int computeXForMonsterCount(int monsterCount) {
        int baseX = 740; // for 1 digit kill-count
        int numberOfDigits = (int) Math.log10(monsterCount) + 1; // find out how many digits kill-count

        switch (numberOfDigits) {
            case 2:
                return baseX - 2; // subtract for 2 digits kill-count
            case 3:
                return baseX - 5; // subtract for 3 digits kill-count
            default:
                return baseX;
        }
    }

    @Override
    public int loopDuration() {
        return 300;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (slayerImageRect.contains(e.getPoint())) {
            checkSlayerKillCount = true;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

}