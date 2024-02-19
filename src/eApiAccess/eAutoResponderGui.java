package eApiAccess;

import BotUtils.eActions;
import org.json.JSONObject;
import simple.robot.api.ClientContext;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class eAutoResponderGui extends JFrame {
    public static final String[] ACTIVITY = {"Script purpose", "PVM", "Chilling", "Skilling", "Money making"};
    private static final String API_KEY_FILE_PATH = "esmaabi_gpt_api_key_data.enc";
    public static final String[] GPT_MODELS = {"GPT 3.5 Turbo", "GPT 4"};
    private static final Logger logger = Logger.getLogger(eAutoResponderGui.class.getName());
    static JPasswordField apiKeyPasswordField;
    private JComboBox<String> chatActivityComboBox;
    private JComboBox<String> selectGptModelComboBox;
    private JCheckBox properGrammar;
    private JCheckBox gptActiveAtHomeArea;
    private JCheckBox useMouseActions;
    private JCheckBox activateChatGPT;
    private JButton startButton;
    private JButton pauseButton;
    private JButton saveButton;
    private JButton loadButton;
    private ClientContext ctx;

    /// Encryption
    private static final String ENCRYPTION_ALGO = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_ALGO = "PBKDF2WithHmacSHA256";
    private static final String SALT = "aRandomSaltForPBE"; // Change this to a long random string!
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;
    JPasswordField passphraseField = new JPasswordField();

    public eAutoResponderGui() {
        setTitle("Bot settings by Esmaabi");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        setLocation(mouseLocation.x, mouseLocation.y);
        setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getResource("esmaabi-icon.png"))).getImage());

        initGUI();

        pack();
    }

    private void initGUI() {
        JPanel contentPane = new JPanel(new GridBagLayout());
        contentPane.setBackground(Color.DARK_GRAY);
        setContentPane(contentPane);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);

        // Add title
        constraints.gridy = 0;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        addLabel("Please choose options below!", contentPane, constraints, true);

        constraints.gridwidth = 1; // Resetting gridwidth
        constraints.anchor = GridBagConstraints.WEST; // Resetting anchor

        constraints.gridy++; // Moving to next row
        constraints.gridx = 0; // Resetting x-axis position to 0

        // Activate ChatGPT checkbox
        addLabel("Activate ChatGPT: ", contentPane, constraints, false);
        constraints.gridx = 1; // Setting x-axis position to 1
        activateChatGPT = addCheckBox(contentPane, constraints);
        activateChatGPT.setToolTipText("Enable or disable ChatGPT answering.");

        constraints.gridx = 0; // Resetting x-axis position to 0
        constraints.gridy++; // Moving to next row

        // Choose GPT model
        addLabel("Select GPT model: ", contentPane, constraints, false);
        constraints.gridx = 1; // Setting x-axis position to 1
        selectGptModelComboBox = addComboBox(contentPane, constraints, GPT_MODELS);
        selectGptModelComboBox.setPreferredSize(new Dimension(150, selectGptModelComboBox.getPreferredSize().height));
        selectGptModelComboBox.setToolTipText("What GPT model you'd like to use?");

        constraints.gridx = 0; // Resetting x-axis position to 0
        constraints.gridy++; // Moving to next row

        // Activity type
        addLabel("Select activity: ", contentPane, constraints, false);
        constraints.gridx = 1; // Setting x-axis position to 1
        chatActivityComboBox = addComboBox(contentPane, constraints, ACTIVITY);
        chatActivityComboBox.setPreferredSize(new Dimension(150, chatActivityComboBox.getPreferredSize().height));
        chatActivityComboBox.setToolTipText("Choose what you want the bot to answer");

        constraints.gridx = 0; // Resetting x-axis position to 0
        constraints.gridy++; // Moving to next row

        // GPT active at home checkbox
        addLabel("GPT active at home? ", contentPane, constraints, false);
        constraints.gridx = 1; // Setting x-axis position to 1
        gptActiveAtHomeArea = addCheckBox(contentPane, constraints);
        gptActiveAtHomeArea.setToolTipText("Select if you want GPT to be active at home area.");

        constraints.gridx = 0; // Resetting x-axis position to 0
        constraints.gridy++; // Moving to next row

        // Grammar checkbox
        addLabel("Use proper grammar? ", contentPane, constraints, false);
        constraints.gridx = 1; // Setting x-axis position to 1
        properGrammar = addCheckBox(contentPane, constraints);
        properGrammar.setToolTipText("Click if you don't want to use abbreviations, etc.");

        constraints.gridx = 0; // Resetting x-axis position to 0
        constraints.gridy++; // Moving to next row

        // MenuActions checkbox
        addLabel("Use mouse clicking? ", contentPane, constraints, false);
        constraints.gridx = 1; // Setting x-axis position to 1
        useMouseActions = addCheckBox(contentPane, constraints);
        useMouseActions.setToolTipText("Do not check if you wish to use menu actions instead of mouse.");

        constraints.gridx = 0; // Resetting x-axis position to 0
        constraints.gridy++; // Moving to next row

        // API Key
        addLabel("Insert API key:", contentPane, constraints, false);
        constraints.gridx = 1; // Setting x-axis position to 1
        apiKeyPasswordField = addPasswordField(contentPane, constraints);
        apiKeyPasswordField.setPreferredSize(new Dimension(150, apiKeyPasswordField.getPreferredSize().height));
        apiKeyPasswordField.setEchoChar('\u2022'); // Set the echo character as a dot or asterisk
        apiKeyPasswordField.setToolTipText("Supported: GPT 3.5-turbo"); // Set the tooltip text for the API key field

        // Save Button
        constraints.gridx = 0; // Resetting x-axis position to 0
        constraints.gridy++; // Moving to next row
        constraints.anchor = GridBagConstraints.CENTER;
        saveButton = addButton("Save", contentPane, constraints);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveApiKeyToFile(true);
            }
        });
        saveButton.setToolTipText("Save the API key to a file"); // Set the tooltip text for the Save button

        // Load Button
        constraints.gridx = 1; // Setting x-axis position to 1
        loadButton = addButton("Load", contentPane, constraints);
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadApiKeyFromFile(true);
            }
        });
        loadButton.setToolTipText("Load the API key from a file"); // Set the tooltip text for the Load button

        constraints.gridx = 0; // Resetting x-axis position to 0
        constraints.gridy++; // Moving to next row
        constraints.gridwidth = 2; // Resetting gridwidth
        constraints.anchor = GridBagConstraints.CENTER; // Resetting anchor

        // Start and Pause buttons
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        startButton = new JButton("Start");
        startButton.addActionListener(e -> startBot());
        startButton.setBackground(Color.GREEN);
        buttonsPanel.add(startButton);
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> pauseBot());
        pauseButton.setVisible(false); // Setting button invisible
        pauseButton.setBackground(Color.RED);
        buttonsPanel.add(pauseButton);
        contentPane.add(buttonsPanel, constraints);
    }

    private void addLabel(String text, Container container, GridBagConstraints constraints, boolean isTitle) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        if (isTitle) {
            label.setFont(label.getFont().deriveFont(Font.BOLD, 16));
        }
        container.add(label, constraints);
        constraints.gridx++;
    }

    private JComboBox<String> addComboBox(Container container, GridBagConstraints constraints, String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        container.add(comboBox, constraints);
        constraints.gridx = 0;
        constraints.gridy++;
        return comboBox;
    }


    private JCheckBox addCheckBox(Container container, GridBagConstraints constraints) {
        JCheckBox checkBox = new JCheckBox();
        container.add(checkBox, constraints);
        constraints.gridx = 0;
        constraints.gridy++;
        return checkBox;
    }

    private JTextField addTextField(Container container, GridBagConstraints constraints) {
        JTextField textField = new JTextField();
        container.add(textField, constraints);
        constraints.gridx = 0;
        constraints.gridy++;
        return textField;
    }

    private JButton addButton(String text, Container container, GridBagConstraints constraints) {
        JButton button = new JButton(text);
        container.add(button, constraints);
        return button;
    }

    private void startBot() {
        eAutoResponser.botStarted = true;
        eAutoResponser.gptStarted = activateChatGPT.isSelected();
        pauseButton.setVisible(true);
        startButton.setVisible(false);
        gptActiveAtHomeArea.setEnabled(false);
        eAutoResponser.gptActiveAtHome = gptActiveAtHomeArea.isSelected();
        useMouseActions.setEnabled(false);
        eActions.menuActionMode = !useMouseActions.isSelected();
        activateChatGPT.setEnabled(false);
        if (activateChatGPT.isSelected()) {
            disableChatGPTOptions();
            eAutoResponser.gptIsActive = true;
            eAutoResponser.properGrammarActive = getProperGrammar();
            getSelectedActivity();
            getSelectedGptModel();
        }
    }

    private void pauseBot() {
        eAutoResponser.botStarted = false;
        eAutoResponser.gptStarted = activateChatGPT.isSelected();
        pauseButton.setVisible(false);
        startButton.setVisible(true);
        gptActiveAtHomeArea.setEnabled(true);
        eAutoResponser.gptActiveAtHome = gptActiveAtHomeArea.isSelected();
        useMouseActions.setEnabled(true);
        eActions.menuActionMode = useMouseActions.isSelected();
        activateChatGPT.setEnabled(true);
        if (activateChatGPT.isSelected()) {
            enableChatGPTOptions();
            eAutoResponser.gptIsActive = false;
            eAutoResponser.properGrammarActive = getProperGrammar();
        }
    }

    private boolean getProperGrammar() {
        return properGrammar.isSelected();
    }

    private void getSelectedActivity() {
        String selectedActivity = (String) chatActivityComboBox.getSelectedItem();

        eApiAccess.eAutoResponser.skillingActivity = false;
        eApiAccess.eAutoResponser.pvmActivity = false;
        eApiAccess.eAutoResponser.chillingActivity = false;
        eApiAccess.eAutoResponser.moneyMakingActivity = false;
        eApiAccess.eAutoResponser.scriptPurposeActivity = false;
        eApiAccess.eAutoResponser.scriptPurposeCustomActivity = false;

        switch (Objects.requireNonNull(selectedActivity)) {
            case "Skilling":
                eApiAccess.eAutoResponser.skillingActivity = true;
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

    private void displayMessage(String message, String title, int messageType) {
        JOptionPane optionPane = new JOptionPane(message, messageType);
        JDialog dialog = optionPane.createDialog(this, title);
        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        dialog.setLocation(mouseLocation.x - dialog.getWidth() / 2, mouseLocation.y - dialog.getHeight() / 2);
        dialog.setVisible(true);
    }

    private SecretKey getSecretKey(char[] passphrase) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGO);
        KeySpec spec = new PBEKeySpec(passphrase, SALT.getBytes(), ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }


    private void saveApiKeyToFile(boolean showMessage) {
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
    }


    private JPasswordField addPasswordField(Container container, GridBagConstraints constraints) {
        JPasswordField passwordField = new JPasswordField();
        container.add(passwordField, constraints);
        constraints.gridx = 0;
        constraints.gridy++;
        return passwordField;
    }

    private void enableChatGPTOptions() {
        chatActivityComboBox.setEnabled(true);
        properGrammar.setEnabled(true);
        apiKeyPasswordField.setEnabled(true);
        loadButton.setEnabled(true);
        saveButton.setEnabled(true);
    }

    private void disableChatGPTOptions() {
        chatActivityComboBox.setEnabled(false);
        properGrammar.setEnabled(false);
        apiKeyPasswordField.setEnabled(false);
        loadButton.setEnabled(false);
        saveButton.setEnabled(false);
    }

    private void getSelectedGptModel() {
        String selectedGptModel = (String) selectGptModelComboBox.getSelectedItem();

        eAutoResponser.useGPT3 = false;
        eAutoResponser.useGPT4 = false;

        switch (Objects.requireNonNull(selectedGptModel)) {
            case "GPT 3.5 Turbo":
                eAutoResponser.useGPT3 = true;
                break;
            case "GPT 4":
                eAutoResponser.useGPT4 = true;
                break;
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                eAutoResponderGui frame = new eAutoResponderGui();
                frame.setVisible(true);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to initialize eAutoResponderGui", e);
            }
        });
    }
}