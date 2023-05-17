package ePlankMakerZenyte;

import eRandomEventSolver.eRandomEventForester;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimpleWidget;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@ScriptManifest(author = "Esmaabi", category = Category.MONEYMAKING, description =
        "<br>Most effective plank maker bot on Zenyte! <br><br><b>Features & recommendations:</b><br><br>" +
        "<ul>" +
        "<li>This bot will make chosen planks at Woodcutting Guild;</li>" +
        "<li>You must start at Woodcutting Guild near Sawmill operator</li>" +
        "<li>You must have noted logs and GP in inventory or bot will stop</li>" +
        "<li>It's recommended to wear full Graceful or have few stamina potions in inventory</li>" +
        "<li>The bot will stop if you run out of noted logs or coins.</li></ul><br>" +
        "For more information, check out Esmaabi on SimpleBot!", discord = "Esmaabi#5752",
        name = "ePlankMakerBotZenyte", servers = { "Zenyte" }, version = "1")

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private final static int SAWMILL_OPERATOR = 5422;
    private final static int WIDGET_ID = 403;
    private final static int INVENTORY_BAG_WIDGET_ID = 548;
    private final static int INVENTORY_BAG_CHILD_ID = 58;
    private final WorldPoint DEPOSIT_BOX_LOCATION = new WorldPoint(1649, 3494, 0);
    private final WorldPoint NEAR_DEPOSIT_BOX_LOCATION = new WorldPoint(1649, 3495, 0);
    private final WorldPoint NEAR_SAWMILL_LOCATION = new WorldPoint(1624, 3500, 0);

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
    public static boolean useStaminaPotions;
    public static boolean outOfMoney = false;
    public static woodTypeEnum woodType;
    public static boolean hasStaminaPotions = true;

    @Override
    public int loopDuration() {
        return 150;
    }

    public enum woodTypeEnum {
        LOGS("normal", 101, 1512, 1511, 961, 960),
        OAK("oak",107, 1522, 1521, 8779, 8778),
        TEAK("teak",112, 6334, 6333, 8781, 8780),
        MAHOGANY("mahogany",117, 8836, 6332, 8783, 8782);

        private final String name;
        private final int widgetId;
        private final int logsNoted;
        private final int logs;
        private final int plankNoted;
        private final int plank;

        woodTypeEnum(String name, int widgetId, int logsNoted, int logs, int plankNoted, int plank) {
            this.name = name;
            this.widgetId = widgetId;
            this.logsNoted = logsNoted;
            this.logs = logs;
            this.plankNoted = plankNoted;
            this.plank = plank;
        }

        public String getName() {
            return name;
        }

        public int getWidgetId() {
            return widgetId;
        }

        public int getLogsNotedId() {
            return logsNoted;
        }

        public int getLogsId() {
            return logs;
        }

        public int getPlankNotedId() {
            return plankNoted;
        }

        public int getPlankId() {
            return plank;
        }
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
        tasks.addAll(Arrays.asList(new eRandomEventForester(ctx)));
        System.out.println("Started ePlankMakerBot!");
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
        useStaminaPotions = false;
        countActive = false;
    }

    @Override
    public void onProcess() {
        super.onProcess();

        if (botStarted) {

            int currentCount = getNotedPlanks(woodType.getPlankNotedId());
            handleCount(currentCount);

            if (ctx.pathing.energyLevel() > 30 && !ctx.pathing.running()) {
                ctx.pathing.running(true);
            }

            if (useStaminaPotions) {
                handleDrinkingForEnergy();
            }

            if (ctx.dialogue.dialogueOpen()) {
                processDialogueText();
            }

            if (!outOfMoney) {
                int planksInInventory = ctx.inventory.populate().filter(woodType.getPlankId()).population();
                int woodInInventory = ctx.inventory.populate().filter(woodType.getLogsId()).population();
                if (planksInInventory == 0 && woodInInventory > 0) {
                    makePlanks(woodType.getWidgetId(), woodType.getLogsId(), woodType.getName());
                } else if (planksInInventory > 0 && woodInInventory == 0 || planksInInventory == 0 && woodInInventory == 0) {
                    noteTask(woodType.getLogsNotedId(), woodType.getLogsId(), woodType.getPlankId(), woodType.getName());
                }
            } else {
                updateStatus("Out of GP. Stopping bot.");
                ctx.stopScript();
            }
        } else {
            status = "Start the bot";
        }
    }

    private void processDialogueText() {
        SimpleWidget operatorText = ctx.widgets.getWidget(231, 4);
        if (operatorText != null && !operatorText.isHidden()) {
            String lowerCaseText = operatorText.getText().toLowerCase();
            if (lowerCaseText.contains("money for all of them")) {
                outOfMoney = true;
            }
/*            } else {
                ctx.keyboard.clickKey(SPACE_BUTTON);
            }*/
        }
    }

    private void makePlanks(int widget, int wood, String name) {
        SimpleWidget plankWindow = ctx.widgets.getWidget(WIDGET_ID, widget);
        SimpleNpc sawmillOperator = ctx.npcs.populate().filter(SAWMILL_OPERATOR).next();
        int woodInv = ctx.inventory.populate().filter(wood).population();
        boolean widgetScreenVisible = plankWindow != null && !plankWindow.isHidden();

        if (woodInv == 0) {
            updateStatus("Out of " + name + " logs");
            ctx.updateStatus(currentTime());
            updateStatus("Stopping script");
            ctx.stopScript();
            return;
        }

        if (!widgetScreenVisible) {
            if (ctx.players.getLocal().getLocation().distanceTo(NEAR_SAWMILL_LOCATION) < 5) {
                if (sawmillOperator != null && sawmillOperator.validateInteractable()) {
                    updateStatus("Clicking Sawmill operator");
                    ctx.viewport.turnTo(sawmillOperator);
                    if (sawmillOperator.click("Buy-plank")) {
                        ctx.onCondition(() -> ctx.widgets.getWidget(WIDGET_ID, widget) != null, 250, 10);
                    }
                }
            } else {
                status = "Running to Sawmill operator";
                ctx.pathing.step(NEAR_SAWMILL_LOCATION);
            }
        } else {
            updateStatus("Making " + name + " planks");
            if (plankWindow.click(5)) {
                ctx.sleepCondition(() -> ctx.widgets.getWidget(WIDGET_ID, widget) != null, 600);
            }
        }
    }

    private void noteTask(int woodNoted, int wood, int plank, String name) {
        SimpleObject depositBox = ctx.objects.populate().filter(26254).nearest(DEPOSIT_BOX_LOCATION).next();

        if (ctx.players.getLocal().getLocation().distanceTo(NEAR_DEPOSIT_BOX_LOCATION) < 5) {
            if (depositBox != null && depositBox.validateInteractable()) {
                SimpleItem planksInv = ctx.inventory.populate().filter(plank).next();
                SimpleItem notedWoodInv = ctx.inventory.populate().filter(woodNoted).next();
                SimpleItem woodInv = ctx.inventory.populate().filter(wood).next();

                if (woodInv == null && notedWoodInv == null) {
                    updateStatus("Out of " + name + " logs");
                    ctx.updateStatus(currentTime());
                    updateStatus("Stopping script");
                    ctx.stopScript();
                    return;
                }

                if (planksInv != null && woodInv == null) {
                    clickOnBag();
                    updateStatus("Getting " + name + " planks noted");
                    planksInv.click("Use");
                    ctx.sleep(200);
                    if (depositBox.click("Use")) {
                        ctx.onCondition(() -> ctx.inventory.populate().filter(plank).isEmpty(), 250, 10);
                    }
                }

                if (planksInv == null && woodInv == null) {
                    if (notedWoodInv != null) {
                        clickOnBag();
                        updateStatus("Getting " + name + " logs unnoted");
                        notedWoodInv.click("Use");
                        ctx.sleep(200);
                        if (depositBox.click("Use")) {
                            ctx.onCondition(() -> !ctx.inventory.populate().filter(wood).isEmpty(), 250, 10);
                        }
                    }
                }
            }
        } else {
            status = "Running to Bank deposit box";
            ctx.pathing.step(NEAR_DEPOSIT_BOX_LOCATION);
        }
    }

    private void handleDrinkingForEnergy() {
        if (!ctx.pathing.running()) {
            ctx.pathing.running(true);
        }

        if (ctx.pathing.energyLevel() <= 19) {
            final SimpleItem potionInv = ctx.inventory.populate().filter(Pattern.compile("Stamina potion\\(\\d+\\)")).next();
            final int cached = ctx.pathing.energyLevel();
            if (potionInv == null) {
                updateStatus("Stamina potions: deactivated");
                useStaminaPotions = false;
                hasStaminaPotions = false;
                return;
            }

            updateStatus("Drinking stamina potion");
            if (potionInv != null && potionInv.click("Drink")) {
                hasStaminaPotions = true;
                ctx.onCondition(() -> ctx.pathing.energyLevel() > cached, 250, 12);
            }
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

    private int getNotedPlanks(int notedPlanksId) {
        return ctx.inventory.populate().filter(notedPlanksId).population(true);
    }

/*    private void updateStatus(String newStatus) {
        status = newStatus;
        ctx.updateStatus(status);
        System.out.println(status);
    }*/

    private void updateStatus(String newStatus) {
        status = newStatus;
        ctx.updateStatus(status);
        logger.info(status);
    }

    public static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    @Override
    public void onTerminate() {
        status = "Stopping bot";
        gui.setVisible(false);
        hidePaint = true;
        outOfMoney = false;

        this.ctx.updateStatus("-------------------------------------");
        this.ctx.updateStatus("--------------- " + currentTime() + " ---------------");
        this.ctx.updateStatus("-------------------------------------");
        this.ctx.updateStatus("------- Thank You & Good Luck! -------");
        this.ctx.updateStatus("-------------------------------------");
        this.ctx.updateStatus("Planks made: " + count);
        this.ctx.updateStatus("-------------------------------------");
    }

    @Override
    public void onChatMessage(ChatMessage m) {
        if (m.getMessage() == null) {
            return;
        }

        String message = m.getMessage().toLowerCase();
        String playerName = ctx.players.getLocal().getName().toLowerCase();

        if (message.contains(playerName)) {
            ctx.updateStatus(currentTime() + " Someone asked for you");
            ctx.updateStatus(currentTime() + " Stopping script");
            ctx.stopScript();
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
        //long actionsPerHour = count * 3600000L / (System.currentTimeMillis() - this.startTime);
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
            g.drawString("ePlankMakerBot by Esmaabi", 15, 140);

            g.setFont(text);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + runTime, 15, 160);
            g.drawString("Planks made: " + count + " (" + actionsPerHour + " per/h)", 15, 175);
            g.drawString("Status: " + status, 15, 190);
        }
    }
}
