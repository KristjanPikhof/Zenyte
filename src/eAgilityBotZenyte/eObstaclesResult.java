package eAgilityBotZenyte;

import java.util.List;

public class eObstaclesResult {
    public static String statusMessage;
    public static boolean sleepToPickup;
    public final List<eObstaclesListing> obstacles;

    public eObstaclesResult(String statusMessage, boolean sleepToPickup, List<eObstaclesListing> obstacles) {
        eObstaclesResult.statusMessage = statusMessage;
        eObstaclesResult.sleepToPickup = sleepToPickup;
        this.obstacles = obstacles;
    }

    public static String getStatusMessage() {
        return statusMessage;
    }

    public static boolean sleepToPickupCheck() {
        return sleepToPickup;
    }

    public List<eObstaclesListing> getObstacles() {
        return obstacles;
    }
}

