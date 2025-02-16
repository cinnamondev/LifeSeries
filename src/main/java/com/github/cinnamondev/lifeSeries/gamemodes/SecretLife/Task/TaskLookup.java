package com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Task;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TaskLookup {
    private static final Map<String, Class<? extends AbstractPlayerTask.Builder<?>>> tasks = Map.ofEntries(
            Map.entry("explode-another-player", ExplosiveTrapTask.Builder.class),
            Map.entry("follow-another-player", StayTogetherTask.Builder.class)
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

}
