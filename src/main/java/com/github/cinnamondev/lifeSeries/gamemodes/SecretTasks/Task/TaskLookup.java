package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import org.bukkit.entity.Player;

public class TaskLookup {
    public static PlayerTask setupTaskByString(LifeSeries p, Player owningPlayer, String taskName) {
        return switch (taskName.toLowerCase()) {
            case "explode-another-player" -> new ExplosiveTrapTask(p, owningPlayer);
            case "follow-another-player" -> new StayTogetherTask(p, owningPlayer,)
            default -> throw new IllegalArgumentException("Task name doesn't exist" + taskName.toLowerCase());
        }
    }
}
