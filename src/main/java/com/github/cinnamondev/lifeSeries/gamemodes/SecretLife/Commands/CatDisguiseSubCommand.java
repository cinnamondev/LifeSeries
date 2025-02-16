package com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Task.PlayerTask;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import me.libraryaddict.disguise.LibsDisguises;

import java.util.function.Consumer;

public class CatDisguiseSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command(LifeSeries p, Consumer<PlayerTask> onTaskAdded, Consumer<PlayerTask> onTaskCompletion) {
        return Commands.literal("meowmeowmeowmeow")
                .then(Commands.literal("enable")
                        .executes(ctx -> {

                            return 1;
                        })
                        .then(Commands.argument("player", ArgumentTypes.player())
                                .executes(ctx -> {
                                    return 1;
                                }))
                )
                .then(Commands.literal("disable")
                        .executes(ctx -> {
                            return 1;
                        })
                        .then(Commands.argument("player", ArgumentTypes.player())
                                .executes(ctx -> {
                                    return 1;
                                }))
                );
    }
}
