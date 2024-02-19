package eAgilityBotZenyte.Areas;

import eAgilityBotZenyte.eObstaclesListing;
import net.runelite.api.coords.WorldPoint;
import simple.robot.utils.WorldArea;

import java.util.Arrays;
import java.util.List;

public class eDataPollnivneach {
    // Areas
    public static final WorldArea startArea = new WorldArea(new WorldPoint[]{
            new WorldPoint(3323, 3021, 0),
            new WorldPoint(3357, 3021, 0),
            new WorldPoint(3382, 2988, 0),
            new WorldPoint(3381, 2962, 0),
            new WorldPoint(3345, 2925, 0),
            new WorldPoint(3302, 2917, 0)
    });
    private static final WorldArea firstHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3345, 2969, 1),
            new WorldPoint(3345, 2963, 1),
            new WorldPoint(3349, 2962, 1),
            new WorldPoint(3353, 2963, 1),
            new WorldPoint(3352, 2970, 1),
            new WorldPoint(3345, 2970, 1)
    });

    private static final WorldArea secondHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3356, 2977, 1),
            new WorldPoint(3356, 2972, 1),
            new WorldPoint(3351, 2972, 1),
            new WorldPoint(3351, 2977, 1),
            new WorldPoint(3356, 2977, 1)
    });

    private static final WorldArea thirdHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3359, 2980, 1),
            new WorldPoint(3363, 2980, 1),
            new WorldPoint(3363, 2976, 1),
            new WorldPoint(3359, 2976, 1)
    });

    private static final WorldArea fourthHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3365, 2977, 1),
            new WorldPoint(3365, 2973, 1),
            new WorldPoint(3371, 2973, 1),
            new WorldPoint(3371, 2977, 1)
    });

    private static final WorldArea fifthHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3364, 2984, 1),
            new WorldPoint(3364, 2987, 1),
            new WorldPoint(3371, 2987, 1),
            new WorldPoint(3371, 2984, 1),
            new WorldPoint(3370, 2981, 1),
            new WorldPoint(3365, 2981, 1),
            new WorldPoint(3365, 2983, 1),
            new WorldPoint(3366, 2983, 1),
            new WorldPoint(3366, 2984, 1)
    });

    private static final WorldArea sixthHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3366, 2987, 2),
            new WorldPoint(3354, 2987, 2),
            new WorldPoint(3354, 2979, 2),
            new WorldPoint(3366, 2979, 2)
    });

    private static final WorldArea seventhHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3355, 2996, 2),
            new WorldPoint(3356, 2990, 2),
            new WorldPoint(3371, 2989, 2),
            new WorldPoint(3371, 2996, 2)
    });

    private static final WorldArea eightHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3355, 3006, 2),
            new WorldPoint(3355, 2999, 2),
            new WorldPoint(3363, 2999, 2),
            new WorldPoint(3363, 3006, 2)
    });

    // Cordinates
    public static WorldPoint startPoint = new WorldPoint(3352, 2962, 0);
    public static WorldPoint firstPoint = new WorldPoint(3350, 2968, 1);
    public static WorldPoint secondPoint = new WorldPoint(3355, 2976, 1);
    public static WorldPoint thirdPoint = new WorldPoint(3362, 2977, 1);
    public static WorldPoint fourthPoint = new WorldPoint(3368, 2976, 1);
    public static WorldPoint fifthPoint = new WorldPoint(3365, 2982, 1);
    public static WorldPoint sixthPoint = new WorldPoint(3358, 2984, 2);
    public static WorldPoint seventhPoint = new WorldPoint(3360, 2995, 2);
    public static WorldPoint eightPoint = new WorldPoint(3362, 3002, 2);

    // Obstacles
    public static List<eObstaclesListing> obstaclesPollnivneach = Arrays.asList(
            new eObstaclesListing(startArea, startPoint, "Climb-on", "Basket"),
            new eObstaclesListing(firstHouse, firstPoint, "Jump-on", "Market stall"),
            new eObstaclesListing(secondHouse, secondPoint, "Grab", "Banner"),
            new eObstaclesListing(thirdHouse, thirdPoint, "Leap", "Gap"),
            new eObstaclesListing(fourthHouse, fourthPoint, "Jump-to", "Tree"),
            new eObstaclesListing(fifthHouse, fifthPoint, "Climb", "Rough wall"),
            new eObstaclesListing(sixthHouse, sixthPoint, "Cross", "Monkeybars"),
            new eObstaclesListing(seventhHouse, seventhPoint, "Jump-on", "Tree"),
            new eObstaclesListing(eightHouse, eightPoint, "Jump-to", "Drying line")
    );
}
