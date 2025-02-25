package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public abstract class AbstractTargetedPlayerTask extends AbstractPlayerTask implements TargetedPlayerTask {
    protected OfflinePlayer targetedPlayer;
    public AbstractTargetedPlayerTask(LifeSeries p, Player owningPlayer, OfflinePlayer targetedPlayer, SecretTasks.TaskDifficulty difficulty, Consumer<PlayerTask> onTaskCompletion) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
        this.targetedPlayer = targetedPlayer;
    }
    public AbstractTargetedPlayerTask(LifeSeries p, Builder builder) {
        this(p, builder.owningPlayer, builder.targetPlayer, builder.assignedDifficulty, builder.onTaskCompletion);
    }

    @Override
    public OfflinePlayer getTargetedPlayer() {
        return this.targetedPlayer;
    }

    public abstract static class Builder<T extends Builder<T>> extends AbstractPlayerTask.Builder<T> {
        protected OfflinePlayer targetPlayer;
        public T target(OfflinePlayer targetPlayer) { this.targetPlayer = targetPlayer; return (T) this; }
        @Override
        public LiteralArgumentBuilder<CommandSourceStack> builderCommand(LifeSeries p, LiteralArgumentBuilder<CommandSourceStack> root, Consumer<PlayerTask> onTaskAdded) {
            return root
                    .then(Commands.argument("targetPlayer", ArgumentTypes.player())
                            .executes(ctx -> {
                                Player targetArgument = ctx.getArgument("targetPlayer", PlayerSelectorArgumentResolver.class)
                                        .resolve(ctx.getSource()).getFirst();
                                ctx.getArgument("players", PlayerSelectorArgumentResolver.class)
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
            var players = p.getServer().getOnlinePlayers();
            var oPlayer = players.stream().filter(player -> player.hasPermission("lf.game.bypass-roll"))
                    .skip((int) (players.size() * Math.random()))
                    .findFirst();

            if (oPlayer.isPresent()) {
                return this.target(oPlayer.get()).build(p);
            }
            throw new RuntimeException("buildWithAnySettings couldnt get random");
        }
    }
}

