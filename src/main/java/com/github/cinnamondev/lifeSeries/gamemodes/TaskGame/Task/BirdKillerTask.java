package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
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
        return "duck-hunt";
    }

    @Override
    public String descriptionServerTranslate(Locale locale) {
        return GlobalTranslator.translator().translate("secret-life.tasks." + getTaskKey() + ".description", locale)
                .format(String.valueOf(getThreshold()));
    }

    @Override
    public Component description() {
        return Component.translatable("secret-life.tasks." + getTaskKey() + ".description", String.valueOf(getThreshold()));
    }

    @Override
    public Builder builderProvider() {
        return new Builder();
    }

    public static class Builder extends PlayerTask.Builder<Builder> {
        @Override
        public AbstractPlayerTask build(LifeSeries p) {
            return new BirdKillerTask(p, owningPlayer, onTaskCompletion, assignedDifficulty);
        }
    }
}
