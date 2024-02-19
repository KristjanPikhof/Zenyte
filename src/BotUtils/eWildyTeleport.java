package BotUtils;

import net.runelite.api.coords.WorldPoint;
import simple.hooks.scripts.task.Task;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimplePlayer;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;

public class eWildyTeleport extends Task {

    private static ClientContext ctx;
    private final WorldPoint GNOME_TILE = new WorldPoint(2465, 3495, 0);


    public eWildyTeleport(ClientContext ctx) {
        super(ctx);
        eWildyTeleport.ctx = ctx;
    }


    @Override
    public boolean condition() {
        return eActions.inWilderness(1) && gettingAttackedByPlayer();
    }

    @Override
    public void run() {
        if (eActions.inWilderness(31)) {
            runningSouth();
        } else {
            gettingAttackedGTFO();
        }
    }

    public void gettingAttackedGTFO() {
        SimpleItem pod = ctx.inventory.populate().filter("Royal seed pod").next();

        if (!ctx.pathing.onTile(GNOME_TILE)) {
            if (pod != null) {
                pod.click(0);
                pod.click(0);
                pod.click(0);
                pod.click(0);
                pod.click(0);
                pod.click(0);
            } else {
                alternativeGTFOMethod();
            }
        } else {
            ctx.sendLogout();
            ctx.stopScript();
        }
    }

    public boolean gettingAttackedByPlayer() {
        String localPlayerName = ctx.players.getLocal().getName();

        for (SimplePlayer p : ctx.players.populate().filter(p -> !p.getName().equals(localPlayerName))) {
            if (p.getInteracting() != null &&
                    p.getInteracting().getName() != null &&
                    p.getInteracting().getName().equals(localPlayerName)) {
                return true;
            }
        }
        return false;
    }

    private void runningSouth() {
        WorldPoint startingSpot = ctx.players.getLocal().getPlayer().getWorldLocation();
        WorldPoint southSpot = new WorldPoint(startingSpot.getX(), startingSpot.getY() - 5, startingSpot.getPlane());
        ctx.pathing.step(southSpot);
    }

    private void alternativeGTFOMethod() {
        if (eActions.inWilderness(21)) {
            runningSouth();
        } else {
            usingTeleportTabToGTFO();
        }
    }

    public void usingTeleportTabToGTFO() {
        SimpleItem tab = ctx.inventory.populate().filter(22721).next();

        if (!ctx.pathing.onTile(GNOME_TILE)) {
            if (tab != null) {
                tab.click(0);
                tab.click(0);
                tab.click(0);
                tab.click(0);
                tab.click(0);
                tab.click(0);
            }
        } else {
            ctx.sendLogout();
            ctx.stopScript();
        }
    }

    @Override
    public String status() {
        return "Getting Attacked! " + "Teleporting!";
    }
}
