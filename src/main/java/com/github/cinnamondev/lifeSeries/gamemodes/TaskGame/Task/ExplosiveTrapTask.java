package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.SelfCompletableTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.TaskDifficulty;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ExplosiveTrapTask extends AbstractPlayerTask implements Listener, SelfCompletableTask {
    public ExplosiveTrapTask(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
    }
    private int recentExplosionDeaths = 0;
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerBlewUp(PlayerDeathEvent e) {
        DamageSource source = e.getDamageSource();
        DamageType damageType = e.getDamageSource().getDamageType();
        if (!(damageType.equals(DamageType.EXPLOSION) || damageType.equals(DamageType.PLAYER_EXPLOSION))) { return; }
        recentExplosionDeaths += 1;
        p.getServer().getScheduler()
                .runTaskLater(p,
                        () -> recentExplosionDeaths = Math.max(recentExplosionDeaths-1,0),
                        TimeUnit.MINUTES.toSeconds(3) * 20);
    }

    @Override
    public boolean conditionalCompleteTask() {
        if (recentExplosionDeaths > 0) {
            complete();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public TaskStatus getTaskProgress() {
        return status;
    }

    @Override
    public boolean isTaskGuessable() {
        return true;
    }

    @Override
    public String getTaskKey() {
        return "explode-another-player";
    }

    @Override
    public Builder builderProvider() {
        return new Builder();
    }
    public static class Builder extends PlayerTask.Builder<Builder> {
        @Override
        public AbstractPlayerTask build(LifeSeries p) {
            return new ExplosiveTrapTask(p, owningPlayer, onTaskCompletion, assignedDifficulty);
        }
    }
}
