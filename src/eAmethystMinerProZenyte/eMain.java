package eAmethystMinerProZenyte;

import BotUtils.eActions;
import BotUtils.eBanking;
import Utility.Trivia.eTriviaInfo;
import net.runelite.api.ChatMessageType;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleObjects;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

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
        name = "eAmethystMinerProZenyte", servers = { "Zenyte" }, version = "2.3")

public class eMain extends TaskScript implements LoopingScript {

    // Coordinates
    private final WorldArea miningArea = new WorldArea (new WorldPoint(3043, 9695, 0), new WorldPoint(2993, 9729, 0));
    private final WorldArea amethArea = new WorldArea (new WorldPoint(3016, 9708, 0), new WorldPoint(3030, 9698, 0));
    private final WorldArea bankArea = new WorldArea (new WorldPoint(3011, 9720, 0), new WorldPoint(3015, 9716, 0));

    // Vars
    private static final String eBotName = "eAmethystMinerPro";
    private static final String ePaintText = "Crystals mined";
    private static final SimpleSkills.Skills CHOSEN_SKILL = SimpleSkills.Skills.MINING;
    private long startTime = 0L;
    private long startingSkillLevel;
    private long startingSkillExp;
    private int count;
    static String status = null;
    private int currentExp;
    private long lastAnimation = -1;
    private int comradesInt;

    boolean specialDone = false;
    private final int[] inventoryPickaxe = {30742, 20014, 13243, 12797, 12297, 11920, 1275, 1273, 1271, 1269, 1267, 1265};
    private final String[] names = {"Kristjan", "Hosmann", "Sleeper", "Kristjan Jr"}; // names to ba added to work
    private static boolean hidePaint = false;
    private static String playerGameName;
    private int[] lastCoordinates;
    private boolean maxXpReached;
    private String objectName;
    private String actionName;

    public static int randomSleeping(int minimum, int maximum) {
        return (int)(Math.random() * (maximum - minimum)) + minimum;
    }

    public static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void initializeMethods() {
        eBanking bankingUtils = new eBanking(ctx);
        eActions actionUtils = new eActions(ctx);
        BotUtils.eData dataUtils = new BotUtils.eData(ctx);
        eTriviaInfo triviaInfo = new eTriviaInfo(ctx);
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

        tasks.addAll(Arrays.asList());

        System.out.println("Started eAmethystMiner Pro!");

        this.ctx.updateStatus("--------------- " + BotUtils.eActions.getCurrentTimeFormatted() + " ---------------");
        this.ctx.updateStatus("-------------------------------");
        this.ctx.updateStatus("        " + eBotName + "       ");
        this.ctx.updateStatus("-------------------------------");

        status = "Setting up bot";
        this.startTime = System.currentTimeMillis();
        this.startingSkillLevel = this.ctx.skills.realLevel(CHOSEN_SKILL);
        this.startingSkillExp = this.ctx.skills.experience(CHOSEN_SKILL);
        currentExp = this.ctx.skills.experience(CHOSEN_SKILL);// for actions counter by xp drop
        count = 0;
        ctx.viewport.angle(270);
        ctx.viewport.pitch(true);
        specialDone = false;
        comradesInt = 0;
        lastCoordinates = null;
        maxXpReached = false;
        objectName = "Crystals";
        actionName = "Mine";
    }

