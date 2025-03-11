package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

public interface WatchdogPlayerTask extends PlayerTask {
    boolean isFed();
    void feed();
    default void bark() {
        fail();
    }
}
