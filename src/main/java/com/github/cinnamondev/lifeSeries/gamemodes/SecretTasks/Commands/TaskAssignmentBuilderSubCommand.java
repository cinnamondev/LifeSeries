package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;
import com.github.cinnamondev.lifeSeries.util.TrackableSpawnEggs;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collection;

public class TaskAssignmentBuilderSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command(LifeSeries p, Collection<PlayerTask> tasks) {
        return Commands.literal("task")
                .requires(src -> src.getSender().hasPermission("life.admin.game"))
                .then(Commands.argument("players", ArgumentTypes.players()))
                .then(Commands.literal("fail"))
                .then(Commands.literal("win"))
                .then(Commands.literal("assign"))
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
