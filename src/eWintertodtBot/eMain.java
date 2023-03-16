package eWintertodtBot;

import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Game;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@ScriptManifest(author = "Esmaabi", category = Category.FIREMAKING,
        description = "<br>Most effective Wintertodt Firemaking traing bot on Zenyte! <br><br><b>Features & recommendations:</b><br><br>" +
                "<ul>" +
                "<li>You must start with woodcutting axe </b>equipped</b> or in <b>inventory</b>;</li>" +
                "<li>You must start at Wintertodt bank;</li>" +
                "<li>Do not zoom out <b>to maximum</b>;</li>" +
                "<li>Dragon axe special attack supported;</li>" +
                "<li>Will eat shark, anglers or sara brew;</li>" +
                "<li>Due to limitation will only withdraw anglers;</li>" +
                "<li>Will eat and restock food if out of food!</li></ul>",
        discord = "Esmaabi#5752",
        name = "eWintertodtBotZenyte", servers = { "Zenyte" }, version = "0.1")

public class eMain extends TaskScript implements LoopingScript {

    //coordinates
    private final WorldArea bankArea = new WorldArea (new WorldPoint(1609,3965, 0), new WorldPoint(1650,3931, 0));
    private final WorldArea wintertodtArea = new WorldArea (new WorldPoint(1609,3966, 0), new WorldPoint(1652,4029, 0));
    private final WorldArea crateArea = new WorldArea (new WorldPoint(1625,3978, 0), new WorldPoint(1635,3986, 0));
    private final WorldPoint vialBoxLoc = new WorldPoint(1627, 3982, 0);
    private final WorldPoint herbRootLoc = new WorldPoint(1611, 4006, 0);
    private final WorldPoint brumaRootLoc = new WorldPoint(1622, 3988, 0);
    private final WorldPoint doorsLocation = new WorldPoint(1630, 3968, 0);

    //vars
    private long startTime = 0L;
    private long startingSkillLevel;
    private long startingSkillExp;
    private int count;
    static String status = null;
    private int currentExp;
    private long lastAnimation = -1;

    boolean specialDone = false;
    private final String[] foodItems = {"Shark", "Anglerfish", "Saradomin"};
    private final int[] inventoryItems = {6739, 2347, 590};
    private State playerState;

    public static int randomSleeping(int minimum, int maximum) {
        return (int)(Math.random() * (maximum - minimum)) + minimum;
    }

    public static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
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

    enum State {
        RUNNING,
        WAITING,
    }

