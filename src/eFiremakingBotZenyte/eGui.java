package eFiremakingBotZenyte;

import simple.robot.api.ClientContext;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class eGui extends JFrame {
    private static ClientContext ctx;

    public eGui(ClientContext ctx) {
        eGui.ctx = ctx;
    }

    private static final long serialVersionUID = 1L;
    private static final String[] LOCATIONS = {"Falador East", "Varrock East", "Grand Exchange"};
    private static final String[] LOGS = {
            "Redwood logs", "Magic logs", "Yew logs",
            "Maple logs", "Willow logs", "Oak logs", "Logs"
    };

    private JComboBox<String> locationComboBox;
    private static JComboBox<String> logsComboBox;
    private JButton startButton;
    private JButton pauseButton;

    public eGui() {
        setTitle("eFiremakingBot");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
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

        // Select location
        addLabel("Select Location: ", contentPane, constraints, false);
        constraints.gridx = 1; // Setting x-axis position to 1
        locationComboBox = addComboBox(LOCATIONS, contentPane, constraints);
        locationComboBox.setPreferredSize(new Dimension(150, locationComboBox.getPreferredSize().height));
        locationComboBox.setToolTipText("Choose firemaking location. Start near bank.");

        constraints.gridx = 0; // Resetting x-axis position to 0
        constraints.gridy++; // Moving to next row

        // Select logs
        addLabel("Select Logs: ", contentPane, constraints, false);
        constraints.gridx = 1; // Setting x-axis position to 1
        logsComboBox = addComboBox(LOGS, contentPane, constraints);
        logsComboBox.setPreferredSize(new Dimension(150, logsComboBox.getPreferredSize().height));
        logsComboBox.setToolTipText("Choose logs you want to burn.");

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

    private JComboBox<String> addComboBox(String[] items, Container container, GridBagConstraints constraints) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        container.add(comboBox, constraints);
        constraints.gridx = 0;
        constraints.gridy++;
        return comboBox;
    }

    private void startBot() {
        eMain.botStarted = true;
        pauseButton.setVisible(true);
        startButton.setVisible(false);
        logsComboBox.setEnabled(false);
        locationComboBox.setEnabled(false);
    }

    private void pauseBot() {
        eMain.botStarted = false;
        pauseButton.setVisible(false);
        startButton.setVisible(true);
        logsComboBox.setEnabled(true);
        locationComboBox.setEnabled(true);
    }

    public JComboBox<String> getLocationComboBox() {
        return locationComboBox;
    }

    public static String getLogsComboBox() {
        return Objects.requireNonNull(logsComboBox.getSelectedItem()).toString();
    }
}