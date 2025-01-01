package com.github.cinnamondev.lifeSeries.commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.teams.ScoreHandler;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ScoreSubCommand{

    public static LiteralArgumentBuilder<CommandSourceStack> command(LifeSeries p) {
        return Commands.literal("score")
                .requires(src -> src.getSender().hasPermission("life.admin.game"))
                .then(Commands.argument("player", ArgumentTypes.player())
                        .then(Commands.literal("modify").then(p.getGame().addScoreSubCommand(p)))
                        .executes(ctx -> {
                            Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                    .resolve(ctx.getSource())
                                    .getFirst();
                            int score = p.getScoreboardHandler().getScore(player);
                            ctx.getSource().getSender().sendMessage(
                                    Component.text(player.getName()).style(
                                            Style.style(p.getScoreboardHandler().getTeam(score).getColor(), TextDecoration.BOLD)
                                    ).append(Component.text(" has a score of: "))
                            );
                            return 1;
                        })
                );
    }
}
