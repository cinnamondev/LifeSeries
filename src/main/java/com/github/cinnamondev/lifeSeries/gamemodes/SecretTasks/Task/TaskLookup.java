package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.PlayerTask;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.lang.module.Configuration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TaskLookup {
    private static final Map<String, Class<? extends AbstractPlayerTask.Builder<?>>> tasks = Map.ofEntries(
            Map.entry("explode-another-player", ExplosiveTrapTask.Builder.class),
            Map.entry("follow-another-player", StayTogetherTask.Builder.class),
            Map.entry("meow-at-others", MeowerTask.Builder.class)
    );;

    public static AbstractPlayerTask.Builder<?> getTaskBuilderByKey(String key) {
        try {
            return tasks.get(key.toLowerCase()).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e); // we shouldn't expect this to ever fail, the builder should not have
                                           // any parameters.
        }
    }

    public static Map<String, AbstractPlayerTask.Builder<?>> getAllTaskBuilders() {
        return tasks.entrySet().stream().map((entry) -> {
            try {
                return Map.entry(entry.getKey(), entry.getValue().getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Optional<ConfigurationSection> getTaskConfigurationSection(Plugin p, String taskKey) {
        return Optional.ofNullable(
                p.getConfig().getConfigurationSection("options.secret-life.task-configs." + taskKey)
        );
    }

    public static Optional<ConfigurationSection> getTaskConfigurationSection(Plugin p, PlayerTask playerTask) {
        return getTaskConfigurationSection(p, playerTask.getTaskKey());
    }
    public static Optional<Integer> getTaskAssignmentLimit(Plugin p, String taskKey) {
        var oConfig = getTaskConfigurationSection(p, taskKey);
        if (oConfig.isEmpty()) { return Optional.empty(); }

        var oLimit = oConfig.get().getInt("assignment-limit", -1);
        if (oLimit == -1) { return Optional.empty(); }
        return Optional.of(oLimit);

    }
    public static Optional<Integer> getTaskAssignmentLimit(Plugin p, PlayerTask playerTask) {
        return getTaskAssignmentLimit(p, playerTask.getTaskKey());
    }
}

