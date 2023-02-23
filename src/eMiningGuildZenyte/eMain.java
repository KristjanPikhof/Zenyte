package eMiningGuildZenyte;


import eMiningGuildZenyte.listeners.SkillListener;
import eMiningGuildZenyte.listeners.SkillObserver;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.script.Script;
import simple.robot.utils.WorldArea;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BooleanSupplier;


@ScriptManifest(author = "Esmaabi", category = Category.MINING, description = " " +
        "Please read <b>eMiningGuildZenyte</b> description first!</b><br>" +
        "<br><b>Description</b>:<br>" +
        "<ul>" +
        "<li>You must start with pickaxe </b>equipped</b> or in <b>inventory</b>;</li>" +
        "<li>You must start at mining guild bank;</li>" +
        "<li>Do not zoom out <b>to maximum</b>.</li>" +
        "</ul>",
        discord = "Esmaabi#5752",
        name = "eMiningGuildZenyte", servers = { "Zenyte" }, version = "0.1")

public class eMain extends Script implements SkillListener {


    //vars
    public static boolean started;
    private long startTime = 0L;
    static String status = null;
    public static State playerState;
    private long lastAnimation = -1;
    private int levelsGained;
    private int experienceGained;
    boolean runningSkillListener = true;
    static boolean botTerminated = false;

    //WordArea
    private final WorldPoint miningPosition = new WorldPoint(3021, 9721, 0);
    private final WorldArea miningArea = new WorldArea(new WorldPoint(3018,9724, 0), new WorldPoint(3023,9718, 0));

    //GameObjects
    private final int[] iron = {7488, 7455,};
    private final int[] inventoryPickaxe = {20014, 13243, 12797, 12297, 11920, 1275, 1273, 1271, 1269, 1267, 1265};


    //Vars
    private int oreCount;



    //Stats
    private long startingSkillLevelMining;
    private long startingSkillExpMining;

    enum State {
        MINING,
        WAITING,
    }

    @Override
    public void onExecute() {
        System.out.println("Started eMiningGuildZenyte Zenyte!");
        started = false;
        status = "Setting up config";
        startTime = System.currentTimeMillis(); //paint
        ctx.viewport.angle(0);
        ctx.viewport.pitch(true);
        oreCount = 0;


        this.ctx.updateStatus("----------------------------------");
        this.ctx.updateStatus("        eMiningGuildZenyte       ");
        this.ctx.updateStatus("----------------------------------");

        //MINING
        startingSkillLevelMining = this.ctx.skills.realLevel(SimpleSkills.Skills.MINING);
        startingSkillExpMining = this.ctx.skills.experience(SimpleSkills.Skills.MINING);


        //GUI
        eGui gui = new eGui();
        gui.setVisible(true);

        //skill listener
        runningSkillListener = true;
        SkillObserver skillObserver = new SkillObserver(ctx, new BooleanSupplier() {

            public boolean getAsBoolean() {
                return runningSkillListener;
            }
        });
        skillObserver.addListener(this);
        skillObserver.start();

    }

    @Override
    public void onProcess() {
        if (!started) {
            playerState = State.WAITING;

        } else {

            if (ctx.dialogue.dialogueOpen()) {
                ctx.dialogue.clickContinue();
            }

            if (ctx.inventory.inventoryFull()) {
                bankTask();
            } else {
                miningTask();
            }
        }
    }

    public void bankTask() {
        SimpleObject bankChest = ctx.objects.populate().filter("Bank chest").filterHasAction("Use").nearest().next();
        if (bankChest != null && bankChest.validateInteractable()) {
            if (!ctx.bank.bankOpen()) {
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
        status = "Closing bank";
        ctx.bank.closeBank();
        ctx.onCondition(() -> !ctx.bank.bankOpen(), 250, 10);
    }

    public void miningTask() {
        SimpleObject miningRock = ctx.objects.populate().filter(iron).filterHasAction("Mine").nearest().next();
        if (!ctx.pathing.inArea(miningArea)) {
            // If not, move to mining position
            ctx.pathing.step(miningPosition);
            return;
        }

        // Check if there is a valid mining rock to interact with
        if (miningRock == null || !miningRock.validateInteractable()) {
            return;
        }

        // Check if the inventory is full
        if (ctx.inventory.inventoryFull()) {
            return;
        }

        // Check if there is a dialogue chat open and close it
        if (ctx.dialogue.dialogueOpen()) {
            ctx.dialogue.clickContinue();
        }

        // Mining action
        int sleepTime = randomSleeping(25, 200);
        status = "Sleeping for " + sleepTime + "ms";
        ctx.sleep(sleepTime);
        status = "Mining rocks";
        miningRock.click("Mine");
        lastAnimation = System.currentTimeMillis();
        ctx.sleepCondition(() -> !ctx.players.getLocal().isAnimating() &&
                (System.currentTimeMillis() > (lastAnimation + randomSleeping(50, 200))), 5000);
    }

    public int getInventoryPopulation() {
        return ctx.inventory.populate().population();
    }

    public static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static int randomSleeping(int minimum, int maximum) {
        return (int)(Math.random() * (maximum - minimum)) + minimum;
    }

    @Override
    public void onTerminate() {
        this.ctx.updateStatus("Ores mined: " + oreCount);

        //listener
        runningSkillListener = false;
        started = false;
        botTerminated = true;

        ///vars
        oreCount = 0;

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
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        Color PhilippineRed = new Color(196, 18, 48);
        Color RaisinBlack = new Color(35, 31, 32, 127);
        g.setColor(RaisinBlack);
        g.fillRect(5, 120, 250, 110);
        g.setColor(PhilippineRed);
        g.drawRect(5, 120, 250, 110);
        g.setColor(PhilippineRed);
        g.drawString("eMiningGuild by Esmaabi", 15, 135);
        g.setColor(Color.WHITE);
        long runTime = System.currentTimeMillis() - this.startTime;
        long currentSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.MINING);
        long currentSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.MINING);
        long levelsGained = currentSkillLevel - this.startingSkillLevelMining;
        long expGained = currentSkillExp - this.startingSkillExpMining;
        long xpPerHour = (int) ((expGained * 3600000D) / runTime);
        long itemsPerHour = (int) (oreCount / ((System.currentTimeMillis() - this.startTime) / 3600000.0D));
        g.drawString("Runtime: " + formatTime(runTime), 15, 150);
        g.drawString("Starting: Mining " + this.startingSkillLevelMining + " (+" + levelsGained + ")", 15, 165);
        g.drawString("Current: Mining " + currentSkillLevel, 15, 180);
        g.drawString("Exp gained: " + expGained + " (" + (xpPerHour / 1000L) + "k" + " xp/h)", 15, 195);
        g.drawString("Ores mined: " + oreCount + " (" + itemsPerHour + " / h)", 15, 210);
        g.drawString("Status: " + status, 15, 225);

    }

    @Override
    public void skillLevelAdded(SimpleSkills.Skills skill, int current, int previous, int gained) {
        //System.out.printf("We gained %d levels in %s, we went from +%d to %d", gained, skill.toString(), previous, current);
        //levelsGained += gained;

    }

    @Override
    public void skillExperienceAdded(SimpleSkills.Skills skill, int current, int previous, int gained) {
        //System.out.printf("We gained %d experience in %s, we went from +%d to %d", gained, skill.toString(), previous, current);
        //experienceGained += gained;

        if (playerState == State.MINING) {
            oreCount++;
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
