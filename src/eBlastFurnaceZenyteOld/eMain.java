package eBlastFurnaceZenyteOld;

import Utility.Trivia.eTriviaInfo;
import eApiAccess.eAutoResponderGui;
import eApiAccess.eAutoResponser;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleBank;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static eApiAccess.eAutoResponser.*;

@ScriptManifest(author = "Esmaabi", category = Category.MINIGAMES, description =
                "<br>The most effective Blast Furnace bot on Zenyte!<br>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "Start <b>at Blast Furnace with ores in inventory</b>.<br>"
                + "Make sure that ores are visible in bank.<br> "
                + "If you have stamina potions in inventory bot will use them.<br>"
                + "Chat GPT answering is integrated.",
        discord = "Esmaabi#5752",
        name = "eBlastFurnaceBotOld", servers = { "Zenyte" }, version = "1")

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    public static eAutoResponderGui guiGpt;
    private static final Logger logger = Logger.getLogger(eMain.class.getName());
    private static final SimpleSkills.Skills chosenSkill = SimpleSkills.Skills.SMITHING;
    private final static int INVENTORY_BAG_WIDGET_ID = 548;
    private final static int INVENTORY_BAG_CHILD_ID = 58;
    private final WorldPoint BAR_DISPENSER_LOCATION = new WorldPoint(1940, 4963, 0);
    private final WorldPoint CONVAYOR_BELT_LOCATION = new WorldPoint(1943, 4967, 0);
    private final WorldPoint NEAR_CONVAYOR_BELT_LOCATION = new WorldPoint(1942, 4967, 0);
    private static final String[] BANK_NAME = {"Bank booth", "Bank chest", "Bank counter"};
    private static final String[] BANKER_NAME = {"Banker","Bird's-Eye' Jack", "Arnold Lydspor", "Banker tutor", "Cornelius", "Emerald Benedict", "Eniola", "Fadli", "Financial Wizard", "Financial Seer", "Ghost banker", "Gnome banker", "Gundai", "Jade", "Jumaane", "Magnus Gram", "Nardah Banker", "Odovacar", "Peer the Seer", "Sirsal Banker", "Squire", "TzHaar-Ket-Yil", "TzHaar-Ket-Zuh", "Yusuf"};
    private static final int ENHANCED_ICE_GLOVES = 30030;
    private static final int GOLDSMITH_GAUNTLETS = 776;
    private static final int BAR_DISPENSER = 9092;

    // Variables related to game status
    private long startTime;
    private long startingSkillLevel;
    private long startingSkillExp;
    private int count;
    private boolean oresOnConveyor = true;
    private  boolean roeMade = false;
    private static boolean hidePaint = false;

    // Variables (other)
    private static String playerGameName;
    private static String triviaAnswer;
    private static String status;

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

        tasks.addAll(Arrays.asList(new eAutoResponser(ctx)));

        // Setting up GPT Gui
        eAutoResponser.scriptPurpose = "you're just doing smithing.";
        eAutoResponser.gptStarted = false;
        initializeGptGUI();

        // Starting message
        System.out.println("Started eBlastFurnaceBot!");
        this.ctx.log("--------------- " + getCurrentTimeFormatted() + " ---------------");
        this.ctx.log("------------------------------------");
        this.ctx.log("           eBlastFurnaceBot         ");
        this.ctx.log("------------------------------------");

        // Variables
        updateStatus("Setting up bot");
        this.startTime = System.currentTimeMillis();
        this.startingSkillLevel = this.ctx.skills.realLevel(chosenSkill);
        this.startingSkillExp = this.ctx.skills.experience(chosenSkill);
        gptIsActive = false;
        count = 0;
        oresOnConveyor = false;
        roeMade = false;

        // Viewport settings
        ctx.viewport.angle(120);
        ctx.viewport.pitch(true);
        ctx.viewport.yaw();
    }

    @Override
    public void onProcess() {
        super.onProcess();

        SimplePlayer localPlayer = ctx.players.getLocal();
        Pathing pathing = ctx.pathing;

        if (!gptStarted) {
            return;
        }

        if (!pathing.running()) {
            handleEnergy();
        }

        if (oresOnConveyor) {
            if (localPlayer.getLocation().distanceTo(BAR_DISPENSER_LOCATION) >= 2) {
                status = "Walking to bar dispenser";
                ctx.pathing.step(1940, 4964);
                ctx.onCondition(() -> ctx.pathing.inMotion(), 250, 4);
            } else {

                int barDispenserImposter = ctx.getClient().getObjectDefinition(BAR_DISPENSER).getImpostor().getId();
                if (barDispenserImposter == ObjectID.BAR_DISPENSER_9094 || barDispenserImposter == ObjectID.BAR_DISPENSER) {
                    status = "Bars not ready yet...";
                    return;

                } else {

                    SimpleObject barDispenser = ctx.objects.populate().filter(BAR_DISPENSER).next();
                    SimpleItem iceGloves = ctx.inventory.populate().filter(ENHANCED_ICE_GLOVES).next();

                    if (iceGloves != null) {
                        status = "Wearing " + iceGloves.getName().toLowerCase();
                        if (iceGloves.click("Wear")) {
                            ctx.onCondition(() -> itemNotInInventory(ENHANCED_ICE_GLOVES), 250, 5);
                        }
                    }

                    status = "Clicking bar dispenser";
                    if (barDispenser.click("Take", "Bar dispenser")) {
                        ctx.onCondition(() -> widgetVisible(270, 14), 250, 6);
                    }

                    if (widgetVisible(270, 14)) {
                        clickWidget(270, 14);
                        ctx.onCondition(() -> !itemNotInInventory(ItemID.GOLD_BAR), 250, 5);
                    }

                    if (!itemNotInInventory(ItemID.GOLD_BAR)) {
                        oresOnConveyor = false;
                    }
                }
            }
        }

        if (!itemNotInInventory(ItemID.GOLD_BAR) && !oresOnConveyor) {
            bankTask();
        }

        if (!itemNotInInventory(ItemID.GOLD_ORE) && !oresOnConveyor) {
            SimpleObject conveyorBelt = ctx.objects.populate().filter("Conveyor belt").filterHasAction("Put-ore-on").nearest(CONVAYOR_BELT_LOCATION).next();
            SimpleItem goldsmithingGloves = ctx.inventory.populate().filter(GOLDSMITH_GAUNTLETS).next();
            ctx.pathing.step(NEAR_CONVAYOR_BELT_LOCATION);
            if (conveyorBelt != null && conveyorBelt.validateInteractable()) {
                if (goldsmithingGloves != null) {
                    status = "Wearing " + goldsmithingGloves.getName().toLowerCase();
                    if (goldsmithingGloves.click("Wear")) {
                        ctx.onCondition(() -> itemNotInInventory(GOLDSMITH_GAUNTLETS), 250, 5);
                    }
                }
                status = "Clicking " + conveyorBelt.getName().toLowerCase();
                if (conveyorBelt.click("Put-ore-on")) {
                    ctx.onCondition(() -> itemNotInInventory(ItemID.GOLD_ORE), 250, 5);
                }
            }
        }
    }

    // Banking
    private void bankTask() {
        int ENERGY_THRESHOLD = 30;
        int[] EXCLUDE_DEPOSIT_ITEMS = {ENHANCED_ICE_GLOVES, GOLDSMITH_GAUNTLETS, 12625, 12627, 12629, 12631};
        final Pattern STAMINA_POTION_PATTERN = Pattern.compile("Stamina potion\\(\\d+\\)");


        if (ctx.bank.bankOpen()) {
            int goldBars = ctx.inventory.populate().filter(ItemID.GOLD_BAR).population();

            updateStatus("Depositing items");
            ctx.bank.depositAllExcept(EXCLUDE_DEPOSIT_ITEMS);

            updateStatus("Withdrawing gold ores");
            count += goldBars;
            if (ctx.pathing.energyLevel() <= ENERGY_THRESHOLD && ctx.inventory.populate().filter(STAMINA_POTION_PATTERN).isEmpty()) {
                withdrawItem(12625); // Stamina potion(4)
            }
            if (ctx.bank.withdraw(ItemID.GOLD_ORE, SimpleBank.Amount.ALL)) {
                ctx.onCondition(() -> !itemNotInInventory(ItemID.GOLD_ORE), 250, 8);
            }

            updateStatus("Closing bank");
            ctx.bank.closeBank();
            return;
        }

        if (!ctx.bank.bankOpen()) {
            gettingClosestBank();
        }
    }

    private void gettingClosestBank() {
        String message = "Opening bank";
        SimpleObject bankChest = getBankChest();
        SimpleNpc bankerName = getBanker();
        if (bankChest != null &&
                (bankerName == null || bankChest.distanceTo(ctx.players.getLocal()) <= bankerName.distanceTo(ctx.players.getLocal()))) {
            updateStatus(message);
            bankChest.click(1);
            ctx.onCondition(() -> ctx.bank.bankOpen(), 250 , 20);
        } else if (bankerName != null &&
                (bankChest == null || bankerName.distanceTo(ctx.players.getLocal()) <= bankChest.distanceTo(ctx.players.getLocal()))) {
            updateStatus(message);
            bankerName.click("Bank");
            ctx.onCondition(() -> ctx.bank.bankOpen(), 250 , 20);
        }
    }

    private SimpleObject getBankChest() {
        SimpleObject bankChest = ctx.objects.populate().filter(BANK_NAME).nearest().next();
        if (bankChest != null && bankChest.validateInteractable()) {
            return bankChest;
        }
        return null;
    }

    private SimpleNpc getBanker() {
        SimpleNpc bankerName = ctx.npcs.populate().filter(BANKER_NAME).nearest().next();
        if (bankerName != null && bankerName.validateInteractable()) {
            return bankerName;
        }
        return null;
    }

    // bankOpen utility
    private void withdrawItem(int ID) {
        SimpleWidget quantityOne = ctx.widgets.getWidget(12, 29);
        if (quantityOne != null && !quantityOne.isHidden()) {
            quantityOne.click(0);
        }
        ctx.bank.withdraw(ID, SimpleBank.Amount.ONE);
        clearBankSearch();
    }

    private void clearBankSearch() {
        SimpleWidget searchButton = ctx.widgets.getWidget(12, 40);
        if (searchButton != null && !searchButton.isHidden()) {
            searchButton.click(0);
        }
    }

    private void clickWidget(int wigetId, int childId) {
        SimpleWidget widgetToClick = ctx.widgets.getWidget(wigetId, childId);
        if (widgetToClick != null && !widgetToClick.isHidden()) {
            widgetToClick.click(0);
        }
    }

    private boolean widgetVisible(int wigetId, int childId) {
        SimpleWidget widgetToClick = ctx.widgets.getWidget(wigetId, childId);
        if (widgetToClick != null && !widgetToClick.isHidden()) {
            widgetToClick.click(0);
            return true;
        }
        return false;
    }

    private void clickOnBag() {
        SimpleWidget inventoryBagWidget = ctx.widgets.getWidget(INVENTORY_BAG_WIDGET_ID, INVENTORY_BAG_CHILD_ID);
        if (inventoryBagWidget != null) {
            inventoryBagWidget.click(0);
        }
    }

    private boolean itemNotInInventory(int itemId) {
        return ctx.inventory.populate().filter(itemId).isEmpty();
    }
    private void handleEnergy() {
        Pathing pathing = ctx.pathing;

        if (pathing.energyLevel() >= 30 && !pathing.running()) {
            pathing.running(true);
        }

        if (pathing.energyLevel() < 30) {
            final SimpleItem potion = ctx.inventory.populate().filter(Pattern.compile("Stamina potion\\(\\d+\\)")).filterHasAction("Drink").next();
            final int cached = pathing.energyLevel();
            if (potion == null) {
                return;
            }
            status = ("Drinking " + potion.getName().toLowerCase());
            if (potion != null && potion.click("Drink")) {
                ctx.onCondition(() -> pathing.energyLevel() > cached, 250, 8);
            }
        }
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

    //Utility
    public static String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void updateStatus(String newStatus) {
        status = newStatus;
        ctx.log(status);
    }

    // Trivia
    private void sendAnswer(String answer) {
        if (answer == null) {
            return;
        }

        if (ctx.dialogue.dialogueOpen()) {
            ctx.dialogue.clickContinue();
        }

        StringBuilder writeAnswer = new StringBuilder("::ans ");
        writeAnswer.append(answer);

        Thread thread = new Thread(() -> {
            try {
                int sleepTime = randomSleeping(5000, 10000);
                updateStatus(getCurrentTimeFormatted() + " [Trivia] Sleeping for " + sleepTime + "ms");
                Thread.sleep(sleepTime); // Randomized delay
                ctx.keyboard.sendKeys(writeAnswer.toString());
                triviaAnswer = null;
                updateStatus(getCurrentTimeFormatted() + " [Trivia] Question answered");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void handleTriviaQuestion(String gameMessageTrimmed) {
        for (eTriviaInfo.TriviaQuestion triviaQuestion : eTriviaInfo.TriviaQuestion.values()) {
            if (gameMessageTrimmed.contains(triviaQuestion.getQuestion())) {
                triviaAnswer = triviaQuestion.getAnswer();
                sendAnswer(triviaAnswer);
                break;
            }
        }
    }

    @Override
    public void onTerminate() {

        // Termination message
        ctx.log("-------------- " + getCurrentTimeFormatted() + " --------------");
        ctx.log("You have made: " + count + " gold bars");
        ctx.log("-----------------------------------");
        ctx.log("----- Thank You & Good Luck! ------");
        ctx.log("-----------------------------------");

        // Other variables
        startingSkillLevel = 0L;
        startingSkillExp = 0L;
        count = 0;
        guiGpt.setVisible(false);
        gptStarted = false;
        messageSaved = null;

    }

    @Override
    public void onChatMessage(ChatMessage m) {
        String formattedMessage = m.getFormattedMessage();
        ChatMessageType getType = m.getType();
        net.runelite.api.events.ChatMessage getEvent = m.getChatEvent();
        playerGameName = eAutoResponser.getPlayerName(ctx);

        if (m.getMessage() == null) {
            return;
        }

/*        String eventToString = getEvent.toString().replaceAll("<[^>]+>", "").trim();;
        logger.info(eventToString); // to debug (returns chat type, text, sender)*/

        if (getType == ChatMessageType.PUBLICCHAT) {
            String senderName = getEvent.getName();

            // Remove any text within angle brackets and trim
            senderName = senderName.replaceAll("<[^>]+>", "").trim();

            if (senderName.contains(playerGameName)) {
                logger.info("You wrote: " + formattedMessage);
                return;
            }

            if (!senderName.contains(playerGameName)) {
                eAutoResponser.otherPlayerName = senderName;
                eAutoResponser.messageSaved = formattedMessage;
                logger.info("Player " + eAutoResponser.otherPlayerName + " wrote: " + eAutoResponser.messageSaved);
            }
        }

        if (getType == ChatMessageType.GAMEMESSAGE) {
            String gameMessage = getEvent.getMessage();
            if (gameMessage.contains("your ore goes onto the conveyor belt")) {
                status = "Ores are on conveyor belt!";
                oresOnConveyor = true;
            }
/*
            if (gameMessage.contains("You cut open the fish")) {
                status = "Roe has been made!";
                roeMade = true;
            }*/
        }

        if (getType == ChatMessageType.BROADCAST) {
            String broadcastMessage = getEvent.getMessage();
            String messageTrimmed = broadcastMessage.replaceAll("<[^>]+>", "").trim();
            if (messageTrimmed.contains("Trivia")) {
                logger.info(messageTrimmed);
                handleTriviaQuestion(messageTrimmed);
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
            g.drawString("eBlastFurnaceBot by Esmaabi", 15, 135);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + runTime, 15, 150);
            g.drawString("Skill Level: " + currentSkillLevel + " (+" + skillLevelsGained + "), started at " + this.startingSkillLevel, 15, 165);
            g.drawString("Current Exp: " + currentSkillExp, 15, 180);
            g.drawString("Exp gained: " + skillExpGained + " (" + (skillExpPerHour / 1000L) + "k xp/h)", 15, 195);
            g.drawString("Bars made: " + count + " (" + ctx.paint.valuePerHour(count, startTime) + " per/h)", 15, 210);
            g.drawString("Status: " + status, 15, 225);
        }
    }
}
