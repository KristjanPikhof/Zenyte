package eHerbloreBotZenyte;

import eRandomEventSolver.eRandomEventForester;
import net.runelite.api.ChatMessageType;
import simple.hooks.filters.SimpleBank;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ScriptManifest(
        author = "Esmaabi",
        category = Category.HERBLORE,
        description = "<html>"
                + "<p>The most effective anvil herblore bot on Zenyte!</p>"
                + "<p><strong>Features & recommendations:</strong></p>"
                + "<ul>"
                + "<li>Start with a <strong>empty inventory</strong>.</li>"
                + "<li>Start <strong>near bank booth or bank chest</strong>.</li>"
                + "<li>Have unfinished potions and secondaries visible in bank</li>"
                + "<li>Zoom in to <strong>see bank close</strong> for better performance.</li>"
                + "<li>At the moment only <strong>making potions</strong> supported.</li>"
                + "</ul>"
                + "</html>",
        discord = "Esmaabi#5752",
        name = "eHerbloreBotZenyte",
        servers = {"Zenyte"},
        version = "0.3"
)

public class eMain extends TaskScript implements LoopingScript {

    // Constants
    private static final String[] BANK_NAME = {"Bank booth", "Bank chest"};
    private static final String[] BANKER_NAME = {"Banker","Bird's-Eye' Jack", "Arnold Lydspor", "Banker tutor", "Cornelius", "Emerald Benedict", "Eniola", "Fadli", "Financial Wizard", "Financial Seer", "Ghost banker", "Gnome banker", "Gundai", "Jade", "Jumaane", "Magnus Gram", "Nardah Banker", "Odovacar", "Peer the Seer", "Sirsal Banker", "Squire", "TzHaar-Ket-Yil", "TzHaar-Ket-Zuh", "Yusuf"};
    private final static int INVENTORY_BAG_WIDGET_ID = 548;
    private final static int INVENTORY_BAG_CHILD_ID = 58;

    // Variables
    private long startTime = 0L;
    private long startingSkillLevel;
    private long startingSkillExp;
    private long currentExp;
    private int count;
    private int unfinishedPotionID;
    public static String status = null;
    public static int returnItem;
    private static String nameOfItem = null;
    public int secondIngrediente;
    private long lastAnimation = -1;
    public static boolean botStarted;
    private static boolean hidePaint = false;
    private boolean makingSuperCombat = false;
    private boolean makingStamingPotions = false;
    private static String playerGameName;

    // Gui
    private static eGui gui;
    private void initializeGUI() {
        gui = new eGui();
        gui.setVisible(true);
        gui.setLocale(ctx.getClient().getCanvas().getLocale());
    }

    public enum PotionItems {
        SUPER_ENERGY_POTIONS("super energy potions", 103, 2970),
        STAMINA_POTIONS("stamina potions", 3016, 12640),
        ZAMORAK_BREWS("zamorak brews", 111, 247),
        SUPER_STRENGTH_POTIONS("super strenght potions", 105, 225),
        SUPER_ATTACK_POTION("super attack potion", 101, 221),
        SUPER_DEFENCE_POTIONS("super defence potions", 107, 239),
        RANGING_POTIONS("ranging potions", 109, 245),
        MAGIC_POTIONS("magic potions", 2483, 3138),
        PRAYER_POTIONS("prayer potions", 99, 231),
        SUPER_RESTORE("super restores", 3004, 223),
        SARADOMIN_BREWS("saradomin brews", 3002, 6693),
        SUPERANTIPOISONS("superantipoisons", 101, 235),
        BASTION_POISONS("bastion potion", 22443, 245),
        ANTIFIRE_POTIONS("antifire potions", 2483, 241),
        SUPER_ANTIFIRE_POTIONS("super antifire potions", 2452, 21975),
        SUPER_COMBAT_POTIONS("super combat potions", 111, 0); // Set secondIngrediente to 0 since it's not needed for this item

        private final String nameOfItem;
        private final int unfinishedPotionID;
        private final int secondIngrediente;

        PotionItems(String nameOfItem, int unfinishedPotionID, int secondIngrediente) {
            this.nameOfItem = nameOfItem;
            this.unfinishedPotionID = unfinishedPotionID;
            this.secondIngrediente = secondIngrediente;
        }

        public String getNameOfItem() {
            return nameOfItem;
        }

        public int getUnfinishedPotionID() {
            return unfinishedPotionID;
        }

        public int getSecondIngrediente() {
            return secondIngrediente;
        }
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

        tasks.addAll(Arrays.asList());// Adds tasks to our {task} list for execution

        initializeGUI();

        // Other vars
        System.out.println("Started eHerbloreBot!");
        this.ctx.updateStatus("--------------- " + currentTime() + " ---------------");
        this.ctx.updateStatus("-------------------------------");
        this.ctx.updateStatus("           eHerbloreBot        ");
        this.ctx.updateStatus("-------------------------------");

        // Vars
        updateStatus("Setting up bot");
        this.startTime = System.currentTimeMillis();
        this.startingSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.HERBLORE);
        this.startingSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.HERBLORE);
        currentExp = this.ctx.skills.experience(SimpleSkills.Skills.HERBLORE);
        lastAnimation = System.currentTimeMillis();
        botStarted = false;
        unfinishedPotionID = 0;
        secondIngrediente = 0;
        makingSuperCombat = false;
        makingStamingPotions = false;
        count = 0;
        ctx.viewport.pitch(true);
    }

    @Override
    public void onProcess() {
        super.onProcess();

        if (!botStarted) {
            getTaskItem();
            return;
        }

        if (currentExp != this.ctx.skills.experience(SimpleSkills.Skills.HERBLORE)) {
            count++;
            currentExp = this.ctx.skills.experience(SimpleSkills.Skills.HERBLORE);
        }

        if (ctx.dialogue.dialogueOpen() && !makingStamingPotions) {
            int SPACE_BUTTON = KeyEvent.VK_SPACE;
            ctx.keyboard.clickKey(SPACE_BUTTON);
        }

        SimplePlayer localPlayer = ctx.players.getLocal();
        boolean playerIsAnimating = localPlayer.isAnimating();

        if (makingSuperCombat) {
            if (makingSuperCombats()) {
                if (ctx.bank.bankOpen()) {
                    updateStatus("Closing bank task");
                    ctx.bank.closeBank();
                }

                if (!playerIsAnimating && (System.currentTimeMillis() > (lastAnimation + 3000))) {
                    herbSuperCombatsTask();
                } else if (playerIsAnimating) {
                    lastAnimation = System.currentTimeMillis();
                }
            } else {
                openingBank();
            }
        } else if (makingStamingPotions) {
            boolean unfPotionInv = !ctx.inventory.populate().filter(unfinishedPotionID).isEmpty();
            boolean secondIngredienteInv = !ctx.inventory.populate().filter(secondIngrediente).isEmpty();

            if (unfPotionInv && secondIngredienteInv) {
                if (ctx.bank.bankOpen()) {
                    updateStatus("Closing bank task");
                    ctx.bank.closeBank();
                }
                herbStaminaTask();
            } else {
                openingBank();
            }
        } else {

            boolean unfPotionInv = !ctx.inventory.populate().filter(unfinishedPotionID).isEmpty();
            boolean secondIngredienteInv = !ctx.inventory.populate().filter(secondIngrediente).isEmpty();

            if (unfPotionInv && secondIngredienteInv) {
                if (ctx.bank.bankOpen()) {
                    updateStatus("Closing bank task");
                    ctx.bank.closeBank();
                }

                if (!playerIsAnimating && (System.currentTimeMillis() > (lastAnimation + 3000))) {
                    herbTask();
                } else if (playerIsAnimating) {
                    lastAnimation = System.currentTimeMillis();
                }
            } else {
                openingBank();
            }
        }
    }

    // Banking

    private void openingBank() {
        if (ctx.bank.bankOpen()) {
            updateStatus("Depositing items");
            ctx.bank.depositAllExcept(12640); // Amylase
            updateStatus("Withdrawing herblore supplies");
            if (makingStamingPotions) {
                withdrawItem(unfinishedPotionID, 27);
                withdrawItem(secondIngrediente, 108);
            } else if (makingSuperCombat) {
                withdrawItem(unfinishedPotionID, 7);
                withdrawItem(2440, 7);
                withdrawItem(2436, 7);
                withdrawItem(2442, 7);
            } else {
                withdrawItem(unfinishedPotionID, 14);
                ctx.bank.withdraw(secondIngrediente, 14);
            }
            ctx.onCondition(() -> ctx.inventory.populate().population() > 14, 200, 12);
            updateStatus("Closing bank");
            ctx.bank.closeBank();
            return;
        }

        if (!ctx.bank.bankOpen() && !ctx.players.getLocal().isAnimating()) {
            SimpleObject bankChest = getBankChest();
            if (bankChest != null) {
                updateStatus("Refilling supplies");
                bankChest.click(1);
                ctx.onCondition(() -> ctx.bank.bankOpen(), 200, 12);
            } else {
                SimpleNpc bankerName = getBanker();
                if (bankerName != null) {
                    updateStatus("Refilling supplies");
                    bankerName.click("Bank");
                    ctx.onCondition(() -> ctx.bank.bankOpen(), 200, 12);
                }
            }
        }
    }

    private void withdrawItem(int id, int amount) {
        SimpleItem itemBank = ctx.bank.populate().filter(id).next();
        SimpleItem itemInv = ctx.inventory.populate().filter(id).next();
        if (itemBank != null && itemInv == null) {
            updateStatus("Withdrawing " + itemBank.getName());
            ctx.bank.withdraw(id, amount);
        }
    }
    private SimpleObject getBankChest() {
        SimpleObject bankChest = ctx.objects.populate().filter(BANK_NAME).nearest().next();
        if (bankChest != null && bankChest.distanceTo(ctx.players.getLocal()) <= 10 && bankChest.validateInteractable()) {
            return bankChest;
        }
        return null;
    }

    private SimpleNpc getBanker() {
        SimpleNpc bankerName = ctx.npcs.populate().filter(BANKER_NAME).nearest().next();
        if (bankerName != null && bankerName.distanceTo(ctx.players.getLocal()) <= 10 && bankerName.validateInteractable()) {
            return bankerName;
        }
        return null;
    }


    // Herblore

    private void herbTask() {
        SimpleItem unfPotionInv = ctx.inventory.populate().filter(unfinishedPotionID).reverse().next();
        SimpleItem secondIngredienteInv = ctx.inventory.populate().filter(secondIngrediente).reverse().next();
        boolean suppliesValid = unfPotionInv != null || secondIngredienteInv != null;

        if (!makingStamingPotions) {
            if (ctx.players.getLocal().isAnimating()) {
                return;
            }
        }

        if (!suppliesValid) {
            openingBank();
        } else {
            updateStatus("Making " + nameOfItem);
            clickOnBag();
            ctx.inventory.itemOnItem(unfPotionInv, secondIngredienteInv);
            lastAnimation = System.currentTimeMillis();
        }
    }

    private void herbStaminaTask() {
        SimpleItem unfPotionInv = ctx.inventory.populate().filter(unfinishedPotionID).reverse().next();
        SimpleItem secondIngredienteInv = ctx.inventory.populate().filter(secondIngrediente).next();
        boolean suppliesValid = unfPotionInv != null || secondIngredienteInv != null;

        if (!suppliesValid) {
            openingBank();
        } else {
            if (!ctx.dialogue.dialogueOpen()) {
                updateStatus("Making " + nameOfItem);
                ctx.inventory.itemOnItem(unfPotionInv, secondIngredienteInv);
            } else {
                int SPACE_BUTTON = KeyEvent.VK_SPACE;
                ctx.keyboard.clickKey(SPACE_BUTTON);
                ctx.sleepCondition(() -> unfPotionInv != null, 30000); // maybe ?
            }
        }
    }

    private void herbSuperCombatsTask() {
        SimpleItem unfPotionInv = ctx.inventory.populate().filter(unfinishedPotionID).next();
        SimpleItem superStrenght = ctx.inventory.populate().filter(2440).next();
        SimpleItem superAttack = ctx.inventory.populate().filter(2436).next();
        SimpleItem superDefence = ctx.inventory.populate().filter(2442).next();
        boolean suppliesValid = unfPotionInv != null && superStrenght != null && superAttack != null && superDefence != null;

        if (ctx.players.getLocal().isAnimating()) {
            return;
        }

        if (!suppliesValid) {
            openingBank();
        } else {
            updateStatus("Making " + nameOfItem);
            ctx.inventory.itemOnItem(unfPotionInv, superStrenght);
            lastAnimation = System.currentTimeMillis();
        }
    }

    private boolean makingSuperCombats() {
        boolean unfPotionInv = !ctx.inventory.populate().filter(unfinishedPotionID).isEmpty();
        boolean superStrenght = !ctx.inventory.populate().filter(2440).isEmpty();
        boolean superAttack = !ctx.inventory.populate().filter(2436).isEmpty();
        boolean superDefence = !ctx.inventory.populate().filter(2442).isEmpty();
        return unfPotionInv && superStrenght && superAttack && superDefence;
    }

    private void getTaskItem() {
        PotionItems item = PotionItems.values()[returnItem];
        nameOfItem = item.getNameOfItem();
        unfinishedPotionID = item.getUnfinishedPotionID();
        secondIngrediente = item.getSecondIngrediente();
        makingStamingPotions = item == PotionItems.STAMINA_POTIONS;
        makingSuperCombat = item == PotionItems.SUPER_COMBAT_POTIONS;
    }

    private void clickOnBag() {
        SimpleWidget inventoryBagWidget = ctx.widgets.getWidget(INVENTORY_BAG_WIDGET_ID, INVENTORY_BAG_CHILD_ID);
        if (inventoryBagWidget != null) {
            inventoryBagWidget.click(0);
        }
    }

    //Utility
    public static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void updateStatus(String newStatus) {
        status = newStatus;
        ctx.updateStatus(status);
        System.out.println(status);
    }

    public String getPlayerName() {
        if (playerGameName == null) {
            playerGameName = ctx.players.getLocal().getName();
        }
        return playerGameName;
    }

    @Override
    public void onTerminate() {

        // Other vars
        this.startingSkillLevel = 0L;
        this.startingSkillExp = 0L;
        this.count = 0;
        secondIngrediente = 0;
        unfinishedPotionID = 0;
        makingSuperCombat = false;
        makingStamingPotions = false;
        gui.setVisible(false);

        this.ctx.updateStatus("-------------- " + currentTime() + " --------------");
        this.ctx.updateStatus("----------------------");
        this.ctx.updateStatus("Thank You & Good Luck!");
        this.ctx.updateStatus("----------------------");
    }

    @Override
    public void onChatMessage(ChatMessage m) {
        ChatMessageType getType = m.getType();
        net.runelite.api.events.ChatMessage getEvent = m.getChatEvent();
        playerGameName = getPlayerName();

        if (m.getMessage() == null) {
            return;
        }

        if (getType == ChatMessageType.PUBLICCHAT) {
            String senderName = getEvent.getName();

            // Remove any text within angle brackets and trim
            senderName = senderName.replaceAll("<[^>]+>", "").trim();

            if (senderName.contains(playerGameName)) {
                ctx.updateStatus(currentTime() + " Someone asked for you");
                ctx.updateStatus(currentTime() + " Stopping script");
                ctx.stopScript();
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
        long runTime = System.currentTimeMillis() - this.startTime;
        long currentSkillLevel = this.ctx.skills.realLevel(SimpleSkills.Skills.HERBLORE);
        long currentSkillExp = this.ctx.skills.experience(SimpleSkills.Skills.HERBLORE);
        long skillLevelsGained = currentSkillLevel - this.startingSkillLevel;
        long skillExpGained = currentSkillExp - this.startingSkillExp;

        // Calculate experience and actions per hour
        long skillExpPerHour = skillExpGained * 3600000L / runTime;
        long actionsPerHour = count * 3600000L / (System.currentTimeMillis() - this.startTime);

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
            g.drawString("eHerbloreBot by Esmaabi", 15, 135);
            g.setColor(Color.WHITE);
            g.drawString("Runtime: " + formatTime(runTime), 15, 150);
            g.drawString("Skill Level: " + currentSkillLevel + " (+" + skillLevelsGained + "), started at " + this.startingSkillLevel, 15, 165);
            g.drawString("Current Exp: " + currentSkillExp, 15, 180);
            g.drawString("Exp gained: " + skillExpGained + " (" + (skillExpPerHour / 1000L) + "k xp/h)", 15, 195);
            g.drawString("Potions made: " + count + " (" + actionsPerHour + " per/h)", 15, 210);
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