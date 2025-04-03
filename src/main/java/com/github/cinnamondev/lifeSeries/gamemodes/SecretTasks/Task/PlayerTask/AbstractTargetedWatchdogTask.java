package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
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
