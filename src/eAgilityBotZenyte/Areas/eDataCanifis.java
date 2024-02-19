package eAgilityBotZenyte.Areas;

import eAgilityBotZenyte.eObstaclesListing;
import net.runelite.api.coords.WorldPoint;
import simple.robot.utils.WorldArea;

import java.util.Arrays;
import java.util.List;

public class eDataCanifis {
    // Areas
    public static final WorldArea startArea = new WorldArea(new WorldPoint[]{
            new WorldPoint(3457, 3515, 0),
            new WorldPoint(3459, 3464, 0),
            new WorldPoint(3528, 3465, 0),
            new WorldPoint(3524, 3518, 0)
    });
    private static final WorldArea firstHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3504, 3490, 2),
            new WorldPoint(3504, 3494, 2),
            new WorldPoint(3503, 3499, 2),
            new WorldPoint(3509, 3499, 2),
            new WorldPoint(3512, 3496, 2),
            new WorldPoint(3512, 3494, 2),
            new WorldPoint(3509, 3493, 2),
            new WorldPoint(3509, 3490, 2)
    });

    private static final WorldArea secondHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3505, 3503, 2),
            new WorldPoint(3505, 3507, 2),
            new WorldPoint(3503, 3508, 2),
            new WorldPoint(3496, 3508, 2),
            new WorldPoint(3495, 3506, 2),
            new WorldPoint(3495, 3503, 2)
    });

    private static final WorldArea thirdHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3493, 3506, 2),
            new WorldPoint(3488, 3506, 2),
            new WorldPoint(3488, 3503, 2),
            new WorldPoint(3485, 3503, 2),
            new WorldPoint(3484, 3501, 2),
            new WorldPoint(3484, 3497, 2),
            new WorldPoint(3493, 3498, 2),
            new WorldPoint(3494, 3500, 2),
            new WorldPoint(3494, 3506, 2)
    });

    private static final WorldArea fourthHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3481, 3501, 3),
            new WorldPoint(3481, 3490, 3),
            new WorldPoint(3473, 3491, 3),
            new WorldPoint(3473, 3500, 3),
            new WorldPoint(3475, 3500, 3),
            new WorldPoint(3476, 3501, 3)
    });

    private static final WorldArea fifthHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3476, 3488, 2),
            new WorldPoint(3485, 3488, 2),
            new WorldPoint(3485, 3484, 2),
            new WorldPoint(3482, 3484, 2),
            new WorldPoint(3482, 3480, 2),
            new WorldPoint(3476, 3481, 2)
    });

    private static final WorldArea sixthHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3490, 3480, 3),
            new WorldPoint(3489, 3478, 3),
            new WorldPoint(3487, 3478, 3),
            new WorldPoint(3487, 3467, 3),
            new WorldPoint(3500, 3468, 3),
            new WorldPoint(3503, 3471, 3),
            new WorldPoint(3505, 3471, 3),
            new WorldPoint(3505, 3478, 3),
            new WorldPoint(3498, 3478, 3),
            new WorldPoint(3498, 3480, 3)
    });

    private static final WorldArea seventhHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(3508, 3484, 2),
            new WorldPoint(3517, 3484, 2),
            new WorldPoint(3517, 3477, 2),
            new WorldPoint(3513, 3475, 2),
            new WorldPoint(3513, 3473, 2),
            new WorldPoint(3509, 3474, 2),
            new WorldPoint(3508, 3478, 2),
            new WorldPoint(3507, 3479, 2),
            new WorldPoint(3507, 3482, 2)
    });

    // Cordinates
    public static WorldPoint startPoint = new WorldPoint(3507, 3488, 0);
    public static WorldPoint firstPoint = new WorldPoint(3505, 3496, 2);
    public static WorldPoint secondPoint = new WorldPoint(3498, 3504, 2);
    public static WorldPoint thirdPoint = new WorldPoint(3487, 3499, 2);
    public static WorldPoint fourthPoint = new WorldPoint(3478, 3493, 3);
    public static WorldPoint fifthPoint = new WorldPoint(3479, 3484, 2);
    public static WorldPoint sixthPoint = new WorldPoint(3503, 3475, 3);
    public static WorldPoint seventhPoint = new WorldPoint(3510, 3482, 2);

    // Obstacles
    public static List<eObstaclesListing> obstaclesCanifis = Arrays.asList(
            new eObstaclesListing(startArea, startPoint, "Climb", "Tall tree"),
            new eObstaclesListing(firstHouse, firstPoint, "Jump", "Gap"),
            new eObstaclesListing(secondHouse, secondPoint, "Jump", "Gap"),
            new eObstaclesListing(thirdHouse, thirdPoint, "Jump", "Gap"),
            new eObstaclesListing(fourthHouse, fourthPoint, "Jump", "Gap"),
            new eObstaclesListing(fifthHouse, fifthPoint, "Vault", "Pole-vault"),
            new eObstaclesListing(sixthHouse, sixthPoint, "Jump", "Gap"),
            new eObstaclesListing(seventhHouse, seventhPoint, "Jump", "Gap")
    );
}
