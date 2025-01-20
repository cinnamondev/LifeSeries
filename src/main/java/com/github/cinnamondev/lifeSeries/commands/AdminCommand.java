package com.github.cinnamondev.lifeSeries.commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.commands.AdminSubCommands.*;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class AdminCommand {
    public final static List<String> aliases = List.of("lf");
    public final static String description = "Generic Life Series :)";
    public static LiteralCommandNode<CommandSourceStack> command(LifeSeries p) {
        var command = Commands.literal("life")
                .requires(src -> src.getSender().hasPermission("life.admin"))
                .then(SessionControlSubCommand.command(p))
                .then(ScoreSubCommand.command(p))
                .then(OfflinePunishSubCommand.command(p))
                .then(Commands.literal("help")
                        .executes(ctx -> helpCommand(ctx.getSource().getSender())))
                .executes(ctx -> helpCommand(ctx.getSource().getSender()));

        for (var subCommand : p.getGame().adminSubCommands(p)) {
            command = command.then(subCommand);
        }

        if (p.getRevivalItem() != null) {
            p.getLogger().info("revival item is availalbwe");
            command = command.then(RevivalSubCommand.command(p.getRevivalItem()));
        }

        return command.build();
    }

    private static int helpCommand(CommandSender sender) {
        // TODO: NOT IMPLEMENTED
        sender.sendMessage(Component.text(description));
        return 1;
    }
}
