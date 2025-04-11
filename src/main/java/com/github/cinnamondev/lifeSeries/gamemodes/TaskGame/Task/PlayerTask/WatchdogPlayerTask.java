package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

public interface WatchdogPlayerTask extends PlayerTask {
    int getWatchdogThreshold();
    void feed();
    default void bark() {
        fail();
    }
}
