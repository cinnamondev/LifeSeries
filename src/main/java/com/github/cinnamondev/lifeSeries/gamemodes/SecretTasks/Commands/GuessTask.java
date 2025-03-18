package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
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
import java.util.stream.Stream;

public class GuessTask implements CommandContainer.FilledLiteralCommand {
    private final LifeSeries p;
    private final SecretTasks game;

    public GuessTask(LifeSeries p, SecretTasks game) {
        this.p = p;
        this.game = game;
    }

    @Override
    public List<String> getAliases() {
        return List.of("guessTask");
    }

    @Override
    public String getDescription() {
        return "Guess another player's task.";
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("guess")
                .requires(src -> src.getSender() instanceof Player)
                .then(Commands.argument("player", ArgumentTypes.player()).then(Commands.argument("task", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            Player guesser = (Player) ctx.getSource().getSender();
                            if (!game.canGuessTask(guesser)) {
                                guesser.sendMessage(Component.translatable("secret-life.guessing.cannot-guess-tasks"));
                                return 1;
                            }

                            Player target = ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                    .resolve(ctx.getSource()).getFirst();

                            game.getSecretTask(target).ifPresentOrElse(secretTask -> {
                                if (!secretTask.isTaskGuessable()) {
                                    guesser.sendMessage(Component.translatable("secret-life.guessing.unguessable-task"));
                                    return;
                                }

                                if (p.getConfig().getBoolean("options.secret-life.gamemaster-enabled", false)) {
                                    // use gamemaster players
                                    p.getServer().getOnlinePlayers().stream()
                                            .filter(player -> player.hasPermission("life.gamemaster"))
                                            .forEach(gamemaster -> gamemaster.sendMessage(
                                                    Component.translatable("secret-life.gamemaster.guess")
                                                            .arguments(
                                                                    guesser.displayName(),
                                                                    target.displayName(),
                                                                    Component.text(ctx.getArgument("task", String.class)),
                                                                    secretTask.lore(),
                                                                    SecretTasks.rejectGuessButton(guesser),
                                                                    SecretTasks.acceptGuessButton(secretTask, guesser, false),
                                                                    UtilityComponents.teleportToPlayer(target)
                                                            )
                                            ));
                                } else { // send guess to task owner.
                                    target.sendMessage(
                                            Component.translatable("secret-life.gamemaster.guess")
                                                    .arguments(
                                                            guesser.displayName(),
                                                            target.displayName(),
                                                            Component.text(ctx.getArgument("task", String.class)),
                                                            secretTask.lore(),
                                                            SecretTasks.rejectGuessButton(guesser),
                                                            SecretTasks.acceptGuessButton(secretTask, guesser, false),
                                                            Component.empty() // task owner shouldnt be given teleporty stuff.
                                                    )
                                    );
                                }

                            }, () -> {
                                guesser.sendMessage(Component.translatable("secret-life.guessing.no-or-complete-task"));
                            });
                            return 1;
                        })
        )).build();
    }
}
