package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.*;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class StayTogetherTask extends AbstractTargetedWatchdogTask implements Listener {

    private final double maxDistance = 10;
    private final int timeoutMinutes = 10;
    private int strikes = 2;
    public StayTogetherTask(LifeSeries p, Player owningPlayer, int watchdogInterval, int watchdogThreshold, OfflinePlayer target, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayer, watchdogInterval, watchdogThreshold, target, difficulty, onTaskCompletion);
    }

    private ScheduledTask task = null;
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTaskPlayerMove(PlayerMoveEvent e) {
        if (!e.getPlayer().equals(owningPlayer) || targetedPlayer == null) { return; }

        Player onlineTarget = p.getServer().getPlayer(targetedPlayer.getUniqueId());
        if (onlineTarget == null) { return; }

        Location playerLoc = owningPlayer.getLocation();
        World playerWorld = owningPlayer.getWorld();
        Location targetLoc = onlineTarget.getLocation();
        World targetWorld = onlineTarget.getWorld();
        // if player is out of range of other player
        if (!playerWorld.equals(targetWorld)) { return; }
        if (targetLoc.distanceSquared(playerLoc) > maxDistance) {

        }
        if (targetLoc.distance(playerLoc) > maxDistance || !playerWorld.equals(targetWorld)) {
            if (task == null || // task has yet to run OR is not in a 'to be executed' state.
                    task.getExecutionState().equals(ScheduledTask.ExecutionState.FINISHED) ||
                    task.getExecutionState().equals(ScheduledTask.ExecutionState.CANCELLED)) {
                task = owningPlayer.getScheduler().runDelayed(p, (_t) -> { // check in `timeoutMinutes` time if failed.
                        if (strikes != 0 && checkFailState() == TaskStatus.FAILED) {
                            strikes -=1;
                            if (strikes == 0) { fail(); } else {
                                owningPlayer.sendMessage(
                                        Component.translatable("secret-life.tasks.follow-another-player.reminder", targetedPlayer.getName())
                                                .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD))
                                );
                            }
                        }
                    }, null, TimeUnit.MINUTES.toSeconds(timeoutMinutes) * 20
                );
            }
        }
    }

    @Override
    public boolean endOfSession() {
        if (!getTaskProgress().equals(PlayerTask.TaskStatus.FAILED)) { complete(); return true; }
        return false;
    }

    @Override
    public boolean isTaskGuessable() {
        return true;
    }

    @Override
    public TranslatableComponent description() {
        return Component.translatable("secret-life.tasks.follow-another-player.description")
                .arguments(
                        Component.text(maxDistance),
                        Component.text(Objects.requireNonNull(targetedPlayer.getName())),
                        Component.text(timeoutMinutes),
                        Component.text(strikes)
                );
    }

    @Override
    public String getTaskKey() {
        return "follow-another-player";
    }

    private TaskStatus checkFailState() {
        Player onlineTarget = p.getServer().getPlayer(targetedPlayer.getUniqueId());
        if (onlineTarget == null) { return TaskStatus.ODD_STATE; } // we cant do anything rn!!! kinda messy.
        if (!owningPlayer.isOnline()) { return TaskStatus.FAILED; } // player cant be tracked like this! so they fail :)

        if (owningPlayer.getLocation().distance(onlineTarget.getLocation()) > maxDistance ||
                !owningPlayer.getWorld().equals(onlineTarget.getWorld())) {
            return TaskStatus.FAILED;
        }
        return TaskStatus.IN_PROGRESS;
    }

    public static class Builder extends AbstractTargetedWatchdogTask.Builder<Builder> {
        @Override
        public AbstractPlayerTask build(LifeSeries p) {
            return new StayTogetherTask(p, owningPlayer, 200, getWatchdogThreshold(p), targetPlayer, onTaskCompletion, assignedDifficulty);
        }
    }



}
