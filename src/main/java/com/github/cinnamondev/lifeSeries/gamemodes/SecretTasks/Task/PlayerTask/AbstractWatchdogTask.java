package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public abstract class AbstractWatchdogTask extends AbstractPlayerTask implements WatchdogPlayerTask {
    public AbstractWatchdogTask(LifeSeries p, Player owningPlayer, int watchdogInterval, int watchdogThreshold, Consumer<PlayerTask> onTaskCompletion, SecretTasks.TaskDifficulty difficulty) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
        this.threshold = watchdogThreshold;
        this.interval = watchdogInterval;
        this.taskId = p.getServer().getScheduler().scheduleSyncRepeatingTask(p, this::watchdog, watchdogInterval, watchdogInterval);
    }
    private final int threshold;
    protected final int interval;
    private final int taskId;
    int intervalsSinceLastFeed = 0;

    private void watchdog() {
        if (intervalsSinceLastFeed >= threshold && !getTaskProgress().equals(TaskStatus.COMPLETE)) { // watch dog is not fed
            bark();
            p.getServer().getScheduler().cancelTask(taskId);
        } else {
            this.intervalsSinceLastFeed += interval;
        }
    }

    @Override
    public final void complete() {
        if (p.getServer().getScheduler().isQueued(taskId) || p.getServer().getScheduler().isCurrentlyRunning(taskId)) {
            p.getServer().getScheduler().cancelTask(taskId);
        }
        super.complete();
    }

    @Override
    public final void fail() {
        if (p.getServer().getScheduler().isQueued(taskId) || p.getServer().getScheduler().isCurrentlyRunning(taskId)) {
            p.getServer().getScheduler().cancelTask(taskId);
        }
        super.fail();
    }
    @Override
    public final void feed() {
        this.intervalsSinceLastFeed = 0;
    }

    @Override
    public void bark() {
        fail();
    }

    public abstract static class Builder<T extends AbstractPlayerTask.Builder<T>> extends AbstractPlayerTask.Builder<T> {
        protected int getWatchdogThreshold(LifeSeries p) {
            return p.getConfig().getInt("options.secret-life.watchdog-time", 3600);
        }
    }
}