package eChaosAltarBot;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class eGui extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    public static JComboBox<String> minPlayersInArea;
    public static JComboBox<String> bonesName;
    private final JButton startButton;
    private final JButton pauseButton;
    private final JButton paintButton;

    public eGui() {
        setTitle("eChaosAltarBot");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 300, 150);
        contentPane = new JPanel();
        contentPane.setBackground(Color.DARK_GRAY);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        ImageIcon eIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("esmaabi-icon.png")));
        setIconImage(eIcon.getImage());

        //Select location menu
        JLabel lblSelectLocation = new JLabel("Allowed players: ");
        lblSelectLocation.setForeground(Color.WHITE);
        lblSelectLocation.setBounds(10, 14, 100, 15);
        contentPane.add(lblSelectLocation);

        minPlayersInArea = new JComboBox<String>();
        minPlayersInArea.setModel(new DefaultComboBoxModel<String>(new String[] {"Only you", "You + 1", "You + 2", "You + 3", "Disable logout"}));
        minPlayersInArea.setBounds(110, 11, 150, 20);
        contentPane.add(minPlayersInArea);

        //Select logs menu
        JLabel lblSelectLogs = new JLabel("Select bones: ");
        lblSelectLogs.setForeground(Color.WHITE);
        lblSelectLogs.setBounds(10, 39, 100, 15);
        contentPane.add(lblSelectLogs);

        bonesName = new JComboBox<String>();
        bonesName.setModel(new DefaultComboBoxModel<String>(new String[] {
                "Infernal ashes",
                "Dragon bones",
                "Babydragon bones",
                "Big bones",
                "Normal bones",
                "Lava dragon bones",
                "Hydra bones",
                "Dagannoth bones",
                "Wyrm bones",
                "Wyvern bones",
                "Lava dragon bones",
                "Superior dragon bones",
                "Bat bones",
                "Ourg bones",
                "Abyssal ashes",
                "Malicious ashes",
                "Abyssal ashes",
                "Vile ashes",
                "Fiendish ashes"
        }));
        bonesName.setBounds(110, 36, 150, 20);
        contentPane.add(bonesName);

        //Start Button actions
        startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {

                eMain.botStarted = true;
                pauseButton.setVisible(true);
                startButton.setVisible(false);
                bonesName.setEnabled(false);
                minPlayersInArea.setEnabled(false);
                eMain.bonesName = Objects.requireNonNull(bonesName.getSelectedItem()).toString();
            }
        });
        startButton.setBounds(25, 74, 100, 23);
        contentPane.add(startButton);

        //Stop button actions
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                eMain.botStarted = false;
                pauseButton.setVisible(false);
                startButton.setVisible(true);
                bonesName.setEnabled(true);
                minPlayersInArea.setEnabled(true);
            }
        });
        pauseButton.setBounds(25, 74, 100, 23);
        contentPane.add(pauseButton);

        //Paint button actions
        paintButton = new JButton("Paint");
        paintButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                eMain.hidePaint = !eMain.hidePaint;
            }
        });
        paintButton.setBounds(160, 74, 100, 23);
        contentPane.add(paintButton);
    }
}