    @Override
    public void onExecute() {

        tasks.addAll(Collections.emptyList());

        System.out.println("Started eWintertodtBot!");

        this.ctx.updateStatus("--------------- " + currentTime() + " ---------------");
        this.ctx.updateStatus("-------------------------------");
        this.ctx.updateStatus("       eWintertodtBot      ");
        this.ctx.updateStatus("-------------------------------");

        status = "Setting up bot";
        this.startTime = System.currentTimeMillis();
        this.startingSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.FIREMAKING);
        this.startingSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING);
        currentExp = this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING);// for actions counter by xp drop
        count = 0;
        ctx.viewport.angle(270);
        ctx.viewport.pitch(true);
        specialDone = false;
        playerState = State.RUNNING;

    }

    @Override
    public void onProcess() {
        super.onProcess();

        if (ctx.pathing.energyLevel() > 30 && !ctx.pathing.running()) {
            ctx.pathing.running(true);
        }

        if (currentExp != this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING)) { //action counter
            count++;
            currentExp = this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING);
        }

        if (ctx.players.getLocal().getHealth() < 55) { // eating
            status = "Restoring health";
            eatFood();
            ctx.onCondition(() -> ctx.players.getLocal().getHealth() > 55, 250, 10);
        }

        if (ctx.combat.getSpecialAttackPercentage() == 100
                && ctx.equipment.populate().filter("Dragon axe").population() == 1
                && ctx.players.getLocal().getAnimation() == 2846) { // special attack for dragon axe
            int sleep = randomSleeping(2000, 6000);
            status = "Using special attack in " + sleep + "ms";
            ctx.sleep(sleep);
            ctx.combat.toggleSpecialAttack(true);
            ctx.game.tab(Game.Tab.INVENTORY);
        }

        if (ctx.pathing.inArea(wintertodtArea)) {

            if (getFoodPopulation() == 0) {
                restockTask();

            } else {

                if (rejuvPotionAmount() == 0) {
                    System.out.println("Making rejuv potion");
                    getRejuvPotion();
                }

                if (rejuvPotionAmount() != 0) {

                    if (ctx.players.getLocal().isAnimating()) {
                        return;
                    }

                    checkingForSprite(); //checking for spritesId

                    if (!logsInInventory()) { // Cutting logs
                        if (!ctx.players.getLocal().isAnimating() && (System.currentTimeMillis() > (lastAnimation + randomSleeping(2500, 3500)))) {
                            cuttingTask();
                        } else if (ctx.players.getLocal().isAnimating()) {
                            lastAnimation = System.currentTimeMillis();
                        }
                    }

                    if (pyroIsAlive() && logsInInventory() && !ctx.players.getLocal().isAnimating()) { // Burning logs
                        burningTask();
                    }

                } //rejuv potion check
            } // food check in inventory
        } //winterdoth area

        if (ctx.pathing.inArea(bankArea)) {

            if (ctx.players.getLocal().getHealth() < 90 && !ctx.bank.bankOpen() && getFoodPopulation() != 0) {
                status = "Restoring health at bank";
                eatFood();
                ctx.onCondition(() -> ctx.players.getLocal().getHealth() > 90, 250, 10);
            }

            if (getFoodPopulation() < 15) {
                openingBank();
            } else {
                runningToDoors();
            }

        } // bank area

    }

    public static SimpleItem getItem(String... itemName) {
        return ClientContext.instance().inventory.populate()
                .filter(p -> Stream.of(itemName).anyMatch(arr -> p.getName().toLowerCase().contains(arr.toLowerCase())))
                .next();
    }

    public void openingBank() {
        SimpleObject bankChest = ctx.objects.populate().filter("Bank chest").filterHasAction("Bank").nearest().next();
        if (bankChest == null && !ctx.bank.bankOpen()) {
            status = "Running to bank";
            ctx.pathing.step(1631, 3951);
            ctx.sleepCondition(() -> ctx.pathing.inMotion());
        }

        if (bankChest != null && !ctx.bank.bankOpen()) {
            if (bankChest.validateInteractable()) {
                status = "Opening bank";
                bankChest.click("Bank", "Bank chest");
                ctx.onCondition(() -> ctx.bank.bankOpen(), randomSleeping(2000, 5000));
            }
        }

        if (ctx.bank.bankOpen() && ctx.inventory.populate().filter(13441).population() < 15) {
            status = "Banking";
            ctx.bank.depositAllExcept(inventoryItems);
            ctx.sleep(300);
            ctx.bank.withdraw(13441, 18); //food Anglers
            ctx.sleep(300);
        }

        if (ctx.bank.bankOpen() && ctx.inventory.populate().filter(13441).population() > 15) {
            status = "Closing bank";
            ctx.bank.closeBank();
            ctx.onCondition(() -> !ctx.bank.bankOpen(), 5000);
        }
    }

    private void runningToDoors() {
        SimpleObject minigameDoors = ctx.objects.populate().filter("Doors of Dinh").nearest().next();
        if (minigameDoors == null) {
            status = "Running to doors";
            ctx.pathing.step(1631, 3951);
        }

        if (minigameDoors != null && minigameDoors.validateInteractable()) {
            status = "Enterning doors";
            minigameDoors.click("Enter");
            ctx.sleepCondition(() -> ctx.pathing.inArea(wintertodtArea), 5000);
        }
    }

    public void restockTask() {
        status = "Running to doors";
        if (ctx.players.getLocal().getLocation().distanceTo(doorsLocation) > 5) {
            ctx.pathing.step(doorsLocation);
            ctx.sleepCondition(() -> ctx.players.getLocal().getLocation().distanceTo(doorsLocation) < 5);
        }

        if (ctx.players.getLocal().getLocation().distanceTo(doorsLocation) <= 5) {
            SimpleObject minigameDoors = ctx.objects.populate().filter("Doors of Dinh").nearest().next();
            status = "Leaving";

            if (minigameDoors != null && minigameDoors.validateInteractable()) {
                minigameDoors.click("Enter");
                ctx.onCondition(() -> ctx.dialogue.dialogueOpen(), 250, 10);
            }

            if (!ctx.dialogue.dialogueOpen()) {
                return;
            }
            ctx.dialogue.clickDialogueOption(1);
            ctx.sleepCondition(() -> ctx.pathing.inArea(bankArea), 5000);
        }
    }

    private int rejuvPotionAmount() {
        String[] potionName = {"rejuvenation potion (4)", "rejuvenation potion (3)", "rejuvenation potion (2)", "rejuvenation potion (1)"};
        SimpleItem rejuvPotion = getItem(potionName);
        if (rejuvPotion != null) {
            return rejuvPotion.getQuantity();
        }
        return 0;
    }

    private void eatFood() {
        SimpleItem foodInInv = getItem(foodItems);
        if (foodInInv == null) {
            return;
        }
        foodInInv.click(0);
    }

    private void getRejuvPotion() {
        int unfPotion = 20697;
        int brumaHerb = 20698;
        if (ctx.inventory.populate().filter(unfPotion).isEmpty()) {
            status = "Running to crates";
            if (!ctx.pathing.inArea(crateArea)) {
                ctx.pathing.step(vialBoxLoc);
                ctx.sleepCondition(() -> ctx.players.getLocal().getLocation().distanceTo(vialBoxLoc) < 5);
            }

            if (ctx.pathing.inArea(crateArea)) {
                SimpleObject vialBoxObject = ctx.objects.populate().filter(29320).nearest().next();
                status = "Getting unfinished rejuv";
                if (vialBoxObject != null && vialBoxObject.validateInteractable()) {
                    vialBoxObject.click(0);
                    int cached = ctx.inventory.populate().filter(unfPotion).population();
                    ctx.onCondition(() -> ctx.inventory.populate().filter(unfPotion).population() > cached, 250, 10);
                }
            }
        }

        if (ctx.inventory.populate().filter(brumaHerb).isEmpty() && !ctx.inventory.populate().filter(unfPotion).isEmpty()) {
            if (ctx.players.getLocal().getLocation().distanceTo(herbRootLoc) > 5) {
                status = "Running to herb root";
                ctx.pathing.step(herbRootLoc);
                ctx.onCondition(() -> ctx.players.getLocal().getLocation().distanceTo(herbRootLoc) < 5);
            }

            if (ctx.players.getLocal().getLocation().distanceTo(herbRootLoc) <= 5) {
                SimpleObject herbRoot = ctx.objects.populate().filter(29315).nearest().next();
                status = "Getting herb";
                if (herbRoot != null &&herbRoot.validateInteractable()) {
                    herbRoot.click(0);
                    int cached = ctx.inventory.populate().filter(brumaHerb).population();
                    ctx.onCondition(() -> ctx.inventory.populate().filter(brumaHerb).population() > cached);
                }
            }
        }

        if (!ctx.inventory.populate().filter(unfPotion).isEmpty() && !ctx.inventory.populate().filter(brumaHerb).isEmpty()) {
            status = "Making rejuv potion";
            SimpleItem unfPotionInv = ctx.inventory.populate().filter(unfPotion).next();
            SimpleItem brumaHerbInv = ctx.inventory.populate().filter(brumaHerb).next();
            unfPotionInv.click("Use");
            ctx.sleep(200);
            brumaHerbInv.click(0);
            ctx.sleep(200);
            ctx.pathing.step(1620, 3993);
            ctx.sleep(1000);
        }

    }

    public void burningTask() {
        status = "Burning roots";
        SimpleObject burningBrazier = ctx.objects.populate().filter(29314).filterWithin(10).next();
        if (burningBrazier == null) {
            return;
        }
        burningBrazier.click("Feed");
        int cached = ctx.players.getLocal().getHealth();
        ctx.sleepCondition(() -> ctx.players.getLocal().getHealth() < cached || !logsInInventory(), 10000);
    }

    public void cuttingTask() {
        if (ctx.players.getLocal().getLocation().distanceTo(brumaRootLoc) > 5) {
            status = "Running to bruma root";
            ctx.pathing.step(brumaRootLoc);
            ctx.sleepCondition(() -> ctx.players.getLocal().getLocation().distanceTo(brumaRootLoc) < 5);
        }

        if (ctx.players.getLocal().getLocation().distanceTo(brumaRootLoc) <= 5) {
            SimpleObject brumaRoots = ctx.objects.populate().filter("Bruma roots").nearest().next();
            status = "Cutting bruma root";
            if (brumaRoots != null && brumaRoots.validateInteractable()) {
                brumaRoots.click("Chop");
                ctx.onCondition(() -> ctx.players.getLocal().isAnimating(), 250, 10);
            }
        }
    }

    private boolean logsInInventory() {
        if (ctx.inventory.populate().filter(20695).isEmpty()) {
            return false;
        }
        return true;
    }

    private void healingPyromancerTask() {
        status = "Helping Pyromancer";
        SimpleNpc dyingPyromancer = ctx.npcs.populate().filter(7372).filterWithin(10).filterHasAction("Help").nearest().next(); // "Incapacited pyromancer"
        if (dyingPyromancer != null) {
            status = "Helping Pyromancer";
            dyingPyromancer.click("Help");
            ctx.sleepCondition(() -> ctx.widgets.getWidget(396, 8).getSpriteId() == -1, 3000);
        } //for some reason sometimes it has null point exception with this...
    }

    private void fixingBrazier() {
        status = "Repairing brazier";
        SimpleObject brazierBroken = ctx.objects.populate().filter("Brazier").filterWithin(10).filterHasAction("Fix").nearest().next(); // Repair brazier
        if (brazierBroken != null) {
            status = "Repairing brazier";
            brazierBroken.click("Fix");
            ctx.sleepCondition(() -> ctx.widgets.getWidget(396, 12).getSpriteId() == 1398, 3000);
        } //for some reason sometimes it has null point exception with this...
    }

    private void lightingBrazier() {
        SimpleObject brazierNeedsLight = ctx.objects.populate().filter("Brazier").filterWithin(10).filterHasAction("Light").nearest().next(); // Light brazier
        if (brazierNeedsLight != null) {
            status = "Lighting brazier";
            brazierNeedsLight.click("Light");
            ctx.sleepCondition(() -> ctx.widgets.getWidget(396, 12).getSpriteId() == 1399, 3000);
        }
    }

    private void checkingForSprite() {
        if (ctx.widgets.getWidget(396, 8).getSpriteId() == 1400) {
            System.out.println("Status: Pyro is dead");
            healingPyromancerTask();
            return;
        }

        if (ctx.widgets.getWidget(396, 12).getSpriteId() == 1397) {
            System.out.println("Status: Brazier needs fixing");
            fixingBrazier();
            return;
        }

        if (ctx.widgets.getWidget(396, 12).getSpriteId() == 1398) {
            System.out.println("Status: Brazier not burning");
            lightingBrazier();
            return;
        }

        return;
    }

    private boolean brazierFireIsBurning() {
        if (ctx.widgets.getWidget(396, 12).getSpriteId() == 1399) {
            System.out.println("Status: Fire is burning");
            return true;
        }
        return false;
    }

    private boolean pyroIsAlive() {
        if (ctx.widgets.getWidget(396, 8).getSpriteId() == -1) {
            System.out.println("Status: Pyro is alive");
            return true;
        }
        return false;
    }

    private int getFoodPopulation() {
        return ctx.inventory.populate().filter(foodItems).population();
    }

    @Override
    public void onTerminate() {
        this.startingSkillLevel = 0L;
        this.startingSkillExp = 0L;
        count = 0;
        playerState = State.WAITING;


        this.ctx.updateStatus("-------------- " + currentTime() + " --------------");
        this.ctx.updateStatus("----------------------");
        this.ctx.updateStatus("Thank You & Good Luck!");
        this.ctx.updateStatus("----------------------");
    }

    @Override
    public void onChatMessage(ChatMessage m) {
        if (m.getMessage() != null) {
            String message = m.getMessage().toLowerCase();
            if (message.contains(ctx.players.getLocal().getName().toLowerCase())) {
                ctx.updateStatus(currentTime() + " Someone asked for you");
                ctx.updateStatus(currentTime() + " Stopping script");
                ctx.stopScript();
            } else if (message.contains("get some amethyst")) {
                count++;
            }
        }
    }

    @Override
    public int loopDuration() {
        return 200;
    }

    @Override
    public void paint(Graphics g) {
        Color PhilippineRed = new Color(196, 18, 48);
        Color RaisinBlack = new Color(35, 31, 32, 127);
        g.setColor(RaisinBlack);
        g.fillRect(5, 120, 200, 110);
        g.setColor(PhilippineRed);
        g.drawRect(5, 120, 200, 110);
        g.setColor(PhilippineRed);
        g.drawString("eWintertodtBot by Esmaabi", 15, 135);
        g.setColor(Color.WHITE);
        long runTime = System.currentTimeMillis() - this.startTime;
        long currentSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.FIREMAKING);
        long currentSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING);
        long SkillLevelsGained = currentSkillLevel - this.startingSkillLevel;
        long SkillExpGained = currentSkillExp - this.startingSkillExp;
        long SkillExpPerHour = (int)((SkillExpGained * 3600000D) / runTime);
        long ActionsPerHour = (int) (count / ((System.currentTimeMillis() - this.startTime) / 3600000.0D));
        g.drawString("Runtime: " + formatTime(runTime), 15, 150);
        g.drawString("Starting Level: " + this.startingSkillLevel + " (+" + SkillLevelsGained + ")", 15, 165);
        g.drawString("Current Level: " + currentSkillLevel, 15, 180);
        g.drawString("Exp gained: " + SkillExpGained + " (" + (SkillExpPerHour / 1000L) + "k" + " xp/h)", 15, 195);
        g.drawString("Logs used: " + count + " (" + ActionsPerHour + " per/h)", 15, 210);
        g.drawString("Status: " + status, 15, 225);
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