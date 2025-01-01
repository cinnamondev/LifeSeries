package com.github.cinnamondev.lifeSeries.gamemodes;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.function.Consumer;

public interface Game extends Runnable {
    /// called when a session is resumed (paused: true is present in savefile). pull data from player data in save file
    /// that you may require to bring the game back up to its previous state. i.e. who was boogeyman.
    public default void configureFromResumedSave(ConfigurationSection saveSection) {
        // do nothing for now...
    }
    public default void resetPerSessionData(ConfigurationSection saveSection) {
        // do nothing for now...
    }
    public default void onKilled(LifeSeries p, Player killed, int punishment) {
        boolean isFinalDeath = p.getScoreboardHandler().addScore(
                killed,
                punishment,
                true
        );

        if (isFinalDeath) {
            p.getServer().showTitle(Title.title(
                    Component.text(killed.getName() + " ran out of time!").color(NamedTextColor.RED),
                    Component.text("... by succumbing to nature.").color(NamedTextColor.RED)
            ));
        }
    }
    public default void onKilled(LifeSeries p, Player killed) {
        onKilled(p, killed,-1 * p.getConfig().getInt("options.punishment.death"));
    }
    public default void onKilled(LifeSeries p, Player killed, Player killer) {
        onKilled(p,
                killed,-1 * p.getConfig().getInt("options.punishment.death"),
                killer, p.getConfig().getInt("options.reward.kill")
        );
    }
    public default void onKilled(LifeSeries p, Player killed, int punishment, Player killer, int reward) {
        if (killed.getUniqueId().equals(killer.getUniqueId())) { onKilled(p, killed); return; } // death type suicide

        boolean isFinalDeath = false;
        TeamMeta killerTeam = p.getScoreboardHandler().getTeam(killer);
        boolean canKillTeam = killerTeam.getCanKill().contains(p.getScoreboardHandler().getTeam(killed).getName());

        if (canKillTeam || !p.getConfig().getBoolean("options.punishment.disable-on-disallowed-team", false)) {
            isFinalDeath = p.getScoreboardHandler().addScore(killed, punishment, true);
        }
        if (canKillTeam || !p.getConfig().getBoolean("options.reward.disable-on-disallowed-team", false)) {
            p.getScoreboardHandler().addScore(killer, reward, true); // discard if 'dead', wouldnt make sense!
        }

        if (isFinalDeath) {
            p.getServer().showTitle(Title.title(
                    Component.text(killed.getName() + " ran out of time!").color(NamedTextColor.RED),
                    Component.text("... after being killed by ").color(NamedTextColor.RED).append(
                            Component.text(killer.getName()).style(killerTeam.style())
                    )
            ));
        }
    }
    public default RequiredArgumentBuilder<CommandSourceStack, Integer> addScoreSubCommand(LifeSeries p) {
        return Commands.argument("score", IntegerArgumentType.integer())
                .executes(ctx -> {
                    Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                            .resolve(ctx.getSource())
                            .getFirst();
                    int scoreDelta = ctx.getArgument("score", Integer.class);
                    var oldScore = p.getScoreboardHandler().getScore(player);
                    p.getScoreboardHandler().addScore(
                            player,
                            scoreDelta,
                            true
                    );
                    ctx.getSource().getSender().sendMessage(Component.text("Old time: ")
                            .append(Component.text(oldScore).style(p.getScoreboardHandler().getTeam(oldScore).style()))
                            .append(Component.text(" New time: "))
                            .append(Component.text(Math.max(oldScore + scoreDelta, 0))
                                    .style(p.getScoreboardHandler().getTeam(player).style())
                            )
                    );
                    return 1;
                });
    }
}