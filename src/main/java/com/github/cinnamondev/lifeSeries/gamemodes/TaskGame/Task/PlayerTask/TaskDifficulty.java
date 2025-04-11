package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.util.UtilityComponents;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public enum TaskDifficulty {
    EASY,
    MEDIUM,
    HARD,
    EXTREME; // TODO: future extreme tasks. i.e. fight the dragon, kill a warden, stupid things like that.

    public Component asComponent() {
        return switch (this) {
            case EASY -> Component.translatable("task-difficulty.easy").color(NamedTextColor.GREEN);
            case MEDIUM -> Component.translatable("task-difficulty.medium").color(NamedTextColor.YELLOW);
            case HARD -> Component.translatable("task-difficulty.hard").color(NamedTextColor.RED);
            case EXTREME -> UtilityComponents.glitchyText(1)
                    .append(Component.translatable("task-difficulty.extreme"))
                    .append(UtilityComponents.glitchyText(1)).color(NamedTextColor.DARK_RED);
        };
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> commandArg(String nodeName) {
        return Commands.argument(nodeName, StringArgumentType.word()).suggests((ctx, builder) -> {
            builder.suggest("easy");
            builder.suggest("normal");
            builder.suggest("hard");

            return builder.buildFuture();
        });
    }
    public static TaskDifficulty difficultyResolver(CommandContext<CommandSourceStack> ctx, String nodeName) {
        return switch (ctx.getArgument(nodeName, String.class).toLowerCase()) {
            case "easy" -> TaskDifficulty.EASY;
            case "normal" -> TaskDifficulty.MEDIUM;
            case "hard" -> TaskDifficulty.HARD;
            default -> {
                ctx.getSource().getSender().sendMessage(Component.text("Invalid difficulty", NamedTextColor.RED));
                throw new RuntimeException("Invalid difficulty given");
            }
        };
    }
}