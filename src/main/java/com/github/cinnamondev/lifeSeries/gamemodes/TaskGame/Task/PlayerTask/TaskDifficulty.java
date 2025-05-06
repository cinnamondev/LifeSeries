package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.TaskGame;
import com.github.cinnamondev.lifeSeries.util.UtilityComponents;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public enum TaskDifficulty {
    EASY,
    MEDIUM,
    HARD,
    EXTREME; // TODO: future extreme tasks. i.e. fight the dragon, kill a warden, stupid things like that.

    public static Optional<TaskDifficulty> tryFromString(String string) {
        return switch (string.toUpperCase()) {
            case "EASY" -> Optional.of(TaskDifficulty.EASY);
            case "MEDIUM" -> Optional.of(TaskDifficulty.MEDIUM);
            case "HARD" -> Optional.of(TaskDifficulty.HARD);
            case "EXTREME" -> Optional.of(TaskDifficulty.EXTREME);
            default -> Optional.empty();
        };
    }

    public TaskGame.RollMode toRollMode() {
        return switch (this) {
            case EASY -> TaskGame.RollMode.EASY;
            case MEDIUM -> TaskGame.RollMode.MEDIUM;
            case HARD -> TaskGame.RollMode.HARD;
            default -> null;
        };
    }
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

    public final static class DifficultyArgumentType implements CustomArgumentType.Converted<TaskDifficulty, String> {
        private static final DynamicCommandExceptionType ERROR_NOT_DIFFICULTY = new DynamicCommandExceptionType(name ->
                MessageComponentSerializer.message().serialize(Component.text(name + " is not a valid difficulty."))
        );

        @Override
        public TaskDifficulty convert(String nativeType) throws CommandSyntaxException {
            return switch (nativeType.toLowerCase()) {
                case "easy" -> TaskDifficulty.EASY;
                case "normal" -> TaskDifficulty.MEDIUM;
                case "hard" -> TaskDifficulty.HARD;
                case "extreme" -> TaskDifficulty.EXTREME;
                default -> throw ERROR_NOT_DIFFICULTY.create(nativeType);
            };
        }

        @Override
        public ArgumentType<String> getNativeType() {
            return StringArgumentType.word();
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            Stream.of("easy", "normal", "hard", "extreme")
                    .filter(difficulty -> difficulty.startsWith(builder.getRemainingLowerCase()))
                    .forEach(builder::suggest);

            return builder.buildFuture();
        }
    }
}