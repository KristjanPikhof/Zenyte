package eSacredEelBotZenyte;


import eRandomEventSolver.eRandomEventForester;
import eRunecraftingBotZenyte.listeners.SkillListener;
import eSacredEelBotZenyte.listeners.SkillObserver;
import net.runelite.api.ChatMessageType;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;


@ScriptManifest(author = "Esmaabi", category = Category.FISHING, description = " "
        + "Please read <b>eSacredEelBotZenyte</b> description first!</b><br>"
        + "<br><b>Description</b>:<br>"
        + "It is required to have <b>Fishing rod</b>, <b>Fishing bait</b> & a <b>Knife</b> in inventory.<br>"
        + "Start the scrip near fishing spots at <b>Zul-Andra</b><br>"
        + "Bot will equip Angler equipment if acquired.<br><br> "
        + "For more information check out Esmaabi on SimpleBot!", discord = "Esmaabi#5752",
        name = "eSacredEelBotZenyte", servers = { "Zenyte" }, version = "0.2")

public class eMain extends TaskScript implements SkillListener, LoopingScript {


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
    private static String playerGameName;


    //NPC
    private final int sacredEelSpot = 6488;

    //Inventory
    private final int sacredEel = 13339;
    private final int zulrahScales = 12934;
    private final int knife = 946;
    private final int fishingRod = 307;
    private final int fishingBait = 313;
    private final int anglerHat = 13258;
    private final int anglerTop = 13259;
    private final int anglerWaders = 13260;
    private final int anglerBoots = 13261;

    //Vars
    private int fishCount;
    private int zulrahScalesBeginnging;
    private int zulrahScalesCount;


    //Stats
    private long startingSkillLevelFishing, startingSkillLevelCooking;
    private long startingSkillExpFishing, startingSkillExpCooking;

    @Override
    public int loopDuration() {
        return 150;
    }

    enum State{
        FISHING,
        COOKING,
        WAITING,
    }

    //Tasks
    java.util.List<Task> tasks = new ArrayList<>();

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
        System.out.println("Started eSacredEelBot Zenyte!");
        started = false;
        status = "Setting up config";
        startTime = System.currentTimeMillis(); //paint
        ctx.viewport.angle(180);
        ctx.viewport.pitch(true);
        fishCount = 0;
        zulrahScalesCount = 0;
        zulrahScalesBeginnging = getScalesCount();


        this.ctx.updateStatus("----------------------------------");
        this.ctx.updateStatus("        eSacredEelBotZenyte       ");
        this.ctx.updateStatus("----------------------------------");

        //FISHING
        startingSkillLevelFishing = this.ctx.skills.realLevel(SimpleSkills.Skills.FISHING);
        startingSkillExpFishing = this.ctx.skills.experience(SimpleSkills.Skills.FISHING);

