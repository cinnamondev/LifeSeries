package com.github.cinnamondev.lifeSeries.commands.AdminSubCommands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class SessionControlSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command(LifeSeries p) {
        return Commands.literal("session")
                .requires(src -> src.getSender().hasPermission("life.admin.session"))
                .then(Commands.literal("start").executes(ctx -> {
                    p.startSession();
                    return 1;
                }))
                .then(Commands.literal("stop")
                        .executes(ctx -> { p.stopSession(); return 1;}))
                .then(Commands.literal("end")
                        .executes(ctx -> {
                            p.endOfSession();
                            sessionEndAction(p,0);
                            return 1;
                        })
                        .then(Commands.argument("delay", ArgumentTypes.time()).executes(ctx -> 1))
                )
                .then(Commands.literal("pause").executes(ctx -> {
                    p.pauseSession();
                    return 1;
                }))
                .then(Commands.literal("save").executes(ctx -> {
                    p.saveGame();
                    return 1;
                }));
    }

    private static int sessionEndAction(LifeSeries p, int delay) {
        if (delay >= 10*20) {
            p.getServer().getScheduler().runTaskLater(p, () -> p.getServer().sendMessage(
                    Component.translatable("session-command.ten-second-warning")
                            .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD))
            ), delay - 200);
        }
        if (delay >= TimeUnit.MINUTES.toSeconds(5) * 20) {
            p.getServer().getScheduler().runTaskLater(p, () -> p.getServer().sendMessage(
                    Component.translatable("session-command.five-minute-warning")
                            .style(Style.style(NamedTextColor.YELLOW, TextDecoration.BOLD))
            ), delay - (TimeUnit.MINUTES.toSeconds(5) * 20));
        }
        if (delay >= TimeUnit.MINUTES.toSeconds(15) * 20) {
            p.getServer().getScheduler().runTaskLater(p, () ->
                p.getServer().sendMessage(
                        Component.translatable("session-command.fifteen-minute-warning")
                                .style(Style.style(NamedTextColor.GREEN, TextDecoration.BOLD))
                ), delay - (TimeUnit.MINUTES.toSeconds(15) * 20));
        }

        p.getServer().getScheduler().runTaskLater(p, () -> {
            if (p.getConfig().getBoolean("options.end-of-session.kick-at-end", false)) {
                String server = p.getConfig().getString("options.end-of-session.send-to-server", "null");
                if (server.equalsIgnoreCase("null")) {
                    p.getServer().getOnlinePlayers().stream().filter(player -> !player.hasPermission("life.bypass.kick"))
                            .forEach(Player::kick);
                } else { sendAllPlayersToConfiguredServer(p, server); }
            }
        }, delay);
        return 1;
    }

    /// staggered send all players
    private static void sendAllPlayersToConfiguredServer(LifeSeries p, String configuredServerName) {
        int tickOffset = 0;
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(configuredServerName);
        for (var player: p.getServer().getOnlinePlayers()) {
            if (player.hasPermission("life.bypass.kick")) { continue; }
            p.getServer().getScheduler().runTaskLater(p, () ->
                    player.sendPluginMessage(p, "BungeeCord", out.toByteArray()),
                    tickOffset++
            );
        }
    }
}
