package eRunecraftingBotZenyte;


import net.runelite.api.coords.WorldPoint;
import eRunecraftingBotZenyte.listeners.SkillListener;
import eRunecraftingBotZenyte.listeners.SkillObserver;
import simple.hooks.filters.SimpleBank;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Game;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.script.Script;
import simple.robot.utils.WorldArea;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BooleanSupplier;


@ScriptManifest(author = "Esmaabi", category = Category.RUNECRAFTING, description = " "
        + "Please read <b>eRunecraftingBot</b> description first!</b><br>"
        + "<br><b>Description</b>:<br>"
        + "It is required to have chisel in inventory for <b>Mining</b> and <b>Running Bloods</b> tasks<br>"
        + "Start near dense runestone for <b>Mining</b> task while <b>Zenyte deposit chest</b> is activated<br>"
        + "Start at Crafting Guild with <b>Max cape</b> for other tasks<br><br> "
        + "For more information check out Esmaabi on SimpleBot!", discord = "Esmaabi#5752",
        name = "eRunecraftingBotZenyte", servers = { "Zenyte" }, version = "1")

public class eMain extends Script implements SkillListener {


    //vars
    public static boolean started;
    private long startTime = 0L;
    public static String status = null;
    public static State playerState;
    private long lastAnimation = -1;
    private int darkBlocks;
    private int bloodRunes;
    private int denseEssence;
    private boolean chiselTask = false;
    private boolean miningSouth = true;
    private int levelsGained;
    private int experienceGained;
    boolean runningSkillListener = true;
    private boolean scriptStopped = false;


    //stats
    private long startingSkillLevelMining, startingSkillLevelCrafting, startingSkillLevelRunecrafting;
    private long startingSkillExpMining, startingSkillExpCrafting, startingSkillExpRunecrafting;

    //areas
    private final WorldArea miningArea = new WorldArea (new WorldPoint(1759, 3862, 0), new WorldPoint(1768, 3843, 0));
    private final WorldArea craftingGuild = new WorldArea (new WorldPoint(2938, 3288, 0), new WorldPoint(2929, 3279, 0));
    private final WorldArea arecuusAltarArea = new WorldArea (new WorldPoint(1683, 3893, 0), new WorldPoint(1726, 3872, 0));
    private final WorldArea bloodAltarArea = new WorldArea(new WorldPoint(1710,3836, 0), new WorldPoint(1722,3822, 0));
    private final WorldArea arecuusWholeArea = new WorldArea(new WorldPoint(1745,3898, 0), new WorldPoint(1652,3821, 0));
    private final WorldPoint northDenseObjectLocation = new WorldPoint(1764, 3858, 0);
    private final WorldPoint southDenseObjectLocation = new WorldPoint(1764, 3846, 0);

    private final WorldPoint[] darkAltarToBloodAltar = {
            new WorldPoint(1697, 3880, 0),
            new WorldPoint(1690, 3880, 0),
            new WorldPoint(1684, 3881, 0),
            new WorldPoint(1678, 3881, 0),
            new WorldPoint(1672, 3881, 0),
            new WorldPoint(1667, 3881, 0),
            new WorldPoint(1662, 3881, 0),
            new WorldPoint(1658, 3879, 0),
            new WorldPoint(1658, 3874, 0),
            new WorldPoint(1659, 3870, 0),
            new WorldPoint(1661, 3866, 0),
            new WorldPoint(1665, 3863, 0),
            new WorldPoint(1670, 3862, 0),
            new WorldPoint(1674, 3860, 0),
            new WorldPoint(1679, 3859, 0),
            new WorldPoint(1686, 3858, 0),
            new WorldPoint(1691, 3857, 0),
            new WorldPoint(1696, 3857, 0),
            new WorldPoint(1702, 3857, 0),
            new WorldPoint(1707, 3857, 0),
            new WorldPoint(1713, 3857, 0),
            new WorldPoint(1718, 3856, 0),
            new WorldPoint(1723, 3856, 0),
            new WorldPoint(1728, 3854, 0),
            new WorldPoint(1734, 3851, 0),
            new WorldPoint(1735, 3848, 0),
            new WorldPoint(1735, 3843, 0),
            new WorldPoint(1734, 3837, 0),
            new WorldPoint(1735, 3833, 0),
            new WorldPoint(1733, 3828, 0),
            new WorldPoint(1728, 3826, 0),
            new WorldPoint(1721, 3826, 0),
            new WorldPoint(1719, 3828, 0)
    };

