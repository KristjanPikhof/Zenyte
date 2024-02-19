package eGlassblowingBotZenyte;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;



public class eGui extends JFrame implements ActionListener {

    private final JComboBox<String> modeSelect;
    private final JButton startButton;
    private final JButton pauseButton;
    public static int widgetItem1 = 270;
    public static int widgetItem2;
    public static String nameOfItem;

    public eGui() {
        setTitle("eGlassblowingBot by Esmaabi");
        setLayout(new FlowLayout());
        setSize(550, 350);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e);
                JOptionPane.showMessageDialog(null, "Please read the description!");
            }
        });

        ImageIcon eIcon = new ImageIcon(Objects.requireNonNull(eGui.class.getResource("esmaabi-icon.png")));
        setIconImage(eIcon.getImage());

        final JPanel topPanel = getjPanel();

        add(topPanel, BorderLayout.NORTH);

        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.PAGE_AXIS));

        JLabel modeLabel = new JLabel("Select Mode:");
        middlePanel.add(modeLabel);

        modeSelect = new JComboBox<>(new String[]{
                "Beer glass (lvl 1 & sell)",
                "Empty candle latern (lvl 4 & sell)",
                "Empty oil lamp (lvl 12 & sell)",
                "Vial (lvl 33 & sell)",
                "Empty fishbowl (lvl 42 & sell)",
                "Unpowered orb (lvl 46 & sell)",
                "Latern lens (lvl 49 & sell)",
                "Empty light orb (lvl 87 & drop)"
        });
        modeSelect.setSelectedIndex(0);
        middlePanel.add(modeSelect);


        add(middlePanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));

        startButton = new JButton("Start");
        startButton.addActionListener(this);
        bottomPanel.add(startButton);

        pauseButton = new JButton("Pause");
        pauseButton.setVisible(false);
        pauseButton.addActionListener(this);
        bottomPanel.add(pauseButton);

        JButton closeButton = new JButton("Close Gui");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        bottomPanel.add(closeButton);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private static JPanel getjPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

        JLabel descriptionLabel = new JLabel("<html><b>Please read <b>eGlassblowingBot</b> description first!</b></html>");
        topPanel.add(descriptionLabel);

        JLabel descriptionText = new JLabel("<html><br>" +
                "Introducing the most efficient glassblowing bot for Zenyte! " +
                "<br><br><b>Features and Recommendations:</b><br><br> " + "<ul>" +
                "<li>Start near <b>charter trader crewmember</b> or bot will stop.</li>" +
                "<li>The bot will sell all crafted items, except for empty light orbs.</li>" +
                "<li>It's recommended to wield <b>smoke battlestaff</b> or any elemental staff.</li>" +
                "<li>Make sure you have enough coins and air/fire/astral runes.</li>" +
                "<li>The bot will stop if you run out of coins or runes.</li></ul><br>" +
                "For more information, check out Esmaabi on SimpleBot!</html>");
        topPanel.add(descriptionText);
        return topPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) {
            int selectedIndex = modeSelect.getSelectedIndex();
            Mode mode = Mode.values()[selectedIndex];
            nameOfItem = mode.itemName;
            eMain.status = "Making " + nameOfItem;
            widgetItem2 = mode.widgetItemNumber;

            startButton.setVisible(false);
            pauseButton.setVisible(true);
            modeSelect.setEnabled(false);
            eMain.started = true;
        } else if (e.getSource() == pauseButton) {
            eMain.started = false;
            pauseButton.setVisible(false);
            startButton.setVisible(true);
            modeSelect.setEnabled(true);
        }
    }

    private enum Mode {
        BEER_GLASS("beer glass", 14),
        EMPTY_CANDLE_LANTERN("empty candle latern", 15),
        EMPTY_OIL_LAMP("empty oil lamp", 16),
        EMPTY_VIAL("empty vial", 17),
        EMPTY_FISHBOWL("empty fishbowl", 18),
        EMPOWERED_ORB("enpowered orb", 19),
        LANTERN_LENS("latern lens", 20),
        EMPTY_LIGHT_ORB("empty light orb", 21);

        private final String itemName;
        private final int widgetItemNumber;

        Mode(String itemName, int widgetItemNumber) {
            this.itemName = itemName;
            this.widgetItemNumber = widgetItemNumber;
        }
    }

}