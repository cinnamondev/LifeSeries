package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CatLife.CatLife;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.AbstractWatchdogTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.PlayerTask;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class MeowerTask extends AbstractWatchdogTask {
    protected final CatLife game;
    protected int strikes = 0;

    public MeowerTask(LifeSeries p, CatLife game, Player owningPlayer, int watchdogInterval, int watchdogThreshold, Consumer<PlayerTask> onTaskCompletion, SecretTasks.TaskDifficulty difficulty) {
        super(p, owningPlayer, watchdogInterval, watchdogThreshold, onTaskCompletion, difficulty);
        this.game = game;
        game.getMeowCommand().addMeowListener(this::onPlayerMeow);
    }

    private void onPlayerMeow(Player p) {
        if (!owningPlayer.getLocation().getNearbyPlayers(16).isEmpty()) { feed(); }
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
    public boolean endOfSession() {
        if (!getTaskProgress().equals(TaskStatus.FAILED)) {
            complete();
            return true;
        }
        return false;
    }

    @Override
    public boolean isTaskGuessable() {
        return true;
    }

    @Override
    public String getTaskKey() {
        return "meow-at-others";
    }

    public static class Builder extends AbstractWatchdogTask.Builder<Builder> {
        @Override
        public AbstractPlayerTask build(LifeSeries p) {
            if (p.getGame() instanceof CatLife game) {
                return new MeowerTask(p, game, owningPlayer, 200, getWatchdogThreshold(p), onTaskCompletion, assignedDifficulty);
            } else {
                throw new RuntimeException("MeowerTask requires gamemode of CatLife");
            }
        }
    }
}