    public static int randomSleeping(int minimum, int maximum) {
        return (int)(Math.random() * (maximum - minimum)) + minimum;
    }

    enum State{
        MINING,
        CRAFTING,
        BLOODCRAFTING,
        WAITING,
    }

    @Override
    public void onExecute() {
        System.out.println("Started eRunecraftingBot Zenyte!");
        started = false;
        status = "Setting up config";
        startTime = System.currentTimeMillis(); //paint
        ctx.viewport.angle(90);
        ctx.viewport.pitch(true);
        darkBlocks = 0;
        bloodRunes = 0;
        denseEssence = 0;

        this.ctx.updateStatus("----------------------------------");
        this.ctx.updateStatus("      eRunecraftingBotZenyte      ");
        this.ctx.updateStatus("----------------------------------");

        //Mining
        startingSkillLevelMining = this.ctx.skills.realLevel(SimpleSkills.Skills.MINING);
        startingSkillExpMining = this.ctx.skills.experience(SimpleSkills.Skills.MINING);

        //Crafting
        startingSkillLevelCrafting = this.ctx.skills.realLevel(SimpleSkills.Skills.CRAFTING);
        startingSkillExpCrafting = this.ctx.skills.experience(SimpleSkills.Skills.CRAFTING);

        //Runecrafting
        startingSkillLevelRunecrafting = this.ctx.skills.realLevel(SimpleSkills.Skills.RUNECRAFT);
        startingSkillExpRunecrafting = this.ctx.skills.experience(SimpleSkills.Skills.RUNECRAFT);

        //GUI
        eGui gui = new eGui();
        gui.setVisible(true);

        //skill listener
        runningSkillListener = true;
        SkillObserver skillObserver = new SkillObserver(ctx, new BooleanSupplier() {

            public boolean getAsBoolean() {
                return runningSkillListener;
            }
        });
        skillObserver.addListener(this);
        skillObserver.start();

    }

    @Override
    public void onProcess() {
        if (!started) {
            playerState = State.WAITING;

        } else {

            if (ctx.pathing.energyLevel() > 30 && !ctx.pathing.running()) {
                ctx.pathing.running(true);
            }

            if (playerState == State.MINING) {

                if (miningArea.containsPoint(ctx.players.getLocal().getLocation())) {
                    if (ctx.inventory.populate().population() < 28 && !ctx.bank.depositBoxOpen() && !ctx.players.getLocal().inMotion()) {
                        if (ctx.players.getLocal().getAnimation() == -1 && (System.currentTimeMillis() > (lastAnimation + randomSleeping(3000, 8000)))) {
                            miningTask();
                            ctx.sleep(5000);
                        } else if (ctx.players.getLocal().getAnimation() != -1) {
                            lastAnimation = System.currentTimeMillis();
                        }
                    } else {
                        depositTask();
                    }
                }

            }

            if (playerState == State.CRAFTING) {
                if (ctx.inventory.populate().filter(13445).isEmpty()) {
                    if (!craftingGuild.containsPoint(ctx.players.getLocal().getLocation())) {
                        teleportToBankTask();
                    } else {
                        bankTask();
                    }
                }

                if (!ctx.inventory.populate().filter(13445).isEmpty()) {
                    if (craftingGuild.containsPoint(ctx.players.getLocal().getLocation())) {
                        teleportToArceuusTask();
                    }

                    if (arecuusAltarArea.containsPoint(ctx.players.getLocal().getLocation())) {
                        venerateTask();
                    }
                }
            }

            if (playerState == State.BLOODCRAFTING) {

                if (!ctx.inventory.populate().filter(13446).isEmpty()
                        && ctx.inventory.populate().filter(7938).isEmpty()
                        && bloodAltarArea.containsPoint(ctx.players.getLocal().getLocation())
                        && !chiselTask) { // Making fragments near blood altar
                    status = "Making fragments to bind";
                    chiselTask = true;
                }

                if (!ctx.inventory.populate().filter(13446, 1755).isEmpty()
                        && ctx.inventory.populate().filter(7938).isEmpty()
                        && craftingGuild.containsPoint(ctx.players.getLocal().getLocation())
                        && !chiselTask) { //Making fragments near bank
                    status = "Making fragments";
                    chiselTask = true;
                    chiselTaskStart();
                }

                if (chiselTask) {
                    ctx.sleep(200);
                    chiselTaskStart();
                }

                if (!ctx.inventory.populate().filter(13446).isEmpty()
                        && !ctx.inventory.populate().filter(7938).isEmpty()
                        && craftingGuild.containsPoint(ctx.players.getLocal().getLocation())
                        && !chiselTask) {
                    teleportToArceuusTask();
                }

                if (!ctx.inventory.populate().filter(13446).isEmpty()
                        && !ctx.inventory.populate().filter(7938).isEmpty()
                        && arecuusWholeArea.containsPoint(ctx.players.getLocal().getLocation())
                        && !bloodAltarArea.containsPoint(ctx.players.getLocal().getLocation())) {
                    status = "Getting to Blood Altar";
                    walkPath(bloodAltarArea, darkAltarToBloodAltar, false);
                }

                if (ctx.pathing.inArea(bloodAltarArea) && !chiselTask
                        && (!ctx.inventory.populate().filter(13446).isEmpty() || !ctx.inventory.populate().filter(7938).isEmpty())) {
                    status = "Making Blood runes";
                    SimpleObject bloodAltar = ctx.objects.populate().filter(27978).nearest().next();
                    bloodAltar.click("Bind", "Blood Altar");
                    ctx.sleep(2000);
                }

                if (ctx.pathing.inArea(bloodAltarArea) && !chiselTask
                        && ctx.inventory.populate().filter(7938).isEmpty()
                        && ctx.inventory.populate().filter(13446).isEmpty()) {
                    bloodRunes += ctx.inventory.populate().filter(565).population(true);
                    teleportToBankTask();
                }

                if (craftingGuild.containsPoint(ctx.players.getLocal().getLocation()) && !chiselTask
                        && (ctx.inventory.populate().filter(13446).isEmpty() || ctx.inventory.populate().filter(7938).isEmpty())) {
                    bankTask();
                }
            }
        }
    }


    //mining
    public void depositTask() {
        SimpleObject bankChest = ctx.objects.populate().filter(29090).filterHasAction("Deposit").nearest().next();
        if (!ctx.bank.depositBoxOpen()) {
            if (bankChest != null && bankChest.validateInteractable()) {
                status = "Opening deposit box";
                bankChest.click("Deposit");
                ctx.onCondition(() -> ctx.bank.depositBoxOpen(), randomSleeping(2000, 3500));
            }
        }

        if (ctx.bank.depositBoxOpen()) {
            status = "Depositing inventory";
            ctx.bank.depositAllExcept("Chisel");
            ctx.bank.closeBank();
            miningSouth = true;
        }
    }

    public void miningTask() {
        status = "Mining dense runestone";
        //SimpleObject denseStone = ctx.objects.populate().filter("Dense runestone").filter(object -> object.click("Chip", "Dense runestone")).next();
        SimpleObject denseStoneSouth = ctx.objects.populate().filter(8975).filter("Chip", "Dense runestone").nearest(southDenseObjectLocation).next();
        SimpleObject denseStoneNorth = ctx.objects.populate().filter(8975).filter("Chip", "Dense runestone").nearest(northDenseObjectLocation).next();

        if (miningSouth) {
            status = "Mining south runestone";
            denseStoneSouth.click("Chip", "Dense runestone");
            if (ctx.combat.getSpecialAttackPercentage() == 100) {
                specialAttack();
            }
            ctx.sleep(5000);
            ctx.sleepCondition(() -> ctx.players.getLocal().getAnimation() == -1, randomSleeping(45000, 90000));
            miningSouth = false;
        }

        status = "Mining north runestone";
        denseStoneNorth.click("Chip", "Dense runestone");
        if (ctx.combat.getSpecialAttackPercentage() == 100) {
            specialAttack();
        }
        ctx.sleep(5000);
        ctx.sleepCondition(() -> ctx.players.getLocal().getAnimation() == -1, randomSleeping(45000, 90000));
        miningSouth = true;

    }

