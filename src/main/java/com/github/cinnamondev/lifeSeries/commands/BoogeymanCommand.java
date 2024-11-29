package com.github.cinnamondev.lifeSeries.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

public final class BoogeymanCommand {
    public final static List<String> aliases = List.of("boogeyman", "boogey");
    public final static String description = "Reports if player is the boogeyman";
    public static LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("amitheboogeyman")
                .requires(src -> (src.getSender() instanceof Player p) && p.hasPermission("life.boogeyman"))
                .executes(ctx -> {
                    // TODO: NOT IMPLEMENTED
                    ctx.getSource().getSender().sendMessage(Component.text("Not Implemented!"));
                    return 1;
                })
                .build();
    }
}
