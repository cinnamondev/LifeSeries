package com.github.cinnamondev.lifeSeries.commands.AdminSubCommands;

import com.github.cinnamondev.lifeSeries.revival.RevivalItem;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class RevivalSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command(RevivalItem revivalItem) {
        return Commands.literal("revival")
                .requires(src -> (src.getSender() instanceof Player p) && p.hasPermission("life.admin.revival"))
                .executes(ctx -> {
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
