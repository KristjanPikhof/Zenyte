package eAgilityBotZenyte.Areas;

import eAgilityBotZenyte.eObstaclesListing;
import net.runelite.api.coords.WorldPoint;
import simple.robot.utils.WorldArea;

import java.util.Arrays;
import java.util.List;

public class eDataAlRellekka {
    // Areas
    public static final WorldArea startArea = new WorldArea(new WorldPoint[]{
            new WorldPoint(2692, 3740, 0),
            new WorldPoint(2691, 3712, 0),
            new WorldPoint(2693, 3703, 0),
            new WorldPoint(2689, 3697, 0),
            new WorldPoint(2693, 3689, 0),
            new WorldPoint(2689, 3675, 0),
            new WorldPoint(2691, 3668, 0),
            new WorldPoint(2689, 3661, 0),
            new WorldPoint(2693, 3654, 0),
            new WorldPoint(2693, 3649, 0),
            new WorldPoint(2682, 3644, 0),
            new WorldPoint(2632, 3645, 0),
            new WorldPoint(2618, 3652, 0),
            new WorldPoint(2605, 3655, 0),
            new WorldPoint(2593, 3666, 0),
            new WorldPoint(2636, 3742, 0)
    });
    private static final WorldArea firstHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(2621, 3677, 3),
            new WorldPoint(2621, 3671, 3),
            new WorldPoint(2628, 3671, 3),
            new WorldPoint(2627, 3677, 3)
    });

    private static final WorldArea secondHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(2614, 3669, 3),
            new WorldPoint(2614, 3657, 3),
            new WorldPoint(2623, 3657, 3),
            new WorldPoint(2623, 3669, 3)
    });

    private static final WorldArea thirdHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(2625, 3656, 3),
            new WorldPoint(2625, 3651, 3),
            new WorldPoint(2631, 3651, 3),
            new WorldPoint(2631, 3656, 3)
    });

    private static final WorldArea fourthHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(2642, 3654, 3),
            new WorldPoint(2645, 3654, 3),
            new WorldPoint(2645, 3648, 3),
            new WorldPoint(2638, 3649, 3),
            new WorldPoint(2634, 3661, 3),
            new WorldPoint(2641, 3660, 3)
    });

    private static final WorldArea fifthHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(2651, 3664, 3),
            new WorldPoint(2651, 3656, 3),
            new WorldPoint(2641, 3656, 3),
            new WorldPoint(2641, 3664, 3)
    });

    private static final WorldArea sixthHouse = new WorldArea (new WorldPoint[] {
            new WorldPoint(2662, 3682, 3),
            new WorldPoint(2654, 3682, 3),
            new WorldPoint(2654, 3664, 3),
            new WorldPoint(2663, 3664, 3),
            new WorldPoint(2663, 3680, 3),
            new WorldPoint(2667, 3682, 3),
            new WorldPoint(2667, 3687, 3),
            new WorldPoint(2662, 3687, 3)
    });

    // Cordinates
    public static WorldPoint startPoint = new WorldPoint(2625, 3677, 0);
    public static WorldPoint firstPoint = new WorldPoint(2622, 3672, 3);
    public static WorldPoint secondPoint = new WorldPoint(2622, 3658, 3);
    public static WorldPoint thirdPoint = new WorldPoint(2630, 3655, 3);
    public static WorldPoint fourthPoint = new WorldPoint(2643, 3653, 3);
    public static WorldPoint fifthPoint = new WorldPoint(2647, 3662, 3);
    public static WorldPoint sixthPoint = new WorldPoint(2655, 3676, 3);

    // Obstacles
    public static List<eObstaclesListing> obstaclesRellekka = Arrays.asList(
            new eObstaclesListing(startArea, startPoint, "Climb", "Rough wall"),
            new eObstaclesListing(firstHouse, firstPoint, "Leap", "Gap"),
            new eObstaclesListing(secondHouse, secondPoint, "Cross", "Tightrope"),
            new eObstaclesListing(thirdHouse, thirdPoint, "Leap", "Gap"),
            new eObstaclesListing(fourthHouse, fourthPoint, "Hurdle", "Gap"),
            new eObstaclesListing(fifthHouse, fifthPoint, "Cross", "Tightrope"),
            new eObstaclesListing(sixthHouse, sixthPoint, "Jump-in", "Pile of fish")
    );
}