    @Override
    public void onProcess() {
        super.onProcess();

        handeCount();
        handleRunning();

        if (ctx.players.populate().population() > 1) {
            if (comradesInArea()) {
                if (playerPopulationCheck()) {
                    System.out.println("Using method population == comradesInt");
                    activeMiningScript();
                    return;
                }

                if (!playerPopulationCheck()) {
                    int sleepTime = randomSleeping(6000, 120000);
                    status = "Anti-ban: sleep for " + convertToSec(sleepTime) + "sec";
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
            status = "Using special attack in " + convertToSec(sleep) + "sec";
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
            }

            if (!ctx.inventory.inventoryFull() && !ctx.bank.bankOpen()) {
                if (!ctx.players.getLocal().isAnimating() && (System.currentTimeMillis() > (lastAnimation + randomSleeping(1200, 6000)))) {
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
                lastCoordinates = null; // reset lastCoordinates for the next run
                status = "Depositing inventory";
                int amethystInv = ctx.inventory.populate().filter("Amethyst").population(); // get population of amethyst ore in inventory
                ctx.bank.depositAllExcept(inventoryPickaxe);
                int inventorySpaceBefore = getInventoryPopulation();
                ctx.onCondition(() -> getInventoryPopulation() < inventorySpaceBefore, 250, 10);
                if (maxXpReached) count += amethystInv;
            }
        }
        if (ctx.bank.bankOpen() && !ctx.inventory.inventoryFull()) {
            status = "Closing bank";
            ctx.bank.closeBank();
            ctx.onCondition(() -> !ctx.bank.bankOpen(), 5000);
        }
    }

    private void handeCount() {
        if (ctx.skills.experience(CHOSEN_SKILL) != 200000000) {
            if (currentExp != this.ctx.skills.experience(CHOSEN_SKILL)) {
                count++;
                currentExp = this.ctx.skills.experience(CHOSEN_SKILL);
            }
        } else {
            maxXpReached = true;
        }
    }

    private int convertToSec(long ms) {
        return (int) TimeUnit.MILLISECONDS.toSeconds(ms);
    }

    private void handleRunning() {
        if (ctx.pathing.energyLevel() > 30 && !ctx.pathing.running() && ctx.pathing.inMotion()) {
            ctx.pathing.running(true);
        }
    }

/*    public void miningTask() {
        if (bankArea.containsPoint(ctx.players.getLocal().getLocation()) && !ctx.pathing.inMotion()) {
            status = "Going to mining area";
            takingStepsRMining();
        }

        SimpleObjects crystalsNearby = (SimpleObjects) ctx.objects.populate().filter("Crystals").filterHasAction("Mine");

        while (!crystalsNearby.isEmpty()) {
            SimpleObject nearestCrystal = crystalsNearby.nearest().next();
            WorldPoint crystalLocation = nearestCrystal.getLocation();

            // Check if there are other players mining the same crystal
            boolean isOtherPlayerMining = !ctx.players.populate().filterWithin(crystalLocation, 1).filter(otherPlayer -> !otherPlayer.getName().equals(ctx.players.getLocal().getName())).isEmpty();

            if (crystalsNearby.size() >= 2 && isOtherPlayerMining) {
                ctx.log("Another player is mining the nearest crystal. Looking for another crystal.");
                crystalsNearby = (SimpleObjects) crystalsNearby.filter(otherCrystal -> !otherCrystal.equals(nearestCrystal));
            } else {
                if (nearestCrystal.validateInteractable()) {
                    if (getInventoryPopulation() > 1) {
                        int sleepTime = randomSleeping(0, 6400);
                        status = "Sleeping for " + sleepTime + "ms";
                        ctx.viewport.turnTo(nearestCrystal);
                        ctx.sleep(sleepTime);
                    }
                    status = "Mining amethyst crystals";
                    nearestCrystal.click("Mine", "Crystals");
                    specialDone = false;
                    ctx.onCondition(() -> ctx.players.getLocal().isAnimating(), 5000);
                    return;
                } else {
                    ctx.log("No suitable crystals found nearby.");
                    return;
                }
            }
        }
    }*/

    private void miningTask() {
        final SimplePlayer localPlayer = ctx.players.getLocal();

        if (bankArea.containsPoint(ctx.players.getLocal().getLocation()) && !ctx.pathing.inMotion()) {
            status = "Going to mining area";
            takingStepsRMining();
        }

        SimpleObjects nearestSpot = (SimpleObjects) ctx.objects.populate().filter(objectName);
        ctx.log("Looking for " + objectName.toLowerCase() + " mining spots...");

        while (!nearestSpot.isEmpty()) {
            SimpleObject nearestObject = nearestSpot.filterHasAction(actionName).nearest().next();
            WorldPoint theTreeLocation = nearestObject.getLocation();
            boolean isOtherPlayerMining = !ctx.players.populate().filterWithin(theTreeLocation, 2).filter(otherPlayer -> !otherPlayer.getName().equals(ctx.players.getLocal().getName())).isEmpty();

            if (nearestSpot.size() >= 2 && isOtherPlayerMining) {
                ctx.log("Another player is mining the nearest " + objectName.toLowerCase() + ".");
                ctx.log("Looking for another spot...");
                nearestSpot = (SimpleObjects) nearestSpot.filter(other -> !other.equals(nearestObject));
                continue;
            } else {
                if (nearestObject.validateInteractable()) {
                    WorldPoint objectLocation = nearestObject.getLocation();
                    boolean objectReachable = isObjectReachable(objectLocation);

                    if (objectReachable) {
                        ctx.log(objectName + " found " + (objectLocation.distanceTo(ctx.players.getLocal().getLocation())) + " tile(s) away");
                        nearestObject.menuAction(actionName);
                        status = "Mining " + objectName.toLowerCase();
                        ctx.log(status);
                        ctx.onCondition(localPlayer::isAnimating, 250, 10);
                        return;
                    } else {
                        ctx.log("Next " + objectName.toLowerCase() + " spot is not reachable.");
                        nearestSpot = (SimpleObjects) nearestSpot.filter(otherTree -> !otherTree.equals(nearestObject));
                    }
                } else {
                    ctx.log("No " + objectName.toLowerCase() + " found in the vicinity.");
                    return;
                }
            }
        }
        ctx.log("No suitable " + objectName.toLowerCase() + " spot found nearby.");
    }

    private boolean isObjectReachable(WorldPoint objectLocation) {
        int[] offsets = { 0, 1, -1}; // Adjust these offsets as needed
        for (int offsetX : offsets) {
            for (int offsetY : offsets) {
                WorldPoint offsetLocation = new WorldPoint(objectLocation.getX() + offsetX, objectLocation.getY() + offsetY, objectLocation.getPlane());
                if (ctx.pathing.reachable(offsetLocation)) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean comradesInArea() {
        SimplePlayerQuery<SimplePlayer> comrades = ctx.players.populate().filter(this.names);
        comradesInt = 0;
        if(!comrades.isEmpty()) {
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

        this.ctx.updateStatus("-------------- " + currentTime() + " --------------");
        this.ctx.updateStatus("----------------------");
        this.ctx.updateStatus("Thank You & Good Luck!");
        this.ctx.updateStatus("----------------------");
    }

    @Override
    public void onChatMessage(ChatMessage m) {
        playerGameName = getPlayerName();
        String formattedMessage = m.getFormattedMessage();
        ChatMessageType getType = m.getType();
        net.runelite.api.events.ChatMessage getEvent = m.getChatEvent();
        String senderName = getEvent.getName();
        String gameMessage = getEvent.getMessage();

        if (m.getMessage() == null) {
            return;
        }

        //eAutoResponser.handleGptMessages(getType, senderName, formattedMessage);
        eTriviaInfo.handleBroadcastMessage(getType, gameMessage);

        if (getType == ChatMessageType.PUBLICCHAT) {

            // Remove any text within angle brackets and trim
            senderName = senderName.replaceAll("<[^>]+>", "").trim();

            if (senderName.contains(playerGameName) && !getEvent.getMessage().toLowerCase().contains("smashing")) {
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
        long currentSkillLevel = this.ctx.skills.realLevel(CHOSEN_SKILL);
        long currentSkillExp = this.ctx.skills.experience(CHOSEN_SKILL);
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
            g.drawString("Skill Level: " + currentSkillLevel + " (+" + skillLevelsGained + "), started at " + this.startingSkillLevel, 15, 165);
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