package eGlassblowingBotZenyte;

import net.runelite.api.ChatMessageType;
import simple.hooks.filters.SimpleShop;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.queries.SimpleItemQuery;
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

    // Constants
    private static final SimpleSkills.Skills CHOSEN_SKILL = SimpleSkills.Skills.CRAFTING;
    private static final int BLOWING_PIPE = 1785;
    private static final int MOLTEN_GLASS = 1775;
    private static final int BUCKET_OF_SAND = 1783;
    private static final int SODA_ASH = 1781;
    private static final int REQUIRED_ITEMS = 10;
    private static final Set<Integer> CRAFTING_ITEMS = new HashSet<>(Arrays.asList(1919, 4527, 4525, 229, 6667, 567, 4542));

    // Vars
    private long startTime = 0L;
    private long startingSkillLevel;
    private long startingSkillExp;
    private int count;
    private int currentExp;
    private long lastAnimation = -1;
    public static String status = null;
    public static boolean started;
    private static boolean hidePaint = false;
    private static String playerGameName;
    private boolean errorAppeared;

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

        //tasks.addAll(Arrays.asList());

        System.out.println("Started eGlassblowingBot!");

        this.ctx.updateStatus("--------------- " + currentTime() + " ---------------");
        this.ctx.updateStatus("-------------------------------------");
        this.ctx.updateStatus("         eGlassblowingBotZenyte      ");
        this.ctx.updateStatus("-------------------------------------");
        started = false;

        status = "Setting up bot";
        this.startTime = System.currentTimeMillis();
        this.startingSkillLevel = this.ctx.skills.realLevel(CHOSEN_SKILL);
        this.startingSkillExp = this.ctx.skills.experience(CHOSEN_SKILL);
        currentExp = this.ctx.skills.experience(CHOSEN_SKILL);// for actions counter by xp drop
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
            switchTabs(Game.Tab.MAGIC, Game.Tab.INVENTORY, 4);  // Switch tabs 4 times
            status = "Lunar spellbook required!";
            ctx.log("Stopping script");
            ctx.log("Please change spellbook to Lunar");
            ctx.sleep(5000);
            ctx.stopScript();
        }

        if (!started) {
            status = "Choose mode and start";

        } else {

            if (errorAppeared) {
                sellingGoods();
            }

            if (!ctx.shop.shopOpen() && hasItemsInInventory(10980) && !hasItemsInInventory(MOLTEN_GLASS, BUCKET_OF_SAND, SODA_ASH)) {
                status = "Dropping empty light orbs";
                getItemsFiltered(10980).forEach(item -> ctx.inventory.dropItem(item));
                ctx.onCondition(() -> !hasItemsInInventory(10980), 200, 10);
            }

            if (!hasItemsInInventory(MOLTEN_GLASS, BUCKET_OF_SAND, SODA_ASH)) {
                if (!ctx.shop.shopOpen()) {
                    openShopTask();
                }
                shoppingTask();
            }

            if (hasItemsInInventory(BUCKET_OF_SAND, SODA_ASH) && !hasItemsInInventory(MOLTEN_GLASS)) {
                if (ctx.shop.shopOpen()) {
                    updateStatus("Closing shop");
                    ctx.shop.closeShop();
                }

                if (hasItemsInInventory(SODA_ASH, BUCKET_OF_SAND) && !ctx.shop.shopOpen()) {
                    status = "Making molten glass";
                    ctx.magic.castSpellOnce("Superglass Make");
                    ctx.sleep(1200);
                    ctx.onCondition(() -> hasItemsInInventory(MOLTEN_GLASS), 200, 10);
                }
            }

            if (hasItemsInInventory(MOLTEN_GLASS) && !ctx.shop.shopOpen()) {
                glassblowingItems();
            }
        }
    }

    private void glassblowingItems() {
        SimpleItem glassblowingPipe = ctx.inventory.populate().filter(BLOWING_PIPE).next();
        SimpleItem moltenGlassInv = ctx.inventory.populate().filter(MOLTEN_GLASS).next();
        status = "Glassblowing";

        if (ctx.players.getLocal().getAnimation() != 884 && (System.currentTimeMillis() > (lastAnimation + 3000))) {

            if (glassblowingPipe != null && glassblowingPipe.validateInteractable()
                    && moltenGlassInv != null && moltenGlassInv.validateInteractable()
                    && !ctx.dialogue.dialogueOpen()) {
                glassblowingPipe.click("Use");
                ctx.sleep(100);
                moltenGlassInv.click(0);
                ctx.onCondition(() -> ctx.dialogue.dialogueOpen(), 250, 10);
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
                ctx.onCondition(() -> ctx.players.getLocal().isAnimating(), 250,10);
            }

        } else if (ctx.players.getLocal().isAnimating()) {
            lastAnimation = System.currentTimeMillis();
        }
    }

    private void sellingGoods() {
        if (!ctx.shop.shopOpen()) {
            openShopTask();
        }

        updateStatus("Selling items");

        if (errorAppeared) {
            ctx.shop.sell(BUCKET_OF_SAND, SimpleShop.Amount.FIFTY);
            ctx.shop.sell(SODA_ASH, SimpleShop.Amount.FIFTY);
            ctx.onCondition(() -> hasItemsInInventory(BUCKET_OF_SAND, SODA_ASH));
            errorAppeared = false;
        }

        for (int itemId : CRAFTING_ITEMS) {
            if (!ctx.inventory.populate().filter(itemId).isEmpty()) {
                ctx.shop.sell(itemId, SimpleShop.Amount.FIFTY);
                ctx.onCondition(() -> !hasItemsInInventory(itemId), 250, 8);
            }
        }
    }

    private boolean noCraftingItemsInInventory() {
        for (int itemId : CRAFTING_ITEMS) {
            if (hasItemsInInventory(itemId)) {
                return false;
            }
        }
        return true;
    }

    private void openShopTask() {

        SimpleNpc traderCrew = ctx.npcs.populate().filter("Trader Crewmember").filterHasAction("Trade").nearest().next();

        if (ctx.shop.shopOpen()) return;

        if (traderCrew == null) {
            ctx.log("Trader Crewmember not found");
            ctx.sendLogout();
        }

        if (traderCrew != null && traderCrew.validateInteractable()) {
            updateStatus("Opening shop");
            if (!traderCrew.visibleOnScreen()) {
                ctx.pathing.step(traderCrew.getLocation());
                ctx.viewport.turnTo(traderCrew);
            }
            traderCrew.menuAction("Trade");
            ctx.onCondition(() -> ctx.shop.shopOpen());
        }
    }

    private void shoppingTask() {

        if (!ctx.shop.shopOpen()) return;

        if (!noCraftingItemsInInventory()) {
            sellingGoods();
        }

        if (noCraftingItemsInInventory()) {
            CompletableFuture<Void> buyItemsFuture = CompletableFuture.runAsync(() -> {
                int blowingPipeCount = ctx.inventory.populate().filter(BLOWING_PIPE).population();
                int sandCount = ctx.inventory.populate().filter(BUCKET_OF_SAND).population();
                int sodaAshCount = ctx.inventory.populate().filter(SODA_ASH).population();

                if (blowingPipeCount < 1) {
                    buyItem(BLOWING_PIPE, SimpleShop.Amount.ONE, "glassblowing pipe");
                }

                if (sandCount < REQUIRED_ITEMS) {
                    buyItem(BUCKET_OF_SAND, SimpleShop.Amount.TEN, "soda ashes");
                }

                if (sodaAshCount < REQUIRED_ITEMS) {
                    buyItem(SODA_ASH, SimpleShop.Amount.TEN, "buckets of sand");
                }
            });

            buyItemsFuture.join();

            int sandPopulation = ctx.inventory.populate().filter(BUCKET_OF_SAND).population();
            int sodaAshPopulation = ctx.inventory.populate().filter(SODA_ASH).population();

            if (sandPopulation == REQUIRED_ITEMS && sodaAshPopulation == REQUIRED_ITEMS) {
                updateStatus("Closing shop");
                ctx.shop.closeShop();
            } else {
                sellingGoods();
                ctx.shop.sell(BUCKET_OF_SAND, SimpleShop.Amount.FIFTY);
                ctx.shop.sell(SODA_ASH, SimpleShop.Amount.FIFTY);
                ctx.shop.sell(MOLTEN_GLASS, SimpleShop.Amount.FIFTY);
            }
        }
    }

    private void buyItem(int itemId, SimpleShop.Amount amount, String itemName) {
        updateStatus("Buying " + itemName);
        ctx.shop.buy(itemId, amount);
        ctx.onCondition(() -> hasItemsInInventory(itemId), 100, 10);
    }

    private SimpleItemQuery<SimpleItem> getItemsFiltered(int... itemIds) {
        return ctx.inventory.populate().filter(itemIds);
    }

    private boolean hasItemsInInventory(int... itemIds) {
        return !getItemsFiltered(itemIds).isEmpty();
    }

    private void switchTabs(Game.Tab tabOne, Game.Tab tabTwo, int times) {
        for (int i = 0; i < times; i++) {
            ctx.game.tab(tabOne);
            ctx.game.tab(tabTwo);
        }
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

    public static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
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
        String gameMessage = getEvent.getMessage();

        if (m.getMessage() == null) {
            return;
        }

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
        } else if (message.contains("need some sand to cast") || message.contains("need either some soda ash or seaweed to")) {
            errorAppeared = true;
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

        Utility.Trivia.eTriviaInfo.handleBroadcastMessage(getType, gameMessage);
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
            g.drawString("eGlassblowingBot by Esmaabi", 15, 135);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + runTime, 15, 150);
            g.drawString("Skill Level: " + currentSkillLevel + " (+" + skillLevelsGained + "), started at " + this.startingSkillLevel, 15, 165);
            g.drawString("Current Exp: " + currentSkillExp, 15, 180);
            g.drawString("Exp gained: " + skillExpGained + " (" + (skillExpPerHour / 1000L) + "k xp/h)", 15, 195);
            g.drawString("Actions made: " + count + " (" + actionsPerHour + " per/h)", 15, 210);
            g.drawString("Status: " + status, 15, 225);

        }
    }

}