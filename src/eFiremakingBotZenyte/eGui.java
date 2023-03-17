package eFiremakingBotZenyte;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class eGui extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String[] LOCATIONS = {"Falador East", "Varrock East", "Grand Exchange"};
    private static final String[] LOGS = {
            "Redwood logs", "Magic logs", "Yew logs",
            "Maple logs", "Willow logs", "Oak logs", "Logs"
    };

    private JComboBox<String> locationComboBox;
    private JComboBox<String> logsComboBox;
    private JButton startButton;
    private JButton pauseButton;
    private JButton paintButton;

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
        addLabel("eFiremakingBot options are below:", contentPane, constraints, true);

        constraints.gridwidth = 1; // Reset gridwidth
        constraints.anchor = GridBagConstraints.WEST; // Reset anchor


        constraints.gridy++; // Moving to next row
        constraints.gridx = 0; // Reset x position to 0

        // Select location
        addLabel("Select Location: ", contentPane, constraints, false);
        constraints.gridx = 1; // Setting x position to 1
        locationComboBox = addComboBox(LOCATIONS, contentPane, constraints);

        // Select logs
        addLabel("Select Logs: ", contentPane, constraints, false);
        constraints.gridx = 1; // Setting x position to 1
        logsComboBox = addComboBox(LOGS, contentPane, constraints);

        constraints.gridy++; // Moving to next row
        constraints.gridx = 0; // Reset x position to 0

        // Start button
        startButton = new JButton("Start");
        startButton.addActionListener(e -> startBot());
        contentPane.add(startButton, constraints);

        // Pause button
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> pauseBot());
        contentPane.add(pauseButton, constraints);
        pauseButton.setVisible(false); // Set pause

        // Paint button
        constraints.gridx = 1; // Setting x position to 1
        paintButton = new JButton("Paint");
        paintButton.addActionListener(e -> eMain.hidePaint = !eMain.hidePaint);
        contentPane.add(paintButton, constraints);
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
        eMain.woodName = Objects.requireNonNull(getLogsComboBox().getSelectedItem()).toString();
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

    public JComboBox<String> getLogsComboBox() {
        return logsComboBox;
    }
}