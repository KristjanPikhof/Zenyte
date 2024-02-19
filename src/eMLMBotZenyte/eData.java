package eMLMBotZenyte;


import java.util.Arrays;
import java.util.List;

class eData {
    String examineResult;
    String objectName;
    String action;

    public eData(String examineResult, String objectName, String action) {
        this.examineResult = examineResult;
        this.objectName = objectName;
        this.action = action;
    }

    public static List<eData> rocksData = Arrays.asList(
            new eData("mineral vein.", "Ore vein", "Mine")

    );

}
