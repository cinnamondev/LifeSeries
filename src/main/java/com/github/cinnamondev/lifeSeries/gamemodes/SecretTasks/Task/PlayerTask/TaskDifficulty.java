package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public enum TaskDifficulty {
    EASY,
    MEDIUM,
    HARD;

    public Component asComponent() {
        return switch (this) {
            case EASY -> Component.translatable("task-difficulty.easy").color(NamedTextColor.GREEN);
            case MEDIUM -> Component.translatable("task-difficulty.medium").color(NamedTextColor.YELLOW);
            case HARD -> Component.translatable("task-difficulty.hard").color(NamedTextColor.RED);
        };
    }
}