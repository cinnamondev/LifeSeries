package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

public interface GenericThreshold extends PlayerTask {
    int fallbackThreshold(); // should return constant
    default int getThreshold() {
        return getConfigurationSection()
                .map(c -> c.getInt("threshold", -1))
                .filter(n -> n != -1).orElse(fallbackThreshold());
    }
}
