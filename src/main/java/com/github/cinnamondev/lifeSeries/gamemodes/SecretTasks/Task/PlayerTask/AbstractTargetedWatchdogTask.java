package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public abstract class AbstractTargetedWatchdogTask extends AbstractTargetedPlayerTask implements WatchdogPlayerTask {

    public AbstractTargetedWatchdogTask(LifeSeries p, Player owningPlayer, int interval, OfflinePlayer targetedPlayer, SecretTasks.TaskDifficulty difficulty, Consumer<PlayerTask> onTaskCompletion) {
        super(p, owningPlayer, targetedPlayer, difficulty, onTaskCompletion);
        this.watchdogInterval = interval;
        p.getServer().getScheduler().scheduleSyncRepeatingTask(p, this::watchdog, watchdogInterval, watchdogInterval);
    }

    public AbstractTargetedWatchdogTask(LifeSeries p, Builder builder) {
        this(p, builder.owningPlayer, builder.watchdogInterval, builder.targetPlayer, builder.assignedDifficulty, builder.onTaskCompletion);
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

    public abstract static class Builder<T extends Builder<T>> extends AbstractTargetedPlayerTask.Builder<T> {
        protected int watchdogInterval;
        public T updateInterval(int interval) { this.watchdogInterval = interval; return (T) this;}

        @Override
        public AbstractPlayerTask buildWithAnySettings(LifeSeries p, SecretTasks game) {
            return this.updateInterval(p.getConfig().getInt("options.secret-life.watchdog-time", 3600))
                    .randomTarget(p)
                    .build(p);

        }
    }

}
