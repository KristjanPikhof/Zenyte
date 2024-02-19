package eTeleporterBot;

import BotUtils.eActions;
import BotUtils.eBanking;
import BotUtils.eData;
import BotUtils.eLogGenius;
import Utility.Trivia.eTriviaInfo;
import eApiAccess.eAutoResponderGui;
import eApiAccess.eAutoResponser;
import net.runelite.api.ChatMessageType;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimplePlayer;

import java.awt.*;
import java.util.*;
import java.util.List;

import static eApiAccess.eAutoResponser.botStarted;
import static eApiAccess.eAutoResponser.gptDeactivation;

@ScriptManifest(
        author = "Esmaabi",
        category = Category.MAGIC,
        description = "<html>"
                + "<p>The most effective Teleport based Magic training bot on Zenyte!</p>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "<ul>"
                + "<li>---</li>"
                + "<li>---</li>"
                + "<li>Chat GPT answering is integrated.</li>"
                + "</ul>"
                + "</html>",
        discord = "Esmaabi#5752",
        name = "eTeleporterBot",
        servers = {"Zenyte"},
        version = "0.1"
)

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private static final String eBotName = "eTeleporterBot";
    private static final String ePaintText = "Teleported";
    private static final SimpleSkills.Skills CHOSEN_SKILL = SimpleSkills.Skills.MAGIC;

    // Variables
    private int count;
    private static eAutoResponderGui guiGpt;
    public static boolean hidePaint = false;
    private long startTime = 0L;
    private long startingSkillExp;
    private long startingSkillLevel;

    private final Map<Integer, String> regionTeleportMap;
    eLogGenius elog = new eLogGenius(ctx);

    public eMain() {
        regionTeleportMap = new HashMap<>();
        regionTeleportMap.put(11062, "Camelot Teleport");
        regionTeleportMap.put(12853, "Varrock Teleport");
        regionTeleportMap.put(11577, "Trollheim Teleport");
        regionTeleportMap.put(11828, "Falador Teleport");
        regionTeleportMap.put(10547, "Ardougne Teleport");
        regionTeleportMap.put(12850, "Lumbridge Teleport");
        regionTeleportMap.put(10032, "Watchtower Teleport");
        regionTeleportMap.put(10288, "Watchtower Teleport");
    }
    
    // Gui GPT
    private void initializeGptGui() {
        guiGpt = new eAutoResponderGui();
        guiGpt.setVisible(true);
        guiGpt.setLocale(ctx.getClient().getCanvas().getLocale());
    }

    private void initializeMethods() {
        eBanking bankingUtils = new eBanking(ctx);
        eActions actionUtils = new eActions(ctx);
        eData dataUtils = new eData(ctx);
        eTriviaInfo triviaInfo = new eTriviaInfo(ctx);
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

        tasks.addAll(Arrays.asList(new eAutoResponser(ctx)));
        initializeMethods(); // BotUtils
        initializeGptGui(); // GPT
        eAutoResponser.scriptPurpose = "you're just training magic by teleporting " + getString(ctx.players.getLocal().getLocation().getRegionID()).split(" ")[0];
        gptDeactivation();

        // Other vars
        ctx.log("--------------- " + eActions.getCurrentTimeFormatted() + " ---------------");
        ctx.log("-------------------------------------");
        ctx.log("            " + eBotName + "         ");
        ctx.log("-------------------------------------");

        // Vars
        eActions.updateStatus("Setting up bot");
        this.startTime = System.currentTimeMillis();
        this.startingSkillLevel = this.ctx.skills.realLevel(CHOSEN_SKILL);
        this.startingSkillExp = this.ctx.skills.experience(CHOSEN_SKILL);
        count = 0;
        ctx.viewport.angle(180);
        ctx.viewport.pitch(true);
        eActions.zoomOutViewport();
    }

    @Override
    public void onProcess() {
        super.onProcess();

        final SimplePlayer localPlayer = ctx.players.getLocal();
        final int getRegion = localPlayer.getLocation().getRegionID();

        if (!botStarted) {
            eActions.status = "Please start the bot!";
            return;
        }

        if (!localPlayer.isAnimating()) {
            final String teleportAction = getString(getRegion);
            if (teleportAction != null) {
                eActions.status = "Teleporting to " + teleportAction.split(" ")[0];
                ctx.magic.castSpellOnce(teleportAction);
            } else {
                eActions.status = "Unknown location";
                elog.print("Unknown location");
                ctx.stopScript();
                ctx.sendLogout();
            }
            count++;
        }

        eActions.handleRunning();
    }

    private String getString(int getRegion) {
        return regionTeleportMap.get(getRegion);
    }

    @Override
    public void onTerminate() {

        // Termination message
        ctx.log("-------------- " + eActions.getCurrentTimeFormatted() + " --------------");
        ctx.log(ePaintText + ": " + count);
        ctx.log("-----------------------------------");
        ctx.log("----- Thank You & Good Luck! ------");
        ctx.log("-----------------------------------");

        // Other variables
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
        String senderName = m.getChatEvent().getName();
        String gameMessage = m.getChatEvent().getMessage();

        if (m.getMessage() == null) {
            return;
        }

        if (elog.printChatContaining(m,"successfully pick the", false)) {
            count += 1;
        }

        eAutoResponser.handleGptMessages(getType, senderName, formattedMessage);
        eTriviaInfo.handleBroadcastMessage(getType, gameMessage);
    }

    @Override
    public int loopDuration() {
        return 600;
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
            g.drawString(eBotName + " by Esmaabi", 15, 135);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + runTime, 15, 150);
            g.drawString("Skill Level: " + currentSkillLevel + " (+" + skillLevelsGained + "), started at " + this.startingSkillLevel, 15, 165);
            g.drawString("Current Exp: " + currentSkillExp, 15, 180);
            g.drawString("Exp gained: " + skillExpGained + " (" + (skillExpPerHour / 1000L) + "k xp/h)", 15, 195);
            g.drawString(ePaintText + ": " + count + " (" + actionsPerHour + " per/h)", 15, 210);
            g.drawString("Status: " + eActions.status, 15, 225);

        }
    }
}


