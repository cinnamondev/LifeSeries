package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public abstract class AbstractTargetedWatchdogTask extends AbstractWatchdogTask implements TargetedPlayerTask {
    public AbstractTargetedWatchdogTask(LifeSeries p, Player owningPlayer, int watchdogInterval, OfflinePlayer targetedPlayer, TaskDifficulty difficulty, Consumer<PlayerTask> onTaskCompletion) {
        super(p, owningPlayer, watchdogInterval, onTaskCompletion, difficulty);
        this.targetedPlayer = targetedPlayer;
    }
    protected OfflinePlayer targetedPlayer;

    @Override
    public OfflinePlayer getTargetedPlayer() {
        return this.targetedPlayer;
    }
}
