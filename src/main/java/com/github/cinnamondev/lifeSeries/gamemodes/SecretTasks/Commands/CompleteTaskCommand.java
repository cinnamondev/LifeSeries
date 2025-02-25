package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.SelfCompletableTask;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

public class CompleteTaskCommand implements CommandContainer.FilledLiteralCommand {
    private final LifeSeries p;
    private final SecretTasks game;

    public CompleteTaskCommand(LifeSeries p, SecretTasks secretTasks) {
        this.p = p;
        this.game = secretTasks;
    }

    @Override
    public List<String> getAliases() {
        return List.of("complete");
    }

    @Override
    public String getDescription() {
        return "(For self-completable tasks) Declare a task complete.";
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("finishTask")
                .requires(src -> src.getSender() instanceof Player)
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    game.getSecretTask(player).ifPresentOrElse(_task -> {
                        if (_task instanceof SelfCompletableTask selfTask) {
                            selfTask.conditionalCompleteTask();
                        } else {
                            player.sendMessage(
                                    Component.translatable("secret-life.taskbook.task-not-self-completable")
                            );
                        }
                    }, () -> player.sendMessage(
                            Component.translatable("secret-life.taskbook.no-task-assigned")
                    ));
                    return 1;
                })
                .build();
    }
}
