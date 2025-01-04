package com.github.cinnamondev.lifeSeries.commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.BoogeymanGame;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;


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
                                        TeamMeta team = p.getScoreHandler().getTeam(player);
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
                                .then(Commands.argument("min", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("max", IntegerArgumentType.integer(1))
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

                )
                .then(Commands.literal("curse").then(Commands.argument("player", ArgumentTypes.players())
                        .executes(ctx -> {
                            ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                    .resolve(ctx.getSource())
                                    .forEach(boogeymanGame::addBoogey);
                            return 1;
                        })
                ))
                .then(Commands.literal("cure").then(Commands.argument("player", ArgumentTypes.player())
                        .executes(ctx -> {
                            ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                    .resolve(ctx.getSource())
                                    .forEach(boogeymanGame::cure);
                            return 1;
                        })
                ));
    }

    public static int runRollTask(LifeSeries p, BoogeymanGame boogey, CommandSender src, int ticks, int min, int max) {
        TextComponent message = Component.text("The boogeyman will be chosen in " + TimeUnit.SECONDS.toMinutes(ticks / 20) + " minutes!").color(NamedTextColor.RED);
        p.getServer().showTitle(Title.title(
                message,
                Component.empty()
        ));
        p.getServer().sendMessage(message);

        p.getServer().getScheduler().runTaskLater(p, () -> {
            if (min > max) { src.sendMessage(Component.text("cannot roll, boogey max < min!")); return; }
            if (max == 0) {
                src.sendMessage(Component.text(" cannot roll for 0 boogies! (perhaps disable boogey instead"));
                return;
            }
            boolean res = boogey.roll(min,max);
            if (!res) { src.sendMessage(Component.text("not enough candidates")); } else {
                Sound tickSound = Sound.sound(Key.key("block.note_block.hat"), Sound.Source.AMBIENT, 1f,1f);

                p.getServer().sendMessage(Component.text("The boogeyman is being chosen..."));
                scheduleBroadcastSingleTitleWithSound(p, 0, "3", Style.style(NamedTextColor.GREEN), tickSound);
                scheduleBroadcastSingleTitleWithSound(p, 20, "2", Style.style(NamedTextColor.GREEN), tickSound);
                scheduleBroadcastSingleTitleWithSound(p, 40, "1", Style.style(NamedTextColor.GREEN), tickSound);
                scheduleBroadcastSingleTitleWithSound(p, 60, "You are...", Style.style(NamedTextColor.GREEN), tickSound);

                p.getServer().getScheduler().runTaskLater(p, () -> {
                    p.getServer().getOnlinePlayers().forEach(onlinePlayer -> {
                        TextComponent boogeymanAnnouncement  = Component.text("NOT the boogeyman!")
                                .color(NamedTextColor.GREEN);
                        if (boogey.getBoogeymen().contains(onlinePlayer.getUniqueId())) {
                            boogeymanAnnouncement = Component.text("NOT the boogeyman!")
                                    .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD));
                        }
                        onlinePlayer.showTitle(Title.title(boogeymanAnnouncement, Component.empty()));
                    });
                }, 80);
            }
        }, ticks);
        return 1;
    }
    private static void scheduleBroadcastSingleTitleWithSound(Plugin p, int l, String titleString, Style style, Sound sound){
        p.getServer().playSound(sound);
        p.getServer().getScheduler().runTaskLater(p, () -> {
            p.getServer().showTitle(Title.title(
                    Component.text(titleString).style(style),
                    Component.empty()
            ));
        },l);
    }
}
