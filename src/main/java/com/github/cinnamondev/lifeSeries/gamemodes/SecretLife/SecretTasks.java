package com.github.cinnamondev.lifeSeries.gamemodes.SecretLife;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Commands.CompleteTaskCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Commands.GuessTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Commands.TaskAssignmentBuilderSubCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Commands.TaskBookCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Task.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Task.PlayerTask;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.awt.*;
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
    boolean teamCanGuessTasks(String teamString);
    default boolean teamCanGuessTasks(TeamMeta team) {
        return teamCanGuessTasks(team.getScoreboardTeam().getName());
    }
    boolean playerCanGuessTasks(OfflinePlayer guesser);

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
                new CompleteTaskCommand(p, this),
                new GuessTask(p, this)
        );
    }

    public static Component rejectGuessButton(PlayerTask secretTask) {
        return Component.translatable("general.deny-prompt")
                .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD))
                .clickEvent(ClickEvent.suggestCommand("/lf task " + secretTask.getTaskOwner().getName() + " fail"));
    }
    public static Component acceptGuessButton(PlayerTask secretTask) {
        return Component.translatable("general.accept-prompt")
                .style(Style.style(NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
                .clickEvent(ClickEvent.suggestCommand("/lf task " + secretTask.getTaskOwner().getName() + " win"));
    }
}
