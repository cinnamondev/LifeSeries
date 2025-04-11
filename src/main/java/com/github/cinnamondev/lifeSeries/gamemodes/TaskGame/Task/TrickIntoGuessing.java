package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.SessionLongTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.TaskDifficulty;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class TrickIntoGuessing extends AbstractPlayerTask{
    private final String taskString;
    public TrickIntoGuessing(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty, String taskString) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
        this.taskString = taskString;
    }

    @Override
    public boolean isTaskGuessable() {
        return true;
    }

    @Override
    public String getTaskKey() {
        return "trick-guess";
    }

    @Override
    public void acceptGuess() {
        complete();
    }

    @Override
    public Component taskProgressExplanation() {
        return super.taskProgressExplanation();
    }

    @Override
    public Builder builderProvider() {
        return new Builder();
    }

    @Override
    public String descriptionServerTranslate(Locale locale) {
        return Objects.requireNonNull(GlobalTranslator.translator().translate("secret-life.tasks.trick-guess.description", locale))
                .format(taskString);
    }

    @Override
    public Component description() {
        return Component.translatable("secret-life.tasks.trick-guess.description", taskString);
    }

    public static class Builder extends PlayerTask.Builder<Builder> {
        protected String taskString;
        Builder fakeTask(String fakeTask) { this.taskString = fakeTask; return this; }
        Builder randomFakeTask(Plugin p) {
            ConfigurationSection section = p.getConfig().getConfigurationSection("options.secret-life.task-configs." + "trick-guess");
            if (section == null) { this.taskString = "No fake tasks! (No config) Ask a GM!"; return this; }
            List<String> fakeTasks = section.getStringList("fake-tasks");
            if (fakeTasks.isEmpty()) { this.taskString = "No fake tasks! (Empty List) Ask a GM!"; return this; }

            this.taskString = fakeTasks.get((int) (fakeTasks.size() * Math.random()));
            return this;
        }

        public LiteralArgumentBuilder<CommandSourceStack> builderCommand(LifeSeries p, LiteralArgumentBuilder<CommandSourceStack> root, Consumer<PlayerTask> onTaskAdded, Consumer<PlayerTask> onTaskCompletion) {
            return root.then(Commands.literal("saved")
                            .executes(ctx -> {
                                TaskDifficulty difficulty = TaskDifficulty.difficultyResolver(ctx, "difficulty");
                                ctx.getArgument("players", PlayerSelectorArgumentResolver.class)
                                        .resolve(ctx.getSource()).forEach((player) -> onTaskAdded.accept(
                                                this.player(player).onCompletion(onTaskCompletion).difficulty(difficulty).randomFakeTask(p).build(p)
                                        ));
                                return 1;
                            }))
                    .then(Commands.argument("fakeTask", StringArgumentType.greedyString()).executes(ctx -> {
                        TaskDifficulty difficulty = TaskDifficulty.difficultyResolver(ctx, "difficulty");
                        String taskString = ctx.getArgument("fakeTask", String.class);
                        ctx.getArgument("players", PlayerSelectorArgumentResolver.class)
                                .resolve(ctx.getSource()).forEach((player) -> onTaskAdded.accept(
                                        this.player(player).onCompletion(onTaskCompletion).difficulty(difficulty).fakeTask(taskString).build(p)
                                ));
                        return 1;
                    }));
        }
        @Override
        public AbstractPlayerTask build(LifeSeries p) {
            return new TrickIntoGuessing(p, owningPlayer, onTaskCompletion, assignedDifficulty, taskString);
        }
    }
}
