package com.github.cinnamondev.lifeSeries.commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.BoogeymanGame;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class GameControlCommand {
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

        if (p.getGame() instanceof BoogeymanGame game) {
            return command.then(BoogeymanSubCommand.boogeyman(p, game)).build();
        }
        return command.build();
    }

    private static int helpCommand(CommandSender sender) {
        // TODO: NOT IMPLEMENTED
        sender.sendMessage(Component.text(description));
        return 1;
    }
}
