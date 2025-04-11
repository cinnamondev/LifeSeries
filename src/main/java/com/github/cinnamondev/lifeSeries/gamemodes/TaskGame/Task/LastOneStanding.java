package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.TaskDifficulty;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

/// SPECIAL TASK. DO NOT INCLUDE IN ROLLS. THIS IS SPECIFICALLY FOR GAMEMASTER FOR FINAL SESSION
/// goal: eliminate anyone who remains. obviously the outcome of this task is meaningless, but it serves as a
/// parallel to the life series.
public class LastOneStanding extends AbstractPlayerTask {
    private final int taskID;
    public LastOneStanding(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
        this.taskID = p.getServer().getScheduler().scheduleSyncRepeatingTask(p, this::pollForSuccess, 100, 100);
    }

    @Override
    public void cleanup() {
        p.getServer().getScheduler().cancelTask(taskID);
    }

    private void pollForSuccess() {
        int remainingPlayers = p.getScoreHandler() // count remaining players except current
                .getAllAliveOnlinePlayers().stream()
                .filter(player -> !player.equals(owningPlayer))
                .toList().size();
        complete();
    }

    @Override
    public boolean isTaskGuessable() {
        return false;
    }

    @Override
    public String getTaskKey() {
        return "last-one-standing";
    }

    @Override
    public Builder builderProvider() {
        return new Builder();
    }

    public static class Builder extends PlayerTask.Builder<Builder> {
        @Override
        public AbstractPlayerTask build(LifeSeries p) {
            return new LastOneStanding(p, owningPlayer, onTaskCompletion, assignedDifficulty);
        }
    }
}
