package com.github.cinnamondev.lifeSeries.gamemodes.BoogeymanFeature;


import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public final class AmITheBoogeyMan implements CommandContainer.FilledLiteralCommand {
    public final Boogeyman boogeymanGame;
    public final LifeSeries p;

    public AmITheBoogeyMan(Boogeyman boogeymanGame, LifeSeries p) {
        this.boogeymanGame = boogeymanGame;
        this.p = p;
    }
    @Override
    public List<String> getAliases() {
        return List.of("aib", "boogey", "amiboogey", "boogeyman");
    }

    @Override
    public String getDescription() {
        return "Tells the player if they are the boogeyman";
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("amitheboogeyman")
                .requires(src -> (src.getSender() instanceof Player player) && player.hasPermission("life.boogeyman"))
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        ctx.getSource().getSender().sendMessage(
                                boogeymanGame.isBoogeyman(player) ?
                                        Component.translatable("boogeyman.is-boogey")
                                                .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD))
                                        : Component.translatable("boogeyman.not-boogey")
                                        .style(Style.style(NamedTextColor.GREEN, TextDecoration.BOLD))
                        );
                    }
                    return 1;
                })
                .build();
    }
}
