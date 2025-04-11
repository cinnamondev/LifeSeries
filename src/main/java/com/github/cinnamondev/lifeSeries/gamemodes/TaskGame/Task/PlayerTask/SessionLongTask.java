package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

public interface SessionLongTask extends PlayerTask{
    @Override
    default void endOfSession() {
        if (!isTaskFinished()) { complete(); }
    }
}
