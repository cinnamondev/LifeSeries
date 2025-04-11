package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

import org.bukkit.entity.Player;

import java.util.List;

public class AbstractGroupTaskGlue<T extends GroupTask> {
    private final List<T> actualTasks;
    private boolean ignoreAdditionalCalls = false;
    public AbstractGroupTaskGlue(List<T> actualTasks) {
        this.actualTasks = actualTasks;
    }

    public void complete(Player callingPlayer) {
        if (ignoreAdditionalCalls) { return; } // ignoreAdditionalCalls will prevent complete/fail walking over itself
        ignoreAdditionalCalls = true;
        for (T actualTask : actualTasks) {
            if (actualTask.getTaskOwner().equals(callingPlayer)) { continue; }
            actualTask.complete();
        }
        ignoreAdditionalCalls = false;
    }

    public void fail(Player callingPlayer) {
        if (ignoreAdditionalCalls) { return;}
        ignoreAdditionalCalls = true;
        for (T actualTask : actualTasks) {
            if (actualTask.getTaskOwner().equals(callingPlayer)) { continue; }
            actualTask.fail();
        }
        ignoreAdditionalCalls = false;
    }
}
