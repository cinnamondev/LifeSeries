package com.github.cinnamondev.lifeSeries.commands.AdminSubCommands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ScoreSubCommand{

    public static LiteralArgumentBuilder<CommandSourceStack> command(LifeSeries p) {
        return Commands.literal("score")
                .then(Commands.argument("player", ArgumentTypes.players())
                        .then(Commands.literal("modify").then(Commands.argument("score", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                            .resolve(ctx.getSource()).forEach(player -> modifyPlayerScore(
                                                    p,
                                                    ctx.getSource().getSender(),
                                                    player,
                                                    ctx.getArgument("score", Integer.class)
                                            ));
                                    return 1;
                                })
                        ))
                        .then(Commands.literal("set").then(Commands.argument("score", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                            .resolve(ctx.getSource()).forEach(player -> setPlayerScore(
                                                    p,
                                                    ctx.getSource().getSender(),
                                                    player,
                                                    ctx.getArgument("score", Integer.class)
                                            ));
                                    return 1;
                                })
                        ))
                )
                .then(Commands.literal("untracked")
                        .then(Commands.literal("modify").then(Commands.argument("score", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    modifyUntrackedScore(
                                            p,
                                            ctx.getSource().getSender(),
                                            ctx.getArgument("score", Integer.class)
                                    );
                                    return 1;
                                })
                        ))
                        .then(Commands.literal("set").then(Commands.argument("score", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    setUntrackedScore(
                                            p,
                                            ctx.getSource().getSender(),
                                            ctx.getArgument("score", Integer.class)
                                    );
                                    return 1;
                                })
                        ))
                );
    }

    private static void modifyUntrackedScore(LifeSeries p, CommandSender sender, int deltaSeconds) {
        setUntrackedScore(p, sender, p.getScoreHandler().getUntrackedScore() + deltaSeconds);
    }

    private static void setUntrackedScore(LifeSeries p, CommandSender sender, int score) {
        int oldScore = p.getScoreHandler().getUntrackedScore();
        p.getScoreHandler().setUntrackedScore(score);
        int newScore = p.getScoreHandler().getUntrackedScore();
        TextColor newScoreColor = p.getScoreHandler().getTeam(newScore).getColor();

        sender.sendMessage(Component.translatable("score-command.updated-score",
                Component.translatable("update-score-commands.untracked-name").color(newScoreColor),
                Component.text(newScore).color(newScoreColor),
                Component.text(oldScore).color(p.getScoreHandler().getTeam(oldScore).getColor())
        ));
    }

    private static void modifyPlayerScore(LifeSeries p, CommandSender sender, Player player, int deltaSeconds) {
        setPlayerScore(
                p,
                sender,
                player,
                p.getScoreHandler().getScore(player) + deltaSeconds
        );
    }

    private static void setPlayerScore(LifeSeries p, CommandSender sender, Player player, int newScore) {
        int oldScore = p.getScoreHandler().getScore(player);
        Component oldScoreText = Component.text(oldScore).color(p.getScoreHandler().getTeam(oldScore).getColor());

        p.getScoreHandler().updatePlayerScoreAndTeam(player, (uuid, score) -> newScore);

        int score = p.getScoreHandler().getScore(player);
        Component newScoreText = Component.text(score).color(p.getScoreHandler().getTeam(score).getColor());

        sender.sendMessage(Component.translatable("score-command.updated-score",
                player.displayName(),
                newScoreText,
                oldScoreText
        ));
    }
}
