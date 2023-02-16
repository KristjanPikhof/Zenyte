package eRunecraftingBotZenyte;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

import static eRunecraftingBotZenyte.eMain.status;



public class eGui extends JFrame implements ActionListener {


    private final JComboBox<String> modeSelect;
    private final JButton startButton;
    private final JButton pauseButton;
    private final JButton closeButton;

    public eGui() {
        setTitle("eRunecraftingBot by Esmaabi");
        setLayout(new FlowLayout());
        setSize(550, 250);
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

        JLabel descriptionLabel = new JLabel("<html><b>Please read <b>eRunecraftingBot</b> description first!</b></html>");
        topPanel.add(descriptionLabel);

        JLabel descriptionText = new JLabel("<html><br><b>Description</b>:<br> " +
                "It is required to have chisel in inventory for <b>Mining</b> and <b>Running Bloods</b> tasks<br> " +
                "Start near dense runestone for <b>Mining</b> task while Zenyte deposit chest is activated<br> " +
                "Start at Crafting Guild with <b>Max cape</b> for other tasks<br><br> " +
                "For more information check out Esmaabi on SimpleBot!</html>");
        topPanel.add(descriptionText);

        add(topPanel, BorderLayout.NORTH);

        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.PAGE_AXIS));

        JLabel modeLabel = new JLabel("Select Mode:");
        middlePanel.add(modeLabel);

        modeSelect = new JComboBox<>(new String[]{
                "Mining",
                "Making dark blocks",
                "Running bloods"
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

        closeButton = new JButton("Close Gui");
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
            if (modeSelect.getSelectedIndex() == 0) {
                eMain.playerState = eMain.State.MINING;
                status = "Mining task started";
                startButton.setVisible(false);
                pauseButton.setVisible(true);
                modeSelect.setEnabled(false);
            } else if (modeSelect.getSelectedIndex() == 1) {
                eMain.playerState = eMain.State.CRAFTING;
                status = "Making dark block task started";
                startButton.setVisible(false);
                pauseButton.setVisible(true);
                modeSelect.setEnabled(false);
            } else if (modeSelect.getSelectedIndex() == 2) {
                eMain.playerState = eMain.State.BLOODCRAFTING;
                status = "Blood rune running task started";
                startButton.setVisible(false);
                pauseButton.setVisible(true);
                modeSelect.setEnabled(false);
            } else {
                eMain.playerState = eMain.State.WAITING;
                status = "Please choose a mode to start";
            }
            eMain.started = modeSelect.getSelectedIndex() != -1;
        } else if (e.getSource() == pauseButton) {
            pauseButton.setVisible(false);
            startButton.setVisible(true);
            modeSelect.setEnabled(true);
            eMain.playerState = eMain.State.WAITING;
            status = "Script is paused";
        }
    }

}