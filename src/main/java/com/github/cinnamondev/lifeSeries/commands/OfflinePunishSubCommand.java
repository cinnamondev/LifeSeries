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
import java.util.stream.Collectors;

public class OfflinePunishSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command(LifeSeries p) {
        return Commands.literal("punishOffline")
                .requires(src -> src.getSender().hasPermission("life.admin.game"))
                .executes(ctx -> {
                    List<UUID> onlineUUIDs = p.getServer().getOnlinePlayers().stream().map(Entity::getUniqueId)
                            .toList();
                    p.getScoreHandler().listTrackedUUIDs().stream().filter(uuid -> !onlineUUIDs.contains(uuid))
                            .forEach(uuid -> p.getScoreHandler().updatePlayerScoreAndTeam(uuid, (_uuid, score) ->
                                    score - p.getConfig().getInt("options.punishment.offline")
                            ));
                    return 1;
                });
    }
}
