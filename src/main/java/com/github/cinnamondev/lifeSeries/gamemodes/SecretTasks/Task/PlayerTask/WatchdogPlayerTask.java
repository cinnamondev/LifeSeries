package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;

public interface WatchdogPlayerTask extends PlayerTask {
    int getWatchdogThreshold();
    void feed();
    default void bark() {
        fail();
    }
}
