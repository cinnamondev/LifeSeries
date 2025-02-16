package com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
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
    public AbstractTargetedPlayerTask(LifeSeries p, Player owningPlayer, OfflinePlayer targetedPlayer, Consumer<PlayerTask> onTaskCompletion) {
        super(p, owningPlayer, onTaskCompletion);
        this.targetedPlayer = targetedPlayer;
    }
    public AbstractTargetedPlayerTask(LifeSeries p, Builder builder) {
        super(p, builder.owningPlayer, builder.onTaskCompletion);
        this.targetedPlayer = builder.targetPlayer;
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
    }
}

