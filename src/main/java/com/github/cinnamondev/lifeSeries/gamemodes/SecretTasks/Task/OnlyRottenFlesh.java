package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.function.Consumer;

public class OnlyRottenFlesh extends AbstractPlayerTask implements Listener, SessionLongTask, GenericThreshold {
    public OnlyRottenFlesh(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
    }

    @EventHandler
    public void playerEating(FoodLevelChangeEvent e) {
        if (!e.getEntity().equals(owningPlayer)) { return; }
        if (e.getItem() != null && !e.getItem().getType().equals(Material.ROTTEN_FLESH)) { fail(); }
    }
    @Override
    public boolean isTaskGuessable() { return true; }

    @Override
    public String getTaskKey() { return "flesh-eater"; }

    @Override
    public OnlyRottenFlesh.Builder builderProvider() {
        return new OnlyRottenFlesh.Builder();
    }

    @Override
    public int fallbackThreshold() { return 5; }

    public static class Builder extends PlayerTask.Builder<Builder> {
        @Override
        public AbstractPlayerTask build(LifeSeries p) {
            return new OnlyRottenFlesh(p, owningPlayer, onTaskCompletion, assignedDifficulty);
        }
    }
}
