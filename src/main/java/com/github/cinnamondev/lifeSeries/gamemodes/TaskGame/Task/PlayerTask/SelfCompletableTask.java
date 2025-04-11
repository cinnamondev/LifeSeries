package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

import net.kyori.adventure.text.Component;

public interface SelfCompletableTask extends PlayerTask {
    boolean conditionalCompleteTask();
    default boolean requireVerification() { return false; }
    default Component messageConditionsNotMet() {
        return Component.translatable("secret-life.conditional-task.not-complete");
    }

}
