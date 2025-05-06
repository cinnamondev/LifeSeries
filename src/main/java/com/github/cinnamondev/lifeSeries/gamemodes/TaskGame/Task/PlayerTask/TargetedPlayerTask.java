package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public interface TargetedPlayerTask extends PlayerTask {
    OfflinePlayer getTargetedPlayer();

    @Override
    Builder<?> builderProvider();
    @Override
    default Builder<?> createBuilder() {
        return builderProvider().target(getTargetedPlayer()).player(getTaskOwner()).difficulty(getDifficulty());
    }
    abstract class Builder<T extends Builder<T>> extends PlayerTask.Builder<T> {
        protected OfflinePlayer targetPlayer;
        public T target(OfflinePlayer targetPlayer) { this.targetPlayer = targetPlayer; return (T) this; }
        public T randomTarget() {
            var candidatePlayers = Bukkit.getOnlinePlayers().stream()
                    .filter(player -> !player.hasPermission("lf.game.bypass-roll"))
                    .filter(player -> !player.equals(this.owningPlayer))
                    .toList();

            if (candidatePlayers.isEmpty()) {
                throw new RuntimeException("no target candidate found when rolling task for player " + this.owningPlayer.getName());
            }
            this.targetPlayer = candidatePlayers.get((int) (candidatePlayers.size() * Math.random()));
            //p.getLogger().info("found candidate " + this.targetPlayer.getName());
            return (T) this;
        }
        @Override
        public LiteralArgumentBuilder<CommandSourceStack> builderCommand(LifeSeries p, LiteralArgumentBuilder<CommandSourceStack> root, Consumer<PlayerTask> onTaskAdded, Consumer<PlayerTask> onTaskCompletion) {
            return root.then(Commands.argument("targetPlayer", ArgumentTypes.player())
                    .executes(ctx -> {
                        TaskDifficulty difficulty = ctx.getArgument("difficulty", TaskDifficulty.class);
                        Player targetArgument = ctx.getArgument("targetPlayer", PlayerSelectorArgumentResolver.class)
                                .resolve(ctx.getSource()).getFirst();
                        ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                .resolve(ctx.getSource()).forEach((player) -> onTaskAdded.accept(
                                        this.target(targetArgument)
                                                .player(player)
                                                .onCompletion(onTaskCompletion)
                                                .difficulty(difficulty)
                                                .build(p)
                                ));
                        return 1;
                    })
            );
        }
    }

}
