package eChaosAltarBot;

import BotUtils.eActions;
import eApiAccess.eAutoResponser;
import net.runelite.api.ChatMessageType;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.script.Script;
import simple.robot.utils.WorldArea;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


@ScriptManifest(author = "Esmaabi", category = Category.PRAYER, description =
        "<br>Most effective Chaos Altar prayer training bot on Zenyte! <br><br>" +
                "<b>Features & recommendations:</b><br><br>" +
                "<ul>" +
                "<li>You must near Chaos Altar;</li>" +
                "<li>You must have noted / ashes with you and enough coins in inventory;</li>" +
                "<li>Bot supports variety of bones.</li>" +
                "<li>Bot will log out if out of bones or coins.</li>" +
                "<li>Bot will log out there are more than (chosen) players around</li>" +
                "</ul>", discord = "Esmaabi#5752",
        name = "eChaosAltarBotZenyte", servers = { "Zenyte" }, version = "0.5")

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
    public static boolean logOut = false;
    private long lastAnimation = -1;

    //Items
    public static String bonesName;

    //Locations
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
    private static String playerGameName;

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
        botStarted = false;
        hidePaint = false;
        logOut = false;
        eActions.menuActionMode = true;
    }

    @Override
    public void onProcess() {

        if (botStarted) {

            if (currentExp != this.ctx.skills.experience(SimpleSkills.Skills.PRAYER)) { //action counter
                count++;
                currentExp = this.ctx.skills.experience(SimpleSkills.Skills.PRAYER);
            }

            logOut = getLogoutCount() > 0 ? true : false;

            if (logOut) {
                if (ctx.players.populate().population() > getLogoutCount()) {
                    status = "Logging out";
                    ctx.updateStatus("Logging out.");
                    ctx.updateStatus("Too many players!");
                    ctx.sendLogout();
                    ctx.sendLogout();
                }
            }

            if (ctx.inventory.populate().filter(bonesName).isEmpty() || ctx.inventory.populate().filter("Coins").isEmpty()) {
                status = "Logging out";
                ctx.updateStatus("Logging out.");
                ctx.updateStatus("Out of bones / coins!");
                ctx.sendLogout();
            }

            if (ctx.pathing.inArea(chaosAltarArea)) {

                if (ctx.inventory.populate().filter(bonesName).filter(i -> !i.isStackable()).population() != 0) {

                    if (spotReachable()) {
                        status = "Closing doors";
                        SimpleObject doors = ctx.objects.populate().filter("Large door").next();
                        if (doors != null && doors.validateInteractable()) {
                            BotUtils.eActions.interactWith(doors, "Open");
                            //doors.click("Open");
                            //ctx.sleepCondition(() -> !spotReachable(), 5000);
                            ctx.onCondition(() -> !spotReachable(), 250, 20);
                        }
                    }

                    if (ctx.players.getLocal().getAnimation() == -1 && (System.currentTimeMillis() > (lastAnimation + 3000))) {
                        sacrificeTask();
                    } else if (ctx.players.getLocal().getAnimation() != -1) {
                        lastAnimation = System.currentTimeMillis();
                    }

                } else {
                    if (!spotReachable()) {
                        status = "Opening doors";
                        SimpleObject doors = ctx.objects.populate().filter("Large door").next();
                        if (doors != null && doors.validateInteractable()) {
                            //doors.click("Open");
                            BotUtils.eActions.interactWith(doors, "Open");
                            //ctx.sleepCondition(() -> ctx.pathing.inMotion(), 5000);
                            ctx.onCondition(this::spotReachable, 250, 20);
                        }
                    } else {
                        if (!ctx.dialogue.dialogueOpen()) {
                            status = "Using bones on druid";
                            SimpleNpc druid = ctx.npcs.populate().filter("Elder Chaos druid").next();
                            SimpleItem bonesInventory = ctx.inventory.populate().filter(bonesName).filter(SimpleItem::isStackable).next();
                            if (druid != null && druid.validateInteractable() && bonesInventory != null && bonesInventory.validateInteractable()) {
                                bonesInventory.click("Use");
                                ctx.sleep(200);
                                //druid.click(0);
                                BotUtils.eActions.interactWith(druid, "Use");
                                //ctx.sleepCondition(() -> ctx.dialogue.dialogueOpen(), 8000);
                                ctx.onCondition(() -> ctx.dialogue.dialogueOpen(), 250, 20);
                            }
                        }
                    }
                }
            } else {
                if (ctx.dialogue.dialogueOpen()) {
                    status = "Progressing dialogue";
                    ctx.dialogue.clickDialogueOption(3);
                    int bonesInv = bonesPopulation();
                    ctx.onCondition(() -> bonesPopulation() > bonesInv, 250, 10);
                }

                if (ctx.inventory.populate().filter(bonesName).filter(i -> !i.isStackable()).population() != 0) {
                    status = "Walking back in church";
                    ctx.pathing.step(2956,3820);
                    //ctx.sleepCondition(() -> ctx.pathing.inMotion(), 2000);
                    ctx.onCondition(() -> ctx.pathing.inMotion(), 250, 10);
                }
            }
        }
    }

    private void sacrificeTask() {
        SimpleItem bonesInventory = ctx.inventory.populate().filter(bonesName).filter(i -> !i.isStackable()).reverse().next();
        SimpleObject chaosAltar = ctx.objects.populate().filter("Chaos altar").next();
        status = "Sacrificing task";
        if (bonesInventory != null && bonesInventory.validateInteractable() && chaosAltar != null && chaosAltar.validateInteractable()) {
            ctx.viewport.turnTo(chaosAltar);
            ctx.viewport.pitch();
            bonesInventory.click("Use");
            ctx.sleep(200);
            chaosAltar.click("Use");
            //BotUtils.eActions.interactWith(chaosAltar, String.valueOf(GAME_OBJECT_FIRST_OPTION));
            int bonesInv = bonesPopulation();
            ctx.onCondition(() -> bonesPopulation() < bonesInv, 250, 10);
        }
    }

    private int getLogoutCount() {
        String count = Objects.requireNonNull(eGui.minPlayersInArea.getSelectedItem()).toString();
        int minPlayers = -1;
        switch (count) {
            case "Disable logout":
                minPlayers = 0;
                break;
            case "Only you":
                minPlayers = 1;
                break;
            case "You + 1":
                minPlayers = 2;
                break;
            case "You + 2":
                minPlayers = 3;
                break;
            case "You + 3":
                minPlayers = 4;
                break;
        }
        return minPlayers;
    }


    private boolean spotReachable() {
        return ctx.pathing.reachable(new WorldPoint(2956, 3817, 0));
    }

    private int bonesPopulation() {
        return ctx.inventory.populate().filter(bonesName).population();
    }

    public static String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    @Override
    public void onTerminate() {

        this.ctx.updateStatus("-------------- " + getCurrentTimeFormatted() + " --------------");
        this.ctx.updateStatus("We have sacrificed " + count + " bones/ashes.");
        this.ctx.updateStatus("-----------------------------------");
        this.ctx.updateStatus("----- Thank You & Good Luck! ------");
        this.ctx.updateStatus("-----------------------------------");


        this.startingSkillLevel = 0L;
        this.startingSkillExp = 0L;
        status = "Stopping bot";
        gui.setVisible(false);
        hidePaint = true;
        bonesName = null;
        logOut = false;

    }

    @Override
    public void onChatMessage(ChatMessage m) {
        ChatMessageType getType = m.getType();
        net.runelite.api.events.ChatMessage getEvent = m.getChatEvent();
        playerGameName = eAutoResponser.getPlayerName(ctx);

        if (m.getMessage() == null) {
            return;
        }

        if (getType == ChatMessageType.PUBLICCHAT) {
            String senderName = getEvent.getName();

            // Remove any text within angle brackets and trim
            senderName = senderName.replaceAll("<[^>]+>", "").trim();

            if (senderName.contains(playerGameName)) {
                ctx.updateStatus(getCurrentTimeFormatted() + " Someone asked for you");
                ctx.updateStatus(getCurrentTimeFormatted() + " Stopping script");
                ctx.stopScript();
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
            g.drawString("Skill Level: " + currentSkillLevel + " (+" + SkillLevelsGained + "), started at " + this.startingSkillLevel, 15, 165);
            g.drawString("Current Exp: " + currentSkillExp, 15, 180);
            g.drawString("Exp gained: " + SkillExpGained + " (" + (SkillExpPerHour / 1000L) + "k" + " xp/h)", 15, 195);
            g.drawString("Bones used: " + count + " (" + ActionsPerHour + " per/h)", 15, 210);
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
