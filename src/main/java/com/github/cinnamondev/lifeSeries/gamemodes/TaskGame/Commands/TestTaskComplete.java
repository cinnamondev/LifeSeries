package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Commands;

import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.TaskGame;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.PlayerTask;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;

import java.util.List;

/// for command blocks to test if a players task is in a certain state.
public class TestTaskComplete implements CommandContainer.FilledLiteralCommand {
    private final TaskGame game;
    public TestTaskComplete(TaskGame game) { this.game = game; }
    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("taskState")
                .requires(src -> src.getSender().hasPermission("life.command-block-command"))
                .then(Commands.argument("player", ArgumentTypes.player())
                        .then(Commands.argument("state", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("complete");
                                    builder.suggest("inprogress");
                                    builder.suggest("failed");
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                            .resolve(ctx.getSource()).getFirst();

                                    PlayerTask.TaskStatus status = switch (ctx.getArgument("state", String.class).toLowerCase()) {
                                        case "complete" -> PlayerTask.TaskStatus.COMPLETE;
                                        case "failed" -> PlayerTask.TaskStatus.FAILED;
                                        case "inprogress" -> PlayerTask.TaskStatus.IN_PROGRESS;
                                        default -> throw new IllegalStateException("Invalid state: " + ctx.getArgument("state", String.class));
                                    };

                                    return game.getSecretTask(player).map(task -> task.getTaskProgress() == status)
                                            .orElse(false) ? 1 : 0; // command success
                                }))).build();
    }
}
