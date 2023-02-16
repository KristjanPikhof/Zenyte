package eSacredEelBotZenyte.listeners;

import net.runelite.api.GameState;
import simple.hooks.filters.SimpleSkills;
import simple.robot.api.ClientContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class SkillObserver extends Thread {

    private ArrayList<eSacredEelBotZenyte.listeners.SkillListener> listeners;
    private BooleanSupplier condition;
    private ClientContext ctx;

    public SkillObserver(ClientContext ctx, BooleanSupplier condition) {
        this.ctx = ctx;
        this.listeners = new ArrayList<>();
        this.condition = condition;
    }

    @Override
    public void run() {
        while (ctx.getClient().getGameState() != GameState.LOGGED_IN) {
            ctx.sleep(500);
        }

        HashMap<SimpleSkills.Skills, Integer> experienceMap = skillExperienceHashMap();
        HashMap<SimpleSkills.Skills, Integer> levelMap = skillLevelHashMap();
        while (true) {
            ctx.sleep(100);
            if (ctx.getClient().getGameState() != GameState.LOGGED_IN) continue;
            if (!condition.getAsBoolean()) {
                experienceMap = skillExperienceHashMap();
                levelMap = skillLevelHashMap();
                continue;
            }

            HashMap<SimpleSkills.Skills, Integer> updatedExperienceMap = skillExperienceHashMap();
            HashMap<SimpleSkills.Skills, Integer> updatedLevelMap = skillLevelHashMap();

            for (Map.Entry<SimpleSkills.Skills, Integer> entry : updatedExperienceMap.entrySet()) {
                if (experienceMap.containsKey(entry.getKey())) {
                    int cached = experienceMap.get(entry.getKey());
                    if (entry.getValue() != cached) {
                        experienceTrigger(entry.getKey(), entry.getValue(), cached, (entry.getValue() - cached));
                    }
                }
            }

            for (Map.Entry<SimpleSkills.Skills, Integer> entry : updatedLevelMap.entrySet()) {
                if (levelMap.containsKey(entry.getKey())) {
                    int cached = levelMap.get(entry.getKey());
                    if (entry.getValue() != cached) {
                        levelTrigger(entry.getKey(), entry.getValue(), cached, (entry.getValue() - cached));
                    }
                }
            }

            experienceMap = skillExperienceHashMap();
            levelMap = skillLevelHashMap();
        }
    }

    private HashMap<SimpleSkills.Skills, Integer> skillExperienceHashMap() {
        HashMap<SimpleSkills.Skills, Integer> map = new HashMap<>();
        for (SimpleSkills.Skills skill : SimpleSkills.Skills.values()) {
            map.put(skill, ctx.skills.experience(skill));
        }
        return map;
    }

    private HashMap<SimpleSkills.Skills, Integer> skillLevelHashMap() {
        HashMap<SimpleSkills.Skills, Integer> map = new HashMap<>();
        for (SimpleSkills.Skills skill : SimpleSkills.Skills.values()) {
            map.put(skill, ctx.skills.level(skill));
        }
        return map;
    }


    public void addListener(eSacredEelBotZenyte.listeners.SkillListener skillListener) {
        listeners.add(skillListener);
    }

    public void experienceTrigger(SimpleSkills.Skills skill, int current, int previous, int gained) {
        for (eSacredEelBotZenyte.listeners.SkillListener l : listeners) l.skillExperienceAdded(skill, current, previous, gained);
    }

    public void levelTrigger(SimpleSkills.Skills skill, int current, int previous, int gained) {
        for (SkillListener l : listeners) l.skillLevelAdded(skill, current, previous, gained);
    }
}
