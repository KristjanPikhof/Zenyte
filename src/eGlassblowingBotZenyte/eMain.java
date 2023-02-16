package eGlassblowingBotZenyte;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ScriptManifest(author = "Esmaabi", category = Category.CRAFTING,
        description = "<br>The most effective glassblowing bot on Zenyte! <br><br><b>Features & recommendations:</b><br>" +
                "<ul><li>You must have enough coins & fire / astral runes in inventory;</li>" +
                "<li>You must wield <b>any air staff</b>;</li>" +
                "<li>You must start near charter trader crewmembers;</li>" +
                "<li>Bot will sell to shop all the crafted items except empty light orb (drop);</li>" +
                "<li>Script will stop if you are out of money or runes.</li></ul>",
        discord = "Esmaabi#5752",
        name = "eGlassblowingBotZenyte", servers = { "Zenyte" }, version = "1")

public class eMain extends TaskScript implements LoopingScript {

    //vars
    private long startTime = 0L;
    private long startingSkillLevel;
    private long startingSkillExp;
    private int count;
    private int currentExp;
    public static String status = null;
    private long lastAnimation = -1;
    private long lastSell = -1;
    public static boolean started;

    //items
    private final int[] craftingItems = {1919, 4527, 4525, 229, 6667, 567, 4542};
    private final int blowingPipe = 1785;
    private final int moltenGlass = 1775;
    private final int bucketOfSand = 1783;
    private final int sodaAsh = 1781;

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

        tasks.addAll(Arrays.asList());

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
            status = "Script paused, choose mode";

        } else {

            if (!ctx.shop.shopOpen() && !ctx.inventory.populate().filter(10980).isEmpty()
                    && ctx.inventory.populate().filter(moltenGlass, bucketOfSand, sodaAsh).isEmpty()) { // empty light orbs drop task
                status = "Dropping empty light orbs";
                ctx.inventory.populate().filter(10980).forEach((item) -> ctx.inventory.dropItem(item));
                ctx.onCondition(() -> ctx.inventory.populate().filter(10980).isEmpty(), 200, 10);
            }

            if (!ctx.npcs.populate().filter("Trader Crewmember").isEmpty()) {

                    if (ctx.inventory.populate().filter(moltenGlass, bucketOfSand, sodaAsh).isEmpty() && (System.currentTimeMillis() > (lastSell + 3000))) {
                        shoppingTask();

                    } else if (ctx.shop.shopOpen() && !ctx.inventory.populate().filter(bucketOfSand, sodaAsh).isEmpty()) {
                        lastAnimation = System.currentTimeMillis();
                        loopBroken();
                    }

            } else {
                status = "Trader Crewmember not found";
                ctx.updateStatus(currentTime() + " Trader Crewmember not found");
                ctx.updateStatus(currentTime() + " Stopping script");
                ctx.sleep(2400);
                ctx.stopScript();
            }

            if (ctx.inventory.populate().filter(moltenGlass).isEmpty() && (!ctx.inventory.populate().filter(bucketOfSand).isEmpty()
                    && !ctx.inventory.populate().filter(sodaAsh).isEmpty())) { // Making molten glass

                if (ctx.shop.shopOpen()) {
                    status = "Closing shop";
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
                        status = "Choosing item to make";
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
        for (int itemId : craftingItems) {
            if (!ctx.inventory.populate().filter(itemId).isEmpty()) {
                ctx.shop.sell(itemId, SimpleShop.Amount.FIFTY);
                ctx.onCondition(() -> ctx.inventory.populate().filter(itemId).isEmpty(), 100, 10);
            }
        }
    }

    private void shoppingTask() {
        if (!ctx.shop.shopOpen()) {
            status = "Opening shop";
            SimpleNpc traderCrew = ctx.npcs.populate().filter("Trader Crewmember").filterHasAction("Trade").nearest().next();
            if (traderCrew != null && traderCrew.validateInteractable()) {
                ctx.viewport.turnTo(traderCrew);
                traderCrew.click("Trade", "Trader Crewmember");
                ctx.onCondition(() -> ctx.shop.shopOpen(), 100, 10);
            }
            lastSell = System.currentTimeMillis();
            return;
        }

        if (ctx.shop.shopOpen()) {

            sellingGoods();

            if (ctx.inventory.populate().filter(blowingPipe).isEmpty()) {
                status = "Buying glassblowing pipe";
                ctx.shop.buy(blowingPipe, SimpleShop.Amount.ONE);
                ctx.onCondition(() -> !ctx.inventory.populate().filter(blowingPipe).isEmpty(), 100, 10);
            }

            int[] craftRequirements = {bucketOfSand, sodaAsh};
            String[] itemNames = {"buckets of sand", "soda ashes"};
            for (int i = 0; i < craftRequirements.length; i++) {
                if (ctx.inventory.populate().filter(craftRequirements[i]).isEmpty()) {
                    status = "Buying " + itemNames[i];
                    ctx.shop.buy(craftRequirements[i], SimpleShop.Amount.TEN);
                    int finalItems = i;
                    ctx.onCondition(() -> !ctx.inventory.populate().filter(craftRequirements[finalItems]).isEmpty(), 100, 10);
                }
            }

            int sandPopulation = ctx.inventory.populate().filter(bucketOfSand).population();
            int sodaAshPopulation = ctx.inventory.populate().filter(sodaAsh).population();

            if (sandPopulation == 10 && sodaAshPopulation == 10) {
                status = "Closing shop";
                ctx.shop.closeShop();
            }
        }
    }


    private void loopBroken() {
        status = "Loop broken. Starting over";
        int[] craftingRequirements = {bucketOfSand, sodaAsh, blowingPipe};
        for (int itemId : craftingRequirements) {
            if (!ctx.inventory.populate().filter(itemId).isEmpty()) {
                ctx.shop.sell(itemId, SimpleShop.Amount.FIFTY);
                ctx.onCondition(() -> !ctx.inventory.populate().filter(itemId).isEmpty(), 100, 10);
            }
        }
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
        if (m.getMessage() != null) {
            String message = m.getMessage().toLowerCase();
            if (message.contains(ctx.players.getLocal().getName().toLowerCase())) {
                ctx.updateStatus(currentTime() + " Someone asked for you");
                ctx.updateStatus(currentTime() + " Stopping script");
                ctx.sleep(3000);
                ctx.stopScript();
            } else if (message.contains("don't have enough")) {
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
    }

    @Override
    public int loopDuration() {
        return 200;
    }

    @Override
    public void paint(Graphics g) {
        Color PhilippineRed = new Color(196, 18, 48);
        Color RaisinBlack = new Color(35, 31, 32, 127);
        g.setColor(RaisinBlack);
        g.fillRect(5, 120, 200, 110);
        g.setColor(PhilippineRed);
        g.drawRect(5, 120, 200, 110);
        g.setColor(PhilippineRed);
        g.drawString("eGlassblowingBot by Esmaabi", 15, 135);
        g.setColor(Color.WHITE);
        long runTime = System.currentTimeMillis() - this.startTime;
        long currentSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.CRAFTING);
        long currentSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.CRAFTING);
        long SkillLevelsGained = currentSkillLevel - this.startingSkillLevel;
        long SkillExpGained = currentSkillExp - this.startingSkillExp;
        long SkillExpPerHour = (int)((SkillExpGained * 3600000D) / runTime);
        long ActionsPerHour = (int) (count / ((System.currentTimeMillis() - this.startTime) / 3600000.0D));
        g.drawString("Runtime: " + formatTime(runTime), 15, 150);
        g.drawString("Starting Level: " + this.startingSkillLevel + " (+" + SkillLevelsGained + ")", 15, 165);
        g.drawString("Current Level: " + currentSkillLevel, 15, 180);
        g.drawString("Exp gained: " + SkillExpGained + " (" + (SkillExpPerHour / 1000L) + "k" + " xp/h)", 15, 195);
        g.drawString("Actions made: " + count + " (" + ActionsPerHour + " per/h)", 15, 210);
        g.drawString("Status: " + status, 15, 225);
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