package com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.SecretTasks;
import com.github.cinnamondev.lifeSeries.util.UtilityComponents;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

public class GuessTask implements CommandContainer.FilledLiteralCommand {
    private final LifeSeries p;
    private final SecretTasks game;

    public GuessTask(LifeSeries p, SecretTasks game) {
        this.p = p;
        this.game = game;
    }

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
        return Commands.literal("guess")
                .requires(src -> src.getSender() instanceof Player)
                .requires(src -> src.getSender().hasPermission("lf.secretlife.guess"))
                .then(Commands.argument("player", ArgumentTypes.player()).then(Commands.argument("task", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            Player guesser = (Player) ctx.getSource().getSender();
                            if (!game.playerCanGuessTasks(guesser)) { return 1; } // TODO: rejection message

                            Player target = ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                    .resolve(ctx.getSource()).getFirst();

                            game.getSecretTask(target).ifPresentOrElse(secretTask -> {
                                if (!secretTask.isTaskGuessable()) { return; } // TODO: rejection message

                                p.getServer().getOnlinePlayers().stream()
                                        .filter(player -> player.hasPermission("lf.gamemaster"))
                                        .forEach(player -> player.sendMessage(
                                                Component.translatable("secret-life.gamemaster.guess")
                                                        .arguments(
                                                                player.displayName(),
                                                                target.displayName(),
                                                                Component.text(ctx.getArgument("task", String.class)),
                                                                secretTask.componentWithLore(),
                                                                SecretTasks.rejectGuessButton(secretTask),
                                                                SecretTasks.acceptGuessButton(secretTask),
                                                                UtilityComponents.teleportToPlayer(target)
                                                        )
                                        ));
                            }, () -> {
                                // TODO: rejection message
                            });



                            return 1;
                        })
        )).build();
    }
}
