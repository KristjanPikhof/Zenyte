package eAgilityBotZenyte.Areas;

import eAgilityBotZenyte.eObstaclesListing;
import net.runelite.api.coords.WorldPoint;
import simple.robot.utils.WorldArea;

import java.util.Arrays;
import java.util.List;

public class eDataVarrock {
    // Areas
    public static final WorldArea startArea = new WorldArea(new WorldPoint[]{
            new WorldPoint(3174, 3447, 0),
            new WorldPoint(3175, 3380, 0),
            new WorldPoint(3284, 3378, 0),
            new WorldPoint(3285, 3448, 0)
    });
    private static final WorldArea firstHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3221, 3409, 3),
            new WorldPoint(3213, 3409, 3),
            new WorldPoint(3213, 3421, 3),
            new WorldPoint(3221, 3421, 3)
    });

    private static final WorldArea secondHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3209, 3413, 3),
            new WorldPoint(3209, 3416, 3),
            new WorldPoint(3210, 3418, 3),
            new WorldPoint(3208, 3421, 3),
            new WorldPoint(3202, 3421, 3),
            new WorldPoint(3199, 3416, 3),
            new WorldPoint(3203, 3412, 3)
    });

    private static final WorldArea thirdHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3199, 3418, 1),
            new WorldPoint(3199, 3415, 1),
            new WorldPoint(3191, 3415, 1),
            new WorldPoint(3191, 3418, 1)
    });

    private static final WorldArea fourthHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3199, 3407, 3),
            new WorldPoint(3190, 3407, 3),
            new WorldPoint(3190, 3401, 3),
            new WorldPoint(3199, 3401, 3)
    });

    private static final WorldArea fifthHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3190, 3387, 3),
            new WorldPoint(3190, 3381, 3),
            new WorldPoint(3181, 3381, 3),
            new WorldPoint(3181, 3400, 3),
            new WorldPoint(3201, 3400, 3),
            new WorldPoint(3201, 3405, 3),
            new WorldPoint(3210, 3405, 3),
            new WorldPoint(3210, 3395, 3),
            new WorldPoint(3196, 3387, 3)
    });

    private static final WorldArea sixthHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3217, 3404, 3),
            new WorldPoint(3217, 3392, 3),
            new WorldPoint(3234, 3392, 3),
            new WorldPoint(3234, 3404, 3),
            new WorldPoint(3220, 3406, 3)
    });

    private static final WorldArea seventhHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3241, 3410, 3),
            new WorldPoint(3235, 3410, 3),
            new WorldPoint(3235, 3402, 3),
            new WorldPoint(3241, 3402, 3)
    });

    private static final WorldArea eightHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3241, 3410, 3),
            new WorldPoint(3235, 3410, 3),
            new WorldPoint(3235, 3418, 3),
            new WorldPoint(3241, 3418, 3)
    });

    // Cordinates
    public static WorldPoint startPoint = new WorldPoint(3221, 3414, 0);
    public static WorldPoint firstPoint = new WorldPoint(3214, 3414, 3);
    public static WorldPoint secondPoint = new WorldPoint(3201, 3417, 3);
    public static WorldPoint thirdPoint = new WorldPoint(3194, 3416, 1);
    public static WorldPoint fourthPoint = new WorldPoint(3193, 3402, 3);
    public static WorldPoint fifthPoint = new WorldPoint(3208, 3398, 3);
    public static WorldPoint sixthPoint = new WorldPoint(3232, 3402, 3);
    public static WorldPoint seventhPoint = new WorldPoint(3238, 3408, 3);
    public static WorldPoint eightPoint = new WorldPoint(3237, 3415, 3);

    // Obstacles
    public static List<eObstaclesListing> obstaclesVarrock = Arrays.asList(
            new eObstaclesListing(startArea, startPoint, "Climb", "Rough wall"),
            new eObstaclesListing(firstHouse, firstPoint, "Cross", "Clothes line"),
            new eObstaclesListing(secondHouse, secondPoint, "Leap", "Gap"),
            new eObstaclesListing(thirdHouse, thirdPoint, "Balance", "Wall"),
            new eObstaclesListing(fourthHouse, fourthPoint, "Leap", "Gap"),
            new eObstaclesListing(fifthHouse, fifthPoint, "Leap", "Gap"),
            new eObstaclesListing(sixthHouse, sixthPoint, "Leap", "Gap"),
            new eObstaclesListing(seventhHouse, seventhPoint, "Hurdle", "Ledge"),
            new eObstaclesListing(eightHouse, eightPoint, "Jump-off", "Edge")
    );
}
