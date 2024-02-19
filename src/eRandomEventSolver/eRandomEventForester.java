package eRandomEventSolver;

import net.runelite.api.coords.WorldPoint;
import simple.hooks.scripts.task.Task;
import simple.hooks.wrappers.*;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

import java.awt.event.KeyEvent;

public class eRandomEventForester extends Task {

    // Variables
    public static boolean finished;
    public static boolean taskAquired;
    public static int answerPheasantToKill;
    public static int answerDropId;
    public static int droppedItemId;

    static {
        // Initializing the variables
        finished = false;
        taskAquired = false;
        answerPheasantToKill = -1;
        answerDropId = -1;
        droppedItemId = -1;
    }

    // Constants

    public static final int SPACE_BUTTON = KeyEvent.VK_SPACE;

    // Forest area
    public static final WorldArea forestArea = new WorldArea(new WorldPoint(2586, 4788, 0), new WorldPoint(2618, 4762, 0));

    // NPC IDs
    private static final int FREAKY_FORESTER = 372;
    private static final int ONE_TAILED_PHEASANT = 373;
    private static final int TWO_TAILED_PHEASANT = 5500;
    private static final int THREE_TAILED_PHEASANT = 374;
    private static final int FOUR_TAILED_PHEASANT = 5502;

    // Pheasant meat IDs
    private static final int ONE_TAILED_PHEASANT_MEAT = 6178;
    private static final int TWO_TAILED_PHEASANT_MEAT = 6179;
    private static final int THREE_TAILED_PHEASANT_MEAT = 11704;
    private static final int FOUR_TAILED_PHEASANT_MEAT = 28890;

    public eRandomEventForester(ClientContext ctx) {
        super(ctx);
    }

    @Override
    public boolean condition() { // Condition to run this task
        return ctx.pathing.inArea(forestArea);
    }

    @Override
    public void run() {
        processDialogue();

        if (!taskAquired && !finished) {
            talkToForester();
        }

        if (taskAquired && !finished) {
            handleTask();
        }

        if (finished) {
            handleFinishing();
        }
    }

    private void processDialogue() {
        SimpleWidget foresterText = ctx.widgets.getWidget(231, 4);
        SimpleWidget playerText = ctx.widgets.getWidget(217, 4);
        if (ctx.dialogue.dialogueOpen()) {
            if (foresterText != null && !foresterText.isHidden()) {
                processForesterDialogue(foresterText);
            }

            if (playerText != null && !playerText.isHidden()) {
                updateStatus("Processing dialogue");
                ctx.keyboard.clickKey(SPACE_BUTTON);
            }
        }
    }

    private void processForesterDialogue(SimpleWidget foresterText) {
        String lowerCaseText = foresterText.getText().toLowerCase();
        if (lowerCaseText.contains("meat of a pheasant with one")) {
            updatingTask(ONE_TAILED_PHEASANT, ONE_TAILED_PHEASANT_MEAT, "Task acquired: one tail");
        } else if (lowerCaseText.contains("meat of a pheasant with two")) {
            updatingTask(TWO_TAILED_PHEASANT, TWO_TAILED_PHEASANT_MEAT, "Task acquired: two tails");
        } else if (lowerCaseText.contains("meat of a pheasant with three")) {
            updatingTask(THREE_TAILED_PHEASANT, THREE_TAILED_PHEASANT_MEAT, "Task acquired: three tails");
        } else if (lowerCaseText.contains("meat of a pheasant with four")) {
            updatingTask(FOUR_TAILED_PHEASANT, FOUR_TAILED_PHEASANT_MEAT, "Task acquired: four tails");
        } else if (lowerCaseText.contains("well done")) {
            finished = true;
            updateStatus("Task finished");
            ctx.keyboard.clickKey(SPACE_BUTTON);
        } else {
            updateStatus("Processing dialogue");
            ctx.keyboard.clickKey(SPACE_BUTTON);
        }
    }

    private void updatingTask(int pheasantToKill, int dropId, String status) {
        answerPheasantToKill = pheasantToKill;
        answerDropId = dropId;
        updateStatus(status);
        taskAquired = true;
        ctx.keyboard.clickKey(SPACE_BUTTON);
    }

    private void talkToForester() {
        if (!ctx.dialogue.dialogueOpen()) {
            SimpleNpc theForester = ctx.npcs.populate().filter(FREAKY_FORESTER).next();
            if (theForester != null && theForester.validateInteractable()) {
                theForester.click("Talk-to");
                ctx.onCondition(() -> ctx.dialogue.dialogueOpen(), 3000);
            }
        }
    }

