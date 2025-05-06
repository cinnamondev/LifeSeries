package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.AbstractSharedGroupTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.SessionLongTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.TaskDifficulty;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.function.Consumer;

public class TestGroupTask extends AbstractSharedGroupTask implements Listener, SessionLongTask {
    public TestGroupTask(LifeSeries p, Collection<Player> owningPlayers, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayers, onTaskCompletion, difficulty);
    }

    @Override public boolean isTaskGuessable() { return false; }

    @EventHandler
    public void onPlayerJump(PlayerJumpEvent e) {
        if (!tasks.containsKey(e.getPlayer().getUniqueId())) { return; }
        if (!isTaskFinished()) { complete(); }
    }

    @Override public String getTaskKey() { return "test-group-task"; }

    @Override public Builder builderProvider() { return new Builder(); }

    public static class Builder extends AbstractSharedGroupTask.Builder<Builder> {
        @Override
        public TestGroupTask build(LifeSeries p) {
            return new TestGroupTask(p, this.players, this.onTaskCompletion, this.assignedDifficulty);
        }
    }
}
