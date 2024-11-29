package com.github.cinnamondev.lifeSeries.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class GameControlCommand {
    public final static List<String> aliases = List.of("lf");
    public final static String description = "Generic Life Series :)";
    public static LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("life")
                .requires(src -> src.getSender().hasPermission("life.admin"))
                .then(Commands.literal("session")
                        .requires(src -> src.getSender().hasPermission("life.admin.session"))
                        .executes(ctx -> { return 1; }) // TODO: NOT IMPLEMENTED
                )
                .then(Commands.literal("lives")
                        .requires(src -> src.getSender().hasPermission("life.admin.game"))
                        .executes(ctx -> { return 1; }) // TODO: NOT IMPLEMENTED
                )
                .then(Commands.literal("timer")
                        .requires(src -> src.getSender().hasPermission("life.admin.game"))
                        .executes(ctx -> { return 1; }) // TODO: NOT IMPLEMENTED
                )
                .then(Commands.literal("help")
                        .executes(ctx -> helpCommand(ctx.getSource().getSender())))
                .executes(ctx -> helpCommand(ctx.getSource().getSender()))
                .build();
    }

    private static int helpCommand(CommandSender sender) {
        // TODO: NOT IMPLEMENTED
        sender.sendMessage(Component.text(description));
        return 1;
    }
}
