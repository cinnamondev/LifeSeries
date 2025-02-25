package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.Difficulty;
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
                        player.sendMessage(Component.translatable("secret-life.reroll-out-of-rolls"));
                        return 1;
                    }

                    secretGame.getSecretTask(player).ifPresentOrElse((currentTask) -> {
                        if (currentTask.getDifficulty() == SecretTasks.TaskDifficulty.EASY) {
                            secretGame.addSecretTask(secretGame.rollTaskOfDifficulty(player,
                                    List.of(SecretTasks.TaskDifficulty.MEDIUM, SecretTasks.TaskDifficulty.HARD)
                            ));
                        } else {
                            secretGame.addSecretTask(secretGame.rollTaskOfDifficulty(player,
                                    Collections.singletonList(SecretTasks.TaskDifficulty.HARD)
                            ));
                        }
                        rerolledPlayers.add(player.getUniqueId()); // player gets only one roll (unless in infinite roll team)
                    }, () -> {
                        // TODO: rejection message
                        player.sendMessage(Component.translatable("secret-life.taskbook.no-task-assigned"));
                    });
                    return 1;
                }).build(); // TODO:
    }
}
