package eMasterFarmerTZenyte;

import Utility.Trivia.eTriviaInfo;
import eApiAccess.eAutoResponderGui;
import eApiAccess.eAutoResponser;
import eRandomEventSolver.eRandomEventForester;
import net.runelite.api.ChatMessageType;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.*;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.utils.WorldArea;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static eApiAccess.eAutoResponser.*;

@ScriptManifest(author = "Esmaabi", category = Category.THIEVING, description =
                "<br>The most effective Master Farmer thieving bot on Zenyte!<br>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "Start <b>anywhere</b>.<br>"
                + "You must have thieving level 38.<br> "
                + "You must use normal spellbook.<br>"
                + "Healing as low as 6hp!<br>"
                + "Chat GPT answering is integrated.",
        discord = "Esmaabi#5752",
        name = "eMasterFarmerTZenyte", servers = { "Zenyte" }, version = "1")

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    public static eAutoResponderGui guiGpt;
    private static final Logger logger = Logger.getLogger(eMain.class.getName());
    private final SimpleSkills.Skills chosenSkill = SimpleSkills.Skills.THIEVING;

    private final WorldArea EDGE = new WorldArea(new WorldPoint(3110, 3474, 0), new WorldPoint(3074, 3516, 0));
    private final WorldPoint NEAR_BANK_TILE = new WorldPoint(3092, 3247, 0);
    private final WorldPoint NEAR_PORTAL_TILE = new WorldPoint(3096, 3500, 0);
    private static final WorldArea DRAYNOR = new WorldArea (
            new WorldPoint(3071, 3268, 0),
            new WorldPoint(3070, 3247, 0),
            new WorldPoint(3083, 3238, 0),
            new WorldPoint(3107, 3237, 0),
            new WorldPoint(3104, 3273, 0)
    );

    // Variables
    private long startTime = 0L;
    private long startingSkillLevel;
    private long startingSkillExp;
    private int count;
    static String status = null;
    public static boolean hidePaint = false;
    private boolean teleport;


    // Gui GPT
    public void initializeGptGUI() {
        guiGpt = new eAutoResponderGui();
        guiGpt.setVisible(true);
        guiGpt.setLocale(ctx.getClient().getCanvas().getLocale());
    }

    // Tasks
    private final List<Task> tasks = new ArrayList<>();

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

        tasks.addAll(Arrays.asList(new eAutoResponser(ctx)));// Adds tasks to our {task} list for execution

        // Setting up GPT Gui
        eAutoResponser.scriptPurpose = "you're thieving Master Farmer for seeds.";
        eAutoResponser.gptStarted = false;
        initializeGptGUI();
        gptDeactivation();

        // Intro
        System.out.println("Started eMasterFarmerT!");
        this.ctx.log("--------------- " + getCurrentTimeFormatted() + " ---------------");
        this.ctx.log("------------------------------------");
        this.ctx.log("             eMasterFarmerT         ");
        this.ctx.log("------------------------------------");

        // Vars
        updateStatus("Setting up bot");
        this.startTime = System.currentTimeMillis();
        this.startingSkillLevel = this.ctx.skills.realLevel(chosenSkill);
        this.startingSkillExp = this.ctx.skills.experience(chosenSkill);
        count = 0;
        teleport = false;
        ctx.viewport.pitch(true);

    }

    @Override
    public void onProcess() {
        super.onProcess();

        final Pathing pathing = ctx.pathing;
        final boolean inMotion = ctx.pathing.inMotion();
        final int playerHealth = ctx.combat.health();
        boolean inDraynor = pathing.inArea(DRAYNOR);
        boolean inEdge = pathing.inArea(EDGE);

        if (!botStarted) {
            return;
        }

        if (pathing.energyLevel() > 30 && !pathing.running() && inMotion) {
            pathing.running(true);
        }

        // Check for level
        if (ctx.skills.realLevel(chosenSkill) < 38) {
            status = "Thieving level too low";
            ctx.updateStatus("Stopping script");
            ctx.updateStatus("Thieving level less than 38");
            ctx.stopScript();
        }

        if (inDraynor) {

            if (playerHealth > 6) {
                SimpleNpc masterFarmer = ctx.npcs.populate().filter("Master Farmer").nearest().next();
                if (!ctx.inventory.inventoryFull()) {

                    if (ctx.players.getLocal().getGraphic() != -1) {
                        status = "Stunned";
                        return;
                    }

                    if (masterFarmer != null && masterFarmer.validateInteractable()) {
                        status = "Thieving";
                        masterFarmer.click("Pickpocket");
                    }

                    if (masterFarmer == null) {
                        status = "Taking steps to farmer";
                        pathing.step(3081, 3251);
                        ctx.onCondition(() -> false, 250, 10);
                    }

                    if (ctx.bank.bankOpen()) {
                        ctx.bank.closeBank();
                        return;
                    }

                } else {

                    status = "Banking";

                    if (ctx.players.getLocal().getLocation().distanceTo(NEAR_BANK_TILE) < 10) {
                        SimpleObject bank = ctx.objects.populate().filter("Bank booth").nearest().next();

                        if (ctx.bank.bankOpen()) {
                            ctx.bank.depositInventory();
                            ctx.bank.closeBank();
                            return;
                        }

                        if (!ctx.bank.bankOpen()) {
                            if (bank != null && bank.validateInteractable()) {
                                bank.click("Bank", "Bank booth");
                                ctx.onCondition(() -> ctx.bank.bankOpen(), 250, 10);
                            } else {
                                status = "Taking steps to bank";
                                pathing.step(NEAR_BANK_TILE);
                            }
                        }

                    } else {
                        pathing.step(NEAR_BANK_TILE);
                        ctx.onCondition(() -> ctx.players.getLocal().getLocation().distanceTo(NEAR_BANK_TILE) < 10, 250, 10);
                    }
                }
            }

            if (playerHealth <= 6) {
                if (ctx.magic.castSpellOnce("Zenyte Home Teleport")) {
                    status = "Teleporting to home";
                    ctx.game.tab(Game.Tab.INVENTORY);
                    ctx.onCondition(() -> ctx.players.getLocal().isAnimating(), 300, 10);
                }
            }
        }

        if (inEdge) {

            if (playerHealth <= 6) {
                status = "Restoring hitpoints";
                SimpleObject healingBox = ctx.objects.populate().filter("Box of Restoration").nearest().next();
                if (healingBox != null && healingBox.validateInteractable()) {
                    ctx.sleep(randomSleeping(200, 1800));
                    healingBox.click("Restore", "Box of Restoration");
                    ctx.onCondition(() -> false, 250, 10);
                }
            }

            if (playerHealth > 6) {
                if (!ctx.portalTeleports.portalOpen()) {
                    SimpleObject zenytePortal = ctx.objects.populate().filter("Zenyte Portal").nearest().next();
                    if (zenytePortal != null) {
                        if (!zenytePortal.visibleOnScreen()) {
                            status = "Running to portal";
                            pathing.step(NEAR_PORTAL_TILE);
                        } else {
                            status = "Clicking portal";
                            zenytePortal.click("Teleport", "Zenyte Portal");
                            ctx.onCondition(() -> ctx.portalTeleports.portalOpen(), 250, 10);
                        }
                    }
                } else {
                    status = "Choosing destination...";
                    ctx.portalTeleports.sendTeleport("Cities", "Draynor Village");
                    ctx.game.tab(Game.Tab.INVENTORY);
                    ctx.onCondition(() -> inDraynor, 250, 10);
                }
            }
        }

        if (!inDraynor && !inEdge && !eRandomEventForester.inForesterRandom(ctx) || teleport) {
            status = "I'm lost again...";
            if (ctx.magic.castSpellOnce("Zenyte Home Teleport")) {
                status = "Teleporting to home";
                ctx.game.tab(Game.Tab.INVENTORY);
                ctx.onCondition(() -> ctx.players.getLocal().isAnimating(), 300, 10);
                teleport = false;
            }
        }

    }

    //Utility
    public static String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void updateStatus(String newStatus) {
        status = newStatus;
        ctx.updateStatus(status);
        System.out.println(status);
    }

    @Override
    public void onTerminate() {

        this.ctx.log("-------------- " + getCurrentTimeFormatted() + " --------------");
        this.ctx.log("-----------------------------------");
        this.ctx.log("----- Thank You & Good Luck! ------");
        this.ctx.log("-----------------------------------");

        // Other vars
        this.startingSkillLevel = 0L;
        this.startingSkillExp = 0L;
        this.count = 0;
        guiGpt.setVisible(false);
        gptDeactivation();

    }

    @Override
    public void onChatMessage(ChatMessage m) {
        String formattedMessage = m.getFormattedMessage();
        ChatMessageType getType = m.getType();
        net.runelite.api.events.ChatMessage getEvent = m.getChatEvent();
        String senderName = getEvent.getName();
        String gameMessage = getEvent.getMessage();
        String message = getEvent.getMessage().toLowerCase();

        if (m.getMessage() == null) {
            return;
        }

        if (gptStarted && botStarted) eAutoResponser.handleGptMessages(getType, senderName, formattedMessage);
        eTriviaInfo.handleBroadcastMessage(getType, gameMessage);

        if (getType == ChatMessageType.SPAM) {
            if (message.contains("successfully pick the master farmer")) {
                count++;
            }
        }

        if (getType == ChatMessageType.SPAM) {
            if (message.contains("this while in combat.")) {
                teleport = true;
            }
        }
    }

    @Override
    public int loopDuration() {
        return 150;
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
        long currentSkillLevel = this.ctx.skills.realLevel(chosenSkill);
        long currentSkillExp = this.ctx.skills.experience(chosenSkill);
        long skillLevelsGained = currentSkillLevel - this.startingSkillLevel;
        long skillExpGained = currentSkillExp - this.startingSkillExp;

        // Calculate experience and actions per hour
        long skillExpPerHour = ctx.paint.valuePerHour((int) skillExpGained, startTime);

        // Set up colors
        Color philippineRed = new Color(196, 18, 48);
        Color raisinBlack = new Color(35, 31, 32, 127);

        // Draw paint if not hidden
        if (!hidePaint) {
            g.setColor(raisinBlack);
            g.fillRoundRect(5, 120, 205, 110, 20, 20);

            g.setColor(philippineRed);
            g.drawRoundRect(5, 120, 205, 110, 20, 20);

            g.setColor(philippineRed);
            g.drawString("eMasterFarmerT by Esmaabi", 15, 135);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + runTime, 15, 150);
            g.drawString("Skill Level: " + currentSkillLevel + " (+" + skillLevelsGained + "), started at " + this.startingSkillLevel, 15, 165);
            g.drawString("Current Exp: " + currentSkillExp, 15, 180);
            g.drawString("Exp gained: " + skillExpGained + " (" + (skillExpPerHour / 1000L) + "k xp/h)", 15, 195);
            g.drawString("Stole seeds: " + count + " (" + ctx.paint.valuePerHour(count, startTime) + " per/h)", 15, 210);
            g.drawString("Status: " + status, 15, 225);

        }
    }
}
