package eAgilityBotZenyte;

import Utility.Trivia.eTriviaInfo;
import eApiAccess.eAutoResponderGui;
import eApiAccess.eAutoResponser;
import eAgilityBotZenyte.Areas.*;
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
import simple.hooks.simplebot.Pathing;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleObject;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

import static eApiAccess.eAutoResponser.*;

@ScriptManifest(
        author = "Esmaabi",
        category = Category.AGILITY,
        description = "<html>"
                + "<p>The most effective agility bot on Zenyte!</p>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "<ul>"
                + "<li>Always start the bot <b>at the beginning of agility course!</b>.</li>"
                + "<li>The bot will recognize location and start running to first obstacle.</b>.</li>"
                + "<li>You can reset the bot to new course location by typing <b>\"---\"</b> in chat.</li>"
                + "<li>Bot will pick up marks of grace.</li>"
                + "<li>Chat GPT answering is integrated.</li>"
                + "<li><b>Supported courses:</b> Al-Kharid, Varrock, Canafis, Seers, Pollniveach, Rellekka, Ardougne.</li>"
                + "</ul>"
                + "</html>",
        discord = "Esmaabi#5752",
        name = "eAgilityBotZenyte",
        servers = {"Zenyte"},
        version = "1"
)

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private static final SimpleSkills.Skills CHOSEN_SKILL = SimpleSkills.Skills.AGILITY;
    public static eAutoResponderGui guiGpt;
    private static final Logger logger = Logger.getLogger(eMain.class.getName());
    private static final int MARK_OF_GRACE = 11849;

    // Variables
    private int count;
    private static WorldPoint currentArea;
    private List<eObstaclesListing> currentObstacles;  // Variable to store the obstacles for the current area
    public static boolean hidePaint = false;
    public static boolean sleepToPickup = false;
    private long startTime = 0L;
    private long startingSkillExp;
    private long startingSkillLevel;
    static String status = null;
    private static int startMarks;
    private static int totalMarks;



    // GPT gui
    public void initializeGptGUI() {
        guiGpt = new eAutoResponderGui();
        guiGpt.setVisible(true);
        guiGpt.setLocale(ctx.getClient().getCanvas().getLocale());
    }

    private void initializeMethods() {
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

        tasks.addAll(Arrays.asList(new eApiAccess.eAutoResponser(ctx)));// Adds tasks to our {task} list for execution

        // Setting up GPT Gui
        eAutoResponser.scriptPurpose = "you're grinding to get agility pet.";
        eAutoResponser.gptStarted = false;
        initializeGptGUI();
        gptDeactivation();

        // Intro
        System.out.println("Started eAgilityBot!");
        this.ctx.log("--------------- " + getCurrentTimeFormatted() + " ---------------");
        this.ctx.log("------------------------------------");
        this.ctx.log("               eAgilityBot          ");
        this.ctx.log("------------------------------------");

        // Vars
        updateStatus("Setting up bot");
        this.startTime = System.currentTimeMillis();
        this.startingSkillLevel = this.ctx.skills.realLevel(CHOSEN_SKILL);
        this.startingSkillExp = this.ctx.skills.experience(CHOSEN_SKILL);
        count = 0;
        startMarks = getCountStacked();
        currentArea = null;
        ctx.viewport.pitch(true);
    }

    @Override
    public void onProcess() {
        super.onProcess();

        final Pathing pathing = ctx.pathing;
        final boolean inMotion = ctx.pathing.inMotion();
        final boolean isAnimating = ctx.players.getLocal().isAnimating();

        if (!botStarted) {
            return;
        }

        if (pathing.energyLevel() > 30 && !pathing.running() && inMotion) {
            pathing.running(true);
        }

        if (!ctx.groundItems.populate().filter(MARK_OF_GRACE).filter((i) -> pathing.reachable(i.getLocation())).isEmpty()) {
            handleGroundItem();
        } else {

            if (currentArea == null) {
                currentArea = getPlayerLocation();
                currentObstacles = null;
            }

            if (currentArea != null) {

                if (currentObstacles == null) {
                    currentObstacles = getObstaclesForArea(currentArea).getObstacles();  // Retrieve obstacles
                    updateStatus(eObstaclesResult.getStatusMessage());
                }

                if (currentObstacles != null && !currentObstacles.isEmpty()) {
                    for (eObstaclesListing obstacle : currentObstacles) {
                        if (pathing.inArea(obstacle.obstacleArea)) {
                            if (pathing.reachable(obstacle.obstaclePoint)) {
                                status = obstacle.actionName + " " + obstacle.objectName.toLowerCase();
                                SimpleObject object = ctx.objects.populate().filter(obstacle.objectName).
                                        filter((location) -> location.getLocation().distanceTo(obstacle.obstaclePoint) <= 3).
                                        filterHasAction(obstacle.actionName).nearest().next();

                                if (object != null && object.validateInteractable()) {

                                    if (!object.visibleOnScreen()) {
                                        ctx.viewport.turnTo(object);
                                        ctx.pathing.step(object.getLocation());
                                        ctx.onCondition(object::visibleOnScreen, 250, 8);
                                    }
                                    object.menuAction(obstacle.actionName);
                                    ctx.onCondition(() -> inMotion || isAnimating, 250, 10);
                                }
                            }
                            break;
                        }
                    }

                } else {
                    updateStatus("No obstacles found nearby");
                    currentArea = null;
                    currentObstacles = null;
                }
            }
        }
    }

    // Setting specific obstacles
    private WorldPoint getPlayerLocation() {
        return ctx.players.getLocal().getLocation();
    }

    private eObstaclesResult getObstaclesForArea(WorldPoint playerLocation) {
        Pathing pathing = ctx.pathing;
        if (pathing.inArea(eDataArdougne.startArea, playerLocation)) {
            return new eObstaclesResult("Chosen: Ardougne Rooftops", true, eDataArdougne.obstaclesArdougne);
        } else if (pathing.inArea(eDataPollnivneach.startArea, playerLocation)) {
            return new eObstaclesResult("Chosen: Pollnivneach Rooftops", false, eDataPollnivneach.obstaclesPollnivneach);
        } else if (pathing.inArea(eDataSeers.startArea, playerLocation)) {
            return new eObstaclesResult("Chosen: Seers Rooftops", false, eDataSeers.obstaclesSeers);
        } else if (pathing.inArea(eDataAlKharid.startArea, playerLocation)) {
            return new eObstaclesResult("Chosen: Al-Kharid Rooftops", false, eDataAlKharid.obstaclesALKharid);
        } else if (pathing.inArea(eDataCanifis.startArea, playerLocation)) {
            return new eObstaclesResult("Chosen: Canifis Rooftops", false, eDataCanifis.obstaclesCanifis);
        } else if (pathing.inArea(eDataVarrock.startArea, playerLocation)) {
            return new eObstaclesResult("Chosen: Varrock Rooftops", false, eDataVarrock.obstaclesVarrock);
        } else if (pathing.inArea(eDataAlRellekka.startArea, playerLocation)) {
            return new eObstaclesResult("Chosen: Rellekka Rooftops", false, eDataAlRellekka.obstaclesRellekka);
        }

        return new eObstaclesResult("Not familiar area. Write: \"---\"", false, null);
    }


    // Picking up ground item
    private void handleGroundItem() {
        SimpleGroundItem itemToPickup = ctx.groundItems.populate().filter(eMain.MARK_OF_GRACE).nearest().next();

        if (itemToPickup != null && itemToPickup.validateInteractable()) {
            if (eObstaclesResult.sleepToPickupCheck() && gameType().equals("Zenyte")) {
                if (ctx.pathing.inMotion()) {
                    ctx.sleep(2000);
                }
            }
            int countInv = ctx.inventory.populate().filter(eMain.MARK_OF_GRACE).population(true);
            itemToPickup.menuAction("Take");
            ctx.onCondition(() -> ctx.groundItems.populate().filter(eMain.MARK_OF_GRACE).isEmpty(), 250, 8);
            if (countInv < ctx.inventory.populate().filter(eMain.MARK_OF_GRACE).population(true)) {
                updateStatus(getCurrentTimeFormatted() + " Picked up " + itemToPickup.getName().toLowerCase());
            }
        }
    }

    // MOG
    private int getCountStacked() {
        return ctx.inventory.populate().filter(eMain.MARK_OF_GRACE).population(true);
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

    private String gameType() {
        Game.ClientType clientType = ctx.game.clientType();

        switch (clientType) {
            case ALORA:
                return "Alora";
            case ATLAS:
                return "Atlas";
            case BATTLESCAPE:
                return "Battlescape";
            case KODAI:
                return "Kodai";
            case NOVEA:
                return "Novea";
            case OSRSPS:
                return "Osrsp";
            case VITALITY:
                return "Vitality";
            case ZENYTE:
                return "Zenyte";
            default:
                return "Normal";
        }
    }

    private String getPlayerLocationUtility() {
        WorldPoint location = ctx.players.getLocal().getLocation();
        return "new WorldPoint(" + location.getX() + ", " + location.getY() + ", " + location.getPlane() + ");";
    }

    @Override
    public void onTerminate() {

        this.ctx.log("-------------- " + getCurrentTimeFormatted() + " --------------");
        this.ctx.log("We have done " + count + " laps.");
        this.ctx.log("We collected " + totalMarks + " MOGs.");
        this.ctx.log("-----------------------------------");
        this.ctx.log("----- Thank You & Good Luck! ------");
        this.ctx.log("-----------------------------------");

        // Other vars
        this.startingSkillLevel = 0L;
        this.startingSkillExp = 0L;
        this.count = 0;
        totalMarks = 0;
        startMarks = 0;
        guiGpt.setVisible(false);
        gptDeactivation();
        currentArea = null;
        currentObstacles = null;
        sleepToPickup = false;
    }

    @Override
    public void onChatMessage(ChatMessage m) {

        String formattedMessage = m.getFormattedMessage();
        ChatMessageType getType = m.getType();
        net.runelite.api.events.ChatMessage getEvent = m.getChatEvent();
        String senderName = getEvent.getName();
        String gameMessage = getEvent.getMessage();

        if (m.getMessage() == null) {
            return;
        }

        if (getType == ChatMessageType.PUBLICCHAT) {

            senderName = senderName.replaceAll("<[^>]+>", "").trim();

            if (senderName.contains(getPlayerName(ctx))) {
                if (formattedMessage.toLowerCase().contains("---")) {
                    currentArea = null;
                    currentObstacles = null;
                } else if (formattedMessage.toLowerCase().contains("1")) {
                    logger.info(getPlayerLocationUtility());
                }
            }
        }

        if (getType == ChatMessageType.GAMEMESSAGE) {
            if (gameMessage.contains("laps on the")) {
                count++;
            }
        }

        if (gptStarted && botStarted) eAutoResponser.handleGptMessages(getType, senderName, formattedMessage);
        eTriviaInfo.handleBroadcastMessage(getType, gameMessage);
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

        // Get MOG count
        totalMarks = getCountStacked() - startMarks;

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
            g.fillRoundRect(5, 120, 205, 125, 20, 20);

            g.setColor(philippineRed);
            g.drawRoundRect(5, 120, 205, 125, 20, 20);

            g.setColor(philippineRed);
            g.drawString("eAgilityBot by Esmaabi", 15, 135);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + runTime, 15, 150);
            g.drawString("Skill Level: " + currentSkillLevel + " (+" + skillLevelsGained + "), started at " + this.startingSkillLevel, 15, 165);
            g.drawString("Current Exp: " + currentSkillExp, 15, 180);
            g.drawString("Exp gained: " + skillExpGained + " (" + (skillExpPerHour / 1000L) + "k xp/h)", 15, 195);
            g.drawString("MOG collected: " + totalMarks + " (" + ctx.paint.valuePerHour(totalMarks, startTime) + " per/h)", 15, 210);
            g.drawString("Laps completed: " + count + " (" + actionsPerHour + " per/h)", 15, 225);
            g.drawString("Status: " + status, 15, 240);

        }
    }
}
