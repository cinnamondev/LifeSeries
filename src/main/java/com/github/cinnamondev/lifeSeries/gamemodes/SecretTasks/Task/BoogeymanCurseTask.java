package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.SelfCompletableTask;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.function.Consumer;

public class BoogeymanCurseTask extends AbstractPlayerTask implements SelfCompletableTask, Listener {
    public BoogeymanCurseTask(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, SecretTasks.TaskDifficulty difficulty) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
    }

    @Override
    public boolean conditionalCompleteTask() {
        return false; // TODO
    }

    @Override
    public boolean isTaskGuessable() {
        return false;
    }

    @Override
    public String getTaskKey() {
        return "boogeyman-curse";
    }
}
