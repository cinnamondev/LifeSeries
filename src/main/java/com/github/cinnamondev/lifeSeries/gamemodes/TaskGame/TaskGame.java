package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Commands.*;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.TaskDifficulty;
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
import org.bukkit.event.HandlerList;

import java.util.*;

public interface TaskGame extends CommandContainer {
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
        HandlerList.unregisterAll(secretTask);
        // i think there was some point to this at one point but its just a wtroublemaker!
        // removeSecretTask(secretTask);
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
                TaskAssignmentBuilderSubCommand.command(p, this)
        );
    }

    @Override
    default Collection<FilledLiteralCommand> gameCommands(LifeSeries p) {
        return List.of(
                new TaskBookCommand(p, this),
                new CompleteTaskCommand(p, this),
                new GuessTask(p, this),
                new TestTaskComplete(this)
        );
    }

    static Component rejectGuessButton(Player guesser) {
        return Component.translatable("general.deny-prompt")
                .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD))
                .clickEvent(ClickEvent.callback(_audience -> guesser.sendMessage(Component.translatable("secret-life.guessing.guess-failed"))));
    }

    static Component rejectValidationButton(Player taskOwner) {
        return Component.translatable("general.deny-prompt")
                .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD))
                .clickEvent(ClickEvent.callback(_audience -> taskOwner.sendMessage(Component.translatable("secret-life.validation.rejected"))));
    }

    static Component acceptGuessButton(PlayerTask secretTask, Player guesser, boolean useCommand) {
        var message = Component.translatable("general.accept-prompt")
                .style(Style.style(NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
                .clickEvent(ClickEvent.callback(_audience -> guesser.sendMessage(Component.translatable("secret-life.guessing.guess-success"))));

        if (useCommand) {
            return message.clickEvent(ClickEvent.suggestCommand("/lf task " + secretTask.getTaskOwner().getName() + " fail"));
        } else { // use secretTask method. (old way, keeping this here anyway)
            return message.clickEvent(ClickEvent.callback(_audience -> secretTask.acceptGuess()));
        }
    }
}
