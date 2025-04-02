package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.PlayerTask;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.*;

public class RerollTask implements CommandContainer.FilledLiteralCommand {
    private final LifeSeries p;
    private final SecretTasks secretGame;

    public RerollTask(LifeSeries p, SecretTasks secretGame) {
        this.p = p;
        this.secretGame = secretGame;
    }

    private final ArrayList<UUID> rerolledPlayers = new ArrayList<>();

    @Override
    public List<String> getAliases() {
        return List.of("rolltask");
    }

    @Override
    public String getDescription() {
        return "Reroll your task";
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("reroll")
                .requires(src -> src.getSender() instanceof Player)
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    if (rerolledPlayers.contains(player.getUniqueId()) || !secretGame.canRerollTask(player)) {
                        player.sendMessage(Component.translatable("secret-life.reroll-out-of-rolls")
                                .color(NamedTextColor.RED)
                        );
                        return 1;
                    }

                    secretGame.getSecretTask(player).ifPresentOrElse((currentTask) -> {
                        PlayerTask task;
                        if (currentTask.getDifficulty() == SecretTasks.TaskDifficulty.EASY) {
                            task = secretGame.rollTaskOfDifficulty(player,
                                    List.of(SecretTasks.TaskDifficulty.MEDIUM, SecretTasks.TaskDifficulty.HARD), true, true
                            );
                        } else {
                            task = secretGame.rollTaskOfDifficulty(player,
                                    Collections.singletonList(SecretTasks.TaskDifficulty.HARD), true, true
                            );
                        }
                        task.givePlayerTaskBook(player);

                        rerolledPlayers.add(player.getUniqueId()); // player gets only one roll (unless in infinite roll team)
                    }, () -> player.sendMessage(Component.translatable("secret-life.taskbook.no-task-assigned")
                            .color(NamedTextColor.RED)
                    ));
                    return 1;
                }).build(); // TODO:
    }
}
