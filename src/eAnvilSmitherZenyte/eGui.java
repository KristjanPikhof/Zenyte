package eAnvilSmitherZenyte;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class eGui {
    public static String returnItem;
    public static Component f;

    public static void eGuiDialogueTarget() {
        String[] smithingItems = {"Sword", "Platebody", "Dart tips", "Bolts"};

        ImageIcon eIcon = new ImageIcon(Objects.requireNonNull(eGui.class.getResource("Smithing_cape_logo.png")));

        returnItem = (String) JOptionPane.showInputDialog(f,
                "<html>"
                        + "<b>Select item you want to smith:</b><br><br>"
                        + "<p><strong>Features & recommendations:</strong></p>"
                        + "<ul>"
                        + "<li>Start with a <strong>hammer</strong> in your inventory.</li>"
                        + "<li>Start <strong>with bars in your inventory</strong>.</li>"
                        + "<li>Start at Varrock West Bank or Port Khazard Bank.</li>"
                        + "<li>Zoom out to <strong>see both the anvil and the bank</strong>.</li>"
                        + "<li>Incorporates random sleep times for a more natural behavior.</li>"
                        + "</ul>"
                        + "</html>",
                "Select item you want to smith - eAnvilSmither by Esmaabi",
                JOptionPane.WARNING_MESSAGE, eIcon, smithingItems, smithingItems[2]);
    }

    public static void main(String[] args) {
        eGuiDialogueTarget();
    }
}
