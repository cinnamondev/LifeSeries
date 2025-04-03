package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import com.github.cinnamondev.lifeSeries.util.UtilityComponents;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class AbstractTargetedPlayerTask extends AbstractPlayerTask implements TargetedPlayerTask {
    protected OfflinePlayer targetedPlayer;
    public AbstractTargetedPlayerTask(LifeSeries p, Player owningPlayer, OfflinePlayer targetedPlayer, TaskDifficulty difficulty, Consumer<PlayerTask> onTaskCompletion) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
        this.targetedPlayer = targetedPlayer;
    }
    @Override
    public OfflinePlayer getTargetedPlayer() {
        return this.targetedPlayer;
    }

}

