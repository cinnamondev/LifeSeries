package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.TaskGame;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.TaskDifficulty;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.TaskLookup;
import com.github.cinnamondev.lifeSeries.util.TitleCountdown;
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

import java.util.List;

public class TaskAssignmentBuilderSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command(LifeSeries p, TaskGame taskGame) {
        var assignmentSubCommands = TaskLookup.getAllTaskBuilders().entrySet().stream()
                .map((entry) -> entry
                        .getValue()
                        .builderCommand(p, Commands.literal(entry.getKey()), taskGame::addSecretTask, taskGame::onTaskCompletion)
                ).toList();

        var assignmentCommand = Commands.literal("assign");
        for (LiteralArgumentBuilder<CommandSourceStack> subcommand : assignmentSubCommands) {
            assignmentCommand = assignmentCommand.then(
                    Commands.argument("difficulty", new TaskDifficulty.DifficultyArgumentType())
                            .then(subcommand)
            );
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

    private static RequiredArgumentBuilder<CommandSourceStack, TaskGame.RollMode> rollCommand(LifeSeries p, TaskGame taskGame, boolean countdown) {
        return Commands.argument("mode", new TaskGame.RollMode.RollerArgument()).executes(ctx -> {
            var players = ctx.getArgument("players", PlayerSelectorArgumentResolver.class)
                    .resolve(ctx.getSource());
            TaskGame.RollMode mode = ctx.getArgument("mode", TaskGame.RollMode.class);
            Runnable taskRoller = () -> {
                List<PlayerTask> tasks = taskGame.rollTasks(players, mode, true, true);
                if (tasks.size() != players.size()) {
                    ctx.getSource().getSender()
                            .sendMessage(Component.text("could not find a task for all candidates! check logs"));
                }
            };
            if (countdown) {
                TitleCountdown.lifeCountdown(p, Audience.audience(players),
                        Sound.sound(NamespacedKey.minecraft("block.metal_pressure_plate.click_on"), Sound.Source.MASTER, 0.8f, 1),
                        1,30, taskRoller);
            } else { taskRoller.run(); } // roll without delay
            return 1;
        });
    }
}
