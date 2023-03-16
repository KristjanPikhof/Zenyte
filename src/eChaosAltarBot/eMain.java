package eChaosAltarBot;

import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleBank;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.script.Script;
import simple.robot.utils.WorldArea;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static eChaosAltarBot.eGui.locationName;


@ScriptManifest(author = "Esmaabi", category = Category.PRAYER, description =
        "<br>Most effective Chaos Altar prayer training bot on Zenyte! <br><br><b>Features & recommendations:</b><br><br>" +
        "<ul><li>You must start at chosen bank;</li>" +
        "<li>Have infernal ashes noted and enough gp in inventory;</li>" +
        "<li>At the moment supports only infernal ashes</li>" +
        "<li>Bot will log out if players around</li></ul>", discord = "Esmaabi#5752",
        name = "eChaosAltarBotZenyte", servers = { "Zenyte" }, version = "0.1")

public class eMain extends Script{
    private static eGui gui;
    private long startTime = 0L;
    private long startingSkillLevel;
    private long startingSkillExp;
    private int count;
    private int currentExp;
    static String status = null;
    public static boolean botStarted = false;
    public static boolean hidePaint = false;
    private boolean FMStarted;
    private long lastAnimation = -1;

    //Items
    public static String bonesName;
    static String bankName = "Banker";
    static String bankOpen = "Bank";
    static String tinderBox = "Tinderbox";



    //Locations
    private WorldPoint START_TILE;
    private final WorldPoint nearBankLocVarrock = new WorldPoint(3254, 3426, 0);
    private final WorldPoint nearBankLocFalador = new WorldPoint(3012, 3360, 0);
    private final WorldPoint nearBankLocGE = new WorldPoint(3163, 3482, 0);

    private static final WorldArea chaosAltarArea = new WorldArea (
            new WorldPoint(2946, 3823, 0),
            new WorldPoint(2946, 3819, 0),
            new WorldPoint(2949, 3818, 0),
            new WorldPoint(2949, 3817, 0),
            new WorldPoint(2953, 3817, 0),
            new WorldPoint(2953, 3819, 0),
            new WorldPoint(2958, 3819, 0),
            new WorldPoint(2958, 3823, 0),
            new WorldPoint(2953, 3823, 0),
            new WorldPoint(2953, 3825, 0),
            new WorldPoint(2949, 3825, 0),
            new WorldPoint(2948, 3823, 0),
            new WorldPoint(2945, 3823, 0));

    //Paths

    private static final WorldPoint pathFalador_1 = new WorldPoint(3025, 3361, 0);
    private static final WorldPoint pathFalador_2 = new WorldPoint(3025, 3362, 0);
    private static final WorldPoint pathFalador_3 = new WorldPoint(3025, 3363, 0);
    private static final WorldPoint pathVarrockEast_1 = new WorldPoint(3266, 3428, 0);
    private static final WorldPoint pathVarrockEast_2 = new WorldPoint(3266, 3429, 0);
    private static final WorldPoint pathVarrockEast_3 = new WorldPoint(3266, 3430, 0);
    private static final WorldPoint pathGrandExchange_1 = new WorldPoint(3177, 3478, 0);
    private static final WorldPoint pathGrandExchange_2 = new WorldPoint(3177, 3477, 0);
    private static final WorldPoint pathGrandExchange_3 = new WorldPoint(3177, 3476, 0);

    public enum firemakingLocations {
        FALADOR(pathFalador_1, pathFalador_2, pathFalador_3),
        VARROCK(pathVarrockEast_1, pathVarrockEast_2, pathVarrockEast_3),
        GE(pathGrandExchange_1, pathGrandExchange_2, pathGrandExchange_3);

        private final WorldPoint path1;
        private final WorldPoint path2;
        private final WorldPoint path3;

        firemakingLocations(WorldPoint path1, WorldPoint path2, WorldPoint path3) {
            this.path1 = path1;
            this.path2 = path2;
            this.path3 = path3;
        }

        public WorldPoint getPath1() {
            return path1;
        }

        public WorldPoint getPath2() {
            return path2;
        }

        public WorldPoint getPath3() {
            return path3;
        }
    }

