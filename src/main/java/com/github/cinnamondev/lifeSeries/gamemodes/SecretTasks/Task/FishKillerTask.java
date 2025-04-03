package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.KillMobsTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.TaskDifficulty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class FishKillerTask extends KillMobsTask {
    public FishKillerTask(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
    }

    @Override
    protected Collection<EntityType> candidateEntitys() {
        return List.of(
                EntityType.COD,
                EntityType.TROPICAL_FISH,
                EntityType.PUFFERFISH,
                EntityType.SALMON,
                EntityType.GUARDIAN,
                EntityType.ELDER_GUARDIAN,
                EntityType.AXOLOTL
        );
    }

    @EventHandler
    public void axolotlMurderer(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        if (Objects.equals(entity.getKiller(), owningPlayer) && entity.getType() == EntityType.AXOLOTL) {
            owningPlayer.sendMessage(Component.text("* You felt your sins crawling on your back.")
                    .decorate(TextDecoration.BOLD));
        }
    }
    @Override
    public String getTaskKey() {
        return "fish-killer";
    }

    @Override
    public Builder<? extends Builder<?>> builderProvider() {
        return null;
    }
}
