package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.TaskDifficulty;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Locale;
import java.util.function.Consumer;

public class WitherWardenWar extends AbstractPlayerTask implements Listener {
    private final boolean oneHasKilledOther = false;
    private final boolean otherIsKilled = false;
    public WitherWardenWar(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
    }

    private boolean isEntityWardenOrWither(Entity e) {
        return e.getType() == EntityType.WITHER || e.getType() == EntityType.WARDEN;
    }
    @EventHandler
    public void confirmEntityAttacked(EntityDamageByEntityEvent e) {
        if (isEntityWardenOrWither(e.getEntity()) && isEntityWardenOrWither(e.getDamager())) {
            complete();
        }
    }

    @Override
    public boolean isTaskGuessable() {
        return false;
    }

    @Override
    public String getTaskKey() {
        return "warden-wither-war";
    }

    @Override
    public Builder builderProvider() {
        return new Builder();
    }

    public static class Builder extends PlayerTask.Builder<Builder> {
        @Override
        public PlayerTask build(LifeSeries p) {
            return new WitherWardenWar(p, owningPlayer, onTaskCompletion, assignedDifficulty);
        }
    }
}
