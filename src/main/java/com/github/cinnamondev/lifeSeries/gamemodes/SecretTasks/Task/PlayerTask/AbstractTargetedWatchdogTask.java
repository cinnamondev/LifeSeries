package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public abstract class AbstractTargetedWatchdogTask extends AbstractWatchdogTask implements TargetedPlayerTask {
    public AbstractTargetedWatchdogTask(LifeSeries p, Player owningPlayer, int watchdogInterval, int watchdogThreshold, OfflinePlayer targetedPlayer, SecretTasks.TaskDifficulty difficulty, Consumer<PlayerTask> onTaskCompletion) {
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

    public abstract static class Builder<T extends AbstractTargetedWatchdogTask.Builder<T>> extends AbstractTargetedPlayerTask.Builder<T> {
        protected int watchdogInterval = 200; // default, 10 seconds
        protected int watchdogThreshold;
        public T updateInterval(int interval) { this.watchdogInterval = interval; return (T) this;}
        public T threshold(int ticks) { this.watchdogThreshold = ticks; return (T) this;}

        @Override
        public AbstractPlayerTask buildWithAnySettings(LifeSeries p, SecretTasks game) {
            return threshold(p.getConfig().getInt("options.secret-life.watchdog-time", 3600))
                    .randomTarget(p)
                    .build(p);
        }
    }
}
