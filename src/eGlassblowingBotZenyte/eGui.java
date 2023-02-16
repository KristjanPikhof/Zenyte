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

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

        JLabel descriptionLabel = new JLabel("<html><b>Please read <b>eGlassblowingBot</b> description first!</b></html>");
        topPanel.add(descriptionLabel);

        JLabel descriptionText = new JLabel("<html><br>The most effective glassblowing bot on Zenyte! <br><br><b>Features & recommendations:</b><br><br> " +
                "<ul><li>You must have enough of fire, astral runes & coins in inventory;</li>" +
                "<li>You must wield <b>any air staff</b>;</li>" +
                "<li>You must start near charter trader crewmembers;</li>" +
                "<li>Bot will sell to shop all the crafted items except empty light orb (drop);</li>" +
                "<li>Script will stop if you are out of money / runes.</li></ul><br>" +
                "For more information check out Esmaabi on SimpleBot!</html>");
        topPanel.add(descriptionText);

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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) {
            int selectedIndex = modeSelect.getSelectedIndex();
            switch (selectedIndex) {
                case 0:
                    eMain.status = "Making beer glass";
                    widgetItem2 = 14;
                    break;
                case 1:
                    eMain.status = "Making empty candle latern";
                    widgetItem2 = 15;
                    break;
                case 2:
                    eMain.status = "Making empty oil lamp";
                    widgetItem2 = 16;
                    break;
                case 3:
                    eMain.status = "Making empty vial";
                    widgetItem2 = 17;
                    break;
                case 4:
                    eMain.status = "Making empty fishbowl";
                    widgetItem2 = 18;
                    break;
                case 5:
                    eMain.status = "Making enpowered orb";
                    widgetItem2 = 19;
                    break;
                case 6:
                    eMain.status = "Making latern lens";
                    widgetItem2 = 20;
                    break;
                case 7:
                    eMain.status = "Making empty light orb";
                    widgetItem2 = 21;
                    break;
                default:
                    break;
            }

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

}