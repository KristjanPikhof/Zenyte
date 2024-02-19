package eAutoFighterZenyte;

import eApiAccess.eAutoResponser;
import eAutoFighterZenyte.data.eFood;
import eAutoFighterZenyte.data.eLoots;
import org.json.JSONArray;
import org.json.JSONObject;
import simple.hooks.filters.SimplePrayers;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.wrappers.SimpleNpc;
import net.runelite.api.ItemComposition;
import simple.robot.api.ClientContext;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.spec.KeySpec;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.border.EmptyBorder;


public class AutoFighterUI extends Component {

    // Constants
    public static final String[] ACTIVITY = {"Slayer", "PVM", "Chilling", "Skilling", "Money making"};
    public static final String[] AUTO_RETALIATE_OPTIONS = {"Default", "Force ON", "Force OFF"};
    public static final String[] PRAYER_RESTOR = {"Prayer potion", "Super restore", "Sanfew serum"};
    public static final String[] PRAYER_FLICK = {
            "Piety (lvl 70)", "Rigour (lvl 74)", "Augury (lvl 77)", "Chivalry (lvl 60)", "Mystic Might (lvl 45)", "Eagle Eye (lvl 44)",
            "Ultimate Strength (lvl 31)", "Mystic Lore (lvl 27)", "Hawk Eye (lvl 26)", "Superhuman Strength (lvl 13)",
            "Mystic Will (lvl 9)", "Sharp Eye (lvl 8)", "Burst of Strength (lvl 4)"
    };
    public static final String[] SKILL_NAMES = {"Attack", "Strength", "Defence", "Ranged", "Magic"};
    public static final String[] STATS_BOOST = {
            "None", "Divine super combat potion", "Divine ranging potion", "Divine bastion potion",
            "Divine super attack potion", "Divine super strength potion", "Divine super defence potion", "Divine magic potion",
            "Super combat potion", "Super strength", "Super attack", "Super defence", "Ranging potion", "Bastion potion", "Magic potion"
    };
    private static final Logger logger = Logger.getLogger(AutoFighterUI.class.getName());

    /// Auto Fighter related

    // Variables
    public AutoFighter combat;
    public JButton btnApplyAndStart;
    public JButton btnPauseBot;
    public JComboBox<eFood> comboBoxFoods;
    public JComboBox<String> selectAutoRetaliateComboBox;
    public JComboBox<String> selectPrayerRestoreComboBox;
    public JComboBox<String> selectPrayerFlickComboBox;
    public JComboBox<String> selectSkillToBoostComboBox;
    public JComboBox<String> selectStatsBoostComboBox;
    public JCheckBox chbxuseBuryBones;
    public JCheckBox chkxdrinkStatsBoost;
    public JCheckBox chbxnpcReachable;
    public JCheckBox chbxMainAsSpecWeap;
    public JCheckBox chbxQuickPrayers;
    public JCheckBox chbxuseMouseActions;
    public JList<String> listLoot;
    public JList<String> listNearbyMonsters;
    public JList<String> listSelectedMonsters;
    public JPanel contentPane;
    public JPanel extrasPanel;
    public JPanel generalPanel;
    public JScrollPane scrollPaneLoot;
    public JScrollPane scrollPaneNearbyMonsters;
    public JScrollPane scrollPaneSelectedMonsters;
    public JSpinner lootWithin;
    public JSpinner spinnerBoostStatsAt;
    public JSpinner spinnerHealAt;
    public DefaultListModel<String> modelLoot = new DefaultListModel<String>();
    public DefaultListModel<String> modelNearbyMonsters = new DefaultListModel<String>();
    public DefaultListModel<String> modelSelectedMonsters = new DefaultListModel<String>();
    public JFrame theGui;
    public JLabel lblMonsterList;
    private JButton btnClearLoots;
    private JButton btnLoadJson;
    private JButton btnSaveJson;
    private JCheckBox chckbxEatforSpace;
    private JCheckBox chckbxPrayFlick;
    private JCheckBox chckbxTeleAfterTask;
    private ClientContext ctx;

    /// GPT related

    // Encryption
    private static final String ENCRYPTION_ALGO = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_ALGO = "PBKDF2WithHmacSHA256";
    private static final String SALT = "aRandomSaltForPBE"; // Change this to a long random string!
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;
    JPasswordField passphraseField = new JPasswordField();

    // Constants
    private static final String API_KEY_FILE_PATH = "esmaabi_gpt_api_key_data.enc";
    public static final String[] GPT_MODELS = {"GPT 3.5 Turbo", "GPT 4"};

    // Variables
    public static boolean gptHasBeenStarted = false;
    public static JPasswordField apiKeyPasswordField;
    public JPanel gptPanel;
    public JCheckBox activateChatGPT;
    public JCheckBox gptActiveAtHome;
    public JCheckBox properGrammar;
    private JButton loadButton;
    private JButton saveButton;
    private JComboBox<String> chatActivityComboBox;
    private JComboBox<String> selectGptModelComboBox;


    public AutoFighterUI(AutoFighter combatBot) {
        try {
            this.combat = combatBot;
            SwingUtilities.invokeLater(this::initializeGUI);
        } catch (Exception e) {
            logger.info("An exception occurred while running AutoFighterUI " + e);
        }
    }

