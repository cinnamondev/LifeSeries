package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CatLife.CatLife;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.function.Consumer;

public class MeowAtPlayer extends AbstractTargetedPlayerTask implements Listener, HasRadius {
    protected CatLife game;
    public MeowAtPlayer(LifeSeries p, CatLife game, Player owningPlayer, OfflinePlayer targetedPlayer, TaskDifficulty difficulty, Consumer<PlayerTask> onTaskCompletion) {
        super(p, owningPlayer, targetedPlayer, difficulty, onTaskCompletion);
        this.game = game;
        game.getMeowCommand().addMeowListener(this::onMeow);
    }

    @Override
    public void cleanup() {
        game.getMeowCommand().removeMeowListener(this::onMeow);
    }

    private void onMeow(Player meower) {
        if (!meower.equals(owningPlayer)) { return; }
        Player target = targetedPlayer.getPlayer();
        if (target == null) { return; }
        if (withinRadius(target.getLocation(), meower.getLocation())) { complete(); }
    }
    @Override
    public boolean isTaskGuessable() {
        return true;
    }

    @Override
    public String getTaskKey() {
        return "meow-at-player";
    }

    @Override
    public int fallbackRadius() {
        return 20;
    }

    @Override
    public MeowAtPlayer.Builder builderProvider() {
        return new MeowAtPlayer.Builder();
    }

    public static class Builder extends TargetedPlayerTask.Builder<MeowAtPlayer.Builder> {
        @Override
        public AbstractPlayerTask build(LifeSeries p) {
            if (p.getGame() instanceof CatLife game) {
                return new MeowAtPlayer(p, game, owningPlayer, targetPlayer, assignedDifficulty, onTaskCompletion);
            } else {
                throw new RuntimeException("MeowAtPlayer requires gamemode of CatLife");
            }
        }
    }
}
