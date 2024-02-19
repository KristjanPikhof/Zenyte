package BotUtils;

import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimplePrayers;
import simple.hooks.interfaces.SimpleLocatable;
import simple.hooks.queries.SimpleItemQuery;
import simple.hooks.simplebot.Game;
import simple.hooks.wrappers.*;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

import java.awt.*;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import static eApiAccess.eAutoResponser.randomSleeping;

public class eActions {

    public static boolean menuActionMode;
    public static boolean specialAttackTool;
    public static final Random random = new Random();
    public static String status = null;
    public static final WorldArea EDGE_HOME_AREA = new WorldArea(new WorldPoint(3110, 3474, 0), new WorldPoint(3074, 3516, 0));
    public static int[] randomEventItems = {7498, 6182, 6180, 6181};
    private static ClientContext ctx;

    public eActions(ClientContext ctx) {
        eActions.ctx = ctx;
    }

    // Utility
    public static void updateStatus(String newStatus) {
        status = newStatus;
        ctx.log(status);
    }

    public static String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static long getCurrentTimeMilli() {
        final Instant currentTime = Instant.now();
        return currentTime.toEpochMilli();
    }

    public static int getRandomInt(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    public enum StackableType {
        STACKABLE, NON_STACKABLE, BOTH
    }


    // Functions
    public static void interactWith(SimpleNpc interactable, String menuAction) {
        if (menuActionMode) {
            interactable.menuAction(menuAction);
        } else {
            interactable.click(menuAction);
        }
    }

    public static void interactWith(SimpleObject interactable, String menuAction) {
        if (menuActionMode) {
            interactable.menuAction(menuAction);
        } else {
            interactable.click(menuAction);
        }
    }

    public static void interactWith(SimpleGroundItem interactable, String menuAction) {
        if (menuActionMode) {
            interactable.menuAction(menuAction);
        } else {
            interactable.click(menuAction);
        }
    }

/*    public static SimpleItemQuery<SimpleItem> getItemsFiltered(StackableType type, int... itemIds) {
        return filterByStackableType(ctx.inventory.populate().filter(itemIds), type);
    }*/

    public static SimpleItemQuery<SimpleItem> getItemsFiltered(StackableType type, int... itemIds) {
        if (ctx == null || ctx.inventory == null) {
            return null;  // Return null if ctx or ctx.inventory is not initialized
        }

        SimpleItemQuery<SimpleItem> items = ctx.inventory.populate();
        if (items == null) {
            return null;
        }

        items = items.filter(itemIds);
        if (items == null) {
            return null;
        }

        return filterByStackableType(items, type);
    }


    public static SimpleItemQuery<SimpleItem> getItemsFiltered(StackableType type, String... itemName) {
        return filterByStackableType(ctx.inventory.populate().filter(itemName), type);
    }

/*    public static boolean hasItemsInInventory(StackableType type, int... itemIds) {
        return !getItemsFiltered(type, itemIds).isEmpty();
    }*/

    public static boolean hasItemsInInventory(StackableType type, int... itemIds) {
        SimpleItemQuery<SimpleItem> items = getItemsFiltered(type, itemIds);
        return items != null && !items.isEmpty();
    }


    public static boolean hasItemsInInventory(StackableType type, String... itemNames) {
        return !getItemsFiltered(type, itemNames).isEmpty();
    }

    public static boolean hasNoItemsInInventory(StackableType type, int... itemIds) {
        for (int itemId : itemIds) {
            if (hasItemsInInventory(type, itemId)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasNoItemsInInventory(StackableType type, String... itemNames) {
        for (String itemName : itemNames) {
            if (hasItemsInInventory(type, itemName)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasItemsInInventory(StackableType type, List<String> itemList) {
        for (String itemName : itemList) {
            if (hasItemsInInventory(type, itemName)) {
                return true;
            }
        }
        return false;
    }

    private static SimpleItemQuery<SimpleItem> filterByStackableType(SimpleItemQuery<SimpleItem> items, StackableType type) {
        if (type == null) {
            type = StackableType.BOTH;
        }
        switch (type) {
            case STACKABLE:
                return items.filter(SimpleItem::isStackable);
            case NON_STACKABLE:
                return items.filter(item -> !item.isStackable());
            case BOTH:
            default:
                return items;
        }
    }


    public static void handleGroundItem(String menuAction, int... itemId) {
        if (ctx.inventory.inventoryFull()) return;

        if (ctx.groundItems.populate().filter(itemId).filter((i) -> ctx.pathing.reachable(i.getLocation())).isEmpty()) {
            return;
        }

        SimpleGroundItem itemToPickup = ctx.groundItems.populate().filter(itemId).nearest().next();

        if (itemToPickup != null && itemToPickup.validateInteractable()) {
            updateStatus(getCurrentTimeFormatted() + " Picking up " + itemToPickup.getName().toLowerCase());
            eActions.interactWith(itemToPickup, menuAction);
            ctx.onCondition(() -> ctx.groundItems.populate().filter(itemId).isEmpty(), 250, 10);
        }
    }

    public static void handleGroundItem(String menuAction, String... itemName) {
        if (ctx.inventory.inventoryFull()) return;

        if (ctx.groundItems.populate().filter(itemName).filter((i) -> ctx.pathing.reachable(i.getLocation())).isEmpty()) {
            return;
        }

        SimpleGroundItem itemToPickup = ctx.groundItems.populate().filter(itemName).nearest().next();

        if (itemToPickup != null && itemToPickup.validateInteractable()) {
            updateStatus(getCurrentTimeFormatted() + " Picking up " + itemToPickup.getName().toLowerCase());
            eActions.interactWith(itemToPickup, menuAction);
            ctx.onCondition(() -> ctx.groundItems.populate().filter(itemName).isEmpty(), 250, 10);
        }
    }



    private static void changeCameraAngleOnThread(SimpleObject object) {
        // Create a ScheduledExecutorService with a single thread
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        // Turn to desired object
        //ctx.viewport.turnTo(object);

        // Get the current camera orientation angle
        int currentAngle = ctx.viewport.yaw();
        //logger.info("currentAngle : " + currentAngle);

        // Generating a random number between 0 and 30
        int angleChange = randomSleeping(0, 45);
        //logger.info("angleChange : " + angleChange);

        // Getting the sign of the angle change based on the current angle
        if (currentAngle >= 329 || currentAngle <= 30) {
            angleChange = -angleChange; // Make the angle change negative for 329-359 and 0-30 range
        }

        // Calculating the new camera angle by adding the angle change
        int newAngle = (currentAngle + angleChange) % 360;
        //logger.info("newAngle : " + newAngle);

        // Setting the new camera angle
        ctx.viewport.angle(newAngle);

        // Shutting down the executor
        executor.shutdown();
    }

    public static boolean isObjectReachableOffset(WorldPoint objectLocation) {
        int[] offsets = { 0, 1, -1, 2, -2}; // Adjust these offsets as needed
        for (int offsetX : offsets) {
            for (int offsetY : offsets) {
                WorldPoint offsetLocation = new WorldPoint(objectLocation.getX() + offsetX, objectLocation.getY() + offsetY, objectLocation.getPlane());
                if (ctx.pathing.reachable(offsetLocation)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void specialAttack(int... itemIds) {
        SimplePlayer localPlayer = ctx.players.getLocal();
        int specialAttackPercentage = ctx.combat.getSpecialAttackPercentage();

        if (specialAttackPercentage != 100) {
            return;
        }

        boolean hasSpecialAttackTool = !ctx.equipment.populate()
                .filter(itemIds)
                .isEmpty();

        if (!hasSpecialAttackTool) {
            ctx.log(eActions.getCurrentTimeFormatted() + " Special attack tool: NOT FOUND");
            ctx.log(eActions.getCurrentTimeFormatted() + " Special attack: Deactivated");
            specialAttackTool = false;
            return;
        }

        if (localPlayer.isAnimating() && ctx.combat.toggleSpecialAttack(true)) {
            updateStatus(eActions.getCurrentTimeFormatted() + " Used special attack");
            ctx.game.tab(Game.Tab.INVENTORY);
        }
    }

    public static void specialAttack(String... itemNames) {
        SimplePlayer localPlayer = ctx.players.getLocal();
        int specialAttackPercentage = ctx.combat.getSpecialAttackPercentage();

        if (specialAttackPercentage != 100) {
            return;
        }

        boolean hasSpecialAttackTool = !ctx.equipment.populate()
                .filter(itemNames)
                .isEmpty();

        if (!hasSpecialAttackTool) {
            ctx.log(eActions.getCurrentTimeFormatted() + " Special attack tool: NOT FOUND");
            ctx.log(eActions.getCurrentTimeFormatted() + " Special attack: Deactivated");
            specialAttackTool = false;
            return;
        }

        if (localPlayer.isAnimating() && ctx.combat.toggleSpecialAttack(true)) {
            updateStatus(eActions.getCurrentTimeFormatted() + " Used special attack");
            ctx.game.tab(Game.Tab.INVENTORY);
        }
    }

    public static int getDistanceToPlayer(SimpleLocatable entity) {
        return entity.distanceTo(ctx.players.getLocal());
    }

    public static boolean isWithinRangeToPlayer(SimpleLocatable entity, int range) {
        return entity.distanceTo(ctx.players.getLocal()) >= range;
    }

    public static void walkPath(WorldArea toArea, WorldPoint[] walkPath, boolean reverse) {
        if (!ctx.pathing.inArea(toArea)) {
            handleRunning();
            ctx.pathing.walkPath(walkPath, reverse);
            ctx.sleep(600);
        }
    }
    public static boolean inArea(WorldArea name) {
        return ctx.pathing.inArea(name);
    }

    public static void handleRunning() {
        if (ctx.pathing.energyLevel() > 30 && !ctx.pathing.running() && ctx.pathing.inMotion()) {
            ctx.pathing.running(true);
        }
    }

    public static void handleInventoryItem(String menuAction, int... itemIds) {
        SimpleItem itemInv = ctx.inventory.populate().filter(itemIds).reverse().next();
        if (Objects.equals(menuAction, "Drop")) {
            if (ctx.inventory.shiftDroppingEnabled()) {
                ctx.inventory.dropItem(itemInv);
            }
        } else if (Objects.equals(menuAction, "Bury")) {
            ctx.inventory.populate().filter(itemIds).forEach((item) -> item.click(0));
        } else {
            ctx.inventory.populate().filter(itemIds).forEach((item) -> item.click(menuAction));
        }
    }

    public static void handleInventoryItem(String menuAction, String... itemNames) {
        SimpleItem itemInv = ctx.inventory.populate().filter(itemNames).reverse().next();
        if (Objects.equals(menuAction, "Drop")) {
            if (ctx.inventory.shiftDroppingEnabled()) {
                ctx.inventory.dropItem(itemInv);
            }
        } else if (Objects.equals(menuAction, "Bury")) {
            ctx.inventory.populate().filter(itemNames).filter(item -> !item.isStackable()).forEach(item -> item.click(0));
        } else {
            ctx.inventory.populate().filter(itemNames).forEach((item) -> item.click(menuAction));
        }
    }

    public static int getNotedItemCount(int itemId) {
        return ctx.inventory.populate().filter(itemId).population(true);
    }

    public static int getNotedItemCount(String itemName) {
        return ctx.inventory.populate().filter(itemName).population(true);
    }

    public static int getItemCountInventory(String itemName) {
        return ctx.inventory.populate().filter(itemName).population();
    }

    public static int getItemCountInventory(int itemIds) {
        return ctx.inventory.populate().filter(itemIds).population();
    }

    public static void drawTileMatrix(ClientContext ctx, Graphics2D g, WorldPoint tile, Color colorName) {
        if (ctx != null && ctx.paint != null) {
            ctx.paint.drawTileMatrix(g, tile, colorName);
        }
    }

    public static void clickOnBagWidget() {
        SimpleWidget inventoryBagWidget = ctx.widgets.getWidget(548, 58);
        if (inventoryBagWidget != null) {
            inventoryBagWidget.click(0);
        }
    }

    public static void clickWidget(int wigetId, int childId) {
        SimpleWidget widgetToClick = ctx.widgets.getWidget(wigetId, childId);
        if (widgetToClick == null || widgetToClick.isHidden()) return;
        widgetToClick.click(0);
    }

    public static void clickWidget(SimpleWidget widgetName) {
        if (widgetName == null) return;
        widgetName.click(0);

    }

    public static boolean isWidgetOpen(int wigetId, int childId) {
        SimpleWidget widgetToClick = ctx.widgets.getWidget(wigetId, childId);
        return widgetToClick != null && !widgetToClick.isHidden();
    }

    public static void openTab(Game.Tab tab) {
        if (!isTabOpen(tab))
            ClientContext.instance().game.tab(tab);
    }

    public static boolean isTabOpen(Game.Tab tab) {
        return ClientContext.instance().game.tab().equals(tab);
    }

    public static void zoomOutViewport() {
        SimpleWidget inventoryBagWidget = ctx.widgets.getWidget(548, 58);
        SimpleWidget optionsWidget = ctx.widgets.getWidget(548, 35);
        SimpleWidget displayWidget = ctx.widgets.getWidget(261, 1).getChild(1);
        SimpleWidget displayFixedSize = ctx.widgets.getWidget(263, 33);
        SimpleWidget zoomOutSlider = ctx.widgets.getWidget(261, 9);

        if (optionsWidget == null) return;
        eActions.clickWidget(optionsWidget);

        if (displayWidget == null) return;
        eActions.clickWidget(zoomOutSlider);
        ctx.onCondition(() -> !zoomOutSlider.isHidden(), 50, 20);
        ctx.sleep(50);
        eActions.clickWidget(displayFixedSize);
        ctx.sleep(50);

        eActions.clickWidget(zoomOutSlider);
        eActions.clickWidget(zoomOutSlider);
        eActions.clickWidget(inventoryBagWidget);
    }

    public static String pluralize(int count, String singular, String plural) {
        return count == 1 ? singular : plural;
    }

    public static int getNpcHealth(SimpleNpc npcName) {

        if (npcName != null && npcName.getHealthRatio() != -1) {

            return npcName.getHealthRatio();

        }
        return -1;
    }

    public static WorldPoint getPlayerLocation() {
        return ctx.players.getLocal().getLocation();
    }

    public static String getPlayerName() {
        return ctx.players.getLocal().getName();
    }

    public static void setPrayers(SimplePrayers.Prayers prayerName, boolean set)  {
        if (ClientContext.instance().prayers.prayerActive(prayerName)) {
            ClientContext.instance().prayers.prayer(prayerName, set);
        }
    }

    public static void handlePortalTeleport(String menuElement, String locationName) {
        WorldPoint NEAR_PORTAL_TILE = new WorldPoint(3096, 3502, 0);
        if (!ctx.portalTeleports.portalOpen()) {
            SimpleObject zenytePortal = ctx.objects.populate().filter("Zenyte Portal").nearest().next();
            if (zenytePortal != null) {
                if (ctx.players.getLocal().getLocation().distanceTo(NEAR_PORTAL_TILE) > 5) {
                    BotUtils.eActions.status = "Running to portal";
                    ctx.pathing.step(NEAR_PORTAL_TILE);
                    ctx.onCondition(zenytePortal::visibleOnScreen, 250, 5);
                } else {
                    BotUtils.eActions.status = "Clicking portal";
                    BotUtils.eActions.interactWith(zenytePortal, "Teleport");
                    ctx.onCondition(() -> ctx.portalTeleports.portalOpen(), 250, 5);
                }
            }
        } else {
            BotUtils.eActions.status = "Choosing destination...";
            ctx.portalTeleports.sendTeleport(menuElement, locationName);
            ctx.game.tab(Game.Tab.INVENTORY);
            ctx.onCondition(() -> ctx.players.getLocal().getGraphic() != -1, 250, 5);
        }
    }

    public static void teleportHomeSpellbook() {
        if (ctx.pathing.inArea(EDGE_HOME_AREA)) return;

        BotUtils.eActions.status = "Teleporting to home";
        BotUtils.eActions.openTab(Game.Tab.MAGIC);

        int widgetNumber;
        switch (ctx.magic.spellBook()) {
            case MODERN:
                widgetNumber = 4;
                break;
            case LUNAR:
                widgetNumber = 99;
                break;
            case ANCIENT:
                widgetNumber = 98;
                break;
            case ARCEUUS:
                widgetNumber = 143;
                break;
            default:
                widgetNumber = -1;
        }

        if (widgetNumber != -1) {
            BotUtils.eActions.clickWidget(218, widgetNumber);
        }

        ctx.onCondition(() -> ctx.players.getLocal().getGraphic() != -1 || ctx.pathing.inArea(EDGE_HOME_AREA), 500, 20);
        ctx.game.tab(Game.Tab.INVENTORY);
    }

    public static WorldPoint getRandomLocationWithinArea(int minX, int maxX, int minY, int maxY) {
        int randomX = minX + (int)(Math.random() * ((maxX - minX) + 1));
        int randomY = minY + (int)(Math.random() * ((maxY - minY) + 1));

        return new WorldPoint(randomX, randomY, 0);
    }

    public static boolean inWilderness(int lvl) {
        SimpleWidget w = ctx.widgets.getWidget(90, 67);
        if(w != null && w.visibleOnScreen() && w.getText().toLowerCase().contains("level")) {
            int wildyLevel = Integer.parseInt(w.getText().toLowerCase().split("level: ")[1]);
            return wildyLevel >= lvl;
        }
        return false;
    }

    public static boolean targetIsVisible(SimpleNpc npc, WorldPoint myLocation, ClientContext ctx) {
        if (!npc.visibleOnScreen()) {
            Random rand = new Random();

            int maxAttempts = 100;
            int attempts = 0;

            while (!npc.visibleOnScreen()) {
                //WorldPoint myLocation = ctx.players.getLocal().getLocation(); // Refreshing current location
                WorldPoint npcLocation = npc.getLocation(); // Refreshing target location

                attempts++;
                if (attempts >= maxAttempts) {
                    break;
                }

                // Recalculate the direction based on the new positions
                int directionX = Integer.compare(npcLocation.getX(), myLocation.getX());
                int directionY = Integer.compare(npcLocation.getY(), myLocation.getY());

                // Recalculate the distance to the NPC to adjust movement magnitude
                int currentDistance = myLocation.distanceTo(npc.getLocation());

                if (currentDistance <= 3) {
                    return false;
                }

                // The closer the player is, the smaller the movement magnitude
                int maxMovement = Math.min(currentDistance, 7); // Upper limit is 7
                int minMovement = Math.max(1, currentDistance / 2); // Half the distance but at least 1

                if (maxMovement <= minMovement) {
                    maxMovement = minMovement + 1;  // Ensure that maxMovement is always greater than minMovement
                }

                int movementMagnitude = rand.nextInt(maxMovement - minMovement + 1) + minMovement;

                // Calculate offsets based on the updated direction and movement magnitude
                int xOffset = directionX * movementMagnitude;
                int yOffset = directionY * movementMagnitude;

                // Move towards the target based on the new calculations
                moveToLocation(myLocation, myLocation.getX() + xOffset, myLocation.getY() + yOffset, ctx);
                ctx.sleep(600);
            }
        }

        return  true;
    }

    public static boolean targetIsVisible(SimpleObject object, WorldPoint myLocation, ClientContext ctx) {
        if (!object.visibleOnScreen()) {
            Random rand = new Random();

            int maxAttempts = 100;
            int attempts = 0;

            while (!object.visibleOnScreen()) {
                //WorldPoint myLocation = ctx.players.getLocal().getLocation(); // Refreshing current location
                WorldPoint npcLocation = object.getLocation(); // Refreshing target location

                attempts++;
                if (attempts >= maxAttempts) {
                    break;
                }

                // Recalculate the direction based on the new positions
                int directionX = Integer.compare(npcLocation.getX(), myLocation.getX());
                int directionY = Integer.compare(npcLocation.getY(), myLocation.getY());

                // Recalculate the distance to the NPC to adjust movement magnitude
                int currentDistance = myLocation.distanceTo(object.getLocation());

                if (currentDistance <= 3) {
                    return false;
                }

                ctx.log("Distance to " + object.getName() + ": " + currentDistance);

                // The closer the player is, the smaller the movement magnitude
                int maxMovement = Math.min(currentDistance, 7); // Upper limit is 7
                int minMovement = Math.max(1, currentDistance / 2); // Half the distance but at least 1

                // Check if maxMovement is equal to minMovement, and adjust if necessary
                if (maxMovement <= minMovement) {
                    maxMovement = minMovement + 1;  // Ensure that maxMovement is always greater than minMovement
                }

                int movementMagnitude = rand.nextInt(maxMovement - minMovement + 1) + minMovement;

                // Calculate offsets based on the updated direction and movement magnitude
                int xOffset = directionX * movementMagnitude;
                int yOffset = directionY * movementMagnitude;

                // Move towards the target based on the new calculations
                moveToLocation(myLocation, myLocation.getX() + xOffset, myLocation.getY() + yOffset, ctx);
                ctx.sleep(600);
            }
        }

        return  true;
    }

    public static boolean targetIsVisible(SimpleGroundItem item, WorldPoint myLocation, ClientContext ctx) {
        if (!item.visibleOnScreen()) {
            Random rand = new Random();

            int maxAttempts = 100;
            int attempts = 0;

            while (!item.visibleOnScreen()) {
                //WorldPoint myLocation = ctx.players.getLocal().getLocation(); // Refreshing current location
                WorldPoint npcLocation = item.getLocation(); // Refreshing target location

                attempts++;
                if (attempts >= maxAttempts) {
                    break;
                }

                // Recalculate the direction based on the new positions
                int directionX = Integer.compare(npcLocation.getX(), myLocation.getX());
                int directionY = Integer.compare(npcLocation.getY(), myLocation.getY());

                // Recalculate the distance to the NPC to adjust movement magnitude
                int currentDistance = myLocation.distanceTo(item.getLocation());

                if (currentDistance <= 3) {
                    return false;
                }

                ctx.log("Distance to " + item.getName() + ": " + currentDistance);

                // The closer the player is, the smaller the movement magnitude
                int maxMovement = Math.min(currentDistance, 7); // Upper limit is 7
                int minMovement = Math.max(1, currentDistance / 2); // Half the distance but at least 1

                // Check if maxMovement is equal to minMovement, and adjust if necessary
                if (maxMovement <= minMovement) {
                    maxMovement = minMovement + 1;  // Ensure that maxMovement is always greater than minMovement
                }

                int movementMagnitude = rand.nextInt(maxMovement - minMovement + 1) + minMovement;

                // Calculate offsets based on the updated direction and movement magnitude
                int xOffset = directionX * movementMagnitude;
                int yOffset = directionY * movementMagnitude;

                // Move towards the target based on the new calculations
                moveToLocation(myLocation, myLocation.getX() + xOffset, myLocation.getY() + yOffset, ctx);
                ctx.sleep(600);
            }
        }

        return  true;
    }

    public static void moveToLocation(WorldPoint playerLocation, int x, int y, ClientContext ctx) {
        WorldPoint targetSpot = new WorldPoint(x, y, playerLocation.getPlane());
        ctx.pathing.step(targetSpot);
    }

}
