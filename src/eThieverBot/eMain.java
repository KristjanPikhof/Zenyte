package eThieverBot;

import BotUtils.eActions;
import BotUtils.eBanking;
import BotUtils.eData;
import BotUtils.eImpCatcher;
import Utility.Trivia.eTriviaInfo;
import eApiAccess.eAutoResponderGui;
import eApiAccess.eAutoResponser;
import net.runelite.api.ChatMessageType;

import net.runelite.api.ItemID;
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
import simple.robot.utils.WorldArea;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static eApiAccess.eAutoResponser.*;

@ScriptManifest(
        author = "Esmaabi",
        category = Category.THIEVING,
        description = "<html>"
                + "<p>The most effective thieving bot on Zenyte!</p>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "<ul>"
                + "<li>Supported areas: Ardougne, Lletya</li>"
                + "<li>To start & select target please <strong>Examine</strong> NPC</li>"
                + "<li>Supported targets: Guard, Warrior Woman, Knight of Ardougne, Paladin, Hero, Goreu</li>"
                + "<li>Bot will eat anything and bank everything.</li>"
                + "<li>You must start with food in inventory that you want to use!</li>"
                + "<li>Bot will <strong>withdraw only food that you started with!</strong></li>"
                + "<li>Chat GPT answering is integrated.</li>"
                + "</ul>"
                + "</html>",
        discord = "Esmaabi#5752",
        name = "eThieverBot",
        servers = {"Zenyte"},
        version = "0.1"
)

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private static final String eBotName = "eThieverBot";
    private static final String ePaintText = "Actions made";
    private static final SimpleSkills.Skills CHOSEN_SKILL = SimpleSkills.Skills.THIEVING;
    private static final Logger logger = Logger.getLogger(eAnglerFisherBot.eMain.class.getName());
    private final WorldArea EDGE = new WorldArea(
            new WorldPoint(3110, 3474, 0),
            new WorldPoint(3074, 3516, 0)
    );
    private static final WorldArea ARDOUGNE = new WorldArea (
            new WorldPoint(2600, 3343, 0),
            new WorldPoint(2690, 3342, 0),
            new WorldPoint(2689, 3264, 0),
            new WorldPoint(2587, 3259, 0)
    );
    private static final WorldArea LLETYA = new WorldArea (
            new WorldPoint(2314, 3196, 0),
            new WorldPoint(2359, 3145, 0)
    );

    // Variables
    private int count;
    private static eAutoResponderGui guiGpt;
    public static boolean hidePaint = false;
    private long startTime = 0L;
    private long startingSkillExp;
    private long startingSkillLevel;
    private int npcToThiev = -1;
    private int foodToEat;


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
        initializeMethods(); // BotUtils
        initializeGptGui(); // GPT
        eAutoResponser.scriptPurpose = "you're thieving for fast xp. ";
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
        getFood();
        ctx.viewport.angle(180);
        ctx.viewport.pitch(true);
        eActions.zoomOutViewport();
    }

    @Override
    public void onProcess() {
        super.onProcess();

        final Pathing pathing = ctx.pathing;
        final int playerHealth = ctx.combat.health();
        boolean inArdougne = pathing.inArea(ARDOUGNE);
        boolean inLletya = pathing.inArea(LLETYA);
        boolean inEdge = pathing.inArea(EDGE);

        if (!botStarted) {
            eActions.status = "Please start the bot!";
            return;
        }

        if (npcToThiev == -1) {
            eActions.status = "Please examine target!";
            return;
        }

        if (foodToEat == -1) {
            eActions.status = "No food found!";
            ctx.stopScript();
        }

        eActions.handleRunning();

        if (eActions.isWidgetOpen(704,1)) {
            ctx.keyboard.pressKey(KeyEvent.VK_ESCAPE);

            SimpleWidget xButton = ctx.widgets.getWidget(704, 1).getChild(13);
            if (xButton != null) {
                xButton.click(0);
                ctx.onCondition(() -> false, 200, 6);
            }
            return;
        }

        if (!inEdge && !inArdougne && !inLletya) {
            eActions.teleportHomeSpellbook();
            return;
        }

       if (inLletya) {
           if (playerHealth > 9) {
               if (ctx.players.getLocal().getGraphic() != -1) {
                   BotUtils.eActions.status = "Stunned";
                   return;
               }

               handlePouch(24);

               if (ctx.inventory.inventoryFull()) {
                   BotUtils.eActions.status = "Banking";
                   eBanking.bankTask(true, 8, -1, true, foodToEat,3, -1);
                   return;
               }

               // Handle pickpocket flow
               handlePickpocketTask(2314,3196,2359,3145);

           } else {
               if (!eatFood()) {
                   BotUtils.eActions.status = "Banking";
                   eBanking.bankTask(true, 8, -1, true, foodToEat,8, -1);
               }
           }
       }

        if (inArdougne) {
            if (playerHealth > 6) {
                if (ctx.players.getLocal().getGraphic() != -1) {
                    BotUtils.eActions.status = "Stunned";
                    return;
                }

                handlePouch(24);

                if (ctx.inventory.inventoryFull()) {
                    BotUtils.eActions.status = "Banking";
                    eBanking.bankTask(true, 8, -1, true, foodToEat,8, -1);
                    return;
                }

                // Handle pickpocket flow
                handlePickpocketTask(2587, 2690, 3259, 3343);

            } else {
                if (!eatFood()) {
                    eActions.teleportHomeSpellbook();
                }
            }
        }

        if (inEdge) {

            if (playerHealth < 10) {
                BotUtils.eActions.status = "Restoring hitpoints";
                SimpleObject healingBox = ctx.objects.populate().filter("Box of Restoration").nearest().next();
                if (healingBox != null && healingBox.validateInteractable()) {
                    BotUtils.eActions.interactWith(healingBox, "Restore");
                    ctx.onCondition(() -> false, 250, 5);
                }

            } else {
                if (!BotUtils.eActions.hasItemsInInventory(eActions.StackableType.NON_STACKABLE, foodToEat)) {
                    BotUtils.eActions.status = "Banking";
                    eBanking.bankTask(false, 8, -1, true, foodToEat,15, -1);
                    handlePouch(1);
                } else {
                    if (npcToThiev == 5297) {
                        eActions.handlePortalTeleport("Cities", "Lletya");
                    } else {
                        eActions.handlePortalTeleport("Cities", "Ardougne");
                    }
                }
            }
        }

        if (eActions.hasItemsInInventory(eActions.StackableType.NON_STACKABLE, ItemID.ROGUE_MASK, ItemID.ROGUE_TOP, ItemID.ROGUE_TROUSERS, ItemID.ROGUE_BOOTS, ItemID.ROGUE_GLOVES)) {
            BotUtils.eActions.updateStatus("Wearing Rogue piece");
            ctx.inventory.populate().filter(ItemID.ROGUE_MASK, ItemID.ROGUE_TOP, ItemID.ROGUE_TROUSERS, ItemID.ROGUE_BOOTS, ItemID.ROGUE_GLOVES).forEach((item) -> item.click("Wear"));
        }
    }

    private void handlePouch(int amount) {
        if (BotUtils.eActions.getNotedItemCount("Coin pouch") >= amount) {
            SimpleItem coinPouch = ctx.inventory.populate().filter("Coin pouch").next();
            if (coinPouch != null) {
                coinPouch.click(0);
                ctx.onCondition(() -> false, 200, 6);
            }
        }
    }

    private boolean eatFood() {
        SimpleItem eatItem = ctx.inventory.populate().filterHasAction("Eat").next();
        if (eatItem == null) return false;

        return eatItem.click("Eat");
    }

    private void openDoor(String name) {
        SimpleObject theDoor = ctx.objects.populate().filter(1535, 11720, 92).nearest().next();
        if (theDoor != null) {
            BotUtils.eActions.status = "Opening door to get to " + name;
            BotUtils.eActions.interactWith(theDoor, "Open");
            ctx.onCondition(() -> !ctx.pathing.inMotion(), 500, 10);
        } else {
            ctx.log("No doors found nearby.");
        }
    }

    private void handlePickpocketTask(int minX, int maxX, int minY, int maxY) {
        SimpleNpc nearestNpc = findNearestNpc();

        if (nearestNpc == null) {
            if (ctx.pathing.inArea(LLETYA)) {
                handleLletyaArea();
                return;
            }
            searchForNpcWithinArea(minX, maxX, minY, maxY);
        } else {
            interactWithNpc(nearestNpc);
        }
    }

    private SimpleNpc findNearestNpc() {
        return ctx.npcs.populate().filter(npcToThiev).filterHasAction("Pickpocket").nearest().next();
    }

    private void handleLletyaArea() {
        npcToThiev = (npcToThiev == 5297) ? 5300 : 5297;
        ctx.pathing.step(2337, 3160);
        ctx.onCondition(() -> false, 250, 10);
    }

    private void searchForNpcWithinArea(int minX, int maxX, int minY, int maxY) {
        WorldPoint randomLocation;
        SimpleNpc nearestNpc = null;
        eActions.status = "Searching for target";

        while (nearestNpc == null) {
            randomLocation = eActions.getRandomLocationWithinArea(minX, maxX, minY, maxY);
            ctx.pathing.step(randomLocation);
            ctx.onCondition(() -> false, 250, 15);
            nearestNpc = findNearestNpc();
        }
    }

    private void interactWithNpc(SimpleNpc nearestNpc) {
        if (ctx.players.getLocal().getLocation().distanceTo(nearestNpc.getLocation()) > 7) {
            ctx.pathing.step(nearestNpc.getLocation());
            ctx.onCondition(() -> false, 250, 10);
        }
        if (!ctx.pathing.reachable(nearestNpc.getLocation())) {
            openDoor(nearestNpc.getName());
        }
        BotUtils.eActions.status = "Thieving " + nearestNpc.getName();
        BotUtils.eActions.interactWith(nearestNpc, "Pickpocket");
    }

    private void getFood() {
        foodToEat = -1;
        SimpleItem foodInv = ctx.inventory.populate().filterHasAction("Eat").next();
        if (foodInv == null)  {
            ctx.log("No food found!");
            return;
        }
        foodToEat = foodInv.getId();
        ctx.log("Chosen food: " + foodInv.getName());
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
        net.runelite.api.events.ChatMessage getEvent = m.getChatEvent();
        String senderName = getEvent.getName();
        String gameMessage = getEvent.getMessage();

        if (m.getMessage() == null) {
            return;
        }

/*        String eventToStringTrimmed = getEvent.toString().replaceAll("<[^>]+>", "").trim();
        logger.info(eventToStringTrimmed); // to debug (returns chat type, text, sender)*/

        String gameMessageTrimmed = trimGameMessage(gameMessage).toLowerCase();
        if (getType == ChatMessageType.SPAM && gameMessageTrimmed.contains("successfully pick the")) {
            count += 1;
        }

        if (getType == ChatMessageType.NPC_EXAMINE) {

            if (gameMessageTrimmed.contains("holy warrior")) {
                npcToThiev = 3105;
                return;
            }

            if (gameMessageTrimmed.contains("keeps the peace")) {
                npcToThiev = 3245;
                return;
            }

            if (gameMessageTrimmed.contains("a member of ardougne")) {
                npcToThiev = 3108;
                return;
            }

            if (gameMessageTrimmed.contains("heroic")) {
                npcToThiev = 3106;
                return;
            }

            if (gameMessageTrimmed.contains("fashion conscious")) {
                npcToThiev = 3100;
                return;
            }

            if (gameMessageTrimmed.contains("an elf")) {
                npcToThiev = 5297;
                return;
            }

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


