package eFreakyForesterSolver;

import net.runelite.api.coords.WorldPoint;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.*;
import simple.robot.script.Script;
import simple.robot.utils.WorldArea;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


@ScriptManifest(author = "Esmaabi", category = Category.UTILITY, description = " "
        + "Will solve <b>Freaky Forester</b> random event for you!</b><br>", discord = "Esmaabi#5752",
        name = "eFreakyForesterSolverZenyte", servers = { "Zenyte" }, version = "0.1")

public class eFreakyForesterMain extends Script {


    //Vars
    private static boolean finished;
    private static boolean taskAquired;
    static int space = KeyEvent.VK_SPACE;


    //Forest area
    private final WorldArea forestArea = new WorldArea(new WorldPoint(2586, 4788, 0), new WorldPoint(2618, 4762, 0));


    //NPC
    private final int freakyForester = 372;
    private final int oneTailedPheasant = 373;
    private final int twoTailedPheasant = 5500;
    private final int threeTailedPheasant = 374;
    private final int fourTailedPheasant = 5502;

    //pheasant meat
    private final int oneTailedPheasantMeat = 6178;
    private final int twoTailedPheasantMeat  = 6179;
    private final int threeTailedPheasantMeat  = 11704;
    private final int fourTailedPheasantMeat  = 28890;

    //DialogueAnswer
    private int answerWithTails;
    private int answerDrop;
    private int droppedItem;

    @Override
    public void onExecute() {
        System.out.println("Started eFreakyForesterSolver for Zenyte!");
        ctx.viewport.angle(180);
        ctx.viewport.pitch(true);

        ctx.updateStatus("------------------------------------");
        ctx.updateStatus("     eFreakyForesterSolver started   ");
        ctx.updateStatus("------------ Time " + currentTime() + " -------------");
        ctx.updateStatus("------------------------------------");

        //vars
        finished = false;
        taskAquired = false;
        answerWithTails = -1;
        answerDrop = -1;
        droppedItem = -1;
    }

