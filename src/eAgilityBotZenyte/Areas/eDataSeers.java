package eAgilityBotZenyte.Areas;

import eAgilityBotZenyte.eObstaclesListing;
import net.runelite.api.coords.WorldPoint;
import simple.robot.utils.WorldArea;

import java.util.Arrays;
import java.util.List;

public class eDataSeers {
    // Areas
    public static final WorldArea startArea = new WorldArea (new WorldPoint[] {
            new WorldPoint(2683, 3514, 0),
            new WorldPoint(2743, 3513, 0),
            new WorldPoint(2744, 3482, 0),
            new WorldPoint(2774, 3482, 0),
            new WorldPoint(2774, 3451, 0),
            new WorldPoint(2681, 3452, 0)
    });
    private static final WorldArea firstHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(2731, 3498, 3),
            new WorldPoint(2731, 3489, 3),
            new WorldPoint(2720, 3489, 3),
            new WorldPoint(2720, 3498, 3)
    });

    private static final WorldArea secondHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(2702, 3499, 2),
            new WorldPoint(2702, 3486, 2),
            new WorldPoint(2714, 3486, 2),
            new WorldPoint(2716, 3500, 2)
    });

    private static final WorldArea thirdHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(2708, 3483, 2),
            new WorldPoint(2708, 3475, 2),
            new WorldPoint(2717, 3475, 2),
            new WorldPoint(2717, 3483, 2)
    });

    private static final WorldArea fourthHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(2697, 3478, 3),
            new WorldPoint(2697, 3468, 3),
            new WorldPoint(2718, 3468, 3),
            new WorldPoint(2718, 3474, 3),
            new WorldPoint(2706, 3474, 3),
            new WorldPoint(2706, 3478, 3)
    });

    private static final WorldArea fifthHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(2689, 3467, 2),
            new WorldPoint(2689, 3457, 2),
            new WorldPoint(2704, 3457, 2),
            new WorldPoint(2704, 3467, 2)
    });

    // Cordinates
    public static WorldPoint startPoint = new WorldPoint(2729, 3489, 0);
    public static WorldPoint firstPoint = new WorldPoint(2721, 3493, 3);
    public static WorldPoint secondPoint = new WorldPoint(2710, 3490, 2);
    public static WorldPoint thirdPoint = new WorldPoint(2710, 3477, 2);
    public static WorldPoint fourthPoint = new WorldPoint(2702, 3470, 3);
    public static WorldPoint fifthPoint = new WorldPoint(2702, 3464, 2);

    // Obstacles
    public static List<eObstaclesListing> obstaclesSeers = Arrays.asList(
            new eObstaclesListing(startArea, startPoint, "Climb-up", "Wall"),
            new eObstaclesListing(firstHouse, firstPoint, "Jump", "Gap"),
            new eObstaclesListing(secondHouse, secondPoint, "Cross", "Tightrope"),
            new eObstaclesListing(thirdHouse, thirdPoint, "Jump", "Gap"),
            new eObstaclesListing(fourthHouse, fourthPoint,  "Jump", "Gap"),
            new eObstaclesListing(fifthHouse, fifthPoint, "Jump", "Edge")
    );
}
