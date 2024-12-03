package com.github.cinnamondev.lifeSeries.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.List;

public final class GameControlCommand {
    public final static List<String> aliases = List.of("lf");
    public final static String description = "Generic Life Series :)";
    public static LiteralCommandNode<CommandSourceStack> command(Plugin p) {
        return Commands.literal("life")
                .requires(src -> src.getSender().hasPermission("life.admin"))
                .then(Commands.literal("session")
                        .requires(src -> src.getSender().hasPermission("life.admin.session"))
                        .executes(ctx -> { return 1; }) // TODO: NOT IMPLEMENTED
                )
                .then(Commands.literal("lives")
                        .requires(src -> src.getSender().hasPermission("life.admin.game"))
                        .requires(src -> p.getConfig().getString("mode").equals("lives"))
                        .executes(ctx -> { return 1; }) // TODO: NOT IMPLEMENTED
                        .then(Commands.argument("player", ArgumentTypes.player()))
                )
                .then(Commands.literal("timer")
                        .requires(src -> src.getSender().hasPermission("life.admin.game"))
                        .requires(src -> p.getConfig().getString("mode").equals("timed"))
                        .executes(ctx -> { return 1; }) // TODO: NOT IMPLEMENTED
                )
                .then(Commands.literal("punishOffline")
                        .requires(src -> src.getSender().hasPermission("life.admin.game"))
                )
                .then(Commands.literal("boogeyman")
                        .requires(src -> src.getSender().hasPermission("life.admin.game"))
                        .then(Commands.literal("whois")
                                .executes(ctx -> { return 1; }) // TODO: NOT IMPLEMENTED
                        )
                        .then(Commands.literal("roll")
                                .then(Commands.argument("player", ArgumentTypes.player())
                                        .executes(ctx -> { return 1; }) // TODO: NOT IMPLEMENTED
                                )
                        )
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
