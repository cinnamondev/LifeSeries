package com.github.cinnamondev.lifeSeries.commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.BoogeymanGame;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class BoogeymanSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> boogeyman(LifeSeries p, BoogeymanGame boogeymanGame) {
        return Commands.literal("boogeyman")
                .requires(src -> src.getSender().hasPermission("life.admin.game"))
                .then(Commands.literal("whois")
                        .executes(ctx -> {
                            // create stylized text for each player (their team colour) and fold into one big message :)
                            Component message = boogeymanGame.getBoogeymen().stream()
                                    .map((uuid) -> {
                                        OfflinePlayer player =  p.getServer().getOfflinePlayer(uuid);
                                        String name = player.getName();
                                        if (name == null) { name = ""; }
                                        TeamMeta team = null; //p.getScoreboardHandler().getTeam(player);
                                        return team.decoratedString(name).append(
                                                Component.text(", ")
                                        );
                                    }).reduce(Component.text("Boogeys: "), Component::append);

                            ctx.getSource().getSender().sendMessage(message);
                            return 1;
                        })
                )
                .then(Commands.literal("roll")
                        .executes(ctx -> runRollTask( // execute w defaults
                                p,
                                boogeymanGame,
                                ctx.getSource().getSender(),
                                0,
                                p.getConfig().getInt("options.boogeyman.min"),
                                p.getConfig().getInt("options.boogeyman.max")
                        ))
                        .then(Commands.argument("delay", ArgumentTypes.time(0))
                                .executes(ctx -> runRollTask( // execute w delay
                                        p,
                                        boogeymanGame,
                                        ctx.getSource().getSender(),
                                        ctx.getArgument("delay", Integer.class),
                                        p.getConfig().getInt("options.boogeyman.min"),
                                        p.getConfig().getInt("options.boogeyman.max")
                                ))
                        )
                        .then(Commands.argument("min", ArgumentTypes.integerRange())
                                .then(Commands.argument("max", ArgumentTypes.integerRange())
                                        .executes(ctx -> runRollTask( // execute w delay/min/max
                                                p,
                                                boogeymanGame,
                                                ctx.getSource().getSender(),
                                                ctx.getArgument("delay", Integer.class),
                                                ctx.getArgument("min", Integer.class),
                                                ctx.getArgument("max", Integer.class)
                                        ))
                        ))

                )
                .then(Commands.literal("cure").then(Commands.argument("player", ArgumentTypes.player())
                        .executes(ctx -> {
                            Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                    .resolve(ctx.getSource())
                                    .getFirst();
                            boogeymanGame.cure(player);
                            return 1;
                        })
                ));
    }

    public static int runRollTask(LifeSeries p, BoogeymanGame boogey, CommandSender src, int ticks, int min, int max) {
        p.getServer().getScheduler().runTaskLater(p, () -> {
            if (max > min) { src.sendMessage(Component.text("cannot roll, boogey max > min!")); return; }
            if (max == 0) {
                src.sendMessage(Component.text(" cannot roll for 0 boogies! (perhaps disable boogey instead"));
                return;
            }
            boolean res = boogey.roll(min,max);
            if (!res) { src.sendMessage(Component.text("not enough candidates")); }
        }, ticks);
        return 1;
    }
}
