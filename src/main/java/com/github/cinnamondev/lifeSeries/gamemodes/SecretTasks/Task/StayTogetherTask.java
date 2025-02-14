package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
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

public class StayTogetherTask implements PlayerTask, Listener {
    private final LifeSeries p;
    private TaskStatus status = TaskStatus.IN_PROGRESS;

    private Player owningPlayer; // task will be assigned when player is online, we can assume that
    private OfflinePlayer target; // listen events will only match when this player is online :) happy happy happy!

    private final double maxDistance = 10;
    private final int timeoutMinutes = 10;
    private int strikes = 2;
    public StayTogetherTask(LifeSeries p, OfflinePlayer owningPlayer, OfflinePlayer target) {
        this.p = p;
    }

    private ScheduledTask task = null;
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTaskPlayerMove(PlayerMoveEvent e) {
        if (!e.getPlayer().equals(owningPlayer)) { return; }

        Player onlineTarget = p.getServer().getPlayer(target.getUniqueId());
        if (onlineTarget == null) { return; }

        Location playerLoc = owningPlayer.getLocation();
        World playerWorld = owningPlayer.getWorld();
        Location targetLoc = onlineTarget.getLocation();
        World targetWorld = onlineTarget.getWorld();
        // if player is out of range of other player
        if (targetLoc.distance(playerLoc) > maxDistance || !playerWorld.equals(targetWorld)) {
            if (task == null || // task has yet to run OR is not in a 'to be executed' state.
                    task.getExecutionState().equals(ScheduledTask.ExecutionState.FINISHED) ||
                    task.getExecutionState().equals(ScheduledTask.ExecutionState.CANCELLED)) {
                task = owningPlayer.getScheduler().runDelayed(p, (_t) -> { // check in `timeoutMinutes` time if failed.
                        if (strikes != 0 && checkFailState() == TaskStatus.FAILED) {
                            strikes -=1;
                            if (strikes == 0) { fail(); } else {
                                owningPlayer.sendMessage(
                                        Component.translatable("secret-life.tasks.stay-together.reminder", target.getName())
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
    public void complete() {
        this.status = TaskStatus.COMPLETE;
    }

    @Override
    public void fail() {
        owningPlayer.showTitle(Title.title(
                Component.translatable("secret-life.failed-task"),
                Component.translatable("secret-life.failed-task.subtitle")
        ));
        this.status = TaskStatus.FAILED;
    }

    @Override
    public boolean endOfSession() {
        if (!getTaskProgress().equals(TaskStatus.FAILED)) { complete(); return true; }
        return false;
    }

    @Override
    public TaskStatus getTaskProgress() {
        return this.status;
    }

    @Override
    public boolean isTaskGuessable() {
        return true;
    }

    @Override
    public Component getTaskName() {
        return Component.translatable("secret-life.tasks.stay-together.name");
    }

    @Override
    public Component getTaskDescription() {
        return Component.translatable("secret-life.tasks.stay-together.description")
                .arguments(
                        Component.text(maxDistance),
                        Component.text(Objects.requireNonNull(target.getName())),
                        Component.text(timeoutMinutes),
                        Component.text(strikes)
                );
    }

    private TaskStatus checkFailState() {
        Player onlineTarget = p.getServer().getPlayer(target.getUniqueId());
        if (onlineTarget == null) { return TaskStatus.ODD_STATE; } // we cant do anything rn!!! kinda messy.
        if (!owningPlayer.isOnline()) { return TaskStatus.FAILED; } // player cant be tracked like this! so they fail :)

        if (owningPlayer.getLocation().distance(onlineTarget.getLocation()) > maxDistance ||
                !owningPlayer.getWorld().equals(onlineTarget.getWorld())) {
            return TaskStatus.FAILED;
        }
    }



}
