package eAmethystMinerZenyte;

import eRandomEventSolver.eRandomEventForester;
import net.runelite.api.ChatMessageType;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Game;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.utils.WorldArea;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@ScriptManifest(author = "Esmaabi", category = Category.MINING,
        description = "<br>Most effective amethyst crystal mining bot on Zenyte! <br><br><b>Features & recommendations:</b><br><br>" +
                "<ul>" +
                "<li>You must start with pickaxe </b>equipped</b> or in <b>inventory</b>;</li>" +
                "<li>You must start at mining guild bank near amethyst crystals;</li>" +
                "<li>Do not zoom out <b>to maximum</b>;</li>" +
                "<li>Dragon pickaxe special attack supported;</li>" +
                "<li>Random sleeping included!</li></ul>",
        discord = "Esmaabi#5752",
        name = "eAmethystMinerZenyte", servers = { "Zenyte" }, version = "0.5")

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

    boolean specialDone = false;
    private final int[] inventoryPickaxe = {30742, 20014, 13243, 12797, 12297, 11920, 1275, 1273, 1271, 1269, 1267, 1265};
    private State playerState;
    private static String playerGameName;
    private int[] lastCoordinates;

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
        MINING,
        WAITING,
    }

    @Override
    public void onExecute() {

        tasks.addAll(Arrays.asList(new eRandomEventForester(ctx)));

        System.out.println("Started eAmethystMiner!");

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
        playerState = State.MINING;
        lastCoordinates = null;

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

            if (miningArea.containsPoint(ctx.players.getLocal().getLocation())) {

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
                lastCoordinates = null;
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

    public void takingStepsRMining() {
        int max = 6;
        int min = 1;
        int[][] coordinates = {{3024, 9708}, {3018, 9704}, {3022, 9707}, {3028, 9704}, {3019, 9706}, {3027, 9705}};

        if (lastCoordinates == null) {
            // first time running the function, generate a new random location
            int randomNum = ThreadLocalRandom.current().nextInt(min, max + min);
            lastCoordinates = coordinates[randomNum - 1];
        }
        ctx.pathing.step(lastCoordinates[0], lastCoordinates[1]);
    }

    public int getInventoryPopulation() {
        return ctx.inventory.populate().population();
    }

    public String getPlayerName() {
        if (playerGameName == null) {
            playerGameName = ctx.players.getLocal().getName();
        }
        return playerGameName;
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

        ChatMessageType getType = m.getType();
        net.runelite.api.events.ChatMessage getEvent = m.getChatEvent();
        playerGameName = getPlayerName();
        String message = m.getMessage().toLowerCase();

        if (m.getMessage() == null) {
            return;
        }

        if (m.getMessage() != null) {
            if (message.contains("get some amethyst")) {
                count++;
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

    @Override
    public void paint(Graphics g) {
        Color PhilippineRed = new Color(196, 18, 48);
        Color RaisinBlack = new Color(35, 31, 32, 127);
        g.setColor(RaisinBlack);
        g.fillRect(5, 120, 200, 110);
        g.setColor(PhilippineRed);
        g.drawRect(5, 120, 200, 110);
        g.setColor(PhilippineRed);
        g.drawString("eAmethystMiner by Esmaabi", 15, 135);
        g.setColor(Color.WHITE);
        long runTime = System.currentTimeMillis() - this.startTime;
        long currentSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.MINING);
        long currentSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.MINING);
        long SkillLevelsGained = currentSkillLevel - this.startingSkillLevel;
        long SkillExpGained = currentSkillExp - this.startingSkillExp;
        long SkillExpPerHour = (int)((SkillExpGained * 3600000D) / runTime);
        long ActionsPerHour = (int) (count / ((System.currentTimeMillis() - this.startTime) / 3600000.0D));
        g.drawString("Runtime: " + formatTime(runTime), 15, 150);
        g.drawString("Starting Level: " + this.startingSkillLevel + " (+" + SkillLevelsGained + ")", 15, 165);
        g.drawString("Current Level: " + currentSkillLevel, 15, 180);
        g.drawString("Exp gained: " + SkillExpGained + " (" + (SkillExpPerHour / 1000L) + "k" + " xp/h)", 15, 195);
        g.drawString("Crystals mined: " + count + " (" + ActionsPerHour + " per/h)", 15, 210);
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