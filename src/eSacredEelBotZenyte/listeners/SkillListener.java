package eSacredEelBotZenyte.listeners;

import simple.hooks.filters.SimpleSkills;

public interface SkillListener {
    void skillLevelAdded(SimpleSkills.Skills skill, int current, int previous, int gained);
    void skillExperienceAdded(SimpleSkills.Skills skill, int current, int previous, int gained);
}
