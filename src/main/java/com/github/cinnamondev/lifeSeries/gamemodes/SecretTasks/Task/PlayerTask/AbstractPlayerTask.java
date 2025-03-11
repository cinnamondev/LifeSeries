package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretLife;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Random;
import java.util.function.Consumer;

public abstract class AbstractPlayerTask implements PlayerTask {
    protected final LifeSeries p;
    protected final Player owningPlayer;
    protected TaskStatus status = TaskStatus.IN_PROGRESS;
    private final Consumer<PlayerTask> taskConsumer;
    private final SecretTasks.TaskDifficulty difficulty;
    public AbstractPlayerTask(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, SecretTasks.TaskDifficulty difficulty) {
        this.p = p;
        this.owningPlayer = owningPlayer;
        this.taskConsumer = onTaskCompletion;
        this.difficulty = difficulty;
    }
    public AbstractPlayerTask(LifeSeries p, Builder builder) {
        this(p, builder.owningPlayer, builder.onTaskCompletion, builder.assignedDifficulty);
    }

    @Override
    public void complete() {
        this.status = TaskStatus.COMPLETE;
        if (taskConsumer != null) { taskConsumer.accept(this); }
    }

    @Override
    public void fail() {
        this.status = TaskStatus.FAILED;
        if (taskConsumer != null) { taskConsumer.accept(this); }
    }

    @Override
    public TaskStatus getTaskProgress() {
        return this.status;
    }

    @Override
    public Player getTaskOwner() {
        return this.owningPlayer;
    }
    @Override
    public SecretTasks.TaskDifficulty getDifficulty() {
        return this.difficulty;
    }

    @Override
    public ConfigurationSection saveTask(ConfigurationSection taskSection) {
        var task = taskSection.createSection(this.getTaskKey());
        task.set("difficulty", this.getDifficulty());
        task.set("progress", this.getTaskProgress());
        return task;
    }

    @Override
    public ItemStack createTaskBook() {
        ItemStack book = SecretLife.baseBook(p, 1);

        Component name = GlobalTranslator.render(getTaskName(), p.getServerLocale());
        Component description = GlobalTranslator.render(getTaskDescription(), p.getServerLocale());

        book.setItemMeta(((BookMeta) book.getItemMeta()).toBuilder()
                .author(Component.text("God"))
                .title(Component.text("Your task."))
                .addPage(name.appendNewline().appendNewline().append(description))
                .build()
        );
        return book;
    }
    public abstract static class Builder<T extends Builder<T>> {
        protected final Random random = new Random();
        protected SecretTasks.TaskDifficulty assignedDifficulty = null;
        protected Player owningPlayer;
        protected Consumer<PlayerTask> onTaskCompletion;
        // Implementation notes:
        // argument `players` will be provided by a higher level in the tree. The only required additional parameters
        // (if any) are specific to your task. A literal for your task name does not need to be included.
        public LiteralArgumentBuilder<CommandSourceStack> builderCommand(LifeSeries p, LiteralArgumentBuilder<CommandSourceStack> root, Consumer<PlayerTask> onTaskAdded) {
            return root.executes(ctx -> {
                ctx.getArgument("players", PlayerSelectorArgumentResolver.class)
                        .resolve(ctx.getSource()).forEach((player) -> {
                            onTaskAdded.accept(this.player(player).build(p));
                        });
                return 1;
            });
        }
        public LiteralArgumentBuilder<CommandSourceStack> builderCommand(LifeSeries p, LiteralArgumentBuilder<CommandSourceStack> root, Consumer<PlayerTask> onTaskAdded, Consumer<PlayerTask> onTaskCompletion) {
            this.onCompletion(onTaskAdded);
            return builderCommand(p, root, onTaskAdded);
        }
        public T player(Player owningPlayer) { this.owningPlayer = owningPlayer; return (T) this; }
        public T onCompletion(Consumer<PlayerTask> taskConsumer) {
            this.onTaskCompletion = taskConsumer;
            return (T) this;
        }
        public T difficulty(SecretTasks.TaskDifficulty difficulty) {
            this.assignedDifficulty = difficulty;
            return (T) this;
        }

        public abstract AbstractPlayerTask build(LifeSeries p);
        ///  allow a task to be built without any specific arguments other than the target player and 'onCompletion'
        ///  intended to be used by task rollers who don't care whats going on. game is passed to the builder so
        ///  game-specific circumstances could be accounted for.
        public abstract AbstractPlayerTask buildWithAnySettings(LifeSeries p, SecretTasks game);
    }
}
