package eChinHunterBotZenyte;

import BotUtils.*;
import Utility.Trivia.eTriviaInfo;
import eApiAccess.eAutoResponderGui;
import eApiAccess.eAutoResponser;
import net.runelite.api.ChatMessageType;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleInventory;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.queries.SimpleEntityQuery;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Pathing;
import simple.hooks.wrappers.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static eApiAccess.eAutoResponser.*;

@ScriptManifest(author = "Esmaabi", category = Category.HUNTER, description =
                "<br>The most effective chinchompa hunter bot on Zenyte!<br>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "Start <b>near any chinchompas</b>.<br>"
                + "Make sure that it's possible to set up box traps on all tiles.<br> "
                + "Have at least 5 box traps in inventory.<br>"
                + "Bot will decide how many traps to place.<br>"
                + "Chat GPT answering is integrated.",
        discord = "Esmaabi#5752",
        name = "eChinHunterBot", servers = { "Zenyte" }, version = "1")

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    public static eAutoResponderGui guiGpt;
    private static final SimpleSkills.Skills chosenSkill = SimpleSkills.Skills.HUNTER;
    private static final int BOX_TRAP_ITEM = 10008;
    private static final String eBotName = "eChinHunterBot";
    private static final String ePaintText = "Chins caught";

    // Variables related to game status
    private long startTime;
    private long startingSkillLevel;
    private long startingLevel;
    private long startingSkillExp;
    private int count;
    private int maxTraps = -1;
    private boolean calculated = false;
    private boolean trapsPickup = false;
    private static boolean hidePaint = false;

    // Variables related to location
    private WorldPoint[] locs;

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

        tasks.addAll(Arrays.asList(new eWildyTeleport(ctx), new eAutoResponser(ctx), new eImpCatcher(ctx)));

        // Setting up GPT Gui
        eAutoResponser.scriptPurpose = "you're just catching chinchompas.";
        eAutoResponser.gptStarted = false;
        initializeGptGUI();
        initializeMethods();
        gptDeactivation();

        // Starting message
        this.ctx.log("--------------- " + BotUtils.eActions.getCurrentTimeFormatted() + " ---------------");
        this.ctx.log("-------------------------------------");
        this.ctx.log("            " + eBotName + "         ");
        this.ctx.log("-------------------------------------");

        // Variables
        ctx.log("Setting up bot");
        this.startTime = System.currentTimeMillis();
        this.startingSkillLevel = this.ctx.skills.realLevel(chosenSkill);
        this.startingSkillExp = this.ctx.skills.experience(chosenSkill);
        startingLevel = startingSkillLevel;
        count = 0;

        // Viewport settings
        ctx.viewport.angle(0);
        ctx.viewport.pitch(true);
        ctx.viewport.yaw();

        // Traps setup tiles
        WorldPoint startingTile = ctx.players.getLocal().getLocation();
        int p = startingTile.getPlane();
        int[][] offsets = {{0, 0}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
        locs = new WorldPoint[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            int x = startingTile.getX() + offsets[i][0];
            int y = startingTile.getY() + offsets[i][1];
            locs[i] = new WorldPoint(x, y, p);
        }
    }

    @Override
    public void onProcess() {
        super.onProcess();

        SimplePlayer localPlayer = ctx.players.getLocal();
        Pathing pathing = ctx.pathing;

        if (!botStarted) {
            return;
        }

        if (localPlayer.getAnimation() != -1 || pathing.inMotion()) {
            return;
        }

        if (pathing.energyLevel() > 30 && !pathing.running() && pathing.inMotion()) {
            pathing.running(true);
        }

        int realLevel = this.ctx.skills.realLevel(chosenSkill);
        if (startingLevel < realLevel) {
            startingLevel = realLevel;
            calculated = false;
        }

        SimpleEntityQuery<SimpleGroundItem> groundItems = ctx.groundItems.populate().filter(BOX_TRAP_ITEM);
        if (!trapsPickup) {

            SimpleGroundItem floorTrap = groundItems.nearest().next();
            if (floorTrap != null && floorTrap.validateInteractable() && !localPlayer.isAnimating()) {
                BotUtils.eActions.status = "Laying trap";
                SimpleInventory invQuery = (SimpleInventory) ctx.inventory.populate().filter(BOX_TRAP_ITEM);
                int trapAmountCached = invQuery.population();
                changeCameraAngleOnThread(floorTrap);
                BotUtils.eActions.interactWith(floorTrap, "Lay");
                ctx.sleepCondition(() -> invQuery.population() > trapAmountCached, 2000);
                return;
            }

            WorldPoint trapTile = getAvailableTrapLocation();
            if (placedTraps() == trapAmount() && trapTile == null && floorTrap == null) {
                BotUtils.eActions.status = "Waiting for action";
                SimpleEntityQuery<SimpleObject> objects = ctx.objects.populate().filter(9382, 9383, 9385, 721).filterHasAction("Reset").filter(t -> objectInLocation(t.getLocation()));
                SimpleObject trap = objects.nearest().next();
                if (trap != null && trap.validateInteractable() && !localPlayer.isAnimating()) {
                    BotUtils.eActions.status = "Resetting traps";
                    int chinsCached = getChinCount();
                    BotUtils.eActions.interactWith(trap, "Reset");
                    ctx.sleep(3000);
                    if (trap.getName().equalsIgnoreCase("shaking box") && ctx.onCondition(() -> getChinCount() > chinsCached, 2400)) {
                        count += (getChinCount() - chinsCached);
                    }
                    ctx.sleepCondition(() -> trapExistsForTile(trap.getLocation()));
                }

            } else {

                BotUtils.eActions.status = "Checking traps in inventory";
                SimpleItem invTrap = ctx.inventory.populate().filter(BOX_TRAP_ITEM).next();

                if (invTrap == null || trapTile == null) {
                    return;
                }


                WorldPoint playerLocation = localPlayer.getLocation();
                if (!playerLocation.equals(trapTile)) {
                    BotUtils.eActions.status = "Walking to next spot";
                    stepOnTile(trapTile.getX(), trapTile.getY(), playerLocation);
                    ctx.onCondition(() -> true, 200, 8);
                }

                if (playerLocation.equals(trapTile)) {
                    BotUtils.eActions.status = "Setting up trap";
                    setupTrap(invTrap, trapTile);
                }
            }

        } else {

            if (!groundItems.isEmpty()) {
                if (!ctx.inventory.inventoryFull()) {
                    SimpleGroundItem trapsGround = groundItems.nearest().next();
                    BotUtils.eActions.status = "Picking up trap(s)";
                    int trapsInv = getTrapCountInv();
                    if (trapsGround != null && trapsGround.validateInteractable() && !localPlayer.isAnimating()) {
                        changeCameraAngleOnThread(trapsGround);
                        BotUtils.eActions.interactWith(trapsGround, "Take");
                        ctx.onCondition(() -> ctx.inventory.populate().filter("Box trap").population() > trapsInv);
                    }
                }
            } else {
                BotUtils.eActions.status = "No traps to pickup";
                trapsPickup = false;
            }
        }
    }

    private boolean objectInLocation(WorldPoint w) {
        for (WorldPoint loc : locs) {
            if (w.equals(loc)) {
                return true;
            }
        }
        return false;
    }

    public WorldPoint getAvailableTrapLocation() {
        for (int i = 0; i < trapAmount(); i++) {
            WorldPoint loc = locs[i];
            if (!trapExistsForTile(loc)) {
                return loc;
            }
        }
        return null;
    }

    // Functions
    private int trapAmount() {
        if (!calculated) {
            int level = ctx.skills.level(SimpleSkills.Skills.HUNTER);
            maxTraps = Math.min(level / 20 + 1, 5);
            ctx.log("You can place a maximum of " + maxTraps + " traps.");
            calculated = true;
        }
        return maxTraps;
    }

    private void setupTrap(final SimpleItem invTrap, final WorldPoint tile) {
        if (invTrap.click(1)) {
            ctx.onCondition(() -> this.trapExistsForTile(tile));
        }
    }

    private boolean trapExistsForTile(final WorldPoint tile) {
        return !ctx.objects.populate().filter(9380, 9382, 9383, 9385, 9384, 2025, 2026, 2028, 721, 9392, 9393, 9390).filter(tile).isEmpty();
    }

    private int placedTraps() {
        int count = 0;
        for (int i = 0; i < trapAmount(); i++) {
            WorldPoint loc = locs[i];
            if (trapExistsForTile(loc)) {
                count++;
            }
        }
        return count;
    }

    private void stepOnTile(int x, int y, WorldPoint playerLocation) {
        WorldPoint NEXT_TILE = new WorldPoint(x, y, 0);
        if (playerLocation.distanceTo(NEXT_TILE) >= 6) {
            ctx.pathing.step(NEXT_TILE);
        } else {
            ctx.pathing.clickSceneTile(NEXT_TILE, false, true);
        }
    }

    // Getting info
    private int getGainedChins() {
        return count;
    }

    private int getChinCount() {
        return ctx.inventory.populate().filter("chinchompa", "red chinchompa", "black chinchompa").population(true);
    }

    private int getTrapCountInv() {
        return ctx.inventory.populate().filter("Box trap").population();
    }

    private void changeCameraAngleOnThread(SimpleGroundItem object) {
        // Create a ScheduledExecutorService with a single thread
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        // Turn to desired object
        ctx.viewport.turnTo(object);

        // Get the current camera orientation angle
        int currentAngle = ctx.viewport.yaw();

        // Generating a random number between 0 and 30
        int angleChange = randomSleeping(0, 30);

        // Getting the sign of the angle change based on the current angle
        if (currentAngle >= 329 || currentAngle <= 30) {
            angleChange = -angleChange; // Make the angle change negative for 329-359 and 0-30 range
        }

        // Calculating the new camera angle by adding the angle change
        int newAngle = (currentAngle + angleChange) % 360;

        // Setting the new camera angle
        ctx.viewport.angle(newAngle);

        // Shutting down the executor
        executor.shutdown();
    }

    private enum EntityType {
        SimpleObjects, SimpleGroundItem
    }

    private boolean trapWithAction(EntityType entityType, WorldPoint tile, String action) {
        switch (entityType) {
            case SimpleObjects:
                return !ctx.objects.populate()
                        .filter(tile)
                        .filterHasAction(action)
                        .isEmpty();
            case SimpleGroundItem:
                return !ctx.groundItems.populate()
                        .filter(tile)
                        .filterHasAction(action)
                        .isEmpty();
            default:
                return false;
        }
    }

    @Override
    public void onTerminate() {

        // Termination message
        ctx.log("-------------- " + BotUtils.eActions.getCurrentTimeFormatted() + " --------------");
        ctx.log(ePaintText + ": " + getGainedChins());
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
        Utility.Trivia.eTriviaInfo.handleBroadcastMessage(getType, gameMessage);

/*        String eventToString = getEvent.toString().replaceAll("<[^>]+>", "").trim();
        logger.info(eventToString); // to debug (returns chat type, text, sender)*/

        if (getType == ChatMessageType.GAMEMESSAGE) {
            if (gameMessage.contains("You cannot lay a trap here")) {
                BotUtils.eActions.status = "Can't lay here!";
                trapsPickup = true;
            }

            if (gameMessage.contains("traps at a time at your Hunter level")) {
                BotUtils.eActions.status = "Can't lay here!";
                trapsPickup = true;
            }
        }

    }

    @Override
    public int loopDuration() {
        return 150;
    }

    @Override
    public void paint(Graphics g) {

        // Check each trap location
        if (locs != null) {
            for (WorldPoint trapTile : locs) {
                if (trapTile != null) {
                    if (trapWithAction(EntityType.SimpleGroundItem, trapTile, "Take")) {
                        BotUtils.eActions.drawTileMatrix(ctx, (Graphics2D) g, trapTile, Color.RED);
                    } else if (trapWithAction(EntityType.SimpleObjects,trapTile, "Reset")) {
                        BotUtils.eActions.drawTileMatrix(ctx, (Graphics2D) g, trapTile, Color.GREEN);
                    } else if (trapWithAction(EntityType.SimpleGroundItem, trapTile, "Lay")) {
                        BotUtils.eActions.drawTileMatrix(ctx, (Graphics2D) g, trapTile, Color.GREEN);
                    } else if (trapWithAction(EntityType.SimpleObjects, trapTile, "Dismantle")) {
                        BotUtils.eActions.drawTileMatrix(ctx, (Graphics2D) g, trapTile, Color.YELLOW);
                    } else {
                        BotUtils.eActions.drawTileMatrix(ctx, (Graphics2D) g, trapTile, Color.DARK_GRAY);
                    }
                }
            }
        }

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
            g.drawString(eBotName + " by Esmaabi", 15, 135);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + runTime, 15, 150);
            g.drawString("Skill Level: " + currentSkillLevel + " (+" + skillLevelsGained + "), started at " + this.startingSkillLevel, 15, 165);
            g.drawString("Current Exp: " + currentSkillExp, 15, 180);
            g.drawString("Exp gained: " + skillExpGained + " (" + (skillExpPerHour / 1000L) + "k xp/h)", 15, 195);
            g.drawString(ePaintText + ": " + count + " (" + ctx.paint.valuePerHour(count, startTime) + " per/h)", 15, 210);
            g.drawString("Status: " + BotUtils.eActions.status, 15, 225);
        }
    }
}
