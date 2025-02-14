package com.github.cinnamondev.lifeSeries.commands.AdminSubCommands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.util.TrackableSpawnEggs;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class GiveSpawnEggSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command(LifeSeries p) {
        return Commands.literal("giveSpawnEgg")
                .requires(src -> src.getSender().hasPermission("life.admin.spawnEgg"))
                .requires(src -> src.getSender() instanceof Player)
                .then(Commands.argument("entity", StringArgumentType.word()))
                .executes(ctx -> {
                    try {
                        EntityType type = EntityType.valueOf(
                                StringArgumentType.getString(ctx, "entity").toUpperCase()
                        );
                        TrackableSpawnEggs.tryGetTrackableSpawnEgg(p, type, 1).ifPresentOrElse((egg) ->
                                ((Player) ctx.getSource().getSender()).give(egg),
                                () -> ctx.getSource().getSender().sendMessage(
                                        Component.text("cant get that egg!!!!")
                                )
                        );

                    } catch (IllegalArgumentException e) {
                        ctx.getSource().getSender().sendMessage(Component.text("invalid entitytype"));
                    }
                    return 1;
                });
    }
}
