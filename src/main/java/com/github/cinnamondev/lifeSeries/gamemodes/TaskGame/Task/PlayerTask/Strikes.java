package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

public interface Strikes extends PlayerTask {
    int fallbackStrikes();
    int decrementAndGetStrikes();
    default boolean strike() {
        if (decrementAndGetStrikes() == 0 && !isTaskFinished()) { fail(); return true; }
        return false;
    }
}
