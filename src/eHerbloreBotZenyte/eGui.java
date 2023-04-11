package eHerbloreBotZenyte;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class eGui {
    public static String returnItem;
    public static Component f;

    public static void eGuiDialogueTarget() {
        String[] smithingItems = {"Super energy potion", "Staming potion", "Zamorak brew", "Super strenght potion", "Super attack potion", "Super defence potion", "Ranging potion", "Prayer potion", "Magic potion", "Saradomin brew", "Super combat potion", "Superantipoison"};

        ImageIcon eIcon = new ImageIcon(Objects.requireNonNull(eGui.class.getResource("Herblore_cape.png")));

        returnItem = (String) JOptionPane.showInputDialog(f,
                "<html>"
                        + "<p>The most effective anvil herblore bot on Zenyte!</p>"
                        + "<p><strong>Features & recommendations:</strong></p>"
                        + "<ul>"
                        + "<li>Start with a <strong>empty inventory</strong>.</li>"
                        + "<li>Start <strong>near bank booth or bank chest</strong>.</li>"
                        + "<li>Have unfinished potions and secondaries visible in bank</li>"
                        + "<li>Zoom in to <strong>see bank close</strong> for better performance.</li>"
                        + "<li>At the moment only <strong>making potions</strong> supported.</li>"
                        + "</ul>"
                        + "</html>",
                "Select potions you want to make - eHerbloreBot by Esmaabi",
                JOptionPane.WARNING_MESSAGE, eIcon, smithingItems, smithingItems[0]);
    }

    public static void main(String[] args) {
        eGuiDialogueTarget();
    }
}
