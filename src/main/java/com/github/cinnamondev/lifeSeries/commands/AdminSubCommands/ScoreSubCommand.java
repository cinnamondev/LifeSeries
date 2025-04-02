package com.github.cinnamondev.lifeSeries.commands.AdminSubCommands;

import com.github.cinnamondev.lifeSeries.teams.ScoreHandler;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
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

    public static LiteralArgumentBuilder<CommandSourceStack> command(ScoreHandler scoreHandler) {
        return Commands.literal("score")
                .then(Commands.argument("player", ArgumentTypes.players())
                        .then(Commands.literal("modify").then(Commands.argument("score", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                            .resolve(ctx.getSource()).forEach(player -> modifyPlayerScore(
                                                    scoreHandler,
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
                                                    scoreHandler,
                                                    ctx.getSource().getSender(),
                                                    player,
                                                    ctx.getArgument("score", Integer.class)
                                            ));
                                    return 1;
                                })
                        ))
                        .then(Commands.literal("get").executes(ctx -> {
                            ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                    .resolve(ctx.getSource()).forEach(player -> {
                                        TeamMeta playerTeam = scoreHandler.getTeam(player);
                                        int playerScore = scoreHandler.getScore(player);
                                        ctx.getSource().getSender().sendMessage(
                                                Component.translatable("score-command.get-score",
                                                        player.displayName(),
                                                        Component.text(playerScore).color(playerTeam.getColor())
                                                )
                                        );
                                    });
                            return 1;
                        }))
                )
                .then(Commands.literal("untracked")
                        .then(Commands.literal("modify").then(Commands.argument("score", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    modifyUntrackedScore(
                                            scoreHandler,
                                            ctx.getSource().getSender(),
                                            ctx.getArgument("score", Integer.class)
                                    );
                                    return 1;
                                })
                        ))
                        .then(Commands.literal("set").then(Commands.argument("score", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    setUntrackedScore(
                                            scoreHandler,
                                            ctx.getSource().getSender(),
                                            ctx.getArgument("score", Integer.class)
                                    );
                                    return 1;
                                })
                        ))
                        .then(Commands.literal("get").executes(ctx -> {
                            int untrackedScore = scoreHandler.getUntrackedScore();
                            TeamMeta untrackedTeam = scoreHandler.getTeam(untrackedScore);
                            ctx.getSource().getSender().sendMessage(
                                    Component.translatable("score-command.get-score",
                                            Component.translatable("score-command.updated-score")
                                                    .color(untrackedTeam.getColor()),
                                            Component.text(untrackedScore).color(untrackedTeam.getColor())
                                    )
                            );
                            return 1;
                        }))
                );
    }

    private static void modifyUntrackedScore(ScoreHandler sh, CommandSender sender, int deltaSeconds) {
        setUntrackedScore(sh, sender, sh.getUntrackedScore() + deltaSeconds);
    }

    private static void setUntrackedScore(ScoreHandler sh, CommandSender sender, int score) {
        int oldScore = sh.getUntrackedScore();
        sh.setUntrackedScore(score);
        int newScore = sh.getUntrackedScore();
        TextColor newScoreColor = sh.getTeam(newScore).getColor();

        sender.sendMessage(Component.translatable("score-command.updated-score",
                Component.translatable("update-score-commands.untracked-name").color(newScoreColor),
                Component.text(newScore).color(newScoreColor),
                Component.text(oldScore).color(sh.getTeam(oldScore).getColor())
        ));
    }

    private static void modifyPlayerScore(ScoreHandler sh, CommandSender sender, Player player, int deltaSeconds) {
        setPlayerScore(
                sh,
                sender,
                player,
                sh.getScore(player) + deltaSeconds
        );
    }

    private static void setPlayerScore(ScoreHandler sh, CommandSender sender, Player player, int newScore) {
        int oldScore = sh.getScore(player);
        Component oldScoreText = Component.text(oldScore).color(sh.getTeam(oldScore).getColor());

        sh.updatePlayerScoreAndTeam(player, (uuid, score) -> newScore);

        int score = sh.getScore(player);
        Component newScoreText = Component.text(score).color(sh.getTeam(score).getColor());

        sender.sendMessage(Component.translatable("score-command.updated-score",
                player.displayName(),
                newScoreText,
                oldScoreText
        ));
    }
}
