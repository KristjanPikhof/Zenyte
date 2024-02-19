package eAgilityBotZenyte.Areas;

import eAgilityBotZenyte.eObstaclesListing;
import net.runelite.api.coords.WorldPoint;
import simple.robot.utils.WorldArea;

import java.util.Arrays;
import java.util.List;

public class eDataArdougne {
    // Areas
    public static final WorldArea startArea = new WorldArea(new WorldPoint[]{
            new WorldPoint(2600, 3343, 0),
            new WorldPoint(2690, 3342, 0),
            new WorldPoint(2689, 3264, 0),
            new WorldPoint(2587, 3259, 0)
    });
    public static final WorldArea firstHouse = new WorldArea(new WorldPoint[]{
            new WorldPoint(2669, 3311, 3),
            new WorldPoint(2669, 3298, 3),
            new WorldPoint(2674, 3298, 3),
            new WorldPoint(2674, 3311, 3)
    });
    public static final WorldArea secondHouse = new WorldArea(new WorldPoint[]{
            new WorldPoint(2666, 3317, 3),
            new WorldPoint(2660, 3317, 3),
            new WorldPoint(2660, 3320, 3),
            new WorldPoint(2666, 3320, 3)
    });
    public static final WorldArea thirdHouse = new WorldArea(new WorldPoint[]{
            new WorldPoint(2658, 3320, 3),
            new WorldPoint(2658, 3317, 3),
            new WorldPoint(2653, 3317, 3),
            new WorldPoint(2653, 3320, 3)
    });
    public static final WorldArea fourthHouse = new WorldArea(new WorldPoint[]{
            new WorldPoint(2651, 3315, 3),
            new WorldPoint(2651, 3310, 3),
            new WorldPoint(2655, 3310, 3),
            new WorldPoint(2655, 3315, 3)
    });
    public static final WorldArea fifthHouse = new WorldArea(new WorldPoint[]{
            new WorldPoint(2649, 3310, 3),
            new WorldPoint(2652, 3310, 3),
            new WorldPoint(2652, 3306, 3),
            new WorldPoint(2654, 3305, 3),
            new WorldPoint(2655, 3303, 3),
            new WorldPoint(2657, 3302, 3),
            new WorldPoint(2652, 3299, 3)
    });
    public static final WorldArea sixthHouse = new WorldArea(new WorldPoint[]{
            new WorldPoint(2659, 3295, 3),
            new WorldPoint(2659, 3300, 3),
            new WorldPoint(2656, 3300, 3),
            new WorldPoint(2654, 3299, 3),
            new WorldPoint(2655, 3295, 3)
    });

    // Cordinates
    public static WorldPoint startPoint = new WorldPoint(2673, 3297, 0);
    public static WorldPoint firstPoint = new WorldPoint(2671, 3309, 3);
    public static WorldPoint secondPoint = new WorldPoint(2662, 3318, 3);
    public static WorldPoint thirdPoint = new WorldPoint(2654, 3318, 3);
    public static WorldPoint fourthPoint = new WorldPoint(2653, 3310, 3);
    public static WorldPoint fifthPoint = new WorldPoint(2653, 3300, 3);
    public static WorldPoint sixthPoint = new WorldPoint(2656, 3297, 3);



    // Obstacles
    public static List<eObstaclesListing> obstaclesArdougne = Arrays.asList(
            new eObstaclesListing(startArea, startPoint, "Climb-up", "Wooden Beams"),
            new eObstaclesListing(firstHouse, firstPoint, "Jump", "Gap"),
            new eObstaclesListing(secondHouse, secondPoint, "Walk-on", "Plank"),
            new eObstaclesListing(thirdHouse, thirdPoint, "Jump", "Gap"),
            new eObstaclesListing(fourthHouse, fourthPoint, "Jump", "Gap"),
            new eObstaclesListing(fifthHouse, fifthPoint, "Balance-across", "Steep roof"),
            new eObstaclesListing(sixthHouse, sixthPoint, "Jump", "Gap")
    );
}