    @Override
    public void onExecute() {
        System.out.println("Started eChaosAltarBot!");
        gui = new eGui();
        gui.setVisible(true);
        gui.setLocale(ctx.getClient().getCanvas().getLocale());
        ctx.viewport.pitch();
        ctx.viewport.angle(270);

        status = "Setting up bot";
        this.startTime = System.currentTimeMillis(); //paint
        this.startingSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.PRAYER);
        this.startingSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.PRAYER);
        currentExp = this.ctx.skills.experience(SimpleSkills.Skills.PRAYER);// for actions counter by xp drop
        count = 0;
        FMStarted = false;
        botStarted = false;
        hidePaint = false;

        //Getting FM starting tile from GUI selection
        WorldPoint[] locationPaths = getSelectedLocationPaths(locationName);
        START_TILE = locationPaths[0];
    }

    @Override
    public void onProcess() {

        if (botStarted) {

            if (currentExp != this.ctx.skills.experience(SimpleSkills.Skills.PRAYER)) { //action counter
                count++;
                currentExp = this.ctx.skills.experience(SimpleSkills.Skills.PRAYER);
            }

            if (ctx.players.populate().population() > 1) {
                ctx.sendLogout();
                ctx.sendLogout();
            }

            if (ctx.pathing.inArea(chaosAltarArea)) {

                if (ctx.inventory.populate().filter(30065).population() != 0) {

                    if (spotReachable()) {
                        System.out.println(spotReachable());
                        status = "Closing doors";
                        SimpleObject doors = ctx.objects.populate().filter("Large door").next();
                        System.out.println(doors);
                        if (doors != null && doors.validateInteractable()) {
                            doors.click("Open");
                            ctx.sleepCondition(() -> !spotReachable(), 5000);
                        }
                    }

                    if (ctx.players.getLocal().getAnimation() == -1 && (System.currentTimeMillis() > (lastAnimation + 3000))) {
                        sacrificeTask();
                    } else if (ctx.players.getLocal().getAnimation() != -1) {
                        lastAnimation = System.currentTimeMillis();
                    }

                } else {
                    if (!spotReachable()) {
                        System.out.println(spotReachable());
                        status = "Opening doors";
                        SimpleObject doors = ctx.objects.populate().filter("Large door").next();
                        System.out.println(doors);
                        if (doors != null && doors.validateInteractable()) {
                            doors.click("Open");
                            ctx.sleepCondition(() -> ctx.pathing.inMotion(), 5000);
                        }
                    } else {
                        if (!ctx.dialogue.dialogueOpen()) {
                            status = "Using noted ashes on druid";
                            SimpleNpc druid = ctx.npcs.populate().filter(7995).next();
                            SimpleItem bonesInventory = ctx.inventory.populate().filter(30067).next();
                            if (druid != null && druid.validateInteractable() && bonesInventory != null && bonesInventory.validateInteractable()) {
                                bonesInventory.click("Use");
                                ctx.sleep(200);
                                druid.click(0);
                                ctx.sleepCondition(() -> ctx.dialogue.dialogueOpen(), 8000);
                            }
                        }
                    }
                }
            } else {
                if (ctx.dialogue.dialogueOpen()) {
                    status = "Progressing dialogue";
                    ctx.dialogue.clickDialogueOption(3);
                    int ashesInv = bonesPopulation();
                    ctx.onCondition(() -> bonesPopulation() > ashesInv, 250, 10);
                }

                if (ctx.inventory.populate().filter(30065).population() != 0) {
                    status = "Walking back in church";
                    ctx.pathing.step(2956,3820);
                    ctx.sleepCondition(() -> ctx.pathing.inMotion(), 2000);
                }
            }
        }
    }

    private void sacrificeTask() {
        SimpleItem bonesInventory = ctx.inventory.populate().filter(30065).next();
        SimpleObject chaosAltar = ctx.objects.populate().filter("Chaos altar").next();
        status = "Sacrificing task";
        if (bonesInventory != null && bonesInventory.validateInteractable() && chaosAltar != null && chaosAltar.validateInteractable()) {
            ctx.viewport.turnTo(chaosAltar);
            ctx.viewport.pitch();
            bonesInventory.click("Use");
            ctx.sleep(200);
            chaosAltar.click("Use");
            int ashesInv = bonesPopulation();
            ctx.onCondition(() -> bonesPopulation() < ashesInv, 250, 10);
        }
    }

    private boolean spotReachable() {
        return ctx.pathing.reachable(new WorldPoint(2956, 3817, 0));
    }

    private int bonesPopulation() {
        return ctx.inventory.populate().filter(30065).population();
    }

    private boolean logsInInventory() {
        return !ctx.inventory.populate().filter(bonesName).isEmpty();
    }

    private void bankTask() {
        if (!ctx.bank.bankOpen()) {
            //SimpleObject bank = ctx.objects.populate().filter(bankName).nearest().next();
            SimpleNpc banker = ctx.npcs.populate().filter(bankName).nearest().next();
            WorldPoint i = nearBankLocation();
            System.out.println("Near bank tile: " + i);
            if (ctx.players.getLocal().getLocation().distanceTo(i) > 10) {
                status = "Running to bank";
                ctx.pathing.step(i);
                ctx.sleepCondition(() -> !ctx.pathing.inMotion(), 1200);
            } else {
                if (banker == null) {
                    return;
                } else {
                    if (banker.validateInteractable()) {
                        status = "Opening bank";
                        banker.click(bankOpen, bankName);
                        ctx.sleepCondition(() -> ctx.bank.bankOpen(), 3000);
                    }
                }
            }
        }

        if (ctx.bank.bankOpen()) {
            if (!logsInInventory()) {
                status = "Banking";
                ctx.bank.depositAllExcept(tinderBox);
                ctx.sleep(200);
                if (ctx.inventory.populate().filter(tinderBox).isEmpty()) {
                    System.out.println("Tinderbox not found in inventory. Withdrawing it.");
                    SimpleWidget quantityOne = ctx.widgets.getWidget(12, 29);
                    if (quantityOne != null && !quantityOne.isHidden()) {
                        quantityOne.click(0);
                    }
                    ctx.bank.withdraw(tinderBox, SimpleBank.Amount.ONE);
                    ctx.bank.closeBank();
                }
                
                if (!ctx.bank.populate().filter(bonesName).isEmpty()) {
                    ctx.bank.withdraw(bonesName, SimpleBank.Amount.ALL);
                    ctx.onCondition(this::logsInInventory, 250, 10);
                }

                if (ctx.bank.populate().filter(bonesName).isEmpty()) {
                    status = "Out of " + bonesName.toLowerCase();
                    ctx.updateStatus("Stopping script");
                    ctx.updateStatus("Out of " + bonesName.toLowerCase());
                    ctx.sleep(10000);
                    ctx.stopScript();
                }
            }
            WorldPoint[] locationPaths = getSelectedLocationPaths(locationName);
            status = "Closing bank";
            ctx.bank.closeBank();
            START_TILE = locationPaths[0];
            FMStarted = false;
            System.out.println("Bank closed");
            System.out.println("Starting FM task?: " + FMStarted);
            System.out.println("START_TILE has been set to: " + START_TILE);
            ctx.viewport.angle(270);
        }
    }

    private WorldPoint nearBankLocation() {
        String selectedItem = (String) locationName.getSelectedItem();
        WorldPoint nearBankLocation = null;
        assert selectedItem != null;
        switch (selectedItem) {
            case "Falador East":
                nearBankLocation = nearBankLocFalador;
                break;
            case "Varrock East":
                nearBankLocation = nearBankLocVarrock;
                break;
            case "Grand Exchange":
                nearBankLocation = nearBankLocGE;
                break;
        }
        return nearBankLocation;
    }

    public WorldPoint[] getSelectedLocationPaths(JComboBox<String> comboBox) {
        String selectedItem = (String) locationName.getSelectedItem();
        firemakingLocations locationSelected = null;

        assert selectedItem != null;
        switch (selectedItem) {
            case "Falador East":
                locationSelected = firemakingLocations.FALADOR;
                break;
            case "Varrock East":
                locationSelected = firemakingLocations.VARROCK;
                break;
            case "Grand Exchange":
                locationSelected = firemakingLocations.GE;
                break;
        }

        assert locationSelected != null;
        WorldPoint path1 = locationSelected.getPath1();
        WorldPoint path2 = locationSelected.getPath2();
        WorldPoint path3 = locationSelected.getPath3();

        return new WorldPoint[] {path1, path2, path3};
    }

    public static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    @Override
    public void onTerminate() {
        this.startingSkillLevel = 0L;
        this.startingSkillExp = 0L;
        status = "Stopping bot";
        gui.setVisible(false);
        hidePaint = true;
        ctx.updateStatus("Logs burned: " + count);

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
            } else if (message.contains("light a fire here")) {
                String startingFmTask = "Starting FM task?: " + FMStarted;
                WorldPoint[] locationPaths = getSelectedLocationPaths(locationName);

                if (START_TILE.equals(locationPaths[0])) {
                    System.out.println("Can't light a fire here: " + START_TILE);
                    START_TILE = locationPaths[1];
                } else if (START_TILE.equals(locationPaths[1])) {
                    System.out.println("Can't light a fire here: " + START_TILE);
                    START_TILE = locationPaths[2];
                } else {
                    System.out.println("Can't light a fire here: " + START_TILE);
                    START_TILE = locationPaths[0];
                }

                FMStarted = false;
                System.out.println(startingFmTask);
                System.out.println("Changing START_TILE to: " + START_TILE);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        long runTime = System.currentTimeMillis() - this.startTime;
        long currentSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.PRAYER);
        long currentSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.PRAYER);
        long SkillLevelsGained = currentSkillLevel - this.startingSkillLevel;
        long SkillExpGained = currentSkillExp - this.startingSkillExp;
        long SkillExpPerHour = (int)((SkillExpGained * 3600000D) / runTime);
        long ActionsPerHour = (int) (count / ((System.currentTimeMillis() - this.startTime) / 3600000.0D));
        Color PhilippineRed = new Color(196, 18, 48);
        Color RaisinBlack = new Color(35, 31, 32, 127);
        if (!hidePaint) {
            g.setColor(RaisinBlack);
            g.fillRoundRect(5, 120, 200, 110, 20, 20);
            g.setColor(PhilippineRed);
            g.drawRoundRect(5, 120, 200, 110, 20, 20);
            g.setColor(PhilippineRed);
            g.drawString("eChaosAltarBot by Esmaabi", 15, 135);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + formatTime(runTime), 15, 150);
            g.drawString("Skill Level: " + this.startingSkillLevel + " (+" + SkillLevelsGained + "), started at " + currentSkillLevel, 15, 165);
            g.drawString("Current Exp: " + currentSkillExp, 15, 180);
            g.drawString("Exp gained: " + SkillExpGained + " (" + (SkillExpPerHour / 1000L) + "k" + " xp/h)", 15, 195);
            g.drawString("Logs used: " + count + " (" + ActionsPerHour + " per/h)", 15, 210);
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
