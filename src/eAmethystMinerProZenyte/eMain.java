package eAmethystMinerProZenyte;

import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.queries.SimplePlayerQuery;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Game;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimplePlayer;
import simple.robot.utils.WorldArea;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static eRandomEventSolver.eRandomEventForester.forestArea;

@ScriptManifest(author = "Esmaabi", category = Category.MINING,
        description = "<br>Most effective amethyst crystal mining bot on Zenyte! <br><br><b>Features & recommendations:</b><br><br>" +
                "<ul>" +
                "<li>You must start with pickaxe </b>equipped</b> or in <b>inventory</b>;</li>" +
                "<li>You must start at mining guild bank near amethyst crystals;</li>" +
                "<li>Do not zoom out <b>to maximum</b>;</li>" +
                "<li>Dragon pickaxe special attack supported;</li>" +
                "<li>Random sleeping included!</li></ul>",
        discord = "Esmaabi#5752",
        name = "eAmethystMinerProZenyte", servers = { "Zenyte" }, version = "2.1")

public class eMain extends TaskScript implements LoopingScript {

    //coordinates
    private final WorldArea miningArea = new WorldArea (new WorldPoint(3043, 9695, 0), new WorldPoint(2993, 9729, 0));
    private final WorldArea amethArea = new WorldArea (new WorldPoint(3016, 9708, 0), new WorldPoint(3030, 9698, 0));
    private final WorldArea bankArea = new WorldArea (new WorldPoint(3011, 9720, 0), new WorldPoint(3015, 9716, 0));

    //vars
    private long startTime = 0L;
    private long startingSkillLevel;
    private long startingSkillExp;
    private int count;
    static String status = null;
    private int currentExp;
    private long lastAnimation = -1;
    private int comradesInt;

    boolean specialDone = false;
    private final int[] inventoryPickaxe = {20014, 13243, 12797, 12297, 11920, 1275, 1273, 1271, 1269, 1267, 1265};
    private final String[] names = {"Test"}; // names to ba added to work
    private static boolean hidePaint = false;

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

