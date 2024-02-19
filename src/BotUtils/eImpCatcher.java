package BotUtils;

import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.task.Task;
import simple.hooks.wrappers.SimpleNpc;
import simple.robot.api.ClientContext;

public class eImpCatcher extends Task {

    public eImpCatcher(ClientContext ctx) {
        super(ctx);
    }

    public enum Impling {
        NINJA("Ninja impling", 84),
        CRYSTAL("Crystal impling", 90),
        DRAGON("Dragon impling", 93),
        LUCKY("Lucky impling", 99);

        private final String name;
        private final int level;

        Impling(String name, int level) {
            this.name = name;
            this.level = level;
        }

        public String getName() {
            return name;
        }

        public int getHunterLevel() {
            return level;
        }
    }

    @Override
    public boolean condition() {
        return !ctx.inventory.inventoryFull() && validImplingExists(ctx.skills.realLevel(SimpleSkills.Skills.HUNTER));
    }

    private boolean validImplingExists(int hunterLevel) {
        for (Impling impling : Impling.values()) {
            if (hunterLevel >= impling.getHunterLevel() && npcIsValid(impling.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {
        int hunterLevel = ctx.skills.realLevel(SimpleSkills.Skills.HUNTER);

        // Find the highest-level impling that can be catched
        Impling targetImpling = null;
        for (Impling impling : Impling.values()) {
            if (hunterLevel >= impling.getHunterLevel()) {
                targetImpling = impling;
            }
        }

        if (targetImpling != null) {
            SimpleNpc npc = ctx.npcs.populate().filter(targetImpling.getName()).filterHasAction("Catch").next();
            if (npc != null && npc.validateInteractable() && ctx.pathing.reachable(npc.getLocation())) {
                eActions.status = "Catching " + npc.getName();
                eActions.interactWith(npc, "Catch");
                ctx.onCondition(() -> npc == null, 250, 12);
                ctx.log(eActions.getCurrentTimeFormatted() + " I got " + npc.getName());
            }
        }
    }

    public boolean npcIsValid(String name) {
        SimpleNpc npc = ctx.npcs.populate().filter(name).filterHasAction("Catch").next();
        return npc != null && ctx.pathing.reachable(npc.getLocation());
    }

    @Override
    public String status() {
        return "Catching Impling";
    }

}

