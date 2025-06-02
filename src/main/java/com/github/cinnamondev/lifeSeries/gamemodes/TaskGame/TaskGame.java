package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Commands.*;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.TaskDifficulty;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface TaskGame extends CommandContainer {
    record TaskMeta(String key, TaskDifficulty difficulty, int min, int max, int assigned) {
        PlayerTask.Builder<?> builder() { return TaskLookup.getTaskBuilderByKey(key); }
    }

    List<TaskMeta> getConfiguredTasks(TaskDifficulty difficulty);
    List<TaskDifficulty> availableDifficultiesForTeam(TeamMeta teamMeta);
    default List<TaskMeta> getAllTasks() {
        return Arrays.stream(TaskDifficulty.values())
                .flatMap(difficulty -> getConfiguredTasks(difficulty).stream())
                .toList();
    }

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

    Map<UUID, PlayerTask> assignedTasks();
    void addSecretTask(PlayerTask secretTask);
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

    public enum RollMode {
        EASY, MEDIUM, HARD, TEAM, ANY;

        public static class RollerArgument implements CustomArgumentType.Converted<RollMode, String> {
            private static final DynamicCommandExceptionType ERROR_NOT_ROLL = new DynamicCommandExceptionType(name ->
                    MessageComponentSerializer.message().serialize(Component.text(name + " is not a valid roll mode."))
            );

            @Override
            public RollMode convert(String nativeType) throws CommandSyntaxException {
                return switch (nativeType.toLowerCase()) {
                    case "easy" -> RollMode.EASY;
                    case "normal" -> RollMode.MEDIUM;
                    case "hard" -> RollMode.HARD;
                    case "team" -> RollMode.TEAM;
                    case "any" -> RollMode.ANY;
                    default -> throw ERROR_NOT_ROLL.create(nativeType);
                };
            }

            @Override
            public ArgumentType<String> getNativeType() {
                return StringArgumentType.word();
            }

            @Override
            public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
                Stream.of("easy", "normal", "hard", "team", "any")
                        .filter(difficulty -> difficulty.startsWith(builder.getRemainingLowerCase()))
                        .forEach(builder::suggest);
                return builder.buildFuture();
            }
        }
    }

    /**
     * Roll tasks for `players` according to `rollMode` and `respectLimits`, if `add` is true, all tasks will be
     * immediately added to the game.
     * @param players List of players to roll tasks for
     * @param rollMode Specifies what kind of constraints on tasks player can roll are.
     * @param respectLimits Whether to ignore the limits on how many players can be assigned a single task.
     *                      Note that group tasks should still obey the assignment bounds when selecting additional
     *                      players.
     * @param add If true, add the task to the game.
     * @return
     */
    List<PlayerTask> rollTasks(List<Player> players, RollMode rollMode, boolean respectLimits, boolean add);

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
