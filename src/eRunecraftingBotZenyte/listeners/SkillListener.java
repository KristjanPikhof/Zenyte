package eRunecraftingBotZenyte.listeners;

import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.task.Task;

import java.util.List;

public interface SkillListener {
    boolean prioritizeTasks();

    List<Task> tasks();

    void skillLevelAdded(SimpleSkills.Skills skill, int current, int previous, int gained);
    void skillExperienceAdded(SimpleSkills.Skills skill, int current, int previous, int gained);
}
