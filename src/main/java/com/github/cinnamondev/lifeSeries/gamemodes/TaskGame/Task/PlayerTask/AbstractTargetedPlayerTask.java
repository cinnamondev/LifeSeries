package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public abstract class AbstractTargetedPlayerTask extends AbstractPlayerTask implements TargetedPlayerTask {
    protected OfflinePlayer targetedPlayer;
    public AbstractTargetedPlayerTask(LifeSeries p, Player owningPlayer, OfflinePlayer targetedPlayer, TaskDifficulty difficulty, Consumer<PlayerTask> onTaskCompletion) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
        this.targetedPlayer = targetedPlayer;
    }
    @Override
    public OfflinePlayer getTargetedPlayer() {
        return this.targetedPlayer;
    }

    @Override
    public ConfigurationSection saveTask() {
        var playerSection = p.getSave().getConfigurationSection("players");
        if (playerSection == null) { playerSection = p.getSave().createSection("players"); }

        var taskSection = playerSection.createSection(getTaskKey());
        taskSection.set("target", getTargetedPlayer().getUniqueId());
        return taskSection; // hm
    }
}