        //COOKING
        startingSkillLevelCooking = this.ctx.skills.realLevel(SimpleSkills.Skills.COOKING);
        startingSkillExpCooking = this.ctx.skills.experience(SimpleSkills.Skills.COOKING);


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
        skillObserver.addListener((eSacredEelBotZenyte.listeners.SkillListener) this);
        skillObserver.start();

    }

    @Override
    public void onProcess() {
        super.onProcess();
        if (!started) {
            playerState = State.WAITING;

        } else {

            if (ctx.inventory.populate().filter(knife).isEmpty()) {
                ctx.updateStatus("Knife missing");
                status = "Knife missing";
                ctx.updateStatus("Stopping script");
                botTerminated = true;
                ctx.sleep(5000);
                ctx.stopScript();
            }

            if (ctx.dialogue.dialogueOpen()) {
                if (ctx.widgets.getWidget(229,1).getText().contains("need a fishing rod")) {
                    ctx.updateStatus("The fishing rod missing");
                    status = "The fishing rod missing";
                    ctx.updateStatus("Stopping script");
                    botTerminated = true;
                    ctx.sleep(5000);
                    ctx.stopScript();
                } else if (ctx.widgets.getWidget(229,1).getText().contains("have any bait")) {
                    ctx.updateStatus("The fish bait has run out.");
                    status = "The fish bait has run out.";
                    ctx.updateStatus("Stopping script");
                    botTerminated = true;
                    ctx.sleep(5000);
                    ctx.stopScript();
                } else {
                    ctx.dialogue.clickContinue();
                }

            }

            if (ctx.dialogue.dialogueOpen()) {
                ctx.dialogue.clickContinue();
            }

            if (!ctx.inventory.populate().filter(anglerHat, anglerTop, anglerWaders, anglerBoots).isEmpty()) {
                ctx.inventory.populate().filter(anglerHat, anglerTop, anglerWaders, anglerBoots).forEach((item) -> item.click("Wear"));
            }

            if (playerState == State.FISHING) {
                if (ctx.inventory.populate().population() < 28) {
                    if (ctx.players.getLocal().getAnimation() == -1 && (System.currentTimeMillis() > (lastAnimation + randomSleeping(3000, 4000)))) {
                        status = "Activating fishing task";
                        fishingTask();
                    } else if (ctx.players.getLocal().getAnimation() != -1) {
                        lastAnimation = System.currentTimeMillis();
                    }
                } else {
                    playerState = State.COOKING;
                }
            }

            if (playerState == State.COOKING) {
                if (ctx.inventory.populate().filter(sacredEel).population() > 0) {
                    if (ctx.players.getLocal().getAnimation() == -1 && (System.currentTimeMillis() > (lastAnimation + randomSleeping(3000, 4000)))) {
                        status = "Activating cooking task";
                        dissectTask();
                    } else if (ctx.players.getLocal().getAnimation() != -1) {
                        lastAnimation = System.currentTimeMillis();
                    }
                } else {
                    playerState = State.FISHING;
                }
            }
        }
    }

    //tasks
    private void dissectTask() {
        SimpleItem knifeInv = ctx.inventory.populate().filter(knife).next();
        SimpleItem eelInv = ctx.inventory.populate().filter(sacredEel).next();
        status = "Dissecting sacred eels";
        ctx.inventory.itemOnItem(knifeInv, eelInv);
        ctx.sleepCondition(() -> ctx.players.getLocal().getAnimation() != -1, 3000);
    }

    private void fishingTask() {
        SimpleNpc fishingSpot = ctx.npcs.populate().filter(sacredEelSpot).nearest().next();

        if (fishingSpot != null && fishingSpot.validateInteractable()) {
            status = "Fishing";
            fishingSpot.click("Bait");
        }
    }

    public int getScalesCount() {
        return ctx.inventory.populate().filter(zulrahScales).population(true);
    }

    public static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static int randomSleeping(int minimum, int maximum) {
        return (int)(Math.random() * (maximum - minimum)) + minimum;
    }

    public String getPlayerName() {
        if (playerGameName == null) {
            playerGameName = ctx.players.getLocal().getName();
        }
        return playerGameName;
    }


    @Override
    public void onTerminate() {
        this.ctx.updateStatus("Sacred eel caught: " + fishCount);
        this.ctx.updateStatus("Zulrah scales dissected: " + zulrahScalesCount);

        //listener
        runningSkillListener = false;
        started = false;
        botTerminated = true;

        ///vars
        fishCount = 0;
        zulrahScalesCount = 0;

        this.ctx.updateStatus("----------------------");
        this.ctx.updateStatus("Thank You & Good Luck!");
        this.ctx.updateStatus("----------------------");
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
                ctx.updateStatus(currentTime() + " Someone asked from you");
                ctx.updateStatus(currentTime() + " Stopping script");
                ctx.stopScript();
            }

            if (!senderName.contains(playerGameName) && getEvent.getMessage().toLowerCase().contains(playerGameName.toLowerCase())) {
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
        g.drawString("eSacredEelBot by Esmaabi", 15, 135);
        g.setColor(Color.WHITE);
        long runTime = System.currentTimeMillis() - this.startTime;
        long currentSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.FISHING);
        long currentSkillLevel2 = this.ctx.skills.realLevel(SimpleSkills.Skills.COOKING);
        long currentSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.FISHING);
        long currentSkillExp2 = this.ctx.skills.experience(SimpleSkills.Skills.COOKING);
        long SkillLevelsGained = currentSkillLevel - this.startingSkillLevelFishing;
        long SkillLevelsGained2 = currentSkillLevel2 - this.startingSkillLevelCooking;
        long SkillExpGained = currentSkillExp - this.startingSkillExpFishing;
        long SkillExpGained2 = currentSkillExp2 - this.startingSkillExpCooking;
        long ExPGainedinSum = SkillExpGained + SkillExpGained2;
        long SkillexpPhour = (int) ((ExPGainedinSum * 3600000D) / runTime);
        long ThingsPerHour = (int) (fishCount / ((System.currentTimeMillis() - this.startTime) / 3600000.0D));
        long ThingsPerHour2 = (int) (zulrahScalesCount / ((System.currentTimeMillis() - this.startTime) / 3600000.0D));
        g.drawString("Runtime: " + formatTime(runTime), 15, 150);
        g.drawString("Starting: Fishing " + this.startingSkillLevelFishing + " (+" + SkillLevelsGained + ")," + " Cooking " + this.startingSkillLevelCooking + " (+" + SkillLevelsGained2 + ")", 15, 165);
        g.drawString("Current: Fishing " + currentSkillLevel + ", Cooking " + currentSkillLevel2, 15, 180);
        g.drawString("Exp gained: " + ExPGainedinSum + " (" + (SkillexpPhour / 1000L) + "k" + " xp/h)", 15, 195);
        g.drawString("Fish caught: " + fishCount + " (" + ThingsPerHour + " / h), Scales: " + zulrahScalesCount + " (" + ThingsPerHour2 + " / h)", 15, 210);
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

        zulrahScalesCount = (getScalesCount() - zulrahScalesBeginnging);

        if (playerState == State.FISHING) {
            fishCount++;
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
