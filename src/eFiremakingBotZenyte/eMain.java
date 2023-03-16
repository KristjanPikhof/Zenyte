package eFiremakingBotZenyte;

import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleBank;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.script.Script;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static eFiremakingBotZenyte.eGui.locationName;

@ScriptManifest(author = "Esmaabi", category = Category.FIREMAKING, description =
        "<br>Most effective firemaking bot on Zenyte! <br><br><b>Features & recommendations:</b><br><br>" +
        "<ul>" +
        "<li>You must start at chosen bank;</li>" +
        "<li>Supported locations: Falador East, Varrock East, Grand Exchange</li>" +
        "<li>Supported trees: all normal trees from redwood to logs.</li></ul>", discord = "Esmaabi#5752",
        name = "eFiremakingBotZenyte", servers = { "Zenyte" }, version = "1")

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

    //Items
    public static String woodName;
    static String bankName = "Banker";
    static String bankOpen = "Bank";
    static String tinderBox = "Tinderbox";



    //Locations
    private WorldPoint START_TILE;
    private final WorldPoint nearBankLocVarrock = new WorldPoint(3254, 3426, 0);
    private final WorldPoint nearBankLocFalador = new WorldPoint(3012, 3360, 0);
    private final WorldPoint nearBankLocGE = new WorldPoint(3163, 3482, 0);

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
        System.out.println("Started eFiremakingBot!");
        gui = new eGui();
        gui.setVisible(true);
        gui.setLocale(ctx.getClient().getCanvas().getLocale());

        status = "Setting up bot";
        this.startTime = System.currentTimeMillis(); //paint
        this.startingSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.FIREMAKING);
        this.startingSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING);
        currentExp = this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING);// for actions counter by xp drop
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

            if (currentExp != this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING)) { //action counter
                count++;
                currentExp = this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING);
            }

            if (!FMStarted) {
                if (logsInInventory()) {
                    if (!ctx.players.getLocal().getLocation().equals(START_TILE)) {
                        status = "Running to start location";
                        ctx.pathing.step(START_TILE);
                        ctx.sleepCondition(() -> ctx.players.getLocal().getLocation().equals(START_TILE), 4000);
                    } else {
                        FMStarted = true;
                        System.out.println("Starting FM task?: " + FMStarted);
                    }
                } else {
                    bankTask();
                }
            } else {
                ctx.viewport.angle(180);
                ctx.viewport.pitch(true);

                if (logsInInventory()) {
                    if (ctx.players.getLocal().getAnimation() == -1) {
                        SimpleItem tinderbox = ctx.inventory.populate().filter(tinderBox).next();
                        SimpleItem woodInventory = ctx.inventory.populate().filter(woodName).next();

                        if (tinderbox != null && woodInventory != null) {
                            status = "Burning " + woodName.toLowerCase() + "...";
                            tinderbox.click("Use");
                            ctx.sleep(100);
                            woodInventory.click(0);
                            WorldPoint cached = ctx.players.getLocal().getLocation();
                            ctx.sleepCondition(() -> ctx.players.getLocal().getLocation() != cached, 5000);
                        }
                    }

                } else if (!logsInInventory() && !ctx.players.getLocal().isAnimating()) {
                    bankTask();
                }
            }
        }
    }

    private boolean logsInInventory() {
        return !ctx.inventory.populate().filter(woodName).isEmpty();
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

                if (!ctx.bank.populate().filter(woodName).isEmpty()) {
                    ctx.bank.withdraw(woodName, SimpleBank.Amount.ALL);
                    ctx.onCondition(this::logsInInventory, 250, 10);
                }

                if (ctx.bank.populate().filter(woodName).isEmpty()) {
                    status = "Out of " + woodName.toLowerCase();
                    ctx.updateStatus("Stopping script");
                    ctx.updateStatus("Out of " + woodName.toLowerCase());
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
        String selectedItem = (String) eGui.locationName.getSelectedItem();
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
        String selectedItem = (String) eGui.locationName.getSelectedItem();
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
                ctx.updateStatus(currentTime() + " Stopping script");
                ctx.stopScript();
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
        long currentSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.FIREMAKING);
        long currentSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.FIREMAKING);
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
            g.drawString("eFiremakingBot by Esmaabi", 15, 135);
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
