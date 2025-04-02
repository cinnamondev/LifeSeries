package com.github.cinnamondev.lifeSeries.gamemodes.Boogeyman.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.Boogeyman.Boogeyman;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import com.github.cinnamondev.lifeSeries.util.TitleCountdown;
import com.google.common.util.concurrent.Runnables;
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
import net.kyori.adventure.title.Title;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class BoogeymanSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> boogeyman(LifeSeries p, Boogeyman boogeymanGame) {
        return Commands.literal("boogeyman")
                .requires(src -> src.getSender().hasPermission("life.admin.game"))
                .then(Commands.literal("whois")
                        .executes(ctx -> {
                            // create stylized text for each player (their team colour) and fold into one big message :)
                            Component message = boogeymanGame.getBoogeyList().stream()
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
                                    .forEach(boogeymanGame::addBoogeyman);
                            return 1;
                        })
                ))
                .then(Commands.literal("cure").then(Commands.argument("player", ArgumentTypes.player())
                        .executes(ctx -> {
                            ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                    .resolve(ctx.getSource())
                                    .forEach(boogeymanGame::cureBoogeyman);
                            return 1;
                        })
                ));
    }

    public static int runRollTask(LifeSeries p, Boogeyman boogey, CommandSender src, int ticks, int min, int max) {
        if (ticks >= TimeUnit.MINUTES.toSeconds(1) * 20) {
            TextComponent message = Component.text("The boogeyman will be chosen in " + TimeUnit.SECONDS.toMinutes(ticks / 20) + " minutes!").color(NamedTextColor.RED);
            p.getServer().showTitle(Title.title(
                    message,
                    Component.empty()
            ));
            p.getServer().sendMessage(message);
        }
        p.getServer().getScheduler().runTaskLater(p, () -> {
            if (min > max) { src.sendMessage(Component.text("cannot roll, boogey max < min!")); return; }
            if (max == 0) {
                src.sendMessage(Component.text(" cannot roll for 0 boogies! (perhaps disable boogey instead"));
                return;
            }
            boolean res = boogey.rollBoogeyman(min, max);
            if (!res) { src.sendMessage(Component.text("not enough candidates")); } else {
                Sound tickSound = Sound.sound(Key.key("block.note_block.hat"), Sound.Source.AMBIENT, 1f,1f);

                p.getServer().sendMessage(Component.text("The boogeyman is being chosen..."));

                TitleCountdown.showSequencedTitles(p, p.getServer(), List.of(
                                Title.title(Component.text("3").color(NamedTextColor.GREEN), Component.empty())),
                        Sound.sound(NamespacedKey.minecraft("block.metal_pressure_plate.click_on"), Sound.Source.MASTER, 0.8f, 1),
                        60, 60, () -> p.getServer().getOnlinePlayers().forEach(onlinePlayer -> {
                            boolean isBoogey = boogey.isBoogeyman(onlinePlayer);
                            TitleCountdown.showSequencedTitles(p, onlinePlayer, List.of(
                                    Title.title(Component.text("You are...").color(NamedTextColor.RED), Component.empty()),
                                    Title.title(
                                            isBoogey ? Component.text("THE boogeyman!").color(NamedTextColor.RED)
                                                    : Component.text("NOT the boogeyman").color(NamedTextColor.GREEN),
                                            Component.empty()
                                    )),
                                    List.of(
                                            Sound.sound(NamespacedKey.minecraft("event.mob-effect.trial-omen"), Sound.Source.MASTER, 1, 1),
                                            isBoogey ? Sound.sound(NamespacedKey.minecraft("entity.ender_dragon.growl"), Sound.Source.MASTER, 1, 1)
                                                    : Sound.sound(NamespacedKey.minecraft("block.bell.resonate"), Sound.Source.MASTER, 1, 1)
                                    ), 1, 180, Runnables.doNothing());
                        })
                );
            }
        }, ticks);
        return 1;
    }
}