    private void handleTask() {
        if (!ctx.dialogue.dialogueOpen()) {
            SimpleNpc taskPheasant = ctx.npcs.populate().filter(answerPheasantToKill).nearest().next();
            SimpleNpc theForester = ctx.npcs.populate().filter(FREAKY_FORESTER).next();
            SimpleItem taskPheasantMeat = ctx.inventory.populate().filter(answerDropId).next();
            if (taskPheasantMeat != null && taskPheasantMeat.validateInteractable()) {
                handleTaskCompletion(theForester);
            } else {
                handlePheasantKillAndPickup(taskPheasant);
            }
        }
    }

    private void handleTaskCompletion(SimpleNpc theForester) {
        updateStatus("Pheasant meat is in inventory");
        if (theForester != null && theForester.validateInteractable()) {
            theForester.click("Talk-to");
            ctx.onCondition(() -> ctx.dialogue.dialogueOpen(), 250, 10);
            taskAquired = false;
        }
    }

    private void handlePheasantKillAndPickup(SimpleNpc taskPheasant) {
        if (ctx.groundItems.populate().filter(answerDropId).isEmpty()) {
            killingPheasant(taskPheasant);
        } else {
            pickupDroppedMeat();
        }
    }

    private void killingPheasant(SimpleNpc taskPheasant) {
        if (taskPheasant != null && taskPheasant.validateInteractable()) {
            taskPheasant.click("Kill");
            ctx.onCondition(() -> !ctx.groundItems.populate().filter(answerDropId).isEmpty(), 250, 10);
        }
    }

    private void pickupDroppedMeat() {
        if (!ctx.inventory.inventoryFull()) {
            SimpleGroundItem droppedMeat = ctx.groundItems.populate().filter(answerDropId).next();
            if (droppedMeat != null && droppedMeat.validateInteractable()) {
                updateStatus("Picking up pheasant meat");
                droppedMeat.click("Take");
                ctx.sleepCondition(() -> !ctx.inventory.populate().filter(answerDropId).isEmpty(), 3000);
            }
        } else {
            freeUpSlots();
        }
    }

    private void handleFinishing() {
        if (droppedItemId != -1) {
            handleDroppedItem();
        }

        SimpleObject finishedPortal = ctx.objects.populate().filter(20843).next(); // Exit portal
        if (finishedPortal != null && finishedPortal.validateInteractable() && droppedItemId == -1) {
            finishedPortal.click("Use");
            ctx.sleepCondition(() -> !ctx.players.getLocal().isAnimating(), 5000);
            if (ctx.players.getLocal().isAnimating()) { // 2110 animation
                resetTaskVariables();
            }
        }
    }

    private void handleDroppedItem() {
        if (ctx.inventory.inventoryFull()) {
            dropTaskPheasantMeat();
        } else {
            pickUpDroppedItem();
        }
    }

    private void dropTaskPheasantMeat() {
        SimpleItem taskPheasantMeat = ctx.inventory.populate().filter(answerDropId).next();
        if (taskPheasantMeat != null && taskPheasantMeat.validateInteractable()) {
            taskPheasantMeat.click("Drop");
            ctx.sleepCondition(() -> ctx.inventory.populate().filter(answerDropId).isEmpty(), 3000);
        }
    }

    private void pickUpDroppedItem() {
        SimpleGroundItem droppedItemEarlier = ctx.groundItems.populate().filter(droppedItemId).next();
        if (droppedItemEarlier != null && droppedItemEarlier.validateInteractable()) {
            droppedItemEarlier.click("Take");
            int cached = ctx.inventory.populate().filter(droppedItemId).population();
            ctx.sleepCondition(() -> ctx.inventory.populate().filter(droppedItemId).population() > cached, 10000);
            updateStatus("Dropped item id: " + droppedItemId + " picked up.");
            droppedItemId = -1;
        }
    }

    private void resetTaskVariables() {
        finished = false;
        taskAquired = false;
        answerPheasantToKill = -1;
        answerDropId = -1;
        droppedItemId = -1;
    }

    private void freeUpSlots() {
        if (droppedItemId != -1) {
            return;
        }

        SimpleItem droppingItem = ctx.inventory.populate().filterHasAction("Drop").next();
        if (droppingItem == null || !droppingItem.validateInteractable()) {
            return;
        }

        ctx.inventory.dropItem(droppingItem);
        droppedItemId = droppingItem.getId();
        updateStatus("Dropped item id: " + droppingItem + " saved.");
    }

    public static boolean inForesterRandom(ClientContext ctx) {
        return ctx.pathing.inArea(forestArea);
    }

    private void updateStatus(String newStatus) {
        String status = newStatus;
        ctx.updateStatus(status);
        System.out.println(status);
    }

    @Override
    public String status() {
        return "eRandom Event Solver Started";
    }
}
