package com.github.cinnamondev.lifeSeries.gamemodes.Timed;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import com.github.cinnamondev.lifeSeries.util.TickTimeUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

public class TimeSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command(LifeSeries p) {
        return Commands.literal("time")
                .then(Commands.argument("player", ArgumentTypes.players())
                        .then(Commands.literal("modify").then(Commands.argument("time", ArgumentTypes.time())
                                .then(Commands.literal("add").executes(ctx -> {
                                    ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                            .resolve(ctx.getSource()).forEach(player -> modifyPlayerScore(
                                                    p,
                                                    ctx.getSource().getSender(),
                                                    player,
                                                    ctx.getArgument("time", Integer.class)/20,
                                                    true
                                            ));
                                    return 1;
                                }))
                                .then(Commands.literal("subtract").executes(ctx -> {
                                    ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                            .resolve(ctx.getSource()).forEach(player -> modifyPlayerScore(
                                                    p,
                                                    ctx.getSource().getSender(),
                                                    player,
                                                    -1 * (ctx.getArgument("time", Integer.class)/20),
                                                    true
                                            ));
                                    return 1;
                                }))
                        ))
                        .then(Commands.literal("set").then(Commands.argument("time", ArgumentTypes.time())
                                .executes(ctx -> {
                                    ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                            .resolve(ctx.getSource()).forEach(player -> setPlayerScore(
                                                    p,
                                                    ctx.getSource().getSender(),
                                                    player,
                                                    ctx.getArgument("time", Integer.class) / 20,
                                                    true
                                            ));
                                    return 1;
                                })
                        ))
                        .then(Commands.literal("get").executes(ctx -> {
                            ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                    .resolve(ctx.getSource()).forEach(player -> ctx.getSource().getSender().sendMessage(
                                            Component.translatable("time-command.get-score",
                                                    player.displayName(),
                                                    TickTimeUtils.playerTime(
                                                            p.getScoreHandler().getScore(player),
                                                            TimeUnit.SECONDS,
                                                            p.getScoreHandler().getTeam(player).getColor()
                                                    )
                                            )
                                    ));
                            return 1;
                        }))
                )
                .then(Commands.literal("untracked")
                        .then(Commands.literal("modify").then(Commands.argument("time", ArgumentTypes.time())
                                .then(Commands.literal("add").executes(ctx -> {
                                    modifyUntrackedScore(
                                            p,
                                            ctx.getSource().getSender(),
                                            ctx.getArgument("time", Integer.class)/20
                                    );
                                    return 1;
                                }))
                                .then(Commands.literal("subtract").executes(ctx -> {
                                    modifyUntrackedScore(
                                            p,
                                            ctx.getSource().getSender(),
                                            -1 * (ctx.getArgument("time", Integer.class)/20)
                                    );
                                    return 1;
                                }))
                        ))
                        .then(Commands.literal("set").then(Commands.argument("time", ArgumentTypes.time())
                                .executes(ctx -> {
                                    setUntrackedScore(
                                            p,
                                            ctx.getSource().getSender(),
                                            ctx.getArgument("time", Integer.class)/20
                                    );
                                    return 1;
                                })
                        ))
                        .then(Commands.literal("get").executes(ctx -> {
                            int untrackedScore = p.getScoreHandler().getUntrackedScore();
                            TeamMeta untrackedTeam = p.getScoreHandler().getTeam(untrackedScore);
                            ctx.getSource().getSender().sendMessage(Component.translatable("time-command.get-score",
                                    Component.translatable("update-score-commands.untracked-name"),
                                    TickTimeUtils.playerTime(untrackedScore, TimeUnit.SECONDS, untrackedTeam.getColor())
                            ));
                            return 1;
                        }))
                );
    }

    private static void modifyUntrackedScore(LifeSeries p, CommandSender sender, int deltaSeconds) {
        setUntrackedScore(p, sender, p.getScoreHandler().getUntrackedScore() + deltaSeconds);
    }

    private static void setUntrackedScore(LifeSeries p, CommandSender sender, int seconds) {
        int oldScore = p.getScoreHandler().getUntrackedScore();
        Component oldTime = TickTimeUtils.playerTime(
                oldScore,
                TimeUnit.SECONDS,
                p.getScoreHandler().getTeam(oldScore).getColor()
        );

        p.getScoreHandler().setUntrackedScore(seconds);

        int newScore = p.getScoreHandler().getUntrackedScore();
        TextColor newScoreColor = p.getScoreHandler().getTeam(newScore).getColor();
        Component newTime = TickTimeUtils.playerTime(
                newScore,
                TimeUnit.SECONDS,
                newScoreColor
        );

        sender.sendMessage(Component.translatable("time-command.updated-score",
                Component.translatable("update-score-commands.untracked-name").color(newScoreColor),
                newTime,
                oldTime
        ));
    }

    private static void modifyPlayerScore(LifeSeries p, CommandSender sender, Player player, int deltaSeconds, boolean tellPlayer) {
        setPlayerScore(
                p,
                sender,
                player,
                p.getScoreHandler().getScore(player) + deltaSeconds,
                tellPlayer
        );
    }

    private static void setPlayerScore(LifeSeries p, CommandSender sender, Player player, int seconds, boolean tellPlayer) {
        int oldScore = p.getScoreHandler().getScore(player);
        Component oldTime = TickTimeUtils.playerTime(
                oldScore,
                TimeUnit.SECONDS,
                p.getScoreHandler().getTeam(player).getColor()
        );

        p.getScoreHandler().updatePlayerScoreAndTeam(player, (uuid, score) -> seconds);

        int newScore = p.getScoreHandler().getScore(player);
        Component newTime = TickTimeUtils.playerTime(
                newScore,
                TimeUnit.SECONDS,
                p.getScoreHandler().getTeam(player).getColor()
        );

        sender.sendMessage(Component.translatable("time-command.updated-score",
                player.displayName(),
                newTime,
                oldTime
        ));

        if (tellPlayer) {
            player.showTitle(Title.title(
                    TickTimeUtils.playerTimeChange(Math.abs(newScore - oldScore), TimeUnit.SECONDS),
                    Component.empty()
            ));
        }
    }
}