    //Making dark blocks & bloodcrafting
    public void bankTask() {
        status = "Starting banking task";
        SimpleObject bankChest = ctx.objects.populate().filter(14886).filterHasAction("Use").nearest().next();//bank chest
        if (!ctx.bank.bankOpen()) {
            if (bankChest != null && bankChest.validateInteractable()) {
                if (!ctx.bank.bankOpen()) {
                    status = "Opening bank";
                    bankChest.click("Use");
                    ctx.onCondition(() -> ctx.bank.bankOpen(), randomSleeping(2000, 3500));
                }
            }
        } else if (ctx.bank.bankOpen()) {
            if (playerState == State.CRAFTING) {
                ctx.bank.depositInventory();
                ctx.bank.withdraw(13445, SimpleBank.Amount.ALL); // dense essence
                if (ctx.bank.populate().filter(13445).isEmpty()) {
                    System.out.println("No dense blocks in bank");
                    ctx.updateStatus("No dense blocks in bank");
                    playerState = State.BLOODCRAFTING;
                }
            }

            if (playerState == State.BLOODCRAFTING) {
                SimpleWidget quantityOne = ctx.widgets.getWidget(12, 29);//Default quantity = 1 button
                if (quantityOne != null && !quantityOne.isHidden()) {
                    quantityOne.click(0);
                }

                ctx.bank.depositAllExcept(1755, 7938); //chisel and dark fragments

                if (!ctx.inventory.populate().filter(1755).isEmpty()) {
                    ctx.bank.withdraw(13446, SimpleBank.Amount.ALL);  // dark essence
                } else {
                    System.out.println("Chisel not found in inventory. Withdrawing it.");
                    ctx.bank.withdraw(1755, SimpleBank.Amount.ONE);
                    ctx.bank.withdraw(13446, SimpleBank.Amount.ALL);  // dark essence
                }
            }

            status = "Closing bank";
            ctx.bank.closeBank();
        }
    }

    public void specialAttack() {
        if (ctx.combat.getSpecialAttackPercentage() == 100
                && ctx.equipment.populate().filter("Dragon pickaxe").population() == 1
                && miningArea.containsPoint(ctx.players.getLocal().getLocation())) {
            ctx.combat.toggleSpecialAttack(true);
            ctx.game.tab(Game.Tab.INVENTORY);
        }
    }


    public void venerateTask() {
        status = "Clicking Dark Altar";
        SimpleObject darkAltar = ctx.objects.populate().filter(27979).filterHasAction("Venerate").nearest().next();
        if (darkAltar != null && darkAltar.validateInteractable()) {
            darkAltar.click("Venerate");
            ctx.sleepCondition(() -> ctx.inventory.populate().filter(13445).isEmpty(), randomSleeping(2000, 4000));
        }
        darkBlocks += ctx.inventory.populate().filter(13446).population();
    }

    private void walkPath(WorldArea toArea, WorldPoint[] walkPath, boolean reverse) {
        while (!ctx.pathing.inArea(toArea)) {
            if (ctx.pathing.energyLevel() > 30 && !ctx.pathing.running()) {
                ctx.pathing.running(true);
            }

            if (scriptStopped) {
                break;
            }

            ctx.pathing.walkPath(walkPath, reverse);
            ctx.sleep(1000);
        }
    }

    public void chiselTaskStart() {
        SimpleItem chiselTool = ctx.inventory.populate().filter(1755).next();
        SimpleItem darkEssence = ctx.inventory.populate().filter(13446).reverse().next();
        int sleepTime = randomSleeping(20, 75);
        if (chiselTool != null && chiselTool.validateInteractable()
                && darkEssence != null && darkEssence.validateInteractable()) {
            while (!ctx.inventory.populate().filter(13446).isEmpty()) {
                chiselTool.click(0);
                ctx.sleep(sleepTime);
                darkEssence.click(0);
                ctx.sleep(sleepTime);
            }
        }
        chiselTask = false;
    }

    //teleporting
    public void teleportToBankTask() {
        status = "Teleporting to Crafting Guild";
        ctx.game.tab(Game.Tab.EQUIPMENT);
        if (!ctx.equipment.populate().filter(13342).isEmpty()) {
            SimpleWidget maxCape = ctx.widgets.getWidget(387, 7);//cape slot
            if (maxCape != null && !maxCape.isHidden()) {
                maxCape.click("Crafting Guild", "Max cape");
                ctx.sleepCondition(() -> craftingGuild.containsPoint(ctx.players.getLocal().getLocation()), randomSleeping(1000, 3000));
            }
        }
        ctx.game.tab(Game.Tab.INVENTORY);
    }

    public void teleportToArceuusTask() {
        status = "Teleporting to Arceuus";
        ctx.game.tab(Game.Tab.MAGIC);
        SimpleWidget homeTeleport = ctx.widgets.getWidget(218, 143);//home teleport
        if (homeTeleport != null & !homeTeleport.isHidden()) {
            homeTeleport.click("Arceuus", "Home Teleport");
            ctx.sleepCondition(() -> arecuusAltarArea.containsPoint(ctx.players.getLocal().getLocation()), randomSleeping(9000, 12000));
        }
        ctx.game.tab(Game.Tab.INVENTORY);
    }

    public static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    @Override
    public void onTerminate() {
        if (playerState == State.MINING) {
            this.ctx.updateStatus("Dense runestones mined: " + denseEssence);
        } else if (playerState == State.CRAFTING) {
            this.ctx.updateStatus("Dark blocks made: " + darkBlocks);
        } else if (playerState == State.BLOODCRAFTING) {
            this.ctx.updateStatus("Blood runes crafted: " + bloodRunes);
        }

        //listener
        runningSkillListener = false;

        ///vars
        denseEssence = 0;
        darkBlocks = 0;
        bloodRunes = 0;
        chiselTask = false;
        scriptStopped = true;

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
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        if (playerState == State.MINING) {
            Color PhilippineRed = new Color(196, 18, 48);
            Color RaisinBlack = new Color(35, 31, 32, 127);
            g.setColor(RaisinBlack);
            g.fillRoundRect(0, 0, 225, 110, 20, 20);
            g.setColor(PhilippineRed);
            g.drawRoundRect(5, 120, 225, 110, 20, 20);
            g.setColor(PhilippineRed);
            g.drawString("eRunecraftingBot by Esmaabi", 15, 135);
            g.setColor(Color.WHITE);
            long runTime = System.currentTimeMillis() - this.startTime;
            long currentSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.MINING);
            long currentSkillLevel2 = this.ctx.skills.realLevel(SimpleSkills.Skills.CRAFTING);
            long currentSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.MINING);
            long currentSkillExp2 = this.ctx.skills.experience(SimpleSkills.Skills.CRAFTING);
            long SkillLevelsGained = currentSkillLevel - this.startingSkillLevelMining;
            long SkillLevelsGained2 = currentSkillLevel2 - this.startingSkillLevelCrafting;
            long SkillExpGained = currentSkillExp - this.startingSkillExpMining;
            long SkillExpGained2 = currentSkillExp2 - this.startingSkillExpCrafting;
            long ExPGainedinSum = SkillExpGained + SkillExpGained2;
            long SkillexpPhour = (int) ((ExPGainedinSum * 3600000D) / runTime);
            long ThingsPerHour = (int) (denseEssence / ((System.currentTimeMillis() - this.startTime) / 3600000.0D));
            g.drawString("Runtime: " + formatTime(runTime), 15, 150);
            g.drawString("Starting: Mining " + this.startingSkillLevelMining + " (+" + SkillLevelsGained + ")," + " Crafting " + this.startingSkillLevelCrafting + " (+" + SkillLevelsGained2 + ")  ", 15, 165);
            g.drawString("Current: Mining " + currentSkillLevel + ", Crafting " + currentSkillLevel2, 15, 180);
            g.drawString("Exp gained: " + ExPGainedinSum + " (" + (SkillexpPhour / 1000L) + "k" + " xp/h)", 15, 195);
            g.drawString("Dense mined: " + denseEssence + " (" + ThingsPerHour + " / h)", 15, 210);
            g.drawString("Status: " + status, 15, 225);

        } else if (playerState == State.CRAFTING || playerState == State.BLOODCRAFTING || playerState == State.WAITING) {
            Color PhilippineRed = new Color(196, 18, 48);
            Color RaisinBlack = new Color(35, 31, 32, 127);
            g.setColor(RaisinBlack);
            g.fillRoundRect(5, 120, 250, 110, 20, 20);
            g.setColor(PhilippineRed);
            g.drawRoundRect(5, 120, 250, 110, 20, 20);
            g.setColor(PhilippineRed);
            g.drawString("eRunecraftingBot by Esmaabi", 15, 135);
            g.setColor(Color.WHITE);
            long runTime = System.currentTimeMillis() - this.startTime;
            long currentSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.RUNECRAFT);
            long currentSkillLevel2 = this.ctx.skills.realLevel(SimpleSkills.Skills.CRAFTING);
            long currentSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.RUNECRAFT);
            long currentSkillExp2 = this.ctx.skills.experience(SimpleSkills.Skills.CRAFTING);
            long SkillLevelsGained = currentSkillLevel - this.startingSkillLevelRunecrafting;
            long SkillLevelsGained2 = currentSkillLevel2 - this.startingSkillLevelCrafting;
            long SkillExpGained = currentSkillExp - this.startingSkillExpRunecrafting;
            long SkillExpGained2 = currentSkillExp2 - this.startingSkillExpCrafting;
            long ExPGainedinSum = SkillExpGained + SkillExpGained2;
            long SkillexpPhour = (int) ((ExPGainedinSum * 3600000D) / runTime);
            long ThingsPerHour = (int) (darkBlocks / ((System.currentTimeMillis() - this.startTime) / 3600000.0D));
            long ThingsPerHour2 = (int) (bloodRunes / ((System.currentTimeMillis() - this.startTime) / 3600000.0D));
            g.drawString("Runtime: " + formatTime(runTime), 15, 150);
            g.drawString("Starting: RuneCraft " + this.startingSkillLevelMining + " (+" + SkillLevelsGained + ")," + " Crafting " + this.startingSkillLevelCrafting + " (+" + SkillLevelsGained2 + ")", 15, 165);
            g.drawString("Current: RuneCraft " + currentSkillLevel + ", Crafting " + currentSkillLevel2, 15, 180);
            g.drawString("Exp gained: " + ExPGainedinSum + " (" + (SkillexpPhour / 1000L) + "k" + " xp/h)", 15, 195);
            g.drawString("Made: Blocks " + darkBlocks + " (" + ThingsPerHour + " / h), Bloods " + bloodRunes + " (" + ThingsPerHour2 + " / h)", 15, 210);
            g.drawString("Status: " + status, 15, 225);
        }
    }

    @Override
    public void skillLevelAdded(SimpleSkills.Skills skill, int current, int previous, int gained) {
        //System.out.printf("We gained %d levels in %s, we went from +%d to %d", gained, skill.toString(), previous, current);
        //levelsGained += gained;

    }

    @Override
    public void skillExperienceAdded(SimpleSkills.Skills skill, int current, int previous, int gained) {
        //System.out.printf("We gained %d experience in %s, we went from +%d to %d", gained, skill.toString(), previous, current);
        //experienceGained += gained;

        if (playerState == State.MINING) {
            denseEssence++;
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
