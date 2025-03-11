package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public abstract class AbstractWatchdogTask extends AbstractPlayerTask implements WatchdogPlayerTask {
    public AbstractWatchdogTask(LifeSeries p, Player owningPlayer, int watchdogInterval, Consumer<PlayerTask> onTaskCompletion, SecretTasks.TaskDifficulty difficulty) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
        this.watchdogInterval = watchdogInterval;
        p.getServer().getScheduler().scheduleSyncRepeatingTask(p, this::watchdog, watchdogInterval, watchdogInterval);
    }
    protected final int watchdogInterval;
    private boolean fed = true;

    private void watchdog() {
        if (!fed && !getTaskProgress().equals(TaskStatus.COMPLETE)) { // watch dog is not fed
            bark();
        } else {
            fed = false;
        }
    }

    @Override
    public void feed() {
        fed = true;
    }

    @Override
    public boolean isFed() {
        return fed;
    }

    @Override
    public void bark() {
        fail();
    }

    public abstract static class Builder<T extends Builder<T>> extends AbstractPlayerTask.Builder<T> {
        protected int watchdogInterval;
        public T updateInterval(int interval) { this.watchdogInterval = interval; return (T) this;}

        @Override
        public AbstractPlayerTask buildWithAnySettings(LifeSeries p, SecretTasks game) {
            return this.updateInterval(p.getConfig().getInt("options.secret-life.watchdog-time", 3600))
                    .build(p);
        }
    }
}