package com.github.cinnamondev.lifeSeries.commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.BoogeymanGame;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.List;

public final class AmITheBoogeyMan {
    public final static List<String> aliases = List.of("boogeyman", "boogey");
    public final static String description = "Reports if player is the boogeyman";
    public static LiteralCommandNode<CommandSourceStack> command(LifeSeries plugin) {
        return Commands.literal("amitheboogeyman")
                .requires(src -> (src.getSender() instanceof Player p) && p.hasPermission("life.boogeyman"))
                .executes(ctx -> {
                    if (plugin.getGame() instanceof BoogeymanGame game) {
                        if (ctx.getSource().getSender() instanceof Player player) {
                            boolean isBoogey = game.isBoogeyman(player);
                            TextComponent message = isBoogey ?
                                    Component.text("You ARE the Boogeyman!")
                                            .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD))
                                    : Component.text("You are NOT the Boogeyman!")
                                    .style(Style.style(NamedTextColor.GREEN, TextDecoration.BOLD));
                            ctx.getSource().getSender().sendMessage(message);
                        }
                    } else {
                        ctx.getSource().getSender().sendMessage("This gamemode does not support the boogeyman!");
                    }
                    return 1;
                })
                .build();
    }
}
