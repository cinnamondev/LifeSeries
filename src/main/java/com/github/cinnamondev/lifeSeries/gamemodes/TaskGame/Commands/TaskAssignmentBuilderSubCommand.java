package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.TaskGame;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.*;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.TaskDifficulty;
import com.github.cinnamondev.lifeSeries.util.TitleCountdown;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Collection;

public class TaskAssignmentBuilderSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command(LifeSeries p, TaskGame taskGame) {
        var assignmentSubCommands = TaskLookup.getAllTaskBuilders().entrySet().stream()
                .map((entry) -> entry
                        .getValue()
                        .builderCommand(p, Commands.literal(entry.getKey()), taskGame::addSecretTask, taskGame::onTaskCompletion)
                ).toList();

        var assignmentCommand = Commands.literal("assign");
        for (LiteralArgumentBuilder<CommandSourceStack> subcommand : assignmentSubCommands) {
            assignmentCommand = assignmentCommand.then(TaskDifficulty.commandArg("difficulty").then(subcommand));
        }

        var getTaskCommand = Commands.literal("get")
                .executes(ctx -> {
                    for (Player player : ctx.getArgument("players", PlayerSelectorArgumentResolver.class)
                            .resolve(ctx.getSource())) {
                        taskGame.getSecretTask(player).ifPresentOrElse((task) -> ctx.getSource().getSender().sendMessage(player.displayName()
                                .append(Component.text("'s task: "))
                                .append(task.lore())
                                .appendSpace()
                                .append(task.getTaskProgress().asComponent().hoverEvent(task.taskProgressExplanation()))
                        ), () -> ctx.getSource().getSender().sendMessage(Component.text("No task assigned to ")
                                    .append(player.displayName()))
                        );
                    }
                    return 1;
                });

        var rollTasksCommand = Commands.literal("roll")
                .then(Commands.literal("countdown").then(rollCommand(p, taskGame, true))) // with countdown
                .then(rollCommand(p, taskGame, false));
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

    private static RequiredArgumentBuilder<CommandSourceStack, String> rollCommand(LifeSeries p, TaskGame taskGame, boolean countdown) {
        return Commands.argument("difficulty", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    builder.suggest("team");
                    builder.suggest("any");
                    builder.suggest("easy");
                    builder.suggest("medium");
                    builder.suggest("hard");
                    return builder.buildFuture();
                })
                .executes(ctx -> {
                    var players = ctx.getArgument("players", PlayerSelectorArgumentResolver.class)
                            .resolve(ctx.getSource());
                    if (countdown) {
                        TitleCountdown.lifeCountdown(p, Audience.audience(players),
                                Sound.sound(NamespacedKey.minecraft("block.metal_pressure_plate.click_on"), Sound.Source.MASTER, 0.8f, 1),
                                1,30, () -> roller(
                                        p,
                                        taskGame,
                                        ctx.getSource().getSender(),
                                        players,
                                        ctx.getArgument("difficulty", String.class).toLowerCase()
                                ));
                    } else { // roll without delay
                        roller(
                                p,
                                taskGame,
                                ctx.getSource().getSender(),
                                players,
                                ctx.getArgument("difficulty", String.class).toLowerCase()
                        );
                    }
                    return 1;
                });
    }

    private static void roller(LifeSeries p, TaskGame taskGame, Audience source, Collection<Player> players, String difficulty) {
        players.forEach(player -> {
            PlayerTask task;
            try {
                task = switch (difficulty) { // discover which tasks to give
                    case "easy" -> taskGame.rollTaskOfDifficulty(player, TaskDifficulty.EASY, true, true);
                    case "medium" -> taskGame.rollTaskOfDifficulty(player, TaskDifficulty.MEDIUM, true, true);
                    case "hard" -> taskGame.rollTaskOfDifficulty(player, TaskDifficulty.HARD, true, true);
                    case "team" -> taskGame.rollTask(player, p.getScoreHandler().getTeam(player), true, true);
                    case "any" -> taskGame.rollTaskOfAnyDifficulty(player, true, true);
                    default -> throw new IllegalStateException("Unexpected value: " + difficulty);
                };
            } catch (Exception e) {
                source.sendMessage(
                        Component.text("Failed to roll task for player ").append(player.displayName())
                                .hoverEvent(Component.text(e.getMessage()))
                );
                //e.printStackTrace();
                p.getLogger().throwing("TaskAssignmentBuilderSubCommand", "roller", e);
            }
        });
    }
}
