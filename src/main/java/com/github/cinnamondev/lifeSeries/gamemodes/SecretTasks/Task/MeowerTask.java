package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CatLife.CatLife;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.*;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class MeowerTask extends AbstractWatchdogTask implements SessionLongTask {
    protected final CatLife game;
    protected int strikes = 0;

    public MeowerTask(LifeSeries p, CatLife game, Player owningPlayer, int watchdogInterval, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayer, watchdogInterval, onTaskCompletion, difficulty);
        this.game = game;
        game.getMeowCommand().addMeowListener(this::onPlayerMeow);
    }

    private void onPlayerMeow(Player p) {
        if (!owningPlayer.getLocation().getNearbyPlayers(16).isEmpty()) { feed(); }
    }

    @Override
    public void cleanup() {
        game.getMeowCommand().removeMeowListener(this::onPlayerMeow);
    }

    @Override
    public void bark() {
        strikes += 1;
        if (strikes > 5) {
            fail();
        } else {
            owningPlayer.sendMessage(Component.translatable("secret-life.tasks.meow-at-others.reminder"));
        }
    }

    @Override
    public boolean isTaskGuessable() {
        return true;
    }

    @Override
    public String getTaskKey() {
        return "meow-at-others";
    }

    @Override
    public MeowerTask.Builder builderProvider() {
        return new MeowerTask.Builder();
    }
    public static class Builder extends PlayerTask.Builder<Builder> {
        @Override
        public AbstractPlayerTask build(LifeSeries p) {
            if (p.getGame() instanceof CatLife game) {
                return new MeowerTask(p, game, owningPlayer, 200, onTaskCompletion, assignedDifficulty);
            } else {
                throw new RuntimeException("MeowerTask requires gamemode of CatLife");
            }
        }
    }
}
