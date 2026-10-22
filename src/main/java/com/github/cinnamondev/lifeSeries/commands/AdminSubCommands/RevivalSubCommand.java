package com.github.cinnamondev.lifeSeries.commands.AdminSubCommands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.revival.RevivalItem;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class RevivalSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command(LifeSeries p) {
        return Commands.literal("revival")
                .requires(src -> (src.getSender() instanceof Player player) && player.hasPermission("life.admin.revival"))
                .then(Commands.literal("limits").then(Commands.argument("player", ArgumentTypes.player())
                        .then(Commands.literal("crafts")
                                .then(Commands.literal("set").then(Commands.argument("crafts", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            int crafts = ctx.getArgument("crafts", Integer.class);
                                            ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                                    .resolve(ctx.getSource()).forEach(player -> {
                                                        ConfigurationSection s = p.getScoreHandler().getPlayerData(player);
                                                        s.set("revival.crafts", crafts);
                                                    });
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )))
                        .then(Commands.literal("uses")
                                .then(Commands.literal("set").then(Commands.argument("uses", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            int uses = ctx.getArgument("uses", Integer.class);
                                            ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                                    .resolve(ctx.getSource()).forEach(player -> {
                                                        ConfigurationSection s = p.getScoreHandler().getPlayerData(player);
                                                        s.set("revival.use", uses);
                                                    });
                                            return Command.SINGLE_SUCCESS;                                        })
                                )))

                ))
                .executes(ctx -> {
                    RevivalItem revivalItem = p.getRevivalItem();
                    Player player = (Player) ctx.getSource().getSender();
                    if (revivalItem == null) {
                        player.sendMessage(Component.translatable("revival-item.command-fail"));
                        return 0;
                    }
                    player.openInventory(
                            revivalItem.getPlayersMenu(player).getInventory()
                    );
                    return 1;
                });
    }
}
