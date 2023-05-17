package eCrystalHustlerZenyte;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class eGui extends JFrame {
    public static final String[] ACTION = {"Woodcutting", "Mining"};
    private JComboBox<String> actionComboBox;
    private JCheckBox useSpecialAttack;
    private JButton startButton;
    private JButton pauseButton;

    public eGui() {
        setTitle("eCrystalHustler");
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

        // Wood type
        addLabel("Select action: ", contentPane, constraints, false);
        constraints.gridx = 1; // Setting x-axis position to 1
        actionComboBox = addComboBox(contentPane, constraints);
        actionComboBox.setPreferredSize(new Dimension(150, actionComboBox.getPreferredSize().height));

        constraints.gridx = 0; // Resetting x-axis position to 0
        constraints.gridy++; // Moving to next row

        // Stamina potions
        addLabel("Use special attack? ", contentPane, constraints, false);
        constraints.gridx = 1; // Setting x-axis position to 1
        useSpecialAttack = addCheckBox(contentPane, constraints);

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

    private JComboBox<String> addComboBox(Container container, GridBagConstraints constraints) {
        JComboBox<String> comboBox = new JComboBox<>(eGui.ACTION);
        container.add(comboBox, constraints);
        constraints.gridx = 0;
        constraints.gridy++;
        return comboBox;
    }

    private void startBot() {
        eMain.botStarted = true;
        pauseButton.setVisible(true);
        startButton.setVisible(false);
        actionComboBox.setEnabled(false);
        useSpecialAttack.setEnabled(false);
        eMain.specialAttackActive = useSpecialAttack.isSelected();
        getSelectedAction();

        if (!eMain.specialAttackTool) {
            useSpecialAttack.setEnabled(true);
            useSpecialAttack.setSelected(false);
            useSpecialAttack.setEnabled(false);
        }
    }

    private void pauseBot() {
        eMain.botStarted = false;
        pauseButton.setVisible(false);
        startButton.setVisible(true);
        useSpecialAttack.setEnabled(true);
        actionComboBox.setEnabled(true);

        if (!eMain.specialAttackTool) {
            useSpecialAttack.setEnabled(true);
            useSpecialAttack.setSelected(false);
        }
    }

    private JCheckBox addCheckBox(Container container, GridBagConstraints constraints) {
        JCheckBox checkBox = new JCheckBox();
        container.add(checkBox, constraints);
        constraints.gridx = 0;
        constraints.gridy++;
        return checkBox;
    }

    private void getSelectedAction() {
        String selectedAction = (String) actionComboBox.getSelectedItem();
        if (Objects.equals(selectedAction, "Woodcutting")) {
            eMain.woodcuttingAction = true;
            eMain.miningAction = false;
        } else if (Objects.equals(selectedAction, "Mining")) {
            eMain.miningAction = true;
            eMain.woodcuttingAction = false;
        }
    }
}
