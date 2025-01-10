package com.github.cinnamondev.lifeSeries.commands.AdminSubCommands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.UUID;

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
