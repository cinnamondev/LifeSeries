package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CatLife.CatLife;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.AbstractWatchdogTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.PlayerTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class MeowerTask extends AbstractWatchdogTask {
    private int strikes = 0;

    public MeowerTask(LifeSeries p, CatLife game, Player owningPlayer, int interval, Consumer<PlayerTask> onTaskCompletion, SecretTasks.TaskDifficulty difficulty) {
        super(p, owningPlayer, interval, onTaskCompletion, difficulty);
        game.getMeowCommand().addMeowListener(this::onPlayerMeow);
    }

    public void onPlayerMeow(Player p) {
        if (!owningPlayer.getLocation().getNearbyPlayers(16).isEmpty()) { feed(); }
    }

    @Override
    public void bark() {
        strikes += 1;
        if (strikes >= 2) {
            fail();
        } else {
            owningPlayer.sendMessage(Component.translatable("secret-life.tasks.meow.reminder"));
        }
        super.bark();
    }

    @Override
    public boolean isTaskGuessable() {
        return true;
    }

    @Override
    public TranslatableComponent getTaskName() {
        return Component.translatable("secret-life.tasks.meow.name");
    }

    @Override
    public TranslatableComponent getTaskDescription() {
        return Component.translatable("secret-life.tasks.meow.description");
    }

    @Override
    public String getTaskKey() {
        return "meow-at-others";
    }
    
    public static class Builder extends AbstractWatchdogTask.Builder<Builder> {
        @Override
        public AbstractPlayerTask build(LifeSeries p) {
            if (p.getGame() instanceof CatLife game) {
                return new MeowerTask(p, game, owningPlayer, this.watchdogInterval, onTaskCompletion, assignedDifficulty);
            } else {
                throw new RuntimeException("MeowerTask requires gamemode of CatLife");
            }
        }
    }
}
