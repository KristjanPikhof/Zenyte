package eWoodcuttingBotZenyte;


import java.util.Arrays;
import java.util.List;

class eDataTrees {
    String examineResult;
    String objectName;
    String action;

    public eDataTrees(String examineResult, String objectName, String action) {
        this.examineResult = examineResult;
        this.objectName = objectName;
        this.action = action;
    }

    public static List<eDataTrees> allTreesData = Arrays.asList(
            new eDataTrees("beautiful old mahogany tree", "Mahogany", "Chop down"),
            new eDataTrees("beautiful old teak tree", "Teak", "Chop down"),
            new eDataTrees("hardy evergreen tree", "Evergreen", "Chop down"),
            new eDataTrees("beautiful old oak", "Oak", "Chop down"),
            new eDataTrees("commonly found tree", "Tree", "Chop down"),
            new eDataTrees("most common trees", "Tree", "Chop down"),
            new eDataTrees("tree has long been dead", "Dead tree", "Chop down"),
            new eDataTrees("only useful for firewood now", "Dead tree", "Chop down"),
            new eDataTrees("splendid tree", "Yew", "Chop down"),
            new eDataTrees("makes good syrup", "Maple tree", "Chop down"),
            new eDataTrees("trees are found near water", "Willow", "Chop down"),
            new eDataTrees("droopy tree", "Willow", "Chop down"),
            new eDataTrees("splendid tree", "Yew", "Chop down"),
            new eDataTrees("tree shimmers with a magical force", "Magic tree", "Chop down"),
            new eDataTrees("enormous majestic tree", "Redwood", "Cut"),
            new eDataTrees("section of the tree has been carved out", "Redwood", "Cut")

    );

}
