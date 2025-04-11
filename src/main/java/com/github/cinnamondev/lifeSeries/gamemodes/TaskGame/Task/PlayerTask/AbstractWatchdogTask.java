package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.util.UtilityComponents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class AbstractWatchdogTask extends AbstractPlayerTask implements WatchdogPlayerTask {
    public AbstractWatchdogTask(LifeSeries p, Player owningPlayer, int watchdogInterval, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
        this.interval = watchdogInterval;
        this.taskId = p.getServer().getScheduler().scheduleSyncRepeatingTask(p, this::watchdog, watchdogInterval, watchdogInterval);
    }
    protected final int interval;
    private final int taskId;
    int intervalsSinceLastFeed = 0;

    @Override
    public int getWatchdogThreshold() {
        return p.getConfig().getInt("options.secret-life.watchdog-time", 3600);
    }

    private void watchdog() {
        if (intervalsSinceLastFeed >= getWatchdogThreshold() && !isTaskFinished()) { // watch dog is not fed, check istaskfinished just in case.
            bark();
            p.getServer().getScheduler().cancelTask(taskId);
        } else {
            this.intervalsSinceLastFeed += interval;
        }
    }

    private void cancelWatchdog() {
        if (p.getServer().getScheduler().isQueued(taskId) || p.getServer().getScheduler().isCurrentlyRunning(taskId)) {
            p.getServer().getScheduler().cancelTask(taskId);
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
    public Component taskProgressExplanation() {
        return Component.text("Time since condition was satisfied: ")
                .append(UtilityComponents.playerTime(intervalsSinceLastFeed, TimeUnit.SECONDS, NamedTextColor.WHITE));
    }

    @Override
    public void bark() {
        fail();
    }
}