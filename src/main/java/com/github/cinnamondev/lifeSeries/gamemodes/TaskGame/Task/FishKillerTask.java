package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.KillMobsTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.TaskDifficulty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
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
    public Component description() {
        return Component.translatable("secret-life.tasks." +getTaskKey()+ ".description",
                String.valueOf(getThreshold()));
    }

    @Override
    public String descriptionServerTranslate(Locale locale) {
        return GlobalTranslator.translator().translate("secret-life.tasks." +getTaskKey()+ ".description", locale)
                .format(String.valueOf(getThreshold()));
    }

    @Override
    public Builder builderProvider() {
        return new Builder();
    }

    public static class Builder extends PlayerTask.Builder<Builder> {
        @Override
        public AbstractPlayerTask build(LifeSeries p) {
            return new FishKillerTask(p, owningPlayer, onTaskCompletion, assignedDifficulty);
        }
    }
}
