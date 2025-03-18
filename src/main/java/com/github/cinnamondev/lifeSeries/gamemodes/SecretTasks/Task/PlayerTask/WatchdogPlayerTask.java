package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

public interface WatchdogPlayerTask extends PlayerTask {
    void feed();
    default void bark() {
        fail();
    }
}
