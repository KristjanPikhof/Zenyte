package eMLMBotZenyte;

import BotUtils.eActions;
import BotUtils.eBanking;
import Utility.Trivia.eTriviaInfo;
import eApiAccess.eAutoResponderGui;
import eApiAccess.eAutoResponser;
import net.runelite.api.ChatMessageType;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleBank;
import simple.hooks.filters.SimpleInventory;
import simple.hooks.filters.SimpleObjects;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.interfaces.SimpleLocatable;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Game;
import simple.hooks.simplebot.Pathing;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimplePlayer;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.logging.Logger;

import static eApiAccess.eAutoResponser.*;
import static simple.hooks.interaction.menuactions.SimpleMenuActionType.GAME_OBJECT_FIRST_OPTION;

@ScriptManifest(
        author = "Esmaabi",
        category = Category.MINING,
        description = "<html>"
                + "<p>The most effective MLM miner bot!</p>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "<ul>"
                + "<li>Chat GPT answering is integrated.</li>"
                + "</ul>"
                + "</html>",
        discord = "Esmaabi#5752",
        name = "eMLMBot",
        servers = {"Zenyte"},
        version = "1"
)

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private static final String eBotName = "eMLMBot";
    private static final String ePaintText = "Pay-dirt mined";
    private static final SimpleSkills.Skills CHOSEN_SKILL = SimpleSkills.Skills.MINING;
    private static eAutoResponderGui guiGpt;
    private final Random random = new Random();
    private static final Logger logger = Logger.getLogger(eMain.class.getName());
    private static final String[] BIRD_NEST = {"Bird nest", "Clue nest (beginner)", "Clue nest (easy)", "Clue nest (medium)", "Clue nest (hard)", "Clue nest (elite)"};
    private static final String[] SPECIAL_ATTACK_TOOL = {
            "Dragon pickaxe (or)",
            "Dragon pickaxe"
    };

    private static final String[] MINING_PICKAXE = {
            "Bronze pickaxe",
            "Iron pickaxe",
            "Steel pickaxe",
            "Blessed pickaxe",
            "Gilded pickaxe",
            "3rd age pickaxe",
            "Black pickaxe",
            "Mithril pickaxe",
            "Adamant pickaxe",
            "Rune pickaxe",
            "Dragon pickaxe",
            "Crystal pickaxe"
    };
    private static final String[] BANK_NAME = {"Bank booth", "Bank chest", "Bank counter"};
    private static final String DEPOSIT_BOX = "Bank Deposit Box";
    private static final String[] BANKER_NAME = {"Banker","Bird's-Eye' Jack", "Arnold Lydspor", "Banker tutor", "Cornelius", "Emerald Benedict", "Eniola", "Fadli", "Financial Wizard", "Financial Seer", "Ghost banker", "Gnome banker", "Gundai", "Jade", "Jumaane", "Magnus Gram", "Nardah Banker", "Odovacar", "Peer the Seer", "Sirsal Banker", "Squire", "TzHaar-Ket-Yil", "TzHaar-Ket-Zuh", "Yusuf"};

    // Variables
    private long startTime = 0L;
    private long startingSkillLevel;
    private long startingSkillExp;
    private int count;
    static String status = null;
    public static boolean hidePaint = false;
    private static String objectName;
    private static String actionName;
    private long lastAnimation = -1;
    public static boolean specialAttackTool = true;
    public static boolean redwoodMode;
    private static String triviaAnswer;
    private boolean objectReachable;

    // Gui GPT
    private void initializeGptGUI() {
        guiGpt = new eAutoResponderGui();
        guiGpt.setVisible(true);
        guiGpt.setLocale(ctx.getClient().getCanvas().getLocale());
    }

    private void initializeMethods() {
        eBanking bankingUtils = new eBanking(ctx);
        eActions actionUtils = new eActions(ctx);
        BotUtils.eData dataUtils = new BotUtils.eData(ctx);
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

        initializeGptGUI();
        eAutoResponser.scriptPurpose = "you're doing some mining. ";
        gptDeactivation();

        // Other vars
        ctx.log("--------------- " + BotUtils.eActions.getCurrentTimeFormatted() + " ---------------");
        ctx.log("-------------------------------------");
        ctx.log("            " + eBotName + "         ");
        ctx.log("-------------------------------------");

        // Vars
        updateStatus("Setting up bot");
        this.startTime = System.currentTimeMillis();
        this.startingSkillLevel = this.ctx.skills.realLevel(CHOSEN_SKILL);
        this.startingSkillExp = this.ctx.skills.experience(CHOSEN_SKILL);
        count = 0;
        ctx.viewport.angle(270);
        ctx.viewport.pitch(true);
        lastAnimation = System.currentTimeMillis();
        specialAttackTool = true;
        redwoodMode = false;
        objectName = "Ore vein";
        actionName = "Mine";
    }

    @Override
    public void onProcess() {
        super.onProcess();

        final SimplePlayer localPlayer = ctx.players.getLocal();
        final Pathing pathing = ctx.pathing;
        final SimpleInventory myInventory = ctx.inventory;

        if (!botStarted) {
            BotUtils.eActions.status = "Please start the bot!";
            return;
        }

/*        if (redwoodMode) {
            if (!myInventory.inventoryFull() && !bankIsOpen()) {

                if (localPlayer.getLocation().getPlane() != 1 && ctx.players.getLocal().getLocation().getRegionID() == 6198) {
                    handleRopeLadder(localPlayer, "Climb-up", 1);

                } else {

                    if (!localPlayer.isAnimating() && !pathing.inMotion() && (System.currentTimeMillis() > (lastAnimation + getRandomInt(1200, 3200)))) {
                        miningVain(localPlayer, objectName);
                    } else if (localPlayer.isAnimating()) {
                        lastAnimation = System.currentTimeMillis();
                    }
                }

            } else {
                if (localPlayer.getLocation().getPlane() == 1 && ctx.players.getLocal().getLocation().getRegionID() == 6198) {
                    handleRopeLadder(localPlayer, "Climb-down", 0);
                } else {
                    bankTask();
                }
            }

        }*/

        if (!redwoodMode) {
            if (!myInventory.inventoryFull() && !bankIsOpen()) {

                if (!localPlayer.isAnimating() && !pathing.inMotion() && (System.currentTimeMillis() > (lastAnimation + getRandomInt(1000, 5000)))) {
                    miningVain(localPlayer, objectName);
                } else if (localPlayer.isAnimating()) {
                    lastAnimation = System.currentTimeMillis();
                }

            } else {
                status = "Ready to bank";
                //bankTask();
            }
        }

        if (pathing.energyLevel() > 30 && !pathing.running() && pathing.inMotion()) {
            pathing.running(true);
        }


        if (localPlayer.isAnimating()) {
            if (specialAttackTool) {
                specialAttack(localPlayer);
            }
        }
    }

    // Mining
    private void miningVain(SimplePlayer localPlayer, String objectName) {
        SimpleObjects nearbyVain = (SimpleObjects) ctx.objects.populate().filter(objectName);
        updateStatus("Looking for " + objectName.toLowerCase() + " spots...");

        while (!nearbyVain.isEmpty()) {
            SimpleObject nearestObject = nearbyVain.filterHasAction(actionName).nearest().next();
            WorldPoint theTreeLocation = nearestObject.getLocation();
            boolean isOtherPlayerMining = !ctx.players.populate().filterWithin(theTreeLocation, 2).filter(otherPlayer -> !otherPlayer.getName().equals(ctx.players.getLocal().getName())).isEmpty();

            if (nearbyVain.size() >= 2 && isOtherPlayerMining) {
                updateStatus("Another player is mining the nearest " + objectName.toLowerCase() + ".");
                updateStatus("Looking for another spot...");
                nearbyVain = (SimpleObjects) nearbyVain.filter(other -> !other.equals(nearestObject));
                continue;
            } else {
                if (nearestObject.validateInteractable()) {
                    WorldPoint objectLocation = nearestObject.getLocation();
                    objectReachable = isObjectReachable(objectLocation);

                    if (objectReachable) {
                        updateStatus(objectName + " found " + (objectLocation.distanceTo(ctx.players.getLocal().getLocation())) + " tile(s) away");
                        nearestObject.menuAction(actionName);
                        status = "Mining " + objectName.toLowerCase();
                        updateStatus(status);
                        ctx.onCondition(localPlayer::isAnimating, 250, 10);
                        return;
                    } else {
                        updateStatus("Next " + objectName.toLowerCase() + " spot is not reachable.");
                        nearbyVain = (SimpleObjects) nearbyVain.filter(otherTree -> !otherTree.equals(nearestObject));
                    }
                } else {
                    updateStatus("No " + objectName.toLowerCase() + " found in the vicinity.");
                    return;
                }
            }
        }
        updateStatus("No suitable " + objectName.toLowerCase() + " spot found nearby.");
    }


    private boolean isObjectReachable(WorldPoint objectLocation) {
        int[] offsets = { 0, 1, -1}; // Adjust these offsets as needed
        for (int offsetX : offsets) {
            for (int offsetY : offsets) {
                WorldPoint offsetLocation = new WorldPoint(objectLocation.getX() + offsetX, objectLocation.getY() + offsetY, objectLocation.getPlane());
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
            updateStatus(BotUtils.eActions.getCurrentTimeFormatted() + " Found " + itemToPickup.getName());
            if (itemToPickup.click("Take")) {
                ctx.onCondition(() -> ctx.groundItems.populate().filter(eMain.BIRD_NEST).isEmpty(), 250, 12);
            }
        }
    }

    private void specialAttack(SimplePlayer localPlayer) {
        int specialAttackPercentage = ctx.combat.getSpecialAttackPercentage();

        if (specialAttackPercentage != 100) {
            return;
        }

        boolean hasSpecialAttackTool = !ctx.equipment.populate()
                .filter(SPECIAL_ATTACK_TOOL)
                .isEmpty();

        if (!hasSpecialAttackTool) {
            updateStatus(BotUtils.eActions.getCurrentTimeFormatted() + " Special attack tool: NOT FOUND");
            updateStatus(BotUtils.eActions.getCurrentTimeFormatted() + " Special attack: Deactivated");
            specialAttackTool = false;
            return;
        }

        if (localPlayer.isAnimating() && ctx.combat.toggleSpecialAttack(true)) {
            updateStatus(BotUtils.eActions.getCurrentTimeFormatted() + " Used special attack");
            ctx.game.tab(Game.Tab.INVENTORY);
        }
    }

    // Banking
    private void bankTask() {
        int inventoryPopulation = ctx.inventory.populate().population();

        if (bankIsOpen() && inventoryPopulation > 1) {
            updateStatus("Depositing items");
            ctx.bank.depositAllExcept(MINING_PICKAXE);
        }

        if (bankIsOpen() && inventoryPopulation <= 1) {
            updateStatus("Closing bank");
            ctx.bank.closeBank();
            return;
        }

        if (!bankIsOpen()) {
            openClosestBank();
        }
    }

    private void openClosestBank() {
        status = "Banking";

        SimpleObject bankChest = getClosestBankChest();
        SimpleNpc banker = getClosestBanker();
        SimpleObject depositBox = getClosestDepositBox();

        double distToBankChest = (bankChest != null) ? ctx.players.getLocal().getLocation().distanceTo(bankChest.getLocation()) : Double.MAX_VALUE;
        double distToBanker = (banker != null) ? ctx.players.getLocal().getLocation().distanceTo(banker.getLocation()) : Double.MAX_VALUE;
        double distToDepositBox = (depositBox != null) ? ctx.players.getLocal().getLocation().distanceTo(depositBox.getLocation()) : Double.MAX_VALUE;

        // Introduce a preference offset for the bank chest.
        double bankChestPreferenceOffset = 5;  // you can adjust this value
        distToBankChest -= bankChestPreferenceOffset;

        // Determine the closest banking method
        double minDistance = Math.min(distToBankChest, Math.min(distToBanker, distToDepositBox));

        if (minDistance == distToBankChest) {
            useBankObject(bankChest);
        } else if (minDistance == distToBanker) {
            useBanker(banker);
        } else if (minDistance == distToDepositBox) {
            useBankObject(depositBox);
        } else {
            updateStatus("No bank found nearby");
        }
    }


    private void useBankObject(SimpleObject objectName) {
        if (objectName == null) return;
        boolean isBankReachabe = isObjectReachable(objectName.getLocation());

        if (!isBankReachabe) {
            SimpleObject rockFalls = ctx.objects.populate().filter("Rockfall").filter(o -> isObjectReachable(o.getLocation())).filterHasAction("Mine").next();

            if (rockFalls != null) {
                BotUtils.eActions.interactWith(rockFalls, "Mine");
                return;
            }
        }

        if (!objectName.visibleOnScreen() || isWithinRangeToPlayer(objectName)) {
            if (ctx.players.getLocal().getLocation().getRegionID() == 6198) {
                ctx.pathing.step(1591, 3477);
            } else {
                ctx.pathing.step(objectName.getLocation());
            }
        } else {
            objectName.menuAction(GAME_OBJECT_FIRST_OPTION);
            ctx.onCondition(this::bankIsOpen, 250, 20);
        }
    }

    private void useBanker(SimpleNpc bankerNpc) {
        if (bankerNpc == null) return;

        boolean isBankReachabe = isObjectReachable(bankerNpc.getLocation());

        if (!isBankReachabe) {
            SimpleObject rockFalls = ctx.objects.populate().filter("Rockfall").filter(o -> isObjectReachable(o.getLocation())).filterHasAction("Mine").next();

            if (rockFalls != null) {
                BotUtils.eActions.interactWith(rockFalls, "Mine");
                return;
            }
        }

        if (!bankerNpc.visibleOnScreen() || isWithinRangeToPlayer(bankerNpc)) {
            ctx.pathing.step(bankerNpc.getLocation());
        } else {
            bankerNpc.click("Bank");
            ctx.onCondition(this::bankIsOpen, 250, 20);
        }
    }



    private boolean isWithinRangeToPlayer(SimpleLocatable entity) {
        return entity.distanceTo(ctx.players.getLocal()) > 8;
    }

    private SimpleObject getClosestDepositBox() {
        return ctx.objects.populate().filter(DEPOSIT_BOX).nearest().next();
    }

    private SimpleObject getClosestBankChest() {
        return ctx.objects.populate().filter(BANK_NAME).filterHasAction("Bank", "Use").nearest().next();
    }

    private SimpleNpc getClosestBanker() {
        return ctx.npcs.populate().filter(BANKER_NAME).nearest().next();
    }

    private boolean bankIsOpen() {
        SimpleBank bank = ctx.bank;
        return bank.bankOpen() || bank.depositBoxOpen();
    }

    private int getDistanceToPlayer(SimpleLocatable entity) {
        return entity.distanceTo(ctx.players.getLocal());
    }

    private void handleRopeLadder(SimplePlayer localPlayer, String action, int expectedPlane) {
        SimpleObject ropeLadder = ctx.objects.populate().filter("Rope ladder").filterHasAction(action).nearest().next();
        if (!ropeLadder.visibleOnScreen()) {
            ctx.viewport.turnTo(ropeLadder);
            ctx.pathing.step(ropeLadder.getLocation());
        }
        ropeLadder.menuAction(action);
        ctx.onCondition(() -> localPlayer.getLocation().getPlane() == expectedPlane, 250, 4);
    }

    //Utility

    private int getRandomInt(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private void updateStatus(String newStatus) {
        status = newStatus;
        ctx.log(status);
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
        objectName = null;
        actionName = null;
        specialAttackTool = false;
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

        if (getType == ChatMessageType.SPAM) {
            String spamMessage = getEvent.getMessage().toLowerCase();

            if (spamMessage.contains("manage to mine some")) {
                count++;
            }
        }

        if (getType == ChatMessageType.GAMEMESSAGE) {
            String gameMessageLowerCase = gameMessage.toLowerCase();

            if (gameMessageLowerCase.contains("t reach that")) {
                objectReachable = false;
            }
        }

        if (gptStarted && botStarted) eAutoResponser.handleGptMessages(getType, senderName, formattedMessage);
        eTriviaInfo.handleBroadcastMessage(getType, gameMessage);

        /*        if (getType == ChatMessageType.OBJECT_EXAMINE) {
            String examineMessage = getEvent.getMessage();
            if (examineMessage == null) {
                return;
            }

            for (eData treeData : eData.rocksData) {
                if (examineMessage.contains(treeData.examineResult)) {
                    objectName = treeData.objectName;
                    actionName = treeData.action;
                    redwoodMode = Objects.equals(objectName, "Redwood");
                    updateStatus("New object selected: " + objectName);
                    break;
                }
            }
        }*/
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
            g.drawString("Status: " + status, 15, 225);

        }
    }
}
