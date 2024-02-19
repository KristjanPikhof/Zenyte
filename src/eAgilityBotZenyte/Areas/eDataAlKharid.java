package eAgilityBotZenyte.Areas;

import eAgilityBotZenyte.eObstaclesListing;
import net.runelite.api.coords.WorldPoint;
import simple.robot.utils.WorldArea;

import java.util.Arrays;
import java.util.List;

public class eDataAlKharid {
    // Areas
    public static final WorldArea startArea = new WorldArea(new WorldPoint[]{
            new WorldPoint(3268, 3213, 0),
            new WorldPoint(3257, 3167, 0),
            new WorldPoint(3284, 3135, 0),
            new WorldPoint(3331, 3137, 0),
            new WorldPoint(3334, 3193, 0),
            new WorldPoint(3318, 3213, 0)
    });
    private static final WorldArea firstHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3270, 3194, 3),
            new WorldPoint(3270, 3189, 3),
            new WorldPoint(3272, 3189, 3),
            new WorldPoint(3272, 3183, 3),
            new WorldPoint(3271, 3183, 3),
            new WorldPoint(3271, 3179, 3),
            new WorldPoint(3276, 3179, 3),
            new WorldPoint(3276, 3184, 3),
            new WorldPoint(3279, 3184, 3),
            new WorldPoint(3279, 3189, 3),
            new WorldPoint(3277, 3189, 3),
            new WorldPoint(3277, 3194, 3)
    });

    private static final WorldArea secondHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3274, 3175, 3),
            new WorldPoint(3274, 3160, 3),
            new WorldPoint(3264, 3160, 3),
            new WorldPoint(3264, 3175, 3)
    });

    private static final WorldArea thirdHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3282, 3177, 3),
            new WorldPoint(3282, 3159, 3),
            new WorldPoint(3304, 3159, 3),
            new WorldPoint(3304, 3170, 3),
            new WorldPoint(3294, 3170, 3),
            new WorldPoint(3294, 3168, 3),
            new WorldPoint(3288, 3168, 3),
            new WorldPoint(3288, 3177, 3)
    });

    private static final WorldArea thirdHouseSmall = new WorldArea (new WorldPoint[] {
            new WorldPoint(3304, 3159, 3),
            new WorldPoint(3294, 3159, 3),
            new WorldPoint(3294, 3169, 3),
            new WorldPoint(3304, 3169, 3)
    });
    private static final WorldArea fourthHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3312, 3167, 1),
            new WorldPoint(3312, 3159, 1),
            new WorldPoint(3320, 3159, 1),
            new WorldPoint(3320, 3167, 1)
    });

    private static final WorldArea fifthHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3319, 3180, 2),
            new WorldPoint(3311, 3180, 2),
            new WorldPoint(3313, 3173, 2),
            new WorldPoint(3319, 3173, 2)
    });

    private static final WorldArea sixthHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3319, 3188, 3),
            new WorldPoint(3311, 3188, 3),
            new WorldPoint(3311, 3179, 3),
            new WorldPoint(3319, 3179, 3)
    });

    private static final WorldArea seventhHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3307, 3189, 3),
            new WorldPoint(3302, 3184, 3),
            new WorldPoint(3295, 3191, 3),
            new WorldPoint(3300, 3196, 3)
    });

    // Cordinates
    public static WorldPoint startPoint = new WorldPoint(3273, 3195, 0);
    public static WorldPoint firstPoint = new WorldPoint(3272, 3182, 3);
    public static WorldPoint secondPoint = new WorldPoint(3268, 3166, 3);
    public static WorldPoint thirdPoint = new WorldPoint(3301, 3163, 3);
    public static WorldPoint fourthPoint = new WorldPoint(3318, 3165, 1);
    public static WorldPoint fifthPoint = new WorldPoint(3316, 3179, 2);
    public static WorldPoint sixthPoint = new WorldPoint(3314, 3186, 3);
    public static WorldPoint seventhPoint = new WorldPoint(3300, 3192, 3);

    // Obstacles
    public static List<eObstaclesListing> obstaclesALKharid = Arrays.asList(
            new eObstaclesListing(startArea, startPoint, "Climb", "Rough wall"),
            new eObstaclesListing(firstHouse, firstPoint, "Cross", "Tightrope"),
            new eObstaclesListing(secondHouse, secondPoint, "Swing-across", "Cable"),
            new eObstaclesListing(thirdHouse, thirdPoint, "Teeth-grip", "Zip line"),
            new eObstaclesListing(fourthHouse, fourthPoint, "Swing-across", "Tropical tree"),
            new eObstaclesListing(fifthHouse, fifthPoint, "Climb", "Roof top beams"),
            new eObstaclesListing(sixthHouse, sixthPoint, "Cross", "Tightrope"),
            new eObstaclesListing(seventhHouse, seventhPoint, "Jump", "Gap")
    );
}
