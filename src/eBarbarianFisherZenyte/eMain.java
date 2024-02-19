package eBarbarianFisherZenyte;

import BotUtils.eActions;
import BotUtils.eBanking;
import BotUtils.eData;
import BotUtils.eImpCatcher;
import Utility.Trivia.eTriviaInfo;
import eApiAccess.eAutoResponderGui;
import eApiAccess.eAutoResponser;
import net.runelite.api.ChatMessageType;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Pathing;
import simple.hooks.wrappers.*;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import static eApiAccess.eAutoResponser.*;

@ScriptManifest(author = "Esmaabi", category = Category.FISHING, description =
                "<br>The most effective chinchompa hunter bot on Zenyte!<br>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "Start <b>near any chinchompas</b>.<br>"
                + "Make sure that it's possible to set up box traps on all tiles.<br> "
                + "Have at least 5 box traps in inventory.<br>"
                + "Bot will decide how many traps to place.<br>"
                + "Chat GPT answering is integrated.",
        discord = "Esmaabi#5752",
        name = "eBarbarianFisherBot", servers = { "Zenyte" }, version = "1")

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    public static eAutoResponderGui guiGpt;
    private static final Logger logger = Logger.getLogger(eMain.class.getName());
    private static final SimpleSkills.Skills CHOSEN_SKILL = SimpleSkills.Skills.FISHING;
    private final static int INVENTORY_BAG_WIDGET_ID = 548;
    private final static int INVENTORY_BAG_CHILD_ID = 58;
    private final List<String> fishNames = Arrays.asList("Leaping sturgeon", "Leaping trout", "Leaping salmon");
    private final List<String> roeNames = Arrays.asList("Roe", "Caviar");

    // Variables related to game status
    private long startTime;
    private long startingSkillLevel;
    private long startingSkillExp;
    private int count;
    private boolean cutFish = true;
    private boolean roeMade = false;
    private static boolean hidePaint = false;

    // Variables (other)
    private static String status;

    // Gui GPT
    public void initializeGptGUI() {
        guiGpt = new eAutoResponderGui();
        guiGpt.setVisible(true);
        guiGpt.setLocale(ctx.getClient().getCanvas().getLocale());
    }

    private void initializeMethods() {
        eBanking bankingUtils = new eBanking(ctx);
        eActions actionUtils = new eActions(ctx);
        eData dataUtils = new eData(ctx);
        eImpCatcher impCatcher = new eImpCatcher(ctx);
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

        tasks.addAll(Arrays.asList(new eAutoResponser(ctx), new eImpCatcher(ctx)));

        // Setting up GPT Gui
        eAutoResponser.scriptPurpose = "you're just doing barbarian fishing to level Fishing fast.";
        eAutoResponser.gptStarted = false;
        initializeGptGUI();

        // Starting message
        System.out.println("Started eBarbarianFisherBot!");
        this.ctx.log("--------------- " + getCurrentTimeFormatted() + " ---------------");
        this.ctx.log("------------------------------------");
        this.ctx.log("          eBarbarianFisherBot       ");
        this.ctx.log("------------------------------------");

        // Variables
        updateStatus("Setting up bot");
        this.startTime = System.currentTimeMillis();
        this.startingSkillLevel = this.ctx.skills.realLevel(CHOSEN_SKILL);
        this.startingSkillExp = this.ctx.skills.experience(CHOSEN_SKILL);
        gptDeactivation();
        count = 0;
        cutFish = false;
        roeMade = false;

        // Viewport settings
        ctx.viewport.angle(0);
        ctx.viewport.pitch(true);
        ctx.viewport.yaw();
    }

    @Override
    public void onProcess() {
        super.onProcess();

        SimplePlayer localPlayer = ctx.players.getLocal();
        Pathing pathing = ctx.pathing;

        if (!botStarted) {
            return;
        }

        if (pathing.energyLevel() > 30 && !pathing.running() && pathing.inMotion()) {
            pathing.running(true);
        }

        if (!invItemValid("Knife") || !invItemValid("Feather") || !invItemValid("Barbarian rod")) {
            updateStatus("Check inventory!");
            updateStatus("You must have: Knife, Feathers & Barbarian rod");
            ctx.stopScript();
        }

        if (ctx.inventory.inventoryFull()) {
            eatRoe();
            dropFish();

        } else {

            if (localPlayer.isAnimating()) {
                cuttingFihs();
            } else {
                updateStatus("Player not fishing!");
                SimpleNpc fishingSpot = ctx.npcs.populate().filter(1542).nearest().next();
                if (fishingSpot != null) {
                    if (BotUtils.eActions.targetIsVisible(fishingSpot, ctx.players.getLocal().getLocation(), ctx)) {
                        clickFishingSpot(fishingSpot);
                    }
                }
            }
        }
    }

    private void eatRoe() {
        SimpleItem roeIdes = ctx.inventory.populate().filter("Roe", "Caviar").next();
        if (roeIdes != null) {
            updateStatus("Eating " + roeIdes.getName().toLowerCase());
            roeIdes.click("Eat");
            clickOnBag();
        }
    }

    private void dropFish() {
        SimpleItem fishIds = ctx.inventory.populate().filter("Leaping sturgeon", "Leaping trout", "Leaping salmon").reverse().next();
        if (fishIds != null) {
            updateStatus("Dropping " + fishIds.getName().toLowerCase());
            if (ctx.inventory.shiftDroppingEnabled()) {
                ctx.inventory.dropItem(fishIds);
            } else {
                fishIds.click("Drop");
            }
            clickOnBag();
            ctx.onCondition(() -> !ctx.inventory.inventoryFull(), 200, 4);
        }
    }

    private void clickFishingSpot(SimpleNpc npc) {
        updateStatus("Clicking fishing spot");
        BotUtils.eActions.interactWith(npc, "Use-rod");
        ctx.onCondition(ctx.players.getLocal()::isAnimating, 200, 6);
    }

    private void cuttingFihs() {
        status = "Fishing...";
        SimpleItem fishIds = ctx.inventory.populate().filter("Leaping sturgeon", "Leaping trout", "Leaping salmon").reverse().next();
        SimpleItem knife = ctx.inventory.populate().filter("Knife").next();
        if (fishIds != null && knife != null) {
            updateStatus("Cutting " + fishIds.getName().toLowerCase());
            knife.click("Use");
            fishIds.click(0);
            clickOnBag();
            ctx.onCondition(() -> roeMade, 200, 4);
            roeMade = false;
        }
    }

    private void clickOnBag() {
        SimpleWidget inventoryBagWidget = ctx.widgets.getWidget(INVENTORY_BAG_WIDGET_ID, INVENTORY_BAG_CHILD_ID);
        if (inventoryBagWidget != null) {
            inventoryBagWidget.click(0);
        }
    }

    private boolean targetIsVisible(SimpleNpc npc) {
        if (!npc.visibleOnScreen()) {
            Random rand = new Random();

            WorldPoint npcLocation = npc.getLocation();
            WorldPoint myLocation = ctx.players.getLocal().getLocation();

            int maxAttempts = 100;
            int attempts = 0;

            while (!npc.visibleOnScreen()) {
                myLocation = ctx.players.getLocal().getLocation(); // Refreshing current location
                npcLocation = npc.getLocation(); // Refreshing target location

                attempts++;
                if (attempts >= maxAttempts) {
                    break;
                }

                // Recalculate the direction based on the new positions
                int directionX = Integer.compare(npcLocation.getX(), myLocation.getX());
                int directionY = Integer.compare(npcLocation.getY(), myLocation.getY());

                // Recalculate the distance to the NPC to adjust movement magnitude
                int currentDistance = (int) ctx.pathing.distanceTo(npc.getLocation());

                if (currentDistance <= 5) {
                    return false;
                }

                ctx.log("Current distance to: " + npc.getName() + ": " + currentDistance);

                // The closer the player is, the smaller the movement magnitude
                int maxMovement = Math.min(currentDistance, 7); // Upper limit is 7
                int minMovement = Math.max(1, currentDistance / 2); // Half the distance but at least 1
                int movementMagnitude = rand.nextInt(maxMovement - minMovement + 1) + minMovement;

                // Calculate offsets based on the updated direction and movement magnitude
                int xOffset = directionX * movementMagnitude;
                int yOffset = directionY * movementMagnitude;

                // Move towards the target based on the new calculations
                moveToLocation(myLocation.getX() + xOffset, myLocation.getY() + yOffset);
                ctx.sleep(600);
            }
        }

        return  true;
    }

    private void moveToLocation(int x, int y) {
        WorldPoint startingSpot = ctx.players.getLocal().getLocation();
        WorldPoint targetSpot = new WorldPoint(x, y, startingSpot.getPlane());
        ctx.pathing.step(targetSpot);
    }

    private boolean invItemValid(String itemName) {
        return !ctx.inventory.populate().filter(itemName).isEmpty();
    }

    //Utility
    public static String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void updateStatus(String newStatus) {
        status = newStatus;
        ctx.log(status);
    }

    @Override
    public void onTerminate() {

        // Termination message
        ctx.log("-------------- " + getCurrentTimeFormatted() + " --------------");
        ctx.log("You have caught: " + count + " leaping fish");
        ctx.log("-----------------------------------");
        ctx.log("----- Thank You & Good Luck! ------");
        ctx.log("-----------------------------------");

        // Other variables
        startingSkillLevel = 0L;
        startingSkillExp = 0L;
        count = 0;
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

        if (m.getMessage() == null) {
            return;
        }

        if (gptStarted && botStarted) eAutoResponser.handleGptMessages(getType, senderName, formattedMessage);
        eTriviaInfo.handleBroadcastMessage(getType, gameMessage);

        if (getType == ChatMessageType.SPAM) {
            String spamMessage = getEvent.getMessage();
            if (spamMessage.contains("You catch a")) {
                status = "Caught a fish!";
                cutFish = true;
                count++;
            }

            if (spamMessage.contains("You cut open the fish")) {
                status = "Roe has been made!";
                roeMade = true;
            }
        }
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
            g.drawString("eBarbarianFisherBot by Esmaabi", 15, 135);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + runTime, 15, 150);
            g.drawString("Skill Level: " + currentSkillLevel + " (+" + skillLevelsGained + "), started at " + this.startingSkillLevel, 15, 165);
            g.drawString("Current Exp: " + currentSkillExp, 15, 180);
            g.drawString("Exp gained: " + skillExpGained + " (" + (skillExpPerHour / 1000L) + "k xp/h)", 15, 195);
            g.drawString("Fish caught: " + count + " (" + ctx.paint.valuePerHour(count, startTime) + " per/h)", 15, 210);
            g.drawString("Status: " + status, 15, 225);
        }
    }
}
