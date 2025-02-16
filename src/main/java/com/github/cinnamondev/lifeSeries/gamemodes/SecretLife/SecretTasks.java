package com.github.cinnamondev.lifeSeries.gamemodes.SecretLife;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Commands.CompleteTaskCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Commands.TaskAssignmentBuilderSubCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Commands.TaskBookCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Task.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Task.PlayerTask;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.OfflinePlayer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface SecretTasks extends CommandContainer {
    enum TaskDifficulty {
        EASY,
        MEDIUM,
        HARD
    }
    AbstractPlayerTask.Builder<?> getRandomSecretTask();
    AbstractPlayerTask.Builder<?> getRandomSecretTask(TaskDifficulty difficulty);
    void addSecretTask(PlayerTask secretTask);
    default void onTaskCompletion(PlayerTask secretTask) {
        if (secretTask.getTaskProgress() == PlayerTask.TaskStatus.COMPLETE) {
            onTaskSuccess(secretTask);
        } else {
            onTaskFailure(secretTask);
        }
        removeSecretTask(secretTask);
    }
    void onTaskSuccess(PlayerTask secretTask);
    void onTaskFailure(PlayerTask secretTask);
    void removeSecretTask(OfflinePlayer taskOwner);
    default void removeSecretTask(PlayerTask secretTask) { removeSecretTask(secretTask.getTaskOwner()); }
    Optional<PlayerTask> getSecretTask(OfflinePlayer taskOwner);

    @Override
    default Collection<LiteralArgumentBuilder<CommandSourceStack>> adminSubCommands(LifeSeries p) {
        return Collections.singletonList(
                TaskAssignmentBuilderSubCommand.command(p, this, this::addSecretTask, this::onTaskCompletion)
        );
    }

    @Override
    default Collection<FilledLiteralCommand> gameCommands(LifeSeries p) {
        return List.of(
                new TaskBookCommand(p, this),
                new CompleteTaskCommand(p, this)
        );
    }
}