    @Override
    public void onProcess() {

        if (ctx.pathing.inArea(forestArea)) {

            SimpleWidget foresterText = ctx.widgets.getWidget(231, 4);
            SimpleWidget playerText = ctx.widgets.getWidget(217, 4);
            if (ctx.dialogue.dialogueOpen()) {
                if (foresterText != null && !foresterText.isHidden()) {

                    if (foresterText.getText().toLowerCase().contains("meat of a pheasant with one")) {
                        answerWithTails = oneTailedPheasant;
                        answerDrop = oneTailedPheasantMeat;
                        ctx.updateStatus("Task acquired: one tail");
                        taskAquired = true;
                        ctx.keyboard.clickKey(space);
                    } else if (foresterText.getText().toLowerCase().contains("meat of a pheasant with two")) {
                        answerWithTails = twoTailedPheasant;
                        answerDrop = twoTailedPheasantMeat;
                        ctx.updateStatus("Task acquired: two tails");
                        taskAquired = true;
                        ctx.keyboard.clickKey(space);
                    } else if (foresterText.getText().toLowerCase().contains("meat of a pheasant with three")) {
                        answerWithTails = threeTailedPheasant;
                        answerDrop = threeTailedPheasantMeat;
                        ctx.updateStatus("Task acquired: three tails");
                        taskAquired = true;
                        ctx.keyboard.clickKey(space);
                    } else if (foresterText.getText().toLowerCase().contains("meat of a pheasant with four")) {
                        answerWithTails = fourTailedPheasant;
                        answerDrop = fourTailedPheasantMeat;
                        ctx.updateStatus("Task acquired: four tails");
                        taskAquired = true;
                        ctx.keyboard.clickKey(space);
                    } else if (foresterText.getText().toLowerCase().contains("well done")) {
                        finished = true;
                        ctx.updateStatus("Task finished");
                        ctx.keyboard.clickKey(space);
                    } else {
                        ctx.updateStatus("Processing dialogue");
                        ctx.keyboard.clickKey(space);
                    }
                }

                if (playerText != null && !playerText.isHidden()) {
                    ctx.updateStatus("Processing dialogue");
                    ctx.keyboard.clickKey(space);
                }
            }

            if (!taskAquired && !finished && !ctx.dialogue.dialogueOpen()) {
                SimpleNpc theForester = ctx.npcs.populate().filter(freakyForester).next();
                if (theForester != null && theForester.validateInteractable()) {
                    theForester.click("Talk-to");
                    ctx.onCondition(() -> ctx.dialogue.dialogueOpen(), 3000);
                }
            }

            if (taskAquired && !finished && !ctx.dialogue.dialogueOpen()) {

                SimpleNpc taskPheasant = ctx.npcs.populate().filter(answerWithTails).nearest().next();
                SimpleNpc theForester = ctx.npcs.populate().filter(freakyForester).next();
                SimpleItem taskPheasantMeat = ctx.inventory.populate().filter(answerDrop).next();
                if (taskPheasantMeat != null && taskPheasantMeat.validateInteractable() && !ctx.dialogue.dialogueOpen()) {
                    ctx.updateStatus("Pheasant meat is in inventory");
                    if (theForester != null && theForester.validateInteractable()) {
                        theForester.click("Talk-to");
                        ctx.onCondition(() -> ctx.dialogue.dialogueOpen(), 250, 10);
                        taskAquired = false;
                    }
                } else {

                    if (taskPheasantMeat == null) {
                        if (taskPheasant != null && taskPheasant.validateInteractable() && ctx.groundItems.populate().filter(answerDrop).isEmpty()) {
                            taskPheasant.click("Kill");
                            ctx.onCondition(() -> !ctx.groundItems.populate().filter(answerDrop).isEmpty(), 250, 10);
                        }
                    }

                    if (!ctx.groundItems.populate().filter(answerDrop).isEmpty()) {
                        if (!ctx.inventory.inventoryFull()) {
                            SimpleGroundItem droppedMeat = ctx.groundItems.populate().filter(answerDrop).next();
                            if (droppedMeat != null && droppedMeat.validateInteractable()) {
                                ctx.updateStatus("Picking up pheasant meat");
                                droppedMeat.click("Take");
                                ctx.sleepCondition(() -> !ctx.inventory.populate().filter(answerDrop).isEmpty(), 3000);
                            }
                        } else {
                            freeUpSlots();
                        }
                    }
                }
            }

            if (finished) {
                if (droppedItem != -1) {
                    if (ctx.inventory.inventoryFull()) {
                        SimpleItem taskPheasantMeat = ctx.inventory.populate().filter(answerDrop).next();
                        if (taskPheasantMeat != null && taskPheasantMeat.validateInteractable()) {
                            taskPheasantMeat.click("Drop");
                            ctx.sleepCondition(() -> ctx.inventory.populate().filter(answerDrop).isEmpty(), 3000);
                        }

                    } else {

                        SimpleGroundItem droppedItemEarlier = ctx.groundItems.populate().filter(droppedItem).next();
                        if (droppedItemEarlier != null && droppedItemEarlier.validateInteractable()) {
                            droppedItemEarlier.click("Take");
                            int cached = ctx.inventory.populate().filter(droppedItem).population();
                            ctx.sleepCondition(() -> ctx.inventory.populate().filter(droppedItem).population() > cached, 10000);
                            ctx.updateStatus("Dropped item id: " + droppedItem + " picked up.");
                        }
                        droppedItem = -1;
                    }
                }

                SimpleObject finishedPortal = ctx.objects.populate().filter(20843).next(); // Exit portal
                if (finishedPortal != null && finishedPortal.validateInteractable() && droppedItem == -1) {
                    finishedPortal.click("Use");
                    ctx.sleepCondition(() -> !ctx.players.getLocal().isAnimating(), 5000);
                }
            }

        } else {
            ctx.stopScript();
        }
    }

    private void freeUpSlots() {
        if (droppedItem != -1) {
            return;
        }

        SimpleItem droppingItem = ctx.inventory.populate().filterHasAction("Drop").next();
        if (droppingItem == null || !droppingItem.validateInteractable()) {
            return;
        }

        ctx.inventory.dropItem(droppingItem);
        droppedItem = droppingItem.getId();
        ctx.updateStatus("Dropped item id: " + droppedItem + " saved.");
    }

    public static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    @Override
    public void onTerminate() {
        //vars
        taskAquired = false;
        finished = false;
        answerWithTails = -1;
        answerDrop = -1;

        ctx.updateStatus("------------------------------------");
        ctx.updateStatus("    eFreakyForesterSolver finished   ");
        ctx.updateStatus("------------ Time " + currentTime() + " -------------");
        ctx.updateStatus("------------------------------------");

    }

    @Override
    public void onChatMessage(ChatMessage m) {
    }

    @Override
    public void paint(Graphics g) {
    }

}

