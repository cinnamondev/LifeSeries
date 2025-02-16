package com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.SecretTasks;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Task.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class TaskAssignmentBuilderSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command(LifeSeries p, SecretTasks taskGame, Consumer<PlayerTask> onTaskAdded, Consumer<PlayerTask> onTaskCompletion) {
        var assignmentSubCommands = TaskLookup.getAllTaskBuilders().entrySet().stream()
                .map((entry) -> entry
                        .getValue()
                        .builderCommand(p, Commands.literal(entry.getKey()), onTaskAdded, onTaskCompletion)
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
        var root = Commands.literal("task")
                .requires(src -> src.getSender().hasPermission("life.admin.game"))
                .then(Commands.argument("players", ArgumentTypes.players())
                        .then(getTaskCommand)
                        .then(Commands.literal("fail"))
                        .then(Commands.literal("win"))
                        .then(assignmentCommand)
                );

        return root;
    }
}
