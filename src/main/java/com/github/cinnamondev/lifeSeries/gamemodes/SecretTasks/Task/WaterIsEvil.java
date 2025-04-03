package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.Boogeyman.AbstractBoogeyman;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.TaskDifficulty;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.function.Consumer;

public class WaterIsEvil extends AbstractPlayerTask implements Listener {

    public WaterIsEvil(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
    }

    @EventHandler
    public void onPlayerEntersWater(PlayerMoveEvent e) {
        if (e.getPlayer().isInWater()) { fail(); }
    }
    @Override
    public boolean isTaskGuessable() {
        return true;
    }

    @Override
    public String getTaskKey() {
        return "water-is-evil";
    }

    @Override
    public Builder<? extends Builder<?>> builderProvider() {
        return null;
    }
}
