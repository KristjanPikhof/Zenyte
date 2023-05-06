package ePotionBuyerZenyte;

import simple.hooks.filters.SimpleShop;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.script.Script;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@ScriptManifest(author = "Esmaabi", category = Category.MONEYMAKING, description =
        "<br>Most effective potion buyer bot on Zenyte! <br><br><b>Features & recommendations:</b><br><br>" +
        "<ul>" +
        "<li>This bot will buy any potion from John;</li>" +
        "<li>You must start at home with money in inventory;</li>" +
        "<li>Insert the potion ID in GUI and bot will start buying</li>" +
        "<li>Bot will stop if out of money.</li></ul>", discord = "Esmaabi#5752",
        name = "ePotionBuyerZenyte", servers = { "Zenyte" }, version = "0.1")

public class eMain extends Script {

    // Variables
    private static eGui gui;
    private long startTime = 0L;
    private int count;
    static String status = null;
    public static boolean botStarted = false;
    public static boolean hidePaint = false;
    public static boolean outOfMoney = false;

    // Constants
    public static int itemId;
    static String NPC_BANKER = "Banker";
    static String bankOpenAction = "Bank";
    private final static int NPC_ZAHUR = 4753;

    private final static int NPC_JOHN = 10007;

    // Gui
    private void initializeGUI() {
        gui = new eGui();
        gui.setVisible(true);
        gui.setLocale(ctx.getClient().getCanvas().getLocale());
    }

    @Override
    public void onExecute() {
        System.out.println("Started ePotionBuyer!");
        initializeGUI();

        status = "Setting up bot";
        this.startTime = System.currentTimeMillis(); //paint
        count = 0;
        botStarted = false;
        hidePaint = false;
    }

    @Override
    public void onProcess() {

        if (botStarted) {
            if (!outOfMoney) {
                int buyItem = ctx.inventory.populate().filter(itemId).population();
                if (buyItem == 0) {
                    buyingTask(NPC_JOHN, itemId);
                } else if (buyItem > 0) {
                    decantTask(NPC_ZAHUR);
                }
            } else {
                updateStatus("Out of GP. Stopping bot.");
                ctx.stopScript();
            }

        }

    }

    private void buyingTask(int npcId, int itemId) {
        final int SLEEP_DURATION = randomSleeping(4000, 10000);

        if (ctx.inventory.inventoryFull()) {
            updateStatus("Closing shop");
            ctx.shop.closeShop();
            return;
        }

        if (!ctx.shop.shopOpen()) {
            updateStatus("Opening shop");
            SimpleNpc tradeTo = ctx.npcs.populate().filter(npcId).nearest().next();
            if (tradeTo != null && tradeTo.validateInteractable()) {
                tradeTo.click("Trade");
                ctx.sleepCondition(() -> !ctx.shop.shopOpen(), 1200);
            }
        }

        if (ctx.shop.shopOpen()) {
            int buyItem = ctx.shop.populate().filter(itemId).population(true);
            updateStatus("Items left in store: " + buyItem);
            if (buyItem < 27) {
                updateStatus("Too few items");
                updateStatus("Sleeping for " + formatToSeconds(SLEEP_DURATION));
                ctx.sleep(SLEEP_DURATION);
            } else {
                updateStatus("Buying from shop");
                ctx.shop.buy(itemId, SimpleShop.Amount.FIFTY);
            }
        }
    }

    private void decantTask(int npcId) {
        int chosenItem = getItemPopulation();

        if (ctx.shop.shopOpen()) {
            updateStatus("Closing shop");
            ctx.shop.closeShop();
        }

        if (chosenItem != 0) {
            chooseWidget(npcId);
        } else {
            updateStatus("No buy item in inventory");
            buyingTask(NPC_JOHN, itemId);
        }
    }

    private void chooseWidget(int npcId) {
        SimpleWidget dialogueOption = ctx.widgets.getWidget(582, 6);

        if (dialogueOption != null) {
            updateStatus("Decanting");
            dialogueOption.click(0);
            int chosenItem = getItemPopulation();
            ctx.onCondition(() -> chosenItem != getItemPopulation(), 250, 10);
            count += chosenItem;
        } else {
            SimpleNpc zahurNpc = ctx.npcs.populate().filter(npcId).nearest().next();
            if (zahurNpc != null && zahurNpc.validateInteractable()) {
                updateStatus("Clicking Zahur");
                zahurNpc.click("Decant");
            }
        }
    }


    private int getItemPopulation() {
        return ctx.inventory.populate().filter(itemId).population();
    }

    private void updateStatus(String newStatus) {
        status = newStatus;
        ctx.updateStatus(status);
        System.out.println(status);
    }

    public static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static int randomSleeping(int minimum, int maximum) {
        return (int)(Math.random() * (maximum - minimum)) + minimum;
    }

    @Override
    public void onTerminate() {
        status = "Stopping bot";
        gui.setVisible(false);
        hidePaint = true;
        outOfMoney = false;
        ctx.updateStatus("Bought: " + count + " items");

        this.ctx.updateStatus("-------------- " + currentTime() + " --------------");
        this.ctx.updateStatus("----------------------");
        this.ctx.updateStatus("Thank You & Good Luck!");
        this.ctx.updateStatus("----------------------");
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
        } else if (message.contains("cannot buy")) {
            outOfMoney = true;
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
            g.drawString("ePotionBuyer by Esmaabi", 15, 140);

            g.setFont(text);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + runTime, 15, 160);
            g.drawString("Bought: " + count + " (" + actionsPerHour + " per/h)", 15, 175);
            g.drawString("Status: " + status, 15, 190);
        }
    }

    private String formatTime(long ms) {
        long s = ms / 1000L;
        long m = s / 60L;
        long h = m / 60L;
        s %= 60L;
        m %= 60L;
        h %= 24L;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public String formatToSeconds(long ms) {
        double seconds = ms / 1000.0;
        return String.format("%.2f", seconds) + "sec";
    }

}
