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

    // Forest area
    private final WorldArea forestArea = new WorldArea(new WorldPoint(2586, 4788, 0), new WorldPoint(2618, 4762, 0));

    // NPC IDs
    private final int FREAKY_FORESTER = 372;
    private final int ONE_TAILED_PHEASANT = 373;
    private final int TWO_TAILED_PHEASANT = 5500;
    private final int THREE_TAILED_PHEASANT = 374;
    private final int FOUR_TAILED_PHEASANT = 5502;

    // Pheasant meat IDs
    private final int ONE_TAILED_PHEASANT_MEAT = 6178;
    private final int TWO_TAILED_PHEASANT_MEAT = 6179;
    private final int THREE_TAILED_PHEASANT_MEAT = 11704;
    private final int FOUR_TAILED_PHEASANT_MEAT = 28890;

    // Dialogue answers
    public static int answerPheasantToKill;
    public static int answerDropId;
    public static int droppedItemId;

    public eRandomEventForester(ClientContext ctx) {
        super(ctx);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean condition() { // Condition to run this task
        return ctx.pathing.inArea(forestArea);
    }

    @Override
    public void run() {

        SimpleWidget foresterText = ctx.widgets.getWidget(231, 4);
        SimpleWidget playerText = ctx.widgets.getWidget(217, 4);
        if (ctx.dialogue.dialogueOpen()) {
            // Constants
            int SPACE_BUTTON = KeyEvent.VK_SPACE;
            if (foresterText != null && !foresterText.isHidden()) {

                if (foresterText.getText().toLowerCase().contains("meat of a pheasant with one")) {
                    answerPheasantToKill = ONE_TAILED_PHEASANT;
                    answerDropId = ONE_TAILED_PHEASANT_MEAT;
                    ctx.updateStatus("Task acquired: one tail");
                    taskAquired = true;
                    ctx.keyboard.clickKey(SPACE_BUTTON);
                } else if (foresterText.getText().toLowerCase().contains("meat of a pheasant with two")) {
                    answerPheasantToKill = TWO_TAILED_PHEASANT;
                    answerDropId = TWO_TAILED_PHEASANT_MEAT;
                    ctx.updateStatus("Task acquired: two tails");
                    taskAquired = true;
                    ctx.keyboard.clickKey(SPACE_BUTTON);
                } else if (foresterText.getText().toLowerCase().contains("meat of a pheasant with three")) {
                    answerPheasantToKill = THREE_TAILED_PHEASANT;
                    answerDropId = THREE_TAILED_PHEASANT_MEAT;
                    ctx.updateStatus("Task acquired: three tails");
                    taskAquired = true;
                    ctx.keyboard.clickKey(SPACE_BUTTON);
                } else if (foresterText.getText().toLowerCase().contains("meat of a pheasant with four")) {
                    answerPheasantToKill = FOUR_TAILED_PHEASANT;
                    answerDropId = FOUR_TAILED_PHEASANT_MEAT;
                    ctx.updateStatus("Task acquired: four tails");
                    taskAquired = true;
                    ctx.keyboard.clickKey(SPACE_BUTTON);
                } else if (foresterText.getText().toLowerCase().contains("well done")) {
                    finished = true;
                    ctx.updateStatus("Task finished");
                    ctx.keyboard.clickKey(SPACE_BUTTON);
                } else {
                    ctx.updateStatus("Processing dialogue");
                    ctx.keyboard.clickKey(SPACE_BUTTON);
                }
            }

            if (playerText != null && !playerText.isHidden()) {
                ctx.updateStatus("Processing dialogue");
                ctx.keyboard.clickKey(SPACE_BUTTON);
            }
        }

        if (!taskAquired && !finished && !ctx.dialogue.dialogueOpen()) {
            SimpleNpc theForester = ctx.npcs.populate().filter(FREAKY_FORESTER).next();
            if (theForester != null && theForester.validateInteractable()) {
                theForester.click("Talk-to");
                ctx.onCondition(() -> ctx.dialogue.dialogueOpen(), 3000);
            }
        }

        if (taskAquired && !finished && !ctx.dialogue.dialogueOpen()) {

            SimpleNpc taskPheasant = ctx.npcs.populate().filter(answerPheasantToKill).nearest().next();
            SimpleNpc theForester = ctx.npcs.populate().filter(FREAKY_FORESTER).next();
            SimpleItem taskPheasantMeat = ctx.inventory.populate().filter(answerDropId).next();
            if (taskPheasantMeat != null && taskPheasantMeat.validateInteractable() && !ctx.dialogue.dialogueOpen()) {
                ctx.updateStatus("Pheasant meat is in inventory");
                if (theForester != null && theForester.validateInteractable()) {
                    theForester.click("Talk-to");
                    ctx.onCondition(() -> ctx.dialogue.dialogueOpen(), 250, 10);
                    taskAquired = false;
                }
            } else {

                if (taskPheasantMeat == null) {
                    if (taskPheasant != null && taskPheasant.validateInteractable() && ctx.groundItems.populate().filter(answerDropId).isEmpty()) {
                        taskPheasant.click("Kill");
                        ctx.onCondition(() -> !ctx.groundItems.populate().filter(answerDropId).isEmpty(), 250, 10);
                    }
                }

                if (!ctx.groundItems.populate().filter(answerDropId).isEmpty()) {
                    if (!ctx.inventory.inventoryFull()) {
                        SimpleGroundItem droppedMeat = ctx.groundItems.populate().filter(answerDropId).next();
                        if (droppedMeat != null && droppedMeat.validateInteractable()) {
                            ctx.updateStatus("Picking up pheasant meat");
                            droppedMeat.click("Take");
                            ctx.sleepCondition(() -> !ctx.inventory.populate().filter(answerDropId).isEmpty(), 3000);
                        }
                    } else {
                        freeUpSlots();
                    }
                }
            }
        }

        if (finished) {
            if (droppedItemId != -1) {
                if (ctx.inventory.inventoryFull()) {
                    SimpleItem taskPheasantMeat = ctx.inventory.populate().filter(answerDropId).next();
                    if (taskPheasantMeat != null && taskPheasantMeat.validateInteractable()) {
                        taskPheasantMeat.click("Drop");
                        ctx.sleepCondition(() -> ctx.inventory.populate().filter(answerDropId).isEmpty(), 3000);
                    }

                } else {

                    SimpleGroundItem droppedItemEarlier = ctx.groundItems.populate().filter(droppedItemId).next();
                    if (droppedItemEarlier != null && droppedItemEarlier.validateInteractable()) {
                        droppedItemEarlier.click("Take");
                        int cached = ctx.inventory.populate().filter(droppedItemId).population();
                        ctx.sleepCondition(() -> ctx.inventory.populate().filter(droppedItemId).population() > cached, 10000);
                        ctx.updateStatus("Dropped item id: " + droppedItemId + " picked up.");
                    }
                    droppedItemId = -1;
                }
            }

            SimpleObject finishedPortal = ctx.objects.populate().filter(20843).next(); // Exit portal
            if (finishedPortal != null && finishedPortal.validateInteractable() && droppedItemId == -1) {
                finishedPortal.click("Use");
                ctx.sleepCondition(() -> !ctx.players.getLocal().isAnimating(), 5000);
            }
        }
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
        ctx.updateStatus("Dropped item id: " + droppedItemId + " saved.");
    }

    @Override
    public String status() {
        return "eRandom Event Solver Started";
    }

}