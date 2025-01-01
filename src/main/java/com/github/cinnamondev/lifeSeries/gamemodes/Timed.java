package com.github.cinnamondev.lifeSeries.gamemodes;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Timed extends BoogeymanGame {
    private final LifeSeries p;
    private final int timerDecrementQuantity;

    public Timed(LifeSeries p, int callIntervalTicks) {
        super(p);
        timerDecrementQuantity = callIntervalTicks / 20; // number of seconds to decremnt by (expect this is to be n*20)
        this.p = p;
    }

    /// tick the game.
    @Override
    public void run() {
        // decrement def
        p.getScoreboardHandler().addUntrackedPlayerScore(-1 * timerDecrementQuantity);

        p.getServer().getOnlinePlayers().forEach(p.getScoreboardHandler()::getScore);
        p.getScoreboardHandler().updateAllTrackedScoresAndTeams((uuid, score) -> score - timerDecrementQuantity)
                .forEach(deadPlayer -> deadPlayer.setHealth(0)); // kill all the reported dead players.

    }

// NOTE TO SELF: REMOVE PAUSE METHOD FROM GAME, PAUSE WILL BE CONSIDERED THE SAME AS STOP, EXCEPT SAVE FILE OPTION
// "paused: true" WILL BE WRITTEN!!!!!!!!! WHEN GAME RESUMES THIS WILL BE SET TO FALSE BUT ADDITIONAL STATISTICS KEPT
// IN THE SAVE (i.e. who was boogey) WILL BE USED TO SET THE GAME BACK TO ITS ORIGINAL STATE.

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, Integer> addScoreSubCommand(LifeSeries p) {

        return Commands.argument("time", ArgumentTypes.time())
                .then(Commands.literal("+").executes(ctx -> addScoreProcessor(
                        ctx.getSource().getSender(),
                        ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                .resolve(ctx.getSource())
                                .getFirst(),
                        ctx.getArgument("time", Integer.class)
                ))).then(Commands.literal("-").executes(ctx -> addScoreProcessor(
                        ctx.getSource().getSender(),
                        ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                .resolve(ctx.getSource())
                                .getFirst(),
                        -1 * ctx.getArgument("time", Integer.class)
                )));
    }

    private int addScoreProcessor(CommandSender sender, Player player, int scoreDelta) {
        var oldScore = p.getScoreboardHandler().getScore(player);
        p.getScoreboardHandler().addScore(
                player,
                scoreDelta/20,
                true
        );
        sender.sendMessage(Component.text("Old time: ")
                .append(Component.text(oldScore).style(p.getScoreboardHandler().getTeam(oldScore).style()))
                .append(Component.text(" New time: "))
                .append(Component.text(Math.max(oldScore + (scoreDelta/20), 0))
                        .style(p.getScoreboardHandler().getTeam(player).style())
                )
        );
        return 1;
    }
}