package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

public interface SessionLongTask extends PlayerTask{
    @Override
    default void endOfSession() {
        if (!isTaskFinished()) { complete(); }
    }
}
