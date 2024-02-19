package BotUtils;

import simple.robot.api.ClientContext;

public class eData {

    public static ClientContext ctx;

    public eData(ClientContext ctx) {
        eData.ctx = ctx;
    }

    public static class Banking {
        public static final String[] BANK_NAMES = {"Bank booth", "Bank chest", "Bank counter"};
        public static final String DEPOSIT_BOX = "Bank Deposit Box";
        public static final String[] BANKER_NAMES = {
                "Banker", "Arnold Lydspor", "Banker tutor", "Cornelius", "Emerald Benedict", "Eniola", "Fadli", "Financial Wizard", "Financial Seer", "Ghost banker", "Gnome banker", "Gundai", "Jade", "Jumaane", "Magnus Gram", "Nardah Banker", "Odovacar", "Peer the Seer", "Sirsal Banker", "Squire", "TzHaar-Ket-Yil", "TzHaar-Ket-Zuh", "Yusuf"
        };
    }

    public static class Woodcutting {
        public static final String[] SPECIAL_ATTACK_TOOL = {
                "Dragon axe (or)",
                "Infernal axe",
                "Dragon axe"
        };

        public static final String[] WOODCUTTING_AXE = {
                "Bronze axe",
                "Iron axe",
                "Steel axe",
                "Blessed axe",
                "Gilded axe",
                "3rd age axe",
                "Black axe",
                "Mithril axe",
                "Adamant axe",
                "Rune axe",
                "Dragon axe",
                "Infernal axe",
                "Crystal axe"
        };
    }

    public static class Fishing {
        public static final String[] SPECIAL_ATTACK_TOOL = {
                "Dragon harpoon",
        };

        public static final String[] EQUIPMENT = {
                "Fishing rod",
                "Sandworms",
        };

        //public static final int[] EQUIPMENT = {307, 13431};
    }

    public static class Mining {
        public static final String[] SPECIAL_ATTACK_TOOL = {
                "Dragon pickaxe",
        };

        public static final int[] EQUIPMENT_NOT_TO_BANK = {30742, 20014, 13243, 12797, 12297, 11920, 1275, 1273, 1271, 1269, 1267, 1265};
    }

}
