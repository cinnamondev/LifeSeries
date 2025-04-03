package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.TaskDifficulty;
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

    private final HashMap<UUID, Integer> rerolledPlayers = new HashMap<>();

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Reroll your task!";
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("reroll")
                .requires(src -> src.getSender() instanceof Player)
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    int rolls = rerolledPlayers.getOrDefault(player.getUniqueId(), 0);
                    int maxRolls = p.getConfig().getInt("options.secret-life.can-reroll.max-rolls");
                    if ((rolls >= maxRolls) || !secretGame.canRerollTask(player)) {
                        player.sendMessage(Component.translatable("secret-life.reroll-out-of-rolls")
                                .color(NamedTextColor.RED)
                        );
                        return 1;
                    }

                    secretGame.getSecretTask(player).ifPresentOrElse((currentTask) -> {
                        if (!currentTask.isTaskRerollable()) { // player cannot roll as their current task forbids it.
                            player.sendMessage(Component.translatable("secret-life.reroll-out-of-rolls")
                                    .color(NamedTextColor.RED));
                            return;
                        }


                        if (currentTask.getDifficulty() == TaskDifficulty.EASY) {
                            secretGame.rollTaskOfDifficulty(player,
                                    List.of(TaskDifficulty.MEDIUM, TaskDifficulty.HARD), true, true
                            );
                        } else {
                            secretGame.rollTaskOfDifficulty(player,
                                    Collections.singletonList(TaskDifficulty.HARD), true, true
                            );
                        }
                        rerolledPlayers.compute(player.getUniqueId(), (u, r) -> r == null ? 1 : r+1);
                    }, () -> player.sendMessage(Component.translatable("secret-life.taskbook.no-task-assigned")
                            .color(NamedTextColor.RED)
                    ));
                    return 1;
                }).build(); // TODO:
    }
}
