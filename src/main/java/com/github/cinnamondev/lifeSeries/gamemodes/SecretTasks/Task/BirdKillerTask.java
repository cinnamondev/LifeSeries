package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class BirdKillerTask extends KillMobsTask {
    protected int hits = 0;
    public BirdKillerTask(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
    }

    @Override
    protected Collection<EntityType> candidateEntitys() {
        return List.of(
                EntityType.BAT,
                EntityType.PHANTOM,
                EntityType.CHICKEN,
                EntityType.PARROT
        );
    }

    @Override
    public String getTaskKey() {
        return "bird-killer";
    }

    @Override
    public Builder<? extends Builder<?>> builderProvider() {
        return null;
    }
}
