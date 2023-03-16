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
    public static JComboBox<String> locationName;
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

        ImageIcon eIcon = new ImageIcon(Objects.requireNonNull(eGlassblowingBotZenyte.eGui.class.getResource("esmaabi-icon.png")));
        setIconImage(eIcon.getImage());

        //Select location menu
        JLabel lblSelectLocation = new JLabel("Select Location: ");
        lblSelectLocation.setForeground(Color.WHITE);
        lblSelectLocation.setBounds(10, 14, 100, 15);
        contentPane.add(lblSelectLocation);

        locationName = new JComboBox<String>();
        locationName.setModel(new DefaultComboBoxModel<String>(new String[] {"Falador East", "Varrock East", "Grand Exchange"}));
        locationName.setBounds(110, 11, 150, 20);
        contentPane.add(locationName);

        //Select logs menu
        JLabel lblSelectLogs = new JLabel("Select bones: ");
        lblSelectLogs.setForeground(Color.WHITE);
        lblSelectLogs.setBounds(10, 39, 100, 15);
        contentPane.add(lblSelectLogs);

        bonesName = new JComboBox<String>();
        bonesName.setModel(new DefaultComboBoxModel<String>(new String[] {
                "Infernal ashes",
                "Dragon bones",
                "Big bones",
                "Bones",
                "Lava dragon bones",
                "Hydra bones",
                "Ourg bones",
                "Babydragon boness",
                "Superior dragon bones["
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
                locationName.setEnabled(false);
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
                locationName.setEnabled(true);
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

