package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

public interface SelfCompletableTask extends PlayerTask {
    boolean conditionalCompleteTask();
    default boolean requireVerification() { return false; }
}
