package eCrystalHustlerZenyte;

import eRandomEventSolver.eRandomEventForester;
import net.runelite.api.ChatMessageType;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleGroundItems;
import simple.hooks.filters.SimpleInventory;
import simple.hooks.filters.SimpleObjects;
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

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.logging.Logger;

@ScriptManifest(author = "Esmaabi", category = Category.OTHER, description =
        "<br>This bot will gather crystal shards for you so you don't have to! <br><br><b>Features & recommendations:</b><br><br>" +
        "<ul>" +
        "<li>Bot will chop or mine Crystal shards;</li>" +
        "<li>You must start at Crystal Mine or near Crystal trees;</li>" +
        "<li>If you use Dragon tools you can also use special attack;</li>" +
        "<li>Bot will pick up bird nests, but will not bank them;</li>" +
        "<li>The bot will stop if trees / rocks not found.</li></ul><br>" +
        "For more information, check out Esmaabi on SimpleBot!", discord = "Esmaabi#5752",
        name = "eCrystalHustlerZenyte", servers = { "Zenyte" }, version = "1")

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private final static int INVENTORY_BAG_WIDGET_ID = 548;
    private final static int INVENTORY_BAG_CHILD_ID = 58;
    private final static int CRYSTAL_SHARD = 30560;
    private static final String BIRD_NEST = "Bird nest";
    private static final String CRYSTAL_TREE = "Crystal Tree";
    private static final String CRYSTAL_ROCK = "Crystalized Rock";
    private static final Set<String> SPECIAL_ATTACK_TOOL = new HashSet<>(
            Arrays.asList(
                    "Dragon pickaxe (or)",
                    "Dragon pickaxe",
                    "Dragon pickaxe (or) (Trailblazer)",
                    "Dragon pickaxe (upgraded)",
                    "Infernal pickaxe",
                    "Dragon axe (or)",
                    "Infernal axe",
                    "Dragon axe"
            ));
    private final WorldPoint DEPOSIT_BOX_LOCATION = new WorldPoint(0, 0, 0);
    private final WorldArea homeArea = new WorldArea(new WorldPoint(3101, 3181, 0), new WorldPoint(3074, 3504, 0));

    // Variables
    private static eGui gui;
    private static final Logger logger = Logger.getLogger(eMain.class.getName());
    private long startTime = 0L;
    private int count;
    private int cachedCount;
    private boolean countActive;
    static String status = null;
    public static boolean botStarted = false;
    public static boolean hidePaint = false;
    public static boolean specialAttackActive;
    public static boolean specialAttackTool = true;
    public static boolean woodcuttingAction;
    public static boolean miningAction;
    private static String playerGameName;

    @Override
    public int loopDuration() {
        return 150;
    }

    // Gui
    private void initializeGUI() {
        gui = new eGui();
        gui.setVisible(true);
        gui.setLocale(ctx.getClient().getCanvas().getLocale());
    }

    //Tasks
    List<Task> tasks = new ArrayList<>();

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
        tasks.addAll(Arrays.asList());
        System.out.println("Started eCrystalHustler!");
        this.startTime = System.currentTimeMillis(); //paint
        ctx.viewport.angle(90);
        ctx.viewport.pitch(true);
        initializeGUI();


        this.ctx.updateStatus("-------------------------------------");
        this.ctx.updateStatus("--------------- " + currentTime() + " ---------------");
        this.ctx.updateStatus("-------------------------------------");
        status = "Setting up bot";
        count = 0;
        botStarted = false;
        hidePaint = false;
        specialAttackActive = false;
        woodcuttingAction = false;
        miningAction = false;
        countActive = false;
    }

    @Override
    public void onProcess() {
        super.onProcess();

        if (botStarted) {

            SimplePlayer localPlayer = ctx.players.getLocal();
            Pathing pathing = ctx.pathing;

            int currentCount = getStackSize(CRYSTAL_SHARD);
            handleCount(currentCount);

            if (woodcuttingAction) {
                if (!localPlayer.isAnimating() && !pathing.inMotion()) {
                    cuttingCrystalTree(localPlayer);
                }

                if (!ctx.inventory.inventoryFull()) {
                    birdNests();
                }
            } else if (miningAction) {
                if (!localPlayer.isAnimating() && !pathing.inMotion()) {
                    miningCrystals(localPlayer);
                }

                if (localPlayer.getHealth() <= 40) {
                    handleEating(localPlayer);
                }
            }

            if (localPlayer.isAnimating()) {
                if (specialAttackActive) {
                    specialAttack(localPlayer);
                }
            }

        } else {
            status = "Start the bot";
        }
    }

    private void cuttingCrystalTree(SimplePlayer localPlayer) {
        SimpleObjects crystalTrees = (SimpleObjects) ctx.objects.populate().filter(CRYSTAL_TREE);
        SimpleObject nearestCrystalTree = crystalTrees.nearest().next();

        if (nearestCrystalTree != null && nearestCrystalTree.validateInteractable()) {
            updateStatus("Nearest tree found " + (nearestCrystalTree.getLocation().distanceTo(ctx.players.getLocal().getLocation()) - 1) + " tile(s) away");
            updateStatus("Clicking" + CRYSTAL_TREE.toLowerCase());
            nearestCrystalTree.click("Chop down");
            status = "Chopping tree...";
            ctx.onCondition(localPlayer::isAnimating, 250, 10);
        }
    }

    private void miningCrystals(SimplePlayer localPlayer) {
        SimpleObjects crystalRocks = (SimpleObjects) ctx.objects.populate().filter(CRYSTAL_ROCK);
        SimpleObject nearestCrystalrock = crystalRocks.nearest().next();

        if (nearestCrystalrock != null && nearestCrystalrock.validateInteractable()) {
            updateStatus("Nearest rock found " + (nearestCrystalrock.getLocation().distanceTo(ctx.players.getLocal().getLocation())) + " tile(s) away");
            updateStatus("Clicking " + CRYSTAL_ROCK.toLowerCase());
            nearestCrystalrock.click("Mine");
            status = "Mining rock...";
            ctx.onCondition(localPlayer::isAnimating, 250, 10);
        }
    }

    private void birdNests() {
        SimpleGroundItems birdNests = (SimpleGroundItems) ctx.groundItems.populate().filter(BIRD_NEST);

        if (!birdNests.isEmpty()) {
            SimpleGroundItem nearestBirdNest = birdNests.nearest().next();

            if (nearestBirdNest != null && nearestBirdNest.validateInteractable()) {
                updateStatus("Picking up bird nest");
                nearestBirdNest.click("Take");
                ctx.onCondition(birdNests::isEmpty, 250, 10);
            }
        }
    }

    private void handleEating(SimplePlayer localPlayer) {
        SimpleInventory foodItems = (SimpleInventory) ctx.inventory.populate().filterHasAction("Eat");
        SimpleItem foodInv = foodItems.next();
        final int cachedHealth = localPlayer.getHealth();

        if (foodInv == null) {
            updateStatus("Food not found");
            teleHome();
            return;
        }

        updateStatus("Restoring hitpoints");
        if (foodInv.click("Eat")) {
            ctx.onCondition(() -> localPlayer.getHealth() > cachedHealth, 250, 12);
        }
    }

    private void specialAttack(SimplePlayer localPlayer) {
        int specialAttackPercentage = ctx.combat.getSpecialAttackPercentage();

        if (specialAttackPercentage != 100) {
            return;
        }

        boolean hasSpecialAttackTool = !ctx.equipment.populate()
                .filter(item -> SPECIAL_ATTACK_TOOL.contains(item.getName()))
                .isEmpty();

        if (!hasSpecialAttackTool) {
            updateStatus("Special attack tool: NOT FOUND");
            updateStatus("Special attack: Deactivated");
            specialAttackTool = false;
            specialAttackActive = false;
            return;
        }

        if (localPlayer.isAnimating() && ctx.combat.toggleSpecialAttack(true)) {
            ctx.game.tab(Game.Tab.INVENTORY);
        }
    }

    private void teleHome() {
        if (!ctx.pathing.inArea(homeArea)) {
            ctx.keyboard.sendKeys("::home");
            ctx.sleep(4000);
        }
    }

    private void clickOnBag() {
        SimpleWidget inventoryBagWidget = ctx.widgets.getWidget(INVENTORY_BAG_WIDGET_ID, INVENTORY_BAG_CHILD_ID);
        if (inventoryBagWidget != null) {
            inventoryBagWidget.click(0);
        }
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

    private int getStackSize(int itemId) {
        return ctx.inventory.populate().filter(itemId).population(true);
    }

    private void updateStatus(String newStatus) {
        status = newStatus;
        ctx.updateStatus(status);
        logger.info(status);
    }

    public String getPlayerName() {
        if (playerGameName == null) {
            playerGameName = ctx.players.getLocal().getName();
        }
        return playerGameName;
    }

    public static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    @Override
    public void onTerminate() {
        status = "Stopping bot";
        gui.setVisible(false);
        hidePaint = true;
        woodcuttingAction = false;
        miningAction = false;
        specialAttackActive = false;

        this.ctx.updateStatus("-------------------------------------");
        this.ctx.updateStatus("--------------- " + currentTime() + " ---------------");
        this.ctx.updateStatus("-------------------------------------");
        this.ctx.updateStatus("------- Thank You & Good Luck! -------");
        this.ctx.updateStatus("-------------------------------------");
        this.ctx.updateStatus("Crystals acquired: " + count);
        this.ctx.updateStatus("-------------------------------------");
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

            if (senderName.contains(playerGameName)) {
                ctx.updateStatus(currentTime() + " Someone asked for you");
                ctx.updateStatus(currentTime() + " Stopping script");
                ctx.stopScript();
            }

        }
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

        // Calculate experience and actions per hour
        long actionsPerHour = ctx.paint.valuePerHour(count, startTime);

        // Set up colors
        Color philippineRed = new Color(196, 18, 48);
        Color raisinBlack = new Color(35, 31, 32, 127);

        // Draw paint if not hidden
        if (!hidePaint) {
            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gradientPaint = new GradientPaint(5, 120, raisinBlack, 220, 230, philippineRed, true);
            g2d.setPaint(gradientPaint);
            g.fillRoundRect(5, 120, 200, 85, 20, 20);

            g.setColor(philippineRed);
            g.drawRoundRect(5, 120, 200, 85, 20, 20);

            Font title = new Font("Arial", Font.BOLD, 12);
            Font text = new Font("Arial", Font.PLAIN, 11);

            g.setFont(title);
            g.setColor(Color.WHITE);
            g.drawString("eCrystalHustler by Esmaabi", 15, 140);

            g.setFont(text);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + runTime, 15, 160);
            g.drawString("Crystals acquired: " + count + " (" + actionsPerHour + " per/h)", 15, 175);
            g.drawString("Status: " + status, 15, 190);
        }
    }
}
