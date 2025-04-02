package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public abstract class AbstractTargetedWatchdogTask extends AbstractWatchdogTask implements TargetedPlayerTask {
    public AbstractTargetedWatchdogTask(LifeSeries p, Player owningPlayer, int watchdogInterval, int watchdogThreshold, OfflinePlayer targetedPlayer, TaskDifficulty difficulty, Consumer<PlayerTask> onTaskCompletion) {
        super(p, owningPlayer, watchdogInterval, watchdogThreshold, onTaskCompletion, difficulty);
        this.targetedPlayer = targetedPlayer;
    }
    protected OfflinePlayer targetedPlayer;

    // the tragedy of no multiple inheritance... curse you java
    @Override
    public OfflinePlayer getTargetedPlayer() {
        return this.targetedPlayer;
    }

    @Override
    public ConfigurationSection saveTask(ConfigurationSection taskSection) {
        var task = super.saveTask(taskSection);
        taskSection.set("target", this.targetedPlayer.getUniqueId());
        return task;
    }

    // a helper more than anything, watchdog stuff should just come from config because im lazy and this is stupid.
    public abstract static class Builder<T extends AbstractPlayerTask.Builder<T>> extends AbstractTargetedPlayerTask.Builder<T> {
        protected int getWatchdogThreshold(LifeSeries p) {
            return p.getConfig().getInt("options.secret-life.watchdog-time", 3600);
        }
    }
}