    private void initializeGUI() {
        // Initialize the main JFrame
        theGui = new JFrame();
        theGui.setResizable(false);
        theGui.setTitle("eAIO Fighter with GPT integration");
        theGui.setBounds(100, 100, 450, 490);
        theGui.setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getResource("data/esmaabi-icon.png"))).getImage());
        theGui.setLocationRelativeTo(combat.ctx.mouse.getComponent());

        // Set content pane properties
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        theGui.setContentPane(contentPane);
        contentPane.setLayout(null);

        // Initialize components and setup
        initComponents();
        setupDropArray();
        onRefreshNearbyMonsters();
        pauseBot();

        // Display the GUI
        theGui.repaint();
        theGui.setVisible(true);
    }

    public void initComponents() {

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBounds(10, 11, 414, 369);
        contentPane.add(tabbedPane);

        //// General Settings tab
        generalPanel = new JPanel();
        generalPanel.setName("");
        tabbedPane.addTab("General", null, generalPanel, null);
        generalPanel.setLayout(null);

        // Choosing food
        JLabel lblFoodType = new JLabel("Choose food:");
        lblFoodType.setBounds(10, 11, 80, 20);
        generalPanel.add(lblFoodType);

        comboBoxFoods = new JComboBox<eFood>(new DefaultComboBoxModel<eFood>(eFood.values()));
        comboBoxFoods.setBounds(95, 9, 110, 23);
        comboBoxFoods.setBackground(Color.GRAY);
        comboBoxFoods.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof eFood) {
                    setText(((eFood) value).getName());
                }

                return this;
            }
        });
        generalPanel.add(comboBoxFoods);

        // Choosing heal HP
        JLabel lblHealAt = new JLabel("HP to heal:");
        lblHealAt.setBounds(213, 11, 62, 19);
        generalPanel.add(lblHealAt);

        spinnerHealAt = new JSpinner();
        spinnerHealAt.setModel(new SpinnerNumberModel(55, 1, 99, 1));
        spinnerHealAt.setBounds(285, 9, 50, 23);
        generalPanel.add(spinnerHealAt);

        // Label NPC(s) nearby
        JLabel lblNearbyMonsters = new JLabel("Nearby NPC(s)");
        lblNearbyMonsters.setBounds(20, 44, 96, 19);
        generalPanel.add(lblNearbyMonsters);

        // Refresh NPC button

        JButton btnRefreshNearbyMonsters = new JButton("Refresh");
        btnRefreshNearbyMonsters.setToolTipText("Refresh NPC list below");
        btnRefreshNearbyMonsters.setBounds(115, 40, 80, 23);
        generalPanel.add(btnRefreshNearbyMonsters);
        btnRefreshNearbyMonsters.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        onRefreshNearbyMonsters();
                    }
                });
            }
        });

        // Nearby NPC list Pane
        scrollPaneNearbyMonsters = new JScrollPane();
        scrollPaneNearbyMonsters.setBounds(10, 74, 187, 124);
        generalPanel.add(scrollPaneNearbyMonsters);
        listNearbyMonsters = new JList<String>(modelNearbyMonsters);
        scrollPaneNearbyMonsters.setViewportView(listNearbyMonsters);
        listNearbyMonsters.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listNearbyMonsters.getSelectedIndex() == listNearbyMonsters.locationToIndex(e.getPoint()) && e.getClickCount() >= 2) {
                    String element = listNearbyMonsters.getSelectedValue();
                    modelNearbyMonsters.removeElement(element);
                    modelSelectedMonsters.addElement(element);
                }
            }
        });

        // Selected NPCs text
        lblMonsterList = new JLabel("Selected NPC(s) list");
        lblMonsterList.setBounds(213, 44, 119, 23);
        generalPanel.add(lblMonsterList);

        // Selected NPC scroll Panel
        listSelectedMonsters = new JList<String>(modelSelectedMonsters);
        listSelectedMonsters.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listSelectedMonsters.getSelectedIndex() == listSelectedMonsters.locationToIndex(e.getPoint()) && e.getClickCount() >= 2) {
                    String element = listSelectedMonsters.getSelectedValue();
                    modelSelectedMonsters.removeElement(element);
                }
            }
        });
        listSelectedMonsters.setBounds(193, 145, 206, 185);

        scrollPaneSelectedMonsters = new JScrollPane();
        scrollPaneSelectedMonsters.setBounds(207, 74, 192, 124);
        generalPanel.add(scrollPaneSelectedMonsters);
        scrollPaneSelectedMonsters.setViewportView(listSelectedMonsters);

        // Items to loot button
        JLabel lblItemsToLoot = new JLabel("Items To Loot");
        lblItemsToLoot.setBounds(20, 204, 135, 23);
        generalPanel.add(lblItemsToLoot);

        // Loot scroll panel
        scrollPaneLoot = new JScrollPane();
        scrollPaneLoot.setBounds(10, 223, 187, 107);
        generalPanel.add(scrollPaneLoot);

        listLoot = new JList<String>(modelLoot);
        scrollPaneLoot.setViewportView(listLoot);

        // Add loot button
        JButton btnAddLoot = new JButton("+");
        btnAddLoot.setBackground(new Color(50, 205, 50));
        btnAddLoot.setToolTipText("Add the ID of item to pickup");
        btnAddLoot.setBounds(210, 231, 59, 23);
        generalPanel.add(btnAddLoot);
        btnAddLoot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("After adding: " + modelLoot.getSize());
                String input = JOptionPane.showInputDialog("Enter item ID");
                if (input.contains(",")) {
                    String[] items = input.trim().split(",");
                    for (String id : items) {
                        ItemComposition itemDefinitions = combat.ctx.definitions.getItemDefinition(Integer.parseInt(id));
                        if (itemDefinitions != null) {
                            modelLoot.addElement("[" + itemDefinitions.getName() + ", " + itemDefinitions.getId() + "]");
                        }
                    }
                } else {
                    ItemComposition itemDefinitions = combat.ctx.definitions.getItemDefinition(Integer.parseInt(input));
                    if (itemDefinitions != null) {
                        modelLoot.addElement("[" + itemDefinitions.getName() + ", " + itemDefinitions.getId() + "]");
                    }
                }
                scrollPaneLoot.revalidate();
                scrollPaneLoot.repaint();
                System.out.println("After adding: " + modelLoot.getSize());
            }
        });

        // Remove loot button
        JButton btnRemoveLoot = new JButton("-");
        btnRemoveLoot.setBackground(new Color(190, 36, 36));
        btnRemoveLoot.setToolTipText("First click the item on the 's' list and then remove it");
        btnRemoveLoot.setBounds(210, 265, 59, 23);
        generalPanel.add(btnRemoveLoot);
        btnRemoveLoot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Before removing: " + modelLoot.getSize());
                modelLoot.removeElement(listLoot.getSelectedValue());
                scrollPaneLoot.revalidate();
                scrollPaneLoot.repaint();
                System.out.println("After removing: " + modelLoot.getSize());
            }
        });

        // Clear loots button
        btnClearLoots = new JButton("Clear");
        btnClearLoots.setToolTipText("Will clear whole 'Items to Loot' list");
        btnClearLoots.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Before clearing: " + modelLoot.getSize());
                modelLoot.clear();
                scrollPaneLoot.revalidate();
                scrollPaneLoot.repaint();
                System.out.println("After clearing: " + modelLoot.getSize());
            }
        });
        btnClearLoots.setBounds(210, 296, 59, 23);
        generalPanel.add(btnClearLoots);

        // Teleport if slayer task is finished
        chckbxTeleAfterTask = new JCheckBox("Tele after Task");
        chckbxTeleAfterTask.setToolTipText("Will teleport home using Zenyte tab after Slayer task done");
        chckbxTeleAfterTask.setBounds(275, 215, 135, 23);
        generalPanel.add(chckbxTeleAfterTask);

        // Eat for space button
        chckbxEatforSpace = new JCheckBox("Eat for Space");
        chckbxEatforSpace.setToolTipText("Will eat if inventory is full and valuable drop appears");
        chckbxEatforSpace.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                combat.eatForSpace = chckbxEatforSpace.isSelected();
            }
        });
        chckbxEatforSpace.setBounds(275, 245, 135, 23);
        generalPanel.add(chckbxEatforSpace);

        // Quick prayer button
        chbxQuickPrayers = new JCheckBox("Quick pray");
        chbxQuickPrayers.setToolTipText("Drinks prayer potions in inventory and enables quick prayers");
        chbxQuickPrayers.setBounds(275, 275, 135, 23);
        generalPanel.add(chbxQuickPrayers);

        // Player flick button
        chckbxPrayFlick = new JCheckBox("Pray flick");
        chckbxPrayFlick.setToolTipText("Will flick chosen prayer from \"Extras\" tab and drink prayer potions");
        chckbxPrayFlick.setBounds(275, 305, 135, 23);
        generalPanel.add(chckbxPrayFlick);

        //// GPT Settings tab
        gptPanel = new JPanel();
        gptPanel.setName("");
        tabbedPane.addTab("GPT settings", null, gptPanel, null);
        gptPanel.setLayout(null);

        // Information
        JLabel lblGptInfo = new JLabel("To use automatic GPT answering please enter your API key.");
        lblGptInfo.setBounds(60, 10, 390, 23);
        gptPanel.add(lblGptInfo);
        JLabel lblGptInfo2 = new JLabel("Get the API key from https://platform.openai.com/account/api-keys");
        lblGptInfo2.setBounds(10, 30, 390, 23);
        gptPanel.add(lblGptInfo2);

        // Activate ChatGPT checkbox
        activateChatGPT = new JCheckBox("Activate ChatGPT");
        activateChatGPT.setToolTipText("Enable or disable ChatGPT answering");
        activateChatGPT.setBounds(125, 64, 150, 23);
        gptPanel.add(activateChatGPT);
        gptPanel.add(activateChatGPT);

        // GPT models
        selectGptModelComboBox = new JComboBox<>(GPT_MODELS);
        selectGptModelComboBox.setBounds(213, 94, 150, 23);
        gptPanel.add(selectGptModelComboBox);
        selectGptModelComboBox.setToolTipText("What GPT model you'd like to use?");
        JLabel lblGptModel = new JLabel("Select GPT model:");
        lblGptModel.setBounds(65, 94, 109, 23);
        gptPanel.add(lblGptModel);

        // Activity type
        chatActivityComboBox = new JComboBox<>(ACTIVITY);
        chatActivityComboBox.setBounds(213, 124, 150, 23);
        gptPanel.add(chatActivityComboBox);
        chatActivityComboBox.setToolTipText("Choose what you want the bot to answer");
        JLabel lblChooseActivity = new JLabel("Select activity:");
        lblChooseActivity.setBounds(65, 124, 109, 23);
        gptPanel.add(lblChooseActivity);

        // GPT active at home
        gptActiveAtHome = new JCheckBox("GPT active at home area?");
        gptActiveAtHome.setToolTipText("Click if GPT must be active at home area");
        gptActiveAtHome.setBounds(125, 154, 200, 23);
        gptPanel.add(gptActiveAtHome);

        // Grammar checkbox
        properGrammar = new JCheckBox("Use proper grammar?");
        properGrammar.setToolTipText("Click if you don't want to use abbreviations, etc");
        properGrammar.setBounds(125, 184, 200, 23);
        gptPanel.add(properGrammar);

        // API Key
        JLabel lblApiKey = new JLabel("Insert API key: ");
        lblApiKey.setBounds(65, 246, 109, 23);
        apiKeyPasswordField = new JPasswordField();
        apiKeyPasswordField.setPreferredSize(new Dimension(150, apiKeyPasswordField.getPreferredSize().height));
        apiKeyPasswordField.setBounds(213, 246, 150, 23);
        apiKeyPasswordField.setEchoChar('\u2022'); // Set the echo character as a dot or asterisk
        apiKeyPasswordField.setToolTipText("Supported: GPT 3.5-turbo & GPT 4"); // Set the tooltip text for the API key field
        gptPanel.add(lblApiKey);
        gptPanel.add(apiKeyPasswordField);

        // Save Button
        saveButton = new JButton("Save");
        saveButton.setBounds(95, 280, 100, 23);
        gptPanel.add(saveButton);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveApiKeyToFile(true);
            }
        });
        saveButton.setToolTipText("Save the API key to a file"); // Set the tooltip text for the Save button

        // Load Button
        loadButton = new JButton("Load");
        loadButton.setBounds(213, 280, 100, 23);
        gptPanel.add(loadButton);
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadApiKeyFromFile(true);
            }
        });
        loadButton.setToolTipText("Load the API key from a file"); // Set the tooltip text for the Load button

        //// Extras tab
        extrasPanel = new JPanel();
        extrasPanel.setName("");
        tabbedPane.addTab("Extras", null, extrasPanel, null);
        extrasPanel.setLayout(null);

        // Information
        JLabel lblExtrasInfo = new JLabel("Extra settings are here to really enhance your experience.");
        lblExtrasInfo.setBounds(40, 10, 390, 23);
        extrasPanel.add(lblExtrasInfo);

        // Prayer potions
        selectPrayerFlickComboBox = new JComboBox<>(PRAYER_FLICK);
        selectPrayerFlickComboBox.setBounds(195, 44, 190, 23);  // +40
        extrasPanel.add(selectPrayerFlickComboBox);
        selectPrayerFlickComboBox.setToolTipText("If prayer flick is activated choose which player to flick.");
        JLabel lblPrayerPotions = new JLabel("Select prayer to flick:");
        lblPrayerPotions.setBounds(30, 44, 150, 23);  // +40
        extrasPanel.add(lblPrayerPotions);

        // Choosing looting distance
        JLabel lblLootIn = new JLabel("Loot within distance:");
        lblLootIn.setBounds(30, 84, 150, 23);  // +40
        extrasPanel.add(lblLootIn);

        lootWithin = new JSpinner();
        lootWithin.setModel(new SpinnerNumberModel(10, 1, 25, 1));
        lootWithin.setBounds(195, 84, 50, 23);  // +40
        extrasPanel.add(lootWithin);

        // Prayer potions
        selectPrayerRestoreComboBox = new JComboBox<>(PRAYER_RESTOR);
        selectPrayerRestoreComboBox.setBounds(195, 124, 190, 23);  // +40
        extrasPanel.add(selectPrayerRestoreComboBox);
        selectPrayerRestoreComboBox.setToolTipText("What prayer restoration potions you'd like to use?");
        JLabel lblPrayerFlick = new JLabel("Select prayer potion:");
        lblPrayerFlick.setBounds(30, 124, 150, 23);  // +40
        extrasPanel.add(lblPrayerFlick);

        // Activate stat-boosting potion drinking checkbox
        chkxdrinkStatsBoost = new JCheckBox("Activate stats-boosting with potion");
        chkxdrinkStatsBoost.setToolTipText("Check if you want to drink stat-boosting potions");
        chkxdrinkStatsBoost.setBounds(100, 164, 250, 23);  // +40
        extrasPanel.add(chkxdrinkStatsBoost);

        // Stat-boosting potions
        selectStatsBoostComboBox = new JComboBox<>(STATS_BOOST);
        selectStatsBoostComboBox.setBounds(195, 194, 190, 23);  // +40
        extrasPanel.add(selectStatsBoostComboBox);
        selectStatsBoostComboBox.setToolTipText("What stat-boosting potion you'd like to use?");
        JLabel lblStatsPotions = new JLabel("Boost stats with:");
        lblStatsPotions.setBounds(30, 194, 150, 23);  // +40
        extrasPanel.add(lblStatsPotions);

        // Boosting skill indicator
        selectSkillToBoostComboBox = new JComboBox<>(SKILL_NAMES);
        selectSkillToBoostComboBox.setBounds(120, 224, 90, 23);  // +40
        extrasPanel.add(selectSkillToBoostComboBox);
        selectSkillToBoostComboBox.setToolTipText("Select skill which will indicate when to drink chosen stat-boosting potion.");
        JLabel lblStatsBoostIntro = new JLabel("Boost stats if");
        lblStatsBoostIntro.setBounds(30, 224, 90, 23);  // +40
        extrasPanel.add(lblStatsBoostIntro);

        // Choosing level to boost stats
        JLabel lblBoostStatsAt = new JLabel("level is lower than");
        lblBoostStatsAt.setBounds(220, 224, 120, 23);  // +40
        extrasPanel.add(lblBoostStatsAt);
        spinnerBoostStatsAt = new JSpinner();
        spinnerBoostStatsAt.setModel(new SpinnerNumberModel(110, 1, 115, 10));
        spinnerBoostStatsAt.setBounds(330, 224, 55, 23);  // +40
        extrasPanel.add(spinnerBoostStatsAt);

        // Auto retaliate options
        selectAutoRetaliateComboBox = new JComboBox<>(AUTO_RETALIATE_OPTIONS);
        selectAutoRetaliateComboBox.setBounds(195, 264, 190, 23);  // +40
        extrasPanel.add(selectAutoRetaliateComboBox);
        selectAutoRetaliateComboBox.setToolTipText("Set auto retaliate value. Default = automatic.");
        JLabel lblAutoRetaliate = new JLabel("Auto retaliate value:");
        lblAutoRetaliate.setBounds(30, 264, 150, 23);  // +40
        extrasPanel.add(lblAutoRetaliate);

        // Activate menu actions
        chbxuseMouseActions = new JCheckBox("Interact with mouse");
        chbxuseMouseActions.setToolTipText("Selecting will use mouse clicking instead of menu actions.");
        chbxuseMouseActions.setBounds(30, 294, 150, 23);  // +40
        extrasPanel.add(chbxuseMouseActions);

        // Use main weapon as special attack weapon at 100%
        chbxMainAsSpecWeap = new JCheckBox("Main weapon to spec");
        chbxMainAsSpecWeap.setToolTipText("Selecting will override special attack weapon and always use main weapon for special attack.");
        chbxMainAsSpecWeap.setBounds(30, 319, 150, 23);
        extrasPanel.add(chbxMainAsSpecWeap);

        // Activate menu actions
        chbxuseBuryBones = new JCheckBox("Bury bones?");
        chbxuseBuryBones.setToolTipText("Will bury all bones and ashes in inventory. NB! Add bones to loot table.");
        chbxuseBuryBones.setBounds(195, 294, 150, 23);  // +40
        extrasPanel.add(chbxuseBuryBones);

        // Check if NPC reachable
        chbxnpcReachable = new JCheckBox("Bypass NPC reachability");
        chbxnpcReachable.setToolTipText("If chosen bot will not check if NPC is reachable (useful if NPC in cage etc).");
        chbxnpcReachable.setBounds(195, 319, 200, 23);
        extrasPanel.add(chbxnpcReachable);


        //// Load json
        btnLoadJson = new JButton("Load settings");
        btnLoadJson.setBackground(new Color(78, 131, 232));
        btnLoadJson.setToolTipText("Will load all saved bot settings.");
        btnLoadJson.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JSONObject data = loadGuiDataFromFile();
                populateGuiWithData(data);
                loadApiKeyFromFile(false);
                displayMessage("Setting loaded successfully!", "eAIO settings by Esmaabi", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        btnLoadJson.setBounds(221, 391, 187, 23);
        contentPane.add(btnLoadJson);

        //// Save json
        btnSaveJson = new JButton("Save settings");
        btnSaveJson.setBackground(Color.DARK_GRAY);
        btnSaveJson.setForeground(new Color(139, 255, 186));
        btnSaveJson.setToolTipText("Will save all bot settings.");
        btnSaveJson.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JSONObject data = gatherGuiData();
                saveGuiDataToFile(data);
                saveApiKeyToFile(false);
                displayMessage("Setting saved successfully!", "eAIO settings by Esmaabi", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        btnSaveJson.setBounds(17, 391, 187, 23);
        contentPane.add(btnSaveJson);

        //// Start button & apply settings
        btnApplyAndStart = new JButton("Apply Settings & Start");
        btnApplyAndStart.setBackground(new Color(34, 139, 34));
        btnApplyAndStart.setToolTipText("Will apply chosen settings and start bot.");
        btnApplyAndStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onApplyNewSettings();
            }
        });
        btnApplyAndStart.setBounds(221, 421, 187, 23);
        contentPane.add(btnApplyAndStart);

        //// Pause button
        btnPauseBot = new JButton("Pause");
        btnPauseBot.setBackground(new Color(222, 50, 22));
        btnPauseBot.setToolTipText("Will pause eAIO Fighter");
        btnPauseBot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pauseBot();
            }
        });
        btnPauseBot.setBounds(17, 421, 187, 23);
        contentPane.add(btnPauseBot);
    }

    //// Auto Fighter functions
    public void onApplyNewSettings() {
        List<Integer> npcIdList = new ArrayList<Integer>();
        for (Object npc : modelSelectedMonsters.toArray()) {
            if (!(npc instanceof String)) {
                continue;
            }
            String npcInfo = (String) npc;
            npcIdList.add(Integer.valueOf(npcInfo.split(",")[2].replace(" ", "").replace("]", "")));
        }
        int[] npcIds = new int[npcIdList.size()];
        for (int i = 0; i < npcIds.length; i++) {
            npcIds[i] = npcIdList.get(i);
        }
        eFood foodType = (eFood) comboBoxFoods.getSelectedItem();
        if (foodType == eFood.NONE) {
            combat.setupEating(null, 150);
        } else {
            assert foodType != null;
            if (!foodType.getName().equals("None")) {
                System.out.println("-------------------------");
                System.out.println("[SETTING] Food to eat: " + foodType.getName());
            }
            combat.setupEating(foodType.getItemId(), (Integer) spinnerHealAt.getValue());
        }
        int[] lootNames = new int[modelLoot.size()];
        if (!modelLoot.isEmpty()) {
            lootNames = new int[modelLoot.size()];
            for (int i = 0; i < lootNames.length; i++) {
                Pattern p = Pattern.compile("\\[(.*?),\\s(\\d+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher m = p.matcher(modelLoot.get(i));
                if (m.find()) {
                    lootNames[i] = Integer.parseInt(m.group(2));
                }
            }
            combat.setupLooting(lootNames, (Integer) lootWithin.getValue());
        }
        combat.setupAttacking(npcIds);
        combat.eatForSpace = chckbxEatforSpace.isSelected();
        combat.quickPrayers = chbxQuickPrayers.isSelected();
        combat.prayFlick = chckbxPrayFlick.isSelected();
        combat.teleOnTaskFinish = chckbxTeleAfterTask.isSelected();
        combat.enableStatBoosting = chkxdrinkStatsBoost.isSelected();
        combat.buryBones = chbxuseBuryBones.isSelected();
        combat.bypassNpcReachable = chbxnpcReachable.isSelected();
        combat.useMainWeaponAsSpec = chbxMainAsSpecWeap.isSelected();
        BotUtils.eActions.menuActionMode = !chbxuseMouseActions.isSelected();
        eAutoResponser.gptActiveAtHome = gptActiveAtHome.isSelected();
        eAutoResponser.botStarted = true;
        eAutoResponser.gptStarted = activateChatGPT.isSelected();
        if (activateChatGPT.isSelected()) {
            disableChatGPTOptions();
            eApiAccess.eAutoResponser.gptIsActive = true;
            eApiAccess.eAutoResponser.properGrammarActive = getProperGrammar();
            getSelectedActivity();
            getSelectedGptModel();
        }
        setSelectedPrayerFlick();
        getSelectedPrayerPotions();
        getSelectedStatsBoostPotion();
        getAutoRetaliateSettings();
        btnPauseBot.setEnabled(true);
    }

    public void pauseBot() {
        eAutoResponser.botStarted = false;
        eAutoResponser.gptStarted = activateChatGPT.isSelected();
        btnPauseBot.setEnabled(false);
        btnApplyAndStart.setVisible(true);
        activateChatGPT.setEnabled(true);
        gptHasBeenStarted = activateChatGPT.isSelected();
        activateChatGPT.setEnabled(true);
        if (activateChatGPT.isSelected()) {
            enableChatGPTOptions();
            eApiAccess.eAutoResponser.gptIsActive = false;
            eAutoResponser.gptActiveAtHome = gptActiveAtHome.isSelected();
            eApiAccess.eAutoResponser.properGrammarActive = getProperGrammar();
        }
    }

    public void closeGui() {
        theGui.dispose();
    }

    public void onRefreshNearbyMonsters() {
        this.modelNearbyMonsters.clear();

        for (SimpleNpc npc : combat.ctx.npcs.populate().filterHasAction("Attack")) {
            if (!this.modelNearbyMonsters.contains("[" + npc.getName() + ", " + npc.getNpcDefinitions().getCombatLevel() + ", " + npc.getId() + "]") && !this.modelSelectedMonsters.contains("[" + npc.getName() + ", " + npc.getNpcDefinitions().getCombatLevel() + ", " + npc.getId() + "]")) {
                this.modelNearbyMonsters.addElement("[" + npc.getName() + ", " + npc.getNpcDefinitions().getCombatLevel() + ", " + npc.getId() + "]");
            }
        }
    }

    public void setupDropArray() {
        for (int i : eLoots.UNIVERSAL_LOOT_IDS) {
            ItemComposition itemDefinitions = combat.ctx.definitions.getItemDefinition(i);
            if (itemDefinitions != null) {
                modelLoot.addElement("[" + itemDefinitions.getName() + ", " + itemDefinitions.getId() + "]");
            }
        }
        if (modelLoot.isEmpty()) {
            for (int i : eLoots.UNIVERSAL_LOOT_IDS) {
                ItemComposition itemDefinitions = combat.ctx.definitions.getItemDefinition(i);
                if (itemDefinitions != null) {
                    modelLoot.addElement("[" + itemDefinitions.getName() + ", " + itemDefinitions.getId() + "]");
                }
            }
        }
    }

    // GPT functions
    private boolean getProperGrammar() {
        return properGrammar.isSelected();
    }

    private void getSelectedActivity() {
        String selectedActivity = (String) chatActivityComboBox.getSelectedItem();

        eApiAccess.eAutoResponser.skillingActivity = false;
        eApiAccess.eAutoResponser.slayerActivity = false;
        eApiAccess.eAutoResponser.pvmActivity = false;
        eApiAccess.eAutoResponser.chillingActivity = false;
        eApiAccess.eAutoResponser.scriptPurposeActivity = false;
        eApiAccess.eAutoResponser.moneyMakingActivity = false;
        eApiAccess.eAutoResponser.scriptPurposeCustomActivity = false;

        switch (Objects.requireNonNull(selectedActivity)) {
            case "Skilling":
                eApiAccess.eAutoResponser.skillingActivity = true;
                break;
            case "Slayer":
                eApiAccess.eAutoResponser.slayerActivity = true;
                break;
            case "PVM":
                eApiAccess.eAutoResponser.pvmActivity = true;
                break;
            case "Money making":
                eApiAccess.eAutoResponser.moneyMakingActivity = true;
                break;
            case "Chilling":
                eApiAccess.eAutoResponser.chillingActivity = true;
                break;
            case "Script purpose":
                eApiAccess.eAutoResponser.scriptPurposeActivity = true;
                break;
            case "Custom reason":
                eApiAccess.eAutoResponser.scriptPurposeCustomActivity = true;
                break;
        }
    }

    private void getSelectedGptModel() {
        String selectedGptModel = (String) selectGptModelComboBox.getSelectedItem();
        System.out.println("[SETTING] Model to use: " + selectedGptModel);

        eApiAccess.eAutoResponser.useGPT3 = false;
        eApiAccess.eAutoResponser.useGPT4 = false;

        switch (Objects.requireNonNull(selectedGptModel)) {
            case "GPT 3.5 Turbo":
                eApiAccess.eAutoResponser.useGPT3 = true;
                break;
            case "GPT 4":
                eApiAccess.eAutoResponser.useGPT4 = true;
                break;
        }
    }

    // Rest functions
    private void getSelectedPrayerPotions() {
        String selectedPotions = (String) selectPrayerRestoreComboBox.getSelectedItem();
        System.out.println("[SETTING] Restore prayer with: " + selectedPotions);

        if (selectedPotions != null) {
            AutoFighter.prayPotionName = selectedPotions;
        }
    }

    private void setSelectedPrayerFlick() {
        String selectedPrayer = (String) selectPrayerFlickComboBox.getSelectedItem();
        System.out.println("[SETTING] Prayer to flick: " + selectedPrayer);

        if (selectedPrayer != null) {
            switch (selectedPrayer) {
                case "Rigour (lvl 74)":
                    AutoFighter.CHOSEN_PRAYER_FLICK = SimplePrayers.Prayers.RIGOUR;
                    break;
                case "Augury (lvl 77)":
                    AutoFighter.CHOSEN_PRAYER_FLICK = SimplePrayers.Prayers.AUGURY;
                    break;
                case "Chivalry (lvl 60)":
                    AutoFighter.CHOSEN_PRAYER_FLICK = SimplePrayers.Prayers.CHIVALRY;
                    break;
                case "Mystic Might (lvl 45)":
                    AutoFighter.CHOSEN_PRAYER_FLICK = SimplePrayers.Prayers.MYSTIC_MIGHT;
                    break;
                case "Eagle Eye (lvl 44)":
                    AutoFighter.CHOSEN_PRAYER_FLICK = SimplePrayers.Prayers.EAGLE_EYE;
                    break;
                case "Ultimate Strength (lvl 31)":
                    AutoFighter.CHOSEN_PRAYER_FLICK = SimplePrayers.Prayers.ULTIMATE_STRENGTH;
                    break;
                case "Mystic Lore (lvl 27)":
                    AutoFighter.CHOSEN_PRAYER_FLICK = SimplePrayers.Prayers.MYSTIC_LORE;
                    break;
                case "Hawk Eye (lvl 26)":
                    AutoFighter.CHOSEN_PRAYER_FLICK = SimplePrayers.Prayers.HAWK_EYE;
                    break;
                case "Superhuman Strength (lvl 13)":
                    AutoFighter.CHOSEN_PRAYER_FLICK = SimplePrayers.Prayers.SUPERHUMAN_STRENGTH;
                    break;
                case "Mystic Will (lvl 9)":
                    AutoFighter.CHOSEN_PRAYER_FLICK = SimplePrayers.Prayers.MYSTIC_WILL;
                    break;
                case "Sharp Eye (lvl 8)":
                    AutoFighter.CHOSEN_PRAYER_FLICK = SimplePrayers.Prayers.SHARP_EYE;
                    break;
                case "Burst of Strength (lvl 4)":
                    AutoFighter.CHOSEN_PRAYER_FLICK = SimplePrayers.Prayers.BURST_OF_STRENGTH;
                    break;
                default:
                    AutoFighter.CHOSEN_PRAYER_FLICK = SimplePrayers.Prayers.PIETY;
                    break;
            }
        } else {
            AutoFighter.CHOSEN_PRAYER_FLICK = null;
        }
    }

    private void getSelectedStatsBoostPotion() {
        String selectedPotion = (String) selectStatsBoostComboBox.getSelectedItem();
        String selectedSkill = (String) selectSkillToBoostComboBox.getSelectedItem();
        int drinkAt = (int) spinnerBoostStatsAt.getValue();

        if (selectedPotion != null && selectedSkill != null) {
            switch (selectedSkill) {
                case "Attack":
                    AutoFighter.chosenSkillIndicator = SimpleSkills.Skills.ATTACK;
                    break;
                case "Strength":
                    AutoFighter.chosenSkillIndicator = SimpleSkills.Skills.STRENGTH;
                    break;
                case "Defence":
                    AutoFighter.chosenSkillIndicator = SimpleSkills.Skills.DEFENCE;
                    break;
                case "Ranged":
                    AutoFighter.chosenSkillIndicator = SimpleSkills.Skills.RANGED;
                    break;
                case "Magic":
                    AutoFighter.chosenSkillIndicator = SimpleSkills.Skills.MAGIC;
                    break;
            }
            if (!selectedPotion.equals("None")) System.out.println("[SETTING] Bot will drink " + selectedPotion.toLowerCase() + " when " + selectedSkill + " level is lower than " + drinkAt);
            AutoFighter.statsBoostingPotionName = selectedPotion;
            AutoFighter.drinkStatBoostAt = drinkAt;
        }
    }

    private void getAutoRetaliateSettings() {
        String selectedValue = (String) selectAutoRetaliateComboBox.getSelectedItem();
        System.out.println("[SETTING] Auto Retaliate mode: " + selectedValue);

        if (selectedValue != null) {
            switch (selectedValue) {
                case "Default":
                    AutoFighter.autoRetaliateDefault = true;
                    break;
                case "Force ON":
                    AutoFighter.autoRetaliateDefault = false;
                    AutoFighter.autoRetaliate = true;
                    break;
                case "Force OFF":
                    AutoFighter.autoRetaliateDefault = false;
                    AutoFighter.autoRetaliate = false;
                    break;
            }
        }
        System.out.println("-------------------------");
    }

    private void displayMessage(String message, String title, int messageType) {
        JOptionPane optionPane = new JOptionPane(message, messageType);
        JDialog dialog = optionPane.createDialog(this, title);
        dialog.setLocationRelativeTo(theGui);
        dialog.setBackground(Color.darkGray);
        dialog.setVisible(true);
    }

    /// Encrypt API Key to save/load
    private SecretKey getSecretKey(char[] passphrase) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGO);
        KeySpec spec = new PBEKeySpec(passphrase, SALT.getBytes(), ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    // Old save / load method with one location.
/*    private void saveApiKeyToFile(boolean showMessage) {
        String apiKey = new String(apiKeyPasswordField.getPassword());
        char[] passphrase = passphraseField.getPassword();
        try {
            // Encryption
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGO);
            SecretKey secretKey = getSecretKey(passphrase);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedApiKey = cipher.doFinal(apiKey.getBytes());
            byte[] iv = cipher.getIV();

            // Convert to JSON and save
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("encryptedKey", Base64.getEncoder().encodeToString(encryptedApiKey));
            jsonObject.put("iv", Base64.getEncoder().encodeToString(iv));

            try (PrintWriter writer = new PrintWriter(new FileWriter(API_KEY_FILE_PATH))) {
                writer.println(jsonObject.toString());
                writer.flush();
                if (showMessage) displayMessage("API key saved successfully", "API Key Saved", JOptionPane.INFORMATION_MESSAGE);
                logger.severe("API key saved successfully");
            }
        } catch (Exception e) {
            logger.severe("Failed to save API key " + e);
            if (showMessage) displayMessage("Failed to save API key", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadApiKeyFromFile(boolean showMessage) {
        char[] passphrase = passphraseField.getPassword();
        try (BufferedReader reader = new BufferedReader(new FileReader(API_KEY_FILE_PATH))) {
            String line = reader.readLine();
            if (line != null) {
                JSONObject jsonObject = new JSONObject(line);
                byte[] encryptedApiKey = Base64.getDecoder().decode(jsonObject.getString("encryptedKey"));
                byte[] iv = Base64.getDecoder().decode(jsonObject.getString("iv"));

                // Decryption
                Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGO);
                SecretKey secretKey = getSecretKey(passphrase);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
                String apiKey = new String(cipher.doFinal(encryptedApiKey));

                apiKeyPasswordField.setText(apiKey);
                if (showMessage) displayMessage("API key loaded successfully", "API Key Loaded", JOptionPane.INFORMATION_MESSAGE);
                logger.info("API key loaded successfully");
            } else {
                if (showMessage) displayMessage("API key not found in the file", "Error", JOptionPane.ERROR_MESSAGE);
                logger.severe("API key not found in the file");
            }
        } catch (Exception e) {
            logger.severe("Failed to load API key. Please add it again " + e);
            if (showMessage) displayMessage("Failed to load API key. Please add it again", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }*/

    // New save / load method with .simplebot\script_store\eAIO Fighter with GPT\ location.
    private void saveApiKeyToFile(boolean showMessage) {
        String apiKey = new String(apiKeyPasswordField.getPassword());
        char[] passphrase = passphraseField.getPassword();

        final File scriptDirectory = new File(combat.getStorageDirectory());
        File apiKeyFile = new File(scriptDirectory, "api_key_settings.json");

        try {
            // Encryption
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGO);
            SecretKey secretKey = getSecretKey(passphrase);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedApiKey = cipher.doFinal(apiKey.getBytes());
            byte[] iv = cipher.getIV();

            // Convert to JSON and save
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("encryptedKey", Base64.getEncoder().encodeToString(encryptedApiKey));
            jsonObject.put("iv", Base64.getEncoder().encodeToString(iv));

            try (PrintWriter writer = new PrintWriter(new FileWriter(apiKeyFile))) {
                writer.println(jsonObject.toString());
                writer.flush();
                if (showMessage) displayMessage("API key saved successfully", "API Key Saved", JOptionPane.INFORMATION_MESSAGE);
                logger.severe("API key saved successfully");
            }
        } catch (Exception e) {
            logger.severe("Failed to save API key " + e);
            if (showMessage) displayMessage("Failed to save API key", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadApiKeyFromFile(boolean showMessage) {
        char[] passphrase = passphraseField.getPassword();

        final File scriptDirectory = new File(combat.getStorageDirectory());
        File apiKeyFile = new File(scriptDirectory, "api_key_settings.json");

        try (BufferedReader reader = new BufferedReader(new FileReader(apiKeyFile))) {
            String line = reader.readLine();
            if (line != null) {
                JSONObject jsonObject = new JSONObject(line);
                byte[] encryptedApiKey = Base64.getDecoder().decode(jsonObject.getString("encryptedKey"));
                byte[] iv = Base64.getDecoder().decode(jsonObject.getString("iv"));

                // Decryption
                Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGO);
                SecretKey secretKey = getSecretKey(passphrase);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
                String apiKey = new String(cipher.doFinal(encryptedApiKey));

                apiKeyPasswordField.setText(apiKey);
                if (showMessage) displayMessage("API key loaded successfully", "API Key Loaded", JOptionPane.INFORMATION_MESSAGE);
                logger.info("API key loaded successfully");
            } else {
                if (showMessage) displayMessage("API key not found in the file", "Error", JOptionPane.ERROR_MESSAGE);
                logger.severe("API key not found in the file");
            }
        } catch (Exception e) {
            logger.severe("Failed to load API key. Please add it again " + e);
            if (showMessage) displayMessage("Failed to load API key. Please add it again", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void enableChatGPTOptions() {
        chatActivityComboBox.setEnabled(true);
        selectGptModelComboBox.setEnabled(true);
        gptActiveAtHome.setEnabled(true);
        properGrammar.setEnabled(true);
        apiKeyPasswordField.setEnabled(true);
        loadButton.setEnabled(true);
        saveButton.setEnabled(true);
    }

    private void disableChatGPTOptions() {
        chatActivityComboBox.setEnabled(false);
        selectGptModelComboBox.setEnabled(false);
        gptActiveAtHome.setEnabled(false);
        properGrammar.setEnabled(false);
        apiKeyPasswordField.setEnabled(false);
        loadButton.setEnabled(false);
        saveButton.setEnabled(false);
    }

    // Save / load settings
/*    private void saveGuiDataToFile(JSONObject data) {
        try {
            Files.write(Paths.get("eAutoFighter_settings.json"), data.toString().getBytes());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save GUI data to file", e);
        }
    }

    private JSONObject loadGuiDataFromFile() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("eAutoFighter_settings.json")));
            return new JSONObject(content);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load GUI data from file", e);
            return null;
        }
    }*/

    private void saveGuiDataToFile(JSONObject data) {
        final File scriptDirectory = new File(combat.getStorageDirectory());
        Path filePath = Paths.get(scriptDirectory.getAbsolutePath(), "eAutoFighter_settings.json");
        try {
            Files.write(filePath, data.toString().getBytes());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save GUI data to file", e);
        }
    }

    private JSONObject loadGuiDataFromFile() {
        final File scriptDirectory = new File(combat.getStorageDirectory());
        Path filePath = Paths.get(scriptDirectory.getAbsolutePath(), "eAutoFighter_settings.json");
        try {
            String content = new String(Files.readAllBytes(filePath));
            return new JSONObject(content);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load GUI data from file", e);
            return null;
        }
    }

    private JSONObject gatherGuiData() {
        JSONObject data = new JSONObject();

        data.put("activateChatGPT", activateChatGPT.isSelected());
        data.put("buryBones", chbxuseBuryBones.isSelected());
        data.put("npcReachable", chbxnpcReachable.isSelected());
        data.put("mainAsSpecWeap", chbxMainAsSpecWeap.isSelected());
        data.put("boostStatsAt", spinnerBoostStatsAt.getValue());
        data.put("drinkStatsBoost", chkxdrinkStatsBoost.isSelected());
        data.put("mouseActions", chbxuseMouseActions.isSelected());
        data.put("eatForSpace", chckbxEatforSpace.isSelected());
        data.put("gptActiveAtHome", gptActiveAtHome.isSelected());
        data.put("healAt", spinnerHealAt.getValue());
        data.put("lootWithin", lootWithin.getValue());
        data.put("prayFlick", chckbxPrayFlick.isSelected());
        data.put("properGrammar", properGrammar.isSelected());
        data.put("quickPrayers", chbxQuickPrayers.isSelected());
        data.put("selectAutoRetaliate", Objects.requireNonNull(selectAutoRetaliateComboBox.getSelectedItem()).toString());
        data.put("selectedFood", Objects.requireNonNull(comboBoxFoods.getSelectedItem()).toString());
        data.put("selectedGptModel", Objects.requireNonNull(selectGptModelComboBox.getSelectedItem()).toString());
        data.put("selectPrayerFlick", Objects.requireNonNull(selectPrayerFlickComboBox.getSelectedItem()).toString());
        data.put("selectedPrayerRestore", Objects.requireNonNull(selectPrayerRestoreComboBox.getSelectedItem()).toString());
        data.put("selectedSkillToBoost", Objects.requireNonNull(selectSkillToBoostComboBox.getSelectedItem()).toString());
        data.put("selectedStatsBoost", Objects.requireNonNull(selectStatsBoostComboBox.getSelectedItem()).toString());
        data.put("teleAfterTask", chckbxTeleAfterTask.isSelected());

        JSONArray lootItems = new JSONArray();
        DefaultListModel<String> modelLoot = (DefaultListModel<String>) listLoot.getModel();
        for (int i = 0; i < modelLoot.getSize(); i++) {
            String itemString = modelLoot.getElementAt(i);
            String itemId = itemString.substring(itemString.lastIndexOf(", ") + 2, itemString.length() - 1); // Extract the item ID from the string
            lootItems.put(Integer.parseInt(itemId)); // Save only the item ID
        }
        data.put("lootList", lootItems);

        return data;
    }

    private void populateGuiWithData(JSONObject data) {
        if (data == null) return;

        activateChatGPT.setSelected(data.getBoolean("activateChatGPT"));
        chbxuseBuryBones.setSelected(data.getBoolean("buryBones"));
        chbxnpcReachable.setSelected(data.getBoolean("npcReachable"));
        chbxMainAsSpecWeap.setSelected(data.getBoolean("mainAsSpecWeap"));
        spinnerBoostStatsAt.setValue(data.getInt("boostStatsAt"));
        chkxdrinkStatsBoost.setSelected(data.getBoolean("drinkStatsBoost"));
        chbxuseMouseActions.setSelected(data.getBoolean("mouseActions"));
        chckbxEatforSpace.setSelected(data.getBoolean("eatForSpace"));
        gptActiveAtHome.setSelected(data.getBoolean("gptActiveAtHome"));
        spinnerHealAt.setValue(data.getInt("healAt"));
        lootWithin.setValue(data.getInt("lootWithin"));
        chckbxPrayFlick.setSelected(data.getBoolean("prayFlick"));
        properGrammar.setSelected(data.getBoolean("properGrammar"));
        chbxQuickPrayers.setSelected(data.getBoolean("quickPrayers"));
        selectAutoRetaliateComboBox.setSelectedItem(data.getString("selectAutoRetaliate"));
        selectGptModelComboBox.setSelectedItem(data.getString("selectedGptModel"));
        selectPrayerFlickComboBox.setSelectedItem(data.getString("selectPrayerFlick"));
        selectPrayerRestoreComboBox.setSelectedItem(data.getString("selectedPrayerRestore"));
        selectSkillToBoostComboBox.setSelectedItem(data.getString("selectedSkillToBoost"));
        selectStatsBoostComboBox.setSelectedItem(data.getString("selectedStatsBoost"));
        chckbxTeleAfterTask.setSelected(data.getBoolean("teleAfterTask"));

        String selectedFoodString = data.getString("selectedFood");
        eFood selectedFoodEnum = eFood.valueOf(selectedFoodString.toUpperCase());
        comboBoxFoods.setSelectedItem(selectedFoodEnum);

        //DefaultListModel<String> modelLoot = new DefaultListModel<>();
        modelLoot.clear();
        for (Object itemIdObj : data.getJSONArray("lootList")) {
            int itemId = (int) itemIdObj;
            ItemComposition itemDefinitions = combat.ctx.definitions.getItemDefinition(itemId);
            if (itemDefinitions != null) {
                modelLoot.addElement("[" + itemDefinitions.getName() + ", " + itemId + "]");
            }
        }
        listLoot.setModel(modelLoot);

        int[] loadedLootNames = new int[modelLoot.getSize()];
        for (int i = 0; i < modelLoot.getSize(); i++) {
            String itemString = modelLoot.getElementAt(i);
            String itemIdStr = itemString.substring(itemString.lastIndexOf(", ") + 2, itemString.length() - 1);
            loadedLootNames[i] = Integer.parseInt(itemIdStr);
        }
        combat.setLootNames(loadedLootNames);
        System.out.println("Loaded loot size: " + modelLoot.getSize());

    }

}