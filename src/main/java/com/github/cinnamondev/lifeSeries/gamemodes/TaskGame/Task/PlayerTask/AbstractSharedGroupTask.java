package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class AbstractSharedGroupTask<T extends AbstractSharedGroupTask<?>> {
    protected ArrayList<GroupTask> tasks = new ArrayList<>();
    protected PlayerTask.TaskStatus status = PlayerTask.TaskStatus.IN_PROGRESS;
    protected final UUID taskUUID = UUID.randomUUID();

    public UUID getTaskUUID() { return this.taskUUID; }

    public ArrayList<GroupTask> getGluedTasks() { return this.tasks; }

    public void complete() {
        cleanup();
        this.status = PlayerTask.TaskStatus.COMPLETE;
        tasks.forEach(task -> task.getTaskConsumer().accept(task));
    }

    public void fail() {
        cleanup();
        this.status = PlayerTask.TaskStatus.FAILED;
        tasks.forEach(task -> task.getTaskConsumer().accept(task));
    }

    public void cleanup() {
    }
    public PlayerTask.TaskStatus getTaskProgress() {
        return this.status;
    }

    public boolean isTaskGuessable() {
        return false;
    }

    public Optional<ConfigurationSection> getConfigurationSection() {
        return Optional.empty();
    }

    public TaskDifficulty getDifficulty() {
        return null;
    }

    public ConfigurationSection saveTask() {
        return null;
    }

    public void acceptGuess() {
        return;
    }

    
    public abstract class Builder<T extends Builder<T>> extends PlayerTask.Builder<T> {
        protected ArrayList<Player> targetedPlayers = new ArrayList<>();

    }
}