    @Override
    public void onExecute() {

        tasks.addAll(Collections.emptyList());

        System.out.println("Started eAmethystMiner Pro!");

        this.ctx.updateStatus("--------------- " + currentTime() + " ---------------");
        this.ctx.updateStatus("-------------------------------");
        this.ctx.updateStatus("       eAmethystMiner      ");
        this.ctx.updateStatus("-------------------------------");

        status = "Setting up bot";
        this.startTime = System.currentTimeMillis();
        this.startingSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.MINING);
        this.startingSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.MINING);
        currentExp = this.ctx.skills.experience(SimpleSkills.Skills.MINING);// for actions counter by xp drop
        count = 0;
        ctx.viewport.angle(270);
        ctx.viewport.pitch(true);
        specialDone = false;
        comradesInt = 0;

    }

    @Override
    public void onProcess() {
        super.onProcess();

        if (ctx.pathing.energyLevel() > 30 && !ctx.pathing.running()) {
            ctx.pathing.running(true);
        }

        if (currentExp != this.ctx.skills.experience(SimpleSkills.Skills.MINING)) {
            count++;
            currentExp = this.ctx.skills.experience(SimpleSkills.Skills.MINING);
        }

        if (ctx.players.populate().population() > 1) {
            if (comradesInArea()) {
                if (playerPopulationCheck()) {
                    System.out.println("Using method population == comradesInt");
                    activeMiningScript();
                    return;
                }

                if (!playerPopulationCheck()) {
                    int sleepTime = randomSleeping(6000, 120000);
                    status = "Anti-ban: sleep for " + sleepTime + "ms";
                    System.out.println("Using method population > comradesInt");
                    ctx.sleep(sleepTime);
                    activeMiningScript();
                    return;
                }
            }
        }

        if (ctx.players.populate().population() == 1) {
            System.out.println("Using method population == 1");
            activeMiningScript();
        }
    }

    private void activeMiningScript() {
        if (ctx.combat.getSpecialAttackPercentage() == 100
                && ctx.equipment.populate().filter("Dragon pickaxe").population() == 1
                && ctx.players.getLocal().getAnimation() == 6758) {
            int sleep = randomSleeping(2000, 8000);
            status = "Using special attack in " + sleep + "ms";
            ctx.sleep(sleep);
            if (ctx.players.getLocal().getAnimation() == 6758) {
                ctx.combat.toggleSpecialAttack(true);
                status = "Continuing mining";
                ctx.game.tab(Game.Tab.INVENTORY);
                specialDone = true;
            } else {
                status = "Special attack cancelled";
                if (ctx.inventory.inventoryFull()) {
                    openingBank();
                } else {
                    specialDone = true;
                    miningTask();

                }
            }
        }

        if (miningArea.containsPoint(ctx.players.getLocal().getLocation()) || forestArea.containsPoint(ctx.players.getLocal().getLocation())) {

            if (ctx.inventory.inventoryFull()) {
                openingBank();
            } else if (!ctx.inventory.inventoryFull() && !ctx.bank.bankOpen()) {
                if (!ctx.players.getLocal().isAnimating() && (System.currentTimeMillis() > (lastAnimation + randomSleeping(1200, 4600)))) {
                    miningTask();
                } else if (ctx.players.getLocal().isAnimating()) {
                    lastAnimation = System.currentTimeMillis();
                }
            }

        } else {
            status = "Player not in mining area";
            ctx.updateStatus(currentTime() + " Player not in mining area");
            ctx.updateStatus(currentTime() + " Stopping script");
            ctx.sleep(2400);
            ctx.stopScript();
        }
    }

    public void openingBank() {
        SimpleObject bankChest = ctx.objects.populate().filter("Bank chest").filterHasAction("Use").nearest().next();
        if (amethArea.containsPoint(ctx.players.getLocal().getLocation()) && !ctx.pathing.inMotion()) {
            status = "Running to bank";
            ctx.pathing.step(3021, 9714);
        }
        if (!ctx.bank.bankOpen()) {
            if (bankChest != null && bankChest.validateInteractable()) {
                status = "Opening bank";
                bankChest.click("Use", "Bank chest");
                ctx.onCondition(() -> ctx.bank.bankOpen(), randomSleeping(2000, 5000));
            }
        }
        if (ctx.bank.bankOpen()) {
            status = "Banking";
            if (ctx.inventory.inventoryFull()) {
                status = "Depositing inventory";
                ctx.bank.depositAllExcept(inventoryPickaxe);
                int inventorySpaceBefore = getInventoryPopulation();
                ctx.onCondition(() -> getInventoryPopulation() < inventorySpaceBefore, 250, 10);
            }
        }
        if (ctx.bank.bankOpen() && !ctx.inventory.inventoryFull()) {
            status = "Closing bank";
            ctx.bank.closeBank();
            ctx.onCondition(() -> !ctx.bank.bankOpen(), 5000);
        }
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

    private boolean comradesInArea() {
        SimplePlayerQuery<SimplePlayer> comrades = ctx.players.populate().filter(this.names);
        comradesInt = 0;
        if(comrades.size() > 0) {
            for(SimplePlayer i : comrades) {
                //ctx.log("Found: " + i.getName() );
                comradesInt ++;
            }
            return true;
        }
        return false;
    }

    private boolean playerPopulationCheck() {
        int population = ctx.players.populate().population();
        System.out.println("Population = " + population + ", comradesInt = " + comradesInt);
        return population == comradesInt;
    }

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
        // Check if mouse is hovering over the paint
        Point mousePos = ctx.mouse.getPoint();
        if (mousePos != null) {
            Rectangle paintRect = new Rectangle(5, 120, 200, 110);
            hidePaint = paintRect.contains(mousePos.getLocation());
        }

        // Get runtime and skill information
        long runTime = System.currentTimeMillis() - this.startTime;
        long currentSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.MINING);
        long currentSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.MINING);
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
            g.drawString("eAmethystMinerPro by Esmaabi", 15, 135);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + formatTime(runTime), 15, 150);
            g.drawString("Skill Level: " + this.startingSkillLevel + " (+" + skillLevelsGained + "), started at " + currentSkillLevel, 15, 165);
            g.drawString("Current Exp: " + currentSkillExp, 15, 180);
            g.drawString("Exp gained: " + skillExpGained + " (" + (skillExpPerHour / 1000L) + "k xp/h)", 15, 195);
            g.drawString("Crystals mined: " + count + " (" + actionsPerHour + " per/h)", 15, 210);
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