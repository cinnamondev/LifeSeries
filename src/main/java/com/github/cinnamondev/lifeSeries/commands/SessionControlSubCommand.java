package com.github.cinnamondev.lifeSeries.commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class SessionControlSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command(LifeSeries p) {
        return Commands.literal("session")
                .requires(src -> src.getSender().hasPermission("life.admin.session"))
                .then(Commands.literal("start").executes(ctx -> {
                    p.startSession();
                    return 1;
                }))
                .then(Commands.literal("stop").executes(ctx -> {
                    p.stopSession();
                    return 1;
                }))
                .then(Commands.literal("pause").executes(ctx -> {
                    p.pauseSession();
                    return 1;
                }))
                .then(Commands.literal("save").executes(ctx -> {
                    p.saveGame();
                    return 1;
                }));
    }
}
