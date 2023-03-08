package eWintertodtBot;

import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Game;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

@ScriptManifest(author = "Esmaabi", category = Category.FIREMAKING,
        description = "<br>Most effective Wintertodt Firemaking traing bot on Zenyte! <br><br><b>Features & recommendations:</b><br><br>" +
                "<ul>" +
                "<li>You must start with pickaxe </b>equipped</b> or in <b>inventory</b>;</li>" +
                "<li>You must start at mining guild bank near amethyst crystals;</li>" +
                "<li>Do not zoom out <b>to maximum</b>;</li>" +
                "<li>Dragon pickaxe special attack supported;</li>" +
                "<li>Random sleeping included!</li></ul>",
        discord = "Esmaabi#5752",
        name = "eWintertodtBotZenyte", servers = { "Zenyte" }, version = "0.1")

public class eMain extends TaskScript implements LoopingScript {

    //coordinates
    private final WorldArea bankArea = new WorldArea (new WorldPoint(1609,3965, 0), new WorldPoint(1650,3931, 0));
    private final WorldArea wintertodtArea = new WorldArea (new WorldPoint(1609,3966, 0), new WorldPoint(1652,4029, 0));

    //vars
    private long startTime = 0L;
    private long startingSkillLevel;
    private long startingSkillExp;
    private int count;
    static String status = null;
    private int currentExp;
    private long lastAnimation = -1;

    boolean specialDone = false;
    private final int[] foodItems = {20014, 13243, 12797, 12297, 11920, 1275, 1273, 1271, 1269, 1267, 1265};
    private final int[] inventoryItems = {6739, 2347};
    private State playerState;

    public static int randomSleeping(int minimum, int maximum) {
        return (int)(Math.random() * (maximum - minimum)) + minimum;
    }

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

    enum State {
        RUNNING,
        WAITING,
    }

    @Override
    public void onExecute() {

        tasks.addAll(Collections.emptyList());

        System.out.println("Started eAmethystMiner!");

        this.ctx.updateStatus("--------------- " + currentTime() + " ---------------");
        this.ctx.updateStatus("-------------------------------");
        this.ctx.updateStatus("       eAmethystMiner      ");
        this.ctx.updateStatus("-------------------------------");

        status = "Setting up bot";
        this.startTime = System.currentTimeMillis();
        this.startingSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.FIREMAKING);
        this.startingSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING);
        currentExp = this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING);// for actions counter by xp drop
        count = 0;
        ctx.viewport.angle(270);
        ctx.viewport.pitch(true);
        specialDone = false;
        playerState = State.RUNNING;

    }

    @Override
    public void onProcess() {
        super.onProcess();

/*        if (ctx.players.populate().filter("Kristjan", "Sleeper").isEmpty() && ctx.players.population() > 1) {
            status = "Anti-ban activated";
        } else {*/

        if (ctx.pathing.energyLevel() > 30 && !ctx.pathing.running()) {
            ctx.pathing.running(true);
        }

        if (currentExp != this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING)) {
            count++;
            currentExp = this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING);
        }

        if (ctx.players.getLocal().getHealth() < 55) {
            SimpleItem foodItem = ctx.inventory.populate().filter(foodItems).next();
            if (foodItem != null && foodItem.validateInteractable()) {
                foodItem.click(0);
                ctx.onCondition(() -> ctx.players.getLocal().getHealth() > 55, 250, 10);
            }
        }

        if (ctx.combat.getSpecialAttackPercentage() == 100
                && ctx.equipment.populate().filter("Dragon axe").population() == 1
                && ctx.players.getLocal().getAnimation() == 6758) {
            int sleep = randomSleeping(2000, 6000);
            status = "Using special attack in " + sleep + "ms";
            ctx.sleep(sleep);
            ctx.combat.toggleSpecialAttack(true);
            ctx.game.tab(Game.Tab.INVENTORY);
        }

        if (wintertodtArea.containsPoint(ctx.players.getLocal().getLocation())) {

            if (rejuvPotionAmount() == 0) {

            }

            if (ctx.inventory.inventoryFull()) {
                openingBank();
            } else if (!ctx.inventory.inventoryFull() && !ctx.bank.bankOpen()) {
                if (!ctx.players.getLocal().isAnimating() && (System.currentTimeMillis() > (lastAnimation + randomSleeping(1200, 4600)))) {
                    miningTask();
                } else if (ctx.players.getLocal().isAnimating()) {
                    lastAnimation = System.currentTimeMillis();
                }
            }

        }

        if (bankArea.containsPoint(ctx.players.getLocal().getLocation())) {

            if (ctx.inventory.populate().filter(13441).population() < 15) {
                openingBank();
            } else {
                runningToDoors();
            }

        }

    }

    public static SimpleItem getItem(String... itemName) {
        return ClientContext.instance().inventory.populate()
                .filter(p -> Stream.of(itemName).anyMatch(arr -> p.getName().toLowerCase().contains(arr.toLowerCase())))
                .next();
    }

    public void openingBank() {
        SimpleObject bankChest = ctx.objects.populate().filter("Bank chest").filterHasAction("Bank").nearest().next();
        if (bankChest == null && !ctx.bank.bankOpen()) {
            status = "Running to bank";
            ctx.pathing.step(1631, 3951);
            return;
        }

        if (bankChest != null && !ctx.bank.bankOpen()) {
            if (bankChest.validateInteractable()) {
                status = "Opening bank";
                bankChest.click("Bank", "Bank chest");
                ctx.onCondition(() -> ctx.bank.bankOpen(), randomSleeping(2000, 5000));
            }
        }

        if (ctx.bank.bankOpen() && ctx.inventory.populate().filter(13441).isEmpty()) {
            status = "Banking";
            ctx.bank.depositAllExcept(inventoryItems);
            ctx.sleep(300);
            ctx.bank.withdraw(13441, 15);
            ctx.sleep(300);
            return;
        }

        if (ctx.bank.bankOpen() && !ctx.inventory.populate().filter(13441).isEmpty()) {
            status = "Closing bank";
            ctx.bank.closeBank();
            ctx.onCondition(() -> !ctx.bank.bankOpen(), 5000);
        }
    }

    private void runningToDoors() {
        SimpleObject minigameDoors = ctx.objects.populate().filter("Doors of Dinh").nearest().next();
        if (minigameDoors == null) {
            status = "Running to doors";
            ctx.pathing.step(1631, 3951);
        }

        if (minigameDoors != null && minigameDoors.validateInteractable()) {
            status = "Enterning doors";
            minigameDoors.click("Enter");
            ctx.sleepCondition(() -> wintertodtArea.containsPoint(ctx.players.getLocal().getLocation()), 5000);
        }
    }

    private int rejuvPotionAmount() {
        String[] potionName = {"rejuvenation"};
        SimpleItem rejuvPotion = getItem(potionName);
        if (rejuvPotion != null) {
            return rejuvPotion.getQuantity();
        }
        return 0;
    }

    private void getRejuvPotion() {
        SimpleObject potionCrate = ctx.objects.populate().filter(29320).nearest().next();
        SimpleObject sproutingRoots = ctx.objects.populate().filter(29315).nearest().next();
    }

    public void miningTask() {
        if (bankArea.containsPoint(ctx.players.getLocal().getLocation()) && !ctx.pathing.inMotion()) {
            status = "Going to mining area";
            takingStepsRMining();
        }
        SimpleObject amethystCrystals = ctx.objects.populate().filter("Crystals").filterHasAction("Mine").nearest().next();
        if (amethystCrystals != null && amethystCrystals.validateInteractable()) {
            if (getInventoryPopulation() > 1) {
                int sleepTime = randomSleeping(0, 6400);
                status = "Sleeping for " + sleepTime + "ms";
                ctx.viewport.turnTo(amethystCrystals);
                ctx.sleep(sleepTime);
            }
            status = "Mining amethyst crystals";
            amethystCrystals.click("Mine", "Crystals");
            specialDone = false;
            ctx.onCondition(() -> ctx.players.getLocal().isAnimating(), 5000);
        }
    }

/*    public void takingStepsRMining() {
        int max = 5;
        int min = 1;
        int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
        switch(randomNum) {
            case 1:
                status = "Taking path: 1";
                ctx.pathing.step(3024, 9708);
                ctx.sleep(randomSleeping(2200, 3200));
                break;
            case 2:
                status = "Taking path: 2";
                ctx.pathing.step(3018, 9704);
                ctx.sleep(randomSleeping(2200, 3200));
                break;
            case 3:
                status = "Taking path:  3";
                ctx.pathing.step(3022, 9707);
                ctx.sleep(randomSleeping(2200, 3200));
                break;
            case 4:
                status = "Taking path: 4";
                ctx.pathing.step(3028, 9704);
                ctx.sleep(randomSleeping(2200, 3200));
                break;
            case 5:
                status = "Taking path: 5";
                ctx.pathing.step(3027, 9705);
                ctx.sleep(randomSleeping(2200, 3200));
                break;
            default:
                status = "Taking path: default";
                ctx.pathing.step(3019, 9706);
                ctx.sleep(randomSleeping(2200, 3200));
                break;
        }
    }*/

    public void takingStepsRMining() {
        int max = 6;
        int min = 1;
        int[][] coordinates = {{3024, 9708}, {3018, 9704}, {3022, 9707}, {3028, 9704}, {3019, 9706}, {3027, 9705}};
        int randomNum = ThreadLocalRandom.current().nextInt(min, max + min);
        ctx.pathing.step(coordinates[randomNum - 1][0], coordinates[randomNum - 1][1]);
    }

    public int getInventoryPopulation() {
        return ctx.inventory.populate().population();
    }



    @Override
    public void onTerminate() {
        this.startingSkillLevel = 0L;
        this.startingSkillExp = 0L;
        count = 0;
        playerState = State.WAITING;


        this.ctx.updateStatus("-------------- " + currentTime() + " --------------");
        this.ctx.updateStatus("----------------------");
        this.ctx.updateStatus("Thank You & Good Luck!");
        this.ctx.updateStatus("----------------------");
    }

    @Override
    public void onChatMessage(ChatMessage m) {
        if (m.getMessage() != null) {
            String message = m.getMessage().toLowerCase();
            if (message.contains(ctx.players.getLocal().getName().toLowerCase())) {
                ctx.updateStatus(currentTime() + " Someone asked for you");
                ctx.updateStatus(currentTime() + " Stopping script");
                ctx.stopScript();
            } else if (message.contains("get some amethyst")) {
                count++;
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
        g.drawString("eWintertodtBot by Esmaabi", 15, 135);
        g.setColor(Color.WHITE);
        long runTime = System.currentTimeMillis() - this.startTime;
        long currentSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.FIREMAKING);
        long currentSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING);
        long SkillLevelsGained = currentSkillLevel - this.startingSkillLevel;
        long SkillExpGained = currentSkillExp - this.startingSkillExp;
        long SkillExpPerHour = (int)((SkillExpGained * 3600000D) / runTime);
        long ActionsPerHour = (int) (count / ((System.currentTimeMillis() - this.startTime) / 3600000.0D));
        g.drawString("Runtime: " + formatTime(runTime), 15, 150);
        g.drawString("Starting Level: " + this.startingSkillLevel + " (+" + SkillLevelsGained + ")", 15, 165);
        g.drawString("Current Level: " + currentSkillLevel, 15, 180);
        g.drawString("Exp gained: " + SkillExpGained + " (" + (SkillExpPerHour / 1000L) + "k" + " xp/h)", 15, 195);
        g.drawString("Logs used: " + count + " (" + ActionsPerHour + " per/h)", 15, 210);
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