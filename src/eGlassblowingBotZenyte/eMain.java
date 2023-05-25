package eGlassblowingBotZenyte;

import eRandomEventSolver.eRandomEventForester;
import net.runelite.api.ChatMessageType;
import simple.hooks.filters.SimpleShop;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Game;
import simple.hooks.simplebot.Magic;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleWidget;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@ScriptManifest(author = "Esmaabi", category = Category.CRAFTING,
        description = "<html><br>" +
                "Introducing the most efficient glassblowing bot for Zenyte! " +
                "<br><br><b>Features and Recommendations:</b><br><br> " + "<ul>" +
                "<li>Start near <b>charter trader crewmember</b> or bot will stop.</li>" +
                "<li>The bot will sell all crafted items, except for empty light orbs.</li>" +
                "<li>It's recommended to wield <b>smoke battlestaff</b> or any elemental staff.</li>" +
                "<li>Make sure you have enough coins and air/fire/astral runes.</li>" +
                "<li>The bot will stop if you run out of coins or runes.</li></ul><br>" +
                "For more information, check out Esmaabi on SimpleBot!</html>",
        discord = "Esmaabi#5752",
        name = "eGlassblowingBotZenyte", servers = { "Zenyte" }, version = "2.1")

public class eMain extends TaskScript implements LoopingScript {

    //vars
    private long startTime = 0L;
    private long startingSkillLevel;
    private long startingSkillExp;
    private int count;
    private int currentExp;
    public static String status = null;
    private long lastAnimation = -1;

    public static boolean started;
    private static boolean hidePaint = false;
    private static String playerGameName;

    //items
    private final Set<Integer> craftingItems = new HashSet<>(Arrays.asList(1919, 4527, 4525, 229, 6667, 567, 4542));
    private static final int blowingPipe = 1785;
    private static final int moltenGlass = 1775;
    private static final int bucketOfSand = 1783;
    private static final int sodaAsh = 1781;
    private static final int REQUIRED_ITEMS = 10;

    public static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
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

        System.out.println("Started eGlassblowingBot!");


        this.ctx.updateStatus("--------------- " + currentTime() + " ---------------");
        this.ctx.updateStatus("-------------------------------------");
        this.ctx.updateStatus("         eGlassblowingBotZenyte      ");
        this.ctx.updateStatus("-------------------------------------");
        started = false;

        status = "Setting up bot";
        this.startTime = System.currentTimeMillis();
        this.startingSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.CRAFTING);
        this.startingSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.CRAFTING);
        currentExp = this.ctx.skills.experience(SimpleSkills.Skills.CRAFTING);// for actions counter by xp drop
        count = 0;
        ctx.viewport.angle(270);
        ctx.viewport.pitch(true);

        //GUI
        eGui gui = new eGui();
        gui.setVisible(true);
    }

    @Override
    public void onProcess() {
        super.onProcess();

        if (currentExp != this.ctx.skills.experience(SimpleSkills.Skills.CRAFTING)) {
            count++;
            currentExp = this.ctx.skills.experience(SimpleSkills.Skills.CRAFTING);
        }

        if (ctx.magic.spellBook() != Magic.SpellBook.LUNAR) {
            ctx.game.tab(Game.Tab.MAGIC);
            ctx.game.tab(Game.Tab.INVENTORY);
            ctx.game.tab(Game.Tab.MAGIC);
            ctx.game.tab(Game.Tab.INVENTORY);
            ctx.game.tab(Game.Tab.MAGIC);
            ctx.game.tab(Game.Tab.INVENTORY);
            ctx.game.tab(Game.Tab.MAGIC);
            status = "Lunar spellbook required!";
            ctx.game.tab(Game.Tab.INVENTORY);
            ctx.updateStatus("Stopping script");
            ctx.game.tab(Game.Tab.MAGIC);
            ctx.updateStatus("and start script over!");
            ctx.game.tab(Game.Tab.INVENTORY);
            ctx.updateStatus("Please change spellbook to normal");
            ctx.game.tab(Game.Tab.MAGIC);
            ctx.sleep(10000);
            ctx.stopScript();
        }

        if (!started) {
            status = "Choose mode and start";

        } else {

            if (!ctx.shop.shopOpen() && !ctx.inventory.populate().filter(10980).isEmpty()
                    && ctx.inventory.populate().filter(moltenGlass, bucketOfSand, sodaAsh).isEmpty()) { // empty light orbs drop task
                status = "Dropping empty light orbs";
                ctx.inventory.populate().filter(10980).forEach((item) -> ctx.inventory.dropItem(item));
                ctx.onCondition(() -> ctx.inventory.populate().filter(10980).isEmpty(), 200, 10);
            }

            if (ctx.inventory.populate().filter(moltenGlass, bucketOfSand, sodaAsh).isEmpty()) {
                if (ctx.shop.shopOpen()) {
                    shoppingTask();
                } else {
                    openShopTask();
                }
            }

            if (ctx.inventory.populate().filter(moltenGlass).isEmpty() && (!ctx.inventory.populate().filter(bucketOfSand).isEmpty()
                    && !ctx.inventory.populate().filter(sodaAsh).isEmpty())) { // Making molten glass

                if (ctx.shop.shopOpen()) {
                    updateStatus("Closing shop");
                    ctx.shop.closeShop();
                }

                if ((!ctx.inventory.populate().filter(sodaAsh).isEmpty() && !ctx.inventory.populate().filter(bucketOfSand).isEmpty())
                        && ctx.inventory.populate().filter(moltenGlass).isEmpty() && !ctx.shop.shopOpen()) {
                    status = "Making molten glass";
                    ctx.magic.castSpellOnce("Superglass Make");
                    ctx.sleep(1200);
                    ctx.onCondition(() -> !ctx.inventory.populate().filter(moltenGlass).isEmpty(), 200, 10);
                }

            }

            if (!ctx.inventory.populate().filter(moltenGlass).isEmpty() && !ctx.shop.shopOpen()) { //Glassblowing items

                SimpleItem glassblowingPipe = ctx.inventory.populate().filter(blowingPipe).next();
                SimpleItem moltenGlassInv = ctx.inventory.populate().filter(moltenGlass).next();
                status = "Glassblowing";

                if (ctx.players.getLocal().getAnimation() != 884 && (System.currentTimeMillis() > (lastAnimation + 3000))) {

                    if (glassblowingPipe != null && glassblowingPipe.validateInteractable()
                            && moltenGlassInv != null && moltenGlassInv.validateInteractable()
                            && !ctx.dialogue.dialogueOpen()) {
                        glassblowingPipe.click("Use");
                        ctx.sleep(100);
                        moltenGlassInv.click(0);
                        ctx.sleepCondition(() -> ctx.dialogue.dialogueOpen(), 2000);
                    }

                    if (ctx.dialogue.dialogueOpen()) {
                        updateStatus("Making " + eGui.nameOfItem);
                        SimpleWidget makeAllButton = ctx.widgets.getWidget(270, 12); //Make ALL button
                        SimpleWidget itemToMake = ctx.widgets.getWidget(eGui.widgetItem1, eGui.widgetItem2); //Item from GUI
                        if (itemToMake.validateInteractable() && !itemToMake.isHidden()) {
                            makeAllButton.click(0);
                            ctx.sleep(50);
                            itemToMake.click(0);
                        }
                        ctx.onCondition(() -> ctx.players.getLocal().isAnimating(), 200,10);
                    }

                } else if (ctx.players.getLocal().isAnimating()) {
                    lastAnimation = System.currentTimeMillis();
                }
            }
        }
    }

    private void sellingGoods() {
        updateStatus("Selling items");
        for (int itemId : craftingItems) {
            if (!ctx.inventory.populate().filter(itemId).isEmpty()) {
                ctx.shop.sell(itemId, SimpleShop.Amount.FIFTY);
                ctx.onCondition(() -> ctx.inventory.populate().filter(itemId).isEmpty(), 100, 10);
            }
        }
    }

    private void openShopTask() {
        if (!ctx.shop.shopOpen()) {
            updateStatus("Opening shop");
            SimpleNpc traderCrew = ctx.npcs.populate().filter("Trader Crewmember").filterHasAction("Trade").nearest().next();
            if (traderCrew != null && traderCrew.validateInteractable()) {
                ctx.viewport.turnTo(traderCrew);
                traderCrew.click("Trade", "Trader Crewmember");
                CompletableFuture<Boolean> shopOpenFuture = CompletableFuture.supplyAsync(() -> ctx.onCondition(() -> ctx.shop.shopOpen(), 100, 10));
                shopOpenFuture.join();
            } else {
                updateStatus("Charter Trader not found");
                ctx.sleep(5000);
                updateStatus("Stopping script");
                ctx.stopScript();
            }
        }
    }

    private void shoppingTask() {

        if (ctx.shop.shopOpen()) {
            sellingGoods();

            CompletableFuture<Void> buyItemsFuture = CompletableFuture.runAsync(() -> {
                int blowingPipeCount = ctx.inventory.populate().filter(blowingPipe).population();
                int sandCount = ctx.inventory.populate().filter(bucketOfSand).population();
                int sodaAshCount = ctx.inventory.populate().filter(sodaAsh).population();

                if (blowingPipeCount < 1) {
                    buyItem(blowingPipe, SimpleShop.Amount.ONE, "glassblowing pipe");
                }

                if (sandCount < REQUIRED_ITEMS) {
                    buyItem(bucketOfSand, SimpleShop.Amount.TEN, "soda ashes");
                }

                if (sodaAshCount < REQUIRED_ITEMS) {
                    buyItem(sodaAsh, SimpleShop.Amount.TEN,"buckets of sand");
                }
            });

            buyItemsFuture.join();

            int sandPopulation = ctx.inventory.populate().filter(bucketOfSand).population();
            int sodaAshPopulation = ctx.inventory.populate().filter(sodaAsh).population();

            if (sandPopulation == REQUIRED_ITEMS && sodaAshPopulation == REQUIRED_ITEMS) {
                updateStatus("Closing shop");
                ctx.shop.closeShop();
            }
        }
    }

    private void buyItem(int itemId, SimpleShop.Amount amount, String itemName) {
        updateStatus("Buying " + itemName);
        ctx.shop.buy(itemId, amount);
        CompletableFuture<Void> itemBoughtFuture = CompletableFuture.runAsync(() -> {
            ctx.onCondition(() -> !ctx.inventory.populate().filter(itemId).isEmpty(), 100, 10);
        });
        itemBoughtFuture.join();
    }

    public String getPlayerName() {
        if (playerGameName == null) {
            playerGameName = ctx.players.getLocal().getName();
        }
        return playerGameName;
    }

    private void updateStatus(String newStatus) {
        status = newStatus;
        ctx.updateStatus(status);
        System.out.println(status);
    }

    @Override
    public void onTerminate() {
        this.startingSkillLevel = 0L;
        this.startingSkillExp = 0L;
        this.ctx.updateStatus("You made a total of " + count + " glass items.");
        count = 0;
        started = false;
        eGui.widgetItem2 = -1;


        this.ctx.updateStatus("--------------- " + currentTime() + " --------------");
        this.ctx.updateStatus("----------------------------------");
        this.ctx.updateStatus("      Thank You & Good Luck!      ");
        this.ctx.updateStatus("----------------------------------");
    }

    @Override
    public void onChatMessage(ChatMessage m) {

        ChatMessageType getType = m.getType();
        net.runelite.api.events.ChatMessage getEvent = m.getChatEvent();
        playerGameName = getPlayerName();

        if (m.getMessage() == null) {
            return;
        }

        if (m.getMessage() != null) {
            String message = m.getMessage().toLowerCase();
            if (message.contains("don't have enough")) {
                ctx.updateStatus(currentTime() + " Out of coins");
                ctx.updateStatus(currentTime() + " Stopping script");
                ctx.sleep(3000);
                ctx.stopScript();
            } else if (message.contains("do not have enough")) {
                ctx.updateStatus(currentTime() + " Out of runes");
                ctx.updateStatus(currentTime() + " Stopping script");
                ctx.sleep(3000);
                ctx.stopScript();
            }
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
    public int loopDuration() {
        return 200;
    }

    public void paint(Graphics g) {
        // Check if mouse is hovering over the paint
        Point mousePos = ctx.mouse.getPoint();
        if (mousePos != null) {
            Rectangle paintRect = new Rectangle(5, 120, 200, 110);
            hidePaint = paintRect.contains(mousePos.getLocation());
        }

        // Get runtime and skill information
        long runTime = System.currentTimeMillis() - this.startTime;
        long currentSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.CRAFTING);
        long currentSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.CRAFTING);
        long skillLevelsGained = currentSkillLevel - this.startingSkillLevel;
        long skillExpGained = currentSkillExp - this.startingSkillExp;

        // Calculate experience and actions per hour
        long skillExpPerHour = skillExpGained * 3600000L / runTime;
        long actionsPerHour = count * 3600000L / (System.currentTimeMillis() - this.startTime);

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
            g.drawString("eGlassblowingBot by Esmaabi", 15, 135);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + formatTime(runTime), 15, 150);
            g.drawString("Skill Level: " + this.startingSkillLevel + " (+" + skillLevelsGained + "), started at " + currentSkillLevel, 15, 165);
            g.drawString("Current Exp: " + currentSkillExp, 15, 180);
            g.drawString("Exp gained: " + skillExpGained + " (" + (skillExpPerHour / 1000L) + "k xp/h)", 15, 195);
            g.drawString("Actions made: " + count + " (" + actionsPerHour + " per/h)", 15, 210);
            g.drawString("Status: " + status, 15, 225);

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

}