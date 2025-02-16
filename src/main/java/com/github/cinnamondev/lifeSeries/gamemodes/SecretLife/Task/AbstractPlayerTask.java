package com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.function.Consumer;

public abstract class AbstractPlayerTask implements PlayerTask {
    protected final LifeSeries p;
    protected final Player owningPlayer;
    protected TaskStatus status = TaskStatus.IN_PROGRESS;
    private final Consumer<PlayerTask> taskConsumer;
    public AbstractPlayerTask(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion) {
        this.p = p;
        this.owningPlayer = owningPlayer;
        this.taskConsumer = onTaskCompletion;
    }
    public AbstractPlayerTask(LifeSeries p, Builder builder) {
        this(p, builder.owningPlayer, builder.onTaskCompletion);
    }

    @Override
    public void complete() {
        if (taskConsumer != null) { taskConsumer.accept(this); }
        this.status = TaskStatus.COMPLETE;
    }

    @Override
    public void fail() {
        this.status = TaskStatus.FAILED;
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
    public ItemStack createTaskBook() {
        ItemStack book =ItemStack.of(Material.WRITTEN_BOOK,1);

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

        public abstract AbstractPlayerTask build(LifeSeries p);
    }
}
