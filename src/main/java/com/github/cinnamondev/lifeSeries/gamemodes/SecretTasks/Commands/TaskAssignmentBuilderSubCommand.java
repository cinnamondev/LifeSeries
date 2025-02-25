package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.*;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class TaskAssignmentBuilderSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command(LifeSeries p, SecretTasks taskGame, Consumer<PlayerTask> onTaskCompletion) {
        var assignmentSubCommands = TaskLookup.getAllTaskBuilders().entrySet().stream()
                .map((entry) -> entry
                        .getValue()
                        .builderCommand(p, Commands.literal(entry.getKey()), onTaskCompletion)
                ).toList();

        var assignmentCommand = Commands.literal("assign");
        for (LiteralArgumentBuilder<CommandSourceStack> subcommand : assignmentSubCommands) {
            assignmentCommand = assignmentCommand.then(subcommand);
        }

        var getTaskCommand = Commands.literal("get")
                .executes(ctx -> {
                    for (Player player : ctx.getArgument("players", PlayerSelectorArgumentResolver.class)
                            .resolve(ctx.getSource())) {
                        taskGame.getSecretTask(player).ifPresentOrElse((task) -> {
                            ctx.getSource().getSender().sendMessage(player.displayName()
                                    .append(Component.text("'s task: "))
                                    .append(task.getTaskName())
                                    .appendSpace()
                                    .append(Component.text(task.getTaskProgress().toString()))
                            );
                        }, () -> ctx.getSource().getSender().sendMessage(Component.text("No task assigned to ")
                                    .append(player.displayName()))
                        );
                    }
                    return 1;
                });

        var rollTasksCommand = Commands.literal("roll")
                .then(Commands.argument("difficulty", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            builder.suggest("team");
                            builder.suggest("any");
                            builder.suggest("easy");
                            builder.suggest("medium");
                            builder.suggest("hard");
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            ctx.getArgument("players", PlayerSelectorArgumentResolver.class)
                                    .resolve(ctx.getSource()).forEach(player -> {
                                        String difficulty = ctx.getArgument("difficulty", String.class).toLowerCase();
                                        var task = switch (difficulty) { // discover which tasks to give
                                            case "easy" -> taskGame.rollTaskOfDifficulty(player, SecretTasks.TaskDifficulty.EASY);
                                            case "medium" -> taskGame.rollTaskOfDifficulty(player,SecretTasks.TaskDifficulty.MEDIUM);
                                            case "hard" -> taskGame.rollTaskOfDifficulty(player,SecretTasks.TaskDifficulty.HARD);
                                            case "team" -> taskGame.rollTask(player,p.getScoreHandler().getTeam(player));
                                            case "all" -> taskGame.rollTaskOfAnyDifficulty(player);
                                            default -> null;
                                        };
                                        if (task != null) {
                                            taskGame.addSecretTask(task);
                                        }
                                    });
                            return 1;
                        })
                );
        var root = Commands.literal("task")
                .requires(src -> src.getSender().hasPermission("life.admin.game"))
                .then(Commands.argument("players", ArgumentTypes.players())
                        .then(getTaskCommand)
                        .then(Commands.literal("fail").executes(ctx -> {
                            for (Player player : ctx.getArgument("players", PlayerSelectorArgumentResolver.class)
                                    .resolve(ctx.getSource())) {
                                taskGame.getSecretTask(player).ifPresent(PlayerTask::fail);
                            }
                            return 1;
                        }))
                        .then(Commands.literal("win").executes(ctx -> {
                            for (Player player : ctx.getArgument("players", PlayerSelectorArgumentResolver.class)
                                    .resolve(ctx.getSource())) {
                                taskGame.getSecretTask(player).ifPresent(PlayerTask::complete);
                            }
                            return 1;
                        }))
                        .then(rollTasksCommand)
                        .then(assignmentCommand)
                );

        return root;
    }
}
