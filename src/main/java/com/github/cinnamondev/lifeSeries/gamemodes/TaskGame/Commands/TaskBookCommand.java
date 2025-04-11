package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.TaskGame;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

public class TaskBookCommand implements CommandContainer.FilledLiteralCommand {
    private final LifeSeries p;
    private final TaskGame game;
    public TaskBookCommand(LifeSeries p, TaskGame game) {
        this.p = p;
        this.game = game;
    }

    @Override
    public List<String> getAliases() {
        return List.of("taskBook", "book");
    }

    @Override
    public String getDescription() {
        return "task book!";
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("secretTask")
                .requires(src -> src.getSender() instanceof Player && src.getSender().hasPermission("life.secret-life.getTaskBook"))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    game.getSecretTask(player).ifPresentOrElse(
                            (task) -> task.givePlayerTaskBook(player),
                            () -> player.sendMessage(
                                    Component.translatable("secret-life.taskbook.no-task-assigned")
                            ));
                    return 1;
                })
                .build();
    }
}
