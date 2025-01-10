package com.github.cinnamondev.lifeSeries.commands.AdminSubCommands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;

public class ScoreSubCommand{

    public static LiteralArgumentBuilder<CommandSourceStack> command(LifeSeries p) {
        return Commands.literal("score")
                .requires(src -> src.getSender().hasPermission("life.admin.game"))
                .then(Commands.argument("player", ArgumentTypes.players())
                        .executes(ctx -> {
                            ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                    .resolve(ctx.getSource()).forEach(player -> {
                                        TeamMeta playerTeam = p.getScoreHandler().getTeam(player);
                                        int score = p.getScoreHandler().getScore(player);
                                        ctx.getSource().getSender().sendMessage(
                                                playerTeam.decoratedString(player.getName())
                                                        .append(Component.text( "has a score of"))
                                                        .append(playerTeam.decoratedString(Integer.toString(score)))
                                        );
                                    });

                            return 1;
                        })
                        .then(Commands.literal("modify").then(Commands.argument("score", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    int score = ctx.getArgument("score", Integer.class);
                                    ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                            .resolve(ctx.getSource()).forEach(player -> {
                                                TeamMeta oldTeam = p.getScoreHandler().getTeam(player);
                                                int oldScore = p.getScoreHandler().getScore(player);
                                                p.getScoreHandler().updatePlayerScoreAndTeam(player, (_uuid, curr) ->
                                                        curr + score
                                                );
                                                TeamMeta newTeam = p.getScoreHandler().getTeam(player);
                                                int newScore = p.getScoreHandler().getScore(player);
                                                ctx.getSource().getSender().sendMessage(
                                                        newTeam.decoratedString(player.getName())
                                                                .append(Component.text( " now has a score of "))
                                                                .append(newTeam.decoratedString(Integer.toString(newScore)))
                                                                .append(Component.text(" (was "))
                                                                .append(oldTeam.decoratedString(Integer.toString(oldScore)))
                                                                .append(Component.text(")"))
                                                );
                                            });
                                    return 1;
                                })
                        ))
                        .then(Commands.literal("set").then(Commands.argument("score", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    int score = ctx.getArgument("score", Integer.class);
                                    ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                            .resolve(ctx.getSource()).forEach(player -> {
                                                TeamMeta oldTeam = p.getScoreHandler().getTeam(player);
                                                int oldScore = p.getScoreHandler().getScore(player);
                                                p.getScoreHandler().updatePlayerScoreAndTeam(player, (_uuid, curr) ->
                                                        score
                                                );
                                                TeamMeta newTeam = p.getScoreHandler().getTeam(player);
                                                int newScore = p.getScoreHandler().getScore(player);
                                                ctx.getSource().getSender().sendMessage(
                                                        newTeam.decoratedString(player.getName())
                                                                .append(Component.text( " now has a score of "))
                                                                .append(newTeam.decoratedString(Integer.toString(newScore)))
                                                                .append(Component.text(" (was "))
                                                                .append(oldTeam.decoratedString(Integer.toString(oldScore)))
                                                                .append(Component.text(")"))
                                                );
                                            });
                                    return 1;
                                })
                        ))
                )
                .then(Commands.literal("untracked set").then(Commands.argument("score", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                            p.getScoreHandler().setUntrackedScore(ctx.getArgument("score", Integer.class));
                            return 1;
                        }))
                );
    }

}
