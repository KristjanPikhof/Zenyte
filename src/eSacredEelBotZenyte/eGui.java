package eSacredEelBotZenyte;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class eGui extends JFrame implements ActionListener {

    private final JButton startButton;
    private final JButton pauseButton;
    private final JButton closeButton;

    public eGui() {
        setTitle("eSacredEelZenyte by Esmaabi");
        setLayout(new FlowLayout());
        setSize(450, 225);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ImageIcon eIcon = new ImageIcon(Objects.requireNonNull(eGui.class.getResource("esmaabi-icon.png")));
        setIconImage(eIcon.getImage());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

        JLabel descriptionLabel = new JLabel("<html><b>Please read <b>eSacredEelBotZenyte</b> description first!</b></html>");
        topPanel.add(descriptionLabel);

        JLabel descriptionText = new JLabel("<html><br><b>Description</b>:<br> " +
                "It is required to have <b>Fishing rod</b>, <b>Fishing bait</b> & a <b>Knife</b> in inventory.<br> " +
                "Start the scrip near fishing spots at <b>Zul-Andra</b><br> " +
                "Bot will equip Angler equipment if acquired.<br><br> " +
                "For more information check out Esmaabi on SimpleBot!</html>");
        topPanel.add(descriptionText);
        add(topPanel, BorderLayout.NORTH);


        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

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
            eMain.started = true;
            eMain.playerState = eMain.State.FISHING;
            eMain.status = "Script is started";
            startButton.setVisible(false);
            pauseButton.setVisible(true);
        } else if (e.getSource() == pauseButton) {
            eMain.playerState = eMain.State.WAITING;
            eMain.status = "Script is paused";
            pauseButton.setVisible(false);
            startButton.setVisible(true);
        }

        if (eMain.botTerminated) {
            dispose();
        }
    }


}

