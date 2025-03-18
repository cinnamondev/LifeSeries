package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Commands.CompleteTaskCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Commands.GuessTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Commands.TaskAssignmentBuilderSubCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Commands.TaskBookCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.List;

public interface SecretTasks extends CommandContainer {
    enum TaskDifficulty {
        EASY,
        MEDIUM,
        HARD;

        public Component asComponent() {
            return switch (this) {
                case EASY -> Component.translatable("task-difficulty.easy").color(NamedTextColor.GREEN);
                case MEDIUM -> Component.translatable("task-difficulty.medium").color(NamedTextColor.YELLOW);
                case HARD -> Component.translatable("task-difficulty.hard").color(NamedTextColor.RED);
            };
        }
    }
    default Collection<String> getAllTaskOf(Collection<TaskDifficulty> difficulties) {
        return difficulties.stream().distinct().map(difficulty -> getTasksOfDifficulty(difficulty))
                .flatMap(Collection::stream)
                .toList();
    }
    default Collection<String> getAllTasksOf(TaskDifficulty... difficulties) {
        return getAllTaskOf(Arrays.asList(difficulties));
    }
    default Collection<String> getAllTasks() {
        return getAllTaskOf(Arrays.asList(TaskDifficulty.values()));
    }
    Collection<String> getAllTasksForTeam(TeamMeta teamMeta);

    Collection<String> getTasksOfDifficulty(TaskDifficulty difficulty);

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
    default Collection<PlayerTask> searchForTaskByKey(String key) {
        return getAllSecretTasks().stream()
                .filter(entry -> entry.getTaskKey().equalsIgnoreCase(key))
                .toList();
    }
    Collection<PlayerTask> getAllSecretTasks();
    boolean canGuessTask(TeamMeta guesser);
    boolean canGuessTask(OfflinePlayer guesser);
    boolean canRerollTask(TeamMeta reroller);
    boolean canRerollTask(OfflinePlayer reroller);

    PlayerTask rollTasks(Player owningPlayer, Collection<String> potentialTasks, boolean respectLimits, boolean add);
    default PlayerTask rollTaskOfDifficulty(Player owningPlayer, TaskDifficulty difficulty, boolean respectLimits, boolean add) {
        return rollTasks(owningPlayer, getTasksOfDifficulty(difficulty), respectLimits, add);
    }
    default PlayerTask rollTaskOfAnyDifficulty(Player owningPlayer, boolean respectLimits, boolean add) { return rollTasks(owningPlayer, getAllTasks(), respectLimits, add); }
    default PlayerTask rollTaskOfDifficulty(Player owningPlayer, Collection<TaskDifficulty> difficulty, boolean respectLimits, boolean add) {
        return rollTasks(owningPlayer,
                difficulty.stream().map(this::getTasksOfDifficulty).flatMap(Collection::stream).toList(),
                respectLimits,
                add
        );

    }
    default PlayerTask rollTask(Player owningPlayer, TeamMeta playerTeam, boolean respectLimits, boolean add) {
        return rollTasks(owningPlayer, getAllTasksForTeam(playerTeam), respectLimits, add);
    }

    @Override
    default Collection<LiteralArgumentBuilder<CommandSourceStack>> adminSubCommands(LifeSeries p) {
        return Collections.singletonList(
                TaskAssignmentBuilderSubCommand.command(p, this, this::addSecretTask)
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

    public static Component rejectGuessButton(Player guesser) {
        return Component.translatable("general.deny-prompt")
                .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD))
                .clickEvent(ClickEvent.callback(_audience -> {
                    guesser.sendMessage(Component.translatable("secret-life.guessing.guess-failed"));
                }));
    }

    public static Component rejectValidationButton(Player taskOwner) {
        return Component.translatable("general.deny-prompt")
                .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD))
                .clickEvent(ClickEvent.callback(_audience -> {
                    taskOwner.sendMessage(Component.translatable("secret-life.validation.rejected"));
                }));
    }

    public static Component acceptGuessButton(PlayerTask secretTask, Player guesser, boolean useCommand) {
        var message = Component.translatable("general.accept-prompt")
                .style(Style.style(NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
                .clickEvent(ClickEvent.callback(_audience -> {
                    guesser.sendMessage(Component.translatable("secret-life.guessing.guess-success"));
                }));

        if (useCommand) {
            return message.clickEvent(ClickEvent.suggestCommand("/lf task " + secretTask.getTaskOwner().getName() + " fail"));
        } else { // use secretTask method. (old way, keeping this here anyway)
            return message.clickEvent(ClickEvent.callback(_audience -> {
                secretTask.acceptGuess();
            }));
        }
    }
}
