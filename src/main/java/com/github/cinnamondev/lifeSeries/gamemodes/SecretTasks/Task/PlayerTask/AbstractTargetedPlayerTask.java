package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public abstract class AbstractTargetedPlayerTask extends AbstractPlayerTask implements TargetedPlayerTask {
    protected OfflinePlayer targetedPlayer;
    public AbstractTargetedPlayerTask(LifeSeries p, Player owningPlayer, OfflinePlayer targetedPlayer, SecretTasks.TaskDifficulty difficulty, Consumer<PlayerTask> onTaskCompletion) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
        this.targetedPlayer = targetedPlayer;
    }

    @Override
    public OfflinePlayer getTargetedPlayer() {
        return this.targetedPlayer;
    }

    @Override
    public ConfigurationSection saveTask(ConfigurationSection taskSection) {
        var task = super.saveTask(taskSection);
        taskSection.set("target", this.targetedPlayer.getUniqueId());
        return task;
    }

    public abstract static class Builder<T extends AbstractTargetedPlayerTask.Builder<T>> extends AbstractPlayerTask.Builder<T> {
        protected OfflinePlayer targetPlayer;
        public T target(OfflinePlayer targetPlayer) { this.targetPlayer = targetPlayer; return (T) this; }
        protected T randomTarget(Plugin p) {
            var candidatePlayers = p.getServer().getOnlinePlayers().stream()
                    .filter(player -> !player.hasPermission("lf.game.bypass-roll"))
                    .filter(player -> !player.equals(this.owningPlayer))
                    .toList();

            if (candidatePlayers.isEmpty()) {
                throw new RuntimeException(" target candidate found when rolling task for player " + this.owningPlayer.getName());
            }
            this.targetPlayer = candidatePlayers.get((int) (candidatePlayers.size() * Math.random()));
            //p.getLogger().info("found candidate " + this.targetPlayer.getName());
            return (T) this;
        }
        @Override
        public LiteralArgumentBuilder<CommandSourceStack> builderCommand(LifeSeries p, LiteralArgumentBuilder<CommandSourceStack> root, Consumer<PlayerTask> onTaskAdded) {
            return root
                    .then(Commands.argument("targetPlayer", ArgumentTypes.player())
                            .executes(ctx -> {
                                Player targetArgument = ctx.getArgument("targetPlayer", PlayerSelectorArgumentResolver.class)
                                        .resolve(ctx.getSource()).getFirst();
                                ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                        .resolve(ctx.getSource()).forEach((player) -> {
                                            onTaskAdded.accept(
                                                    this
                                                            .player(player)
                                                            .target(targetArgument)
                                                            .build(p)
                                            );
                                        });
                                return 1;
                            })
                    );
        }

        @Override
        public AbstractPlayerTask buildWithAnySettings(LifeSeries p, SecretTasks game) {
            return randomTarget(p).build(p);
        }
    }
}

