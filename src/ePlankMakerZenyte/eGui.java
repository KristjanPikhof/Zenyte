package ePlankMakerZenyte;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class eGui extends JFrame {
    public static final String[] ACTION = {"Logs", "Oak", "Teak", "Mahogany"};
    private JComboBox<String> actionComboBox;
    private JCheckBox useStaminaPotionsCheckBox;
    private JButton startButton;
    private JButton pauseButton;

    public eGui() {
        setTitle("ePlankMakerBot");
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
        addLabel("Select wood type: ", contentPane, constraints, false);
        constraints.gridx = 1; // Setting x-axis position to 1
        actionComboBox = addComboBox(contentPane, constraints);
        actionComboBox.setPreferredSize(new Dimension(150, actionComboBox.getPreferredSize().height));

        constraints.gridx = 0; // Resetting x-axis position to 0
        constraints.gridy++; // Moving to next row

        // Stamina potions
        addLabel("Use stamina potions? ", contentPane, constraints, false);
        constraints.gridx = 1; // Setting x-axis position to 1
        useStaminaPotionsCheckBox = addCheckBox(contentPane, constraints);

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
        useStaminaPotionsCheckBox.setEnabled(false);
        eMain.useStaminaPotions = useStaminaPotionsCheckBox.isSelected();
        getSelectedWoodType();

        if (!eMain.hasStaminaPotions) {
            useStaminaPotionsCheckBox.setEnabled(true);
            useStaminaPotionsCheckBox.setSelected(false); // uncheck the checkbox if there are no stamina potions left
            useStaminaPotionsCheckBox.setEnabled(false);
        }
    }

    private void pauseBot() {
        eMain.botStarted = false;
        pauseButton.setVisible(false);
        startButton.setVisible(true);
        useStaminaPotionsCheckBox.setEnabled(true);
        actionComboBox.setEnabled(true);

        if (!eMain.hasStaminaPotions) {
            useStaminaPotionsCheckBox.setEnabled(true);
            useStaminaPotionsCheckBox.setSelected(false); // uncheck the checkbox if there are no stamina potions left
        }
    }

    private JCheckBox addCheckBox(Container container, GridBagConstraints constraints) {
        JCheckBox checkBox = new JCheckBox();
        container.add(checkBox, constraints);
        constraints.gridx = 0;
        constraints.gridy++;
        return checkBox;
    }

    private void getSelectedWoodType() {
        // Retrieve the selected wood type from the combo box
        String selectedAction = (String) actionComboBox.getSelectedItem();
        eMain.woodTypeEnum selectedWoodTypeEnum = null;

        // Find the corresponding wood type enum based on the selected action
        for (eMain.woodTypeEnum woodName : eMain.woodTypeEnum.values()) {
            if (woodName.name().equalsIgnoreCase(selectedAction)) {
                selectedWoodTypeEnum = woodName;
                break;
            }
        }

        if (selectedWoodTypeEnum != null) {
            eMain.woodType = selectedWoodTypeEnum;
        }
    }
}
