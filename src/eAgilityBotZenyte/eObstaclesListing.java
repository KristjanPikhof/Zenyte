package eAgilityBotZenyte;
import net.runelite.api.coords.WorldPoint;
import simple.robot.utils.WorldArea;

public class eObstaclesListing {
    WorldArea obstacleArea;
    WorldPoint obstaclePoint;
    String objectName;
    String actionName;

    public eObstaclesListing(WorldArea obstacleArea, WorldPoint obstaclePoint, String actionName, String objectName) {
        this.obstacleArea = obstacleArea;
        this.obstaclePoint = obstaclePoint;
        this.objectName = objectName;
        this.actionName = actionName;
    }

}
