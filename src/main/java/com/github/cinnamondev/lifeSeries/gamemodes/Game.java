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

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.function.Consumer;

public interface Game extends Runnable {
    public default void onKilled(LifeSeries p, Player killed, int punishment) {}
    public default void onKilled(LifeSeries p, Player killed) {
        onKilled(p, killed, 1);
    }
    public default void onKilled(LifeSeries p, Player killed, int punishment, Player killer, int reward) {

    }
    public default void onKilled(LifeSeries p, Player killed, Player killer) {
        onKilled(p, killed, 1, killer, 2);
    }


}