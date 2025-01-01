package com.github.cinnamondev.lifeSeries.commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OfflinePunishSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command(LifeSeries p) {
        return Commands.literal("punishOffline")
                .requires(src -> src.getSender().hasPermission("life.admin.game"))
                .executes(ctx -> {
                    Component message = Component.text("Punished the following players: ").appendNewline();
                    List<UUID> onlinePlayers = p.getServer().getOnlinePlayers().stream()
                            .map(Entity::getUniqueId).toList();
                    List<OfflinePlayer> punishedPlayers = new ArrayList<>();

                    p.getScoreboardHandler().updateAllTrackedScoresAndTeams((uuid, score) -> {
                        if (onlinePlayers.contains(uuid)) { return score; } // online players left unmodified.
                        punishedPlayers.add(p.getServer().getOfflinePlayer(uuid));
                        return score - p.getConfig().getInt("options.punishment.offline");
                    });

                    for (var player: punishedPlayers) {
                        String name = player.getName();
                        TeamMeta team = p.getScoreboardHandler().getTeam(player);
                        if (name == null) { name = ""; }
                        message = message
                                .append(team.decoratedString(name))
                                .append(Component.text(", "));
                    }
                    ctx.getSource().getSender().sendMessage(message);
                    return 1;
                });
    }
}
