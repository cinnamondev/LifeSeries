package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

public interface PlayerTask extends Listener {
    public enum TaskStatus {
        COMPLETE,
        IN_PROGRESS,
        FAILED;

        public Component asComponent() {
            return switch(this) {
                case COMPLETE -> Component.translatable("task-status.complete").color(NamedTextColor.GREEN);
                case IN_PROGRESS -> Component.translatable("task-status.in-progress").color(NamedTextColor.YELLOW);
                case FAILED -> Component.translatable("task-status.failed").color(NamedTextColor.RED);
            };
        }
    }
    void complete();
    void fail();
    /// cleanup things before something happens (end of session call, server stops.)
    default void cleanup() {}
    /// Tell the player task its the end of the session. Default behaviour is to fail the task, if it's still in progress.
    /// @return `true` if function will have removed task by itself (i.e. by calling `fail()`). If it hasn't removed the task, it should return `false.`
    default void endOfSession() {
        cleanup();
        if (!isTaskFinished()) {
            fail();
        }
    }
    TaskStatus getTaskProgress();
    default boolean isTaskFinished() {
        TaskStatus taskStatus = getTaskProgress();
        return taskStatus == TaskStatus.COMPLETE || taskStatus == TaskStatus.FAILED;
    }
    default boolean isTaskRerollable() { return true; } // disable if you really want to force the reds to play
    boolean isTaskGuessable();
    /// translate bundle item using a locale provided. this SHOULNT be used in typical cases, we only use this in specific situation
    /// i.e. creating books (where server-side translations dont quite work right, so we need to do it here.)
    default String nameServerTranslate(Locale locale) {
        return GlobalTranslator.translator().translate("secret-life.tasks." + getTaskKey() + ".name", locale).format(null);
    }
    default Component name() {
        return Component.translatable("secret-life.tasks." + getTaskKey() + ".name");
    }
    /// translate bundle item using a locale provided. this SHOULNT be used in typical cases, we only use this in specific situation
    /// i.e. creating books (where server-side translations dont quite work right, so we need to do it here.)
    default String descriptionServerTranslate(Locale locale) {
        return GlobalTranslator.translator().translate("secret-life.tasks." + getTaskKey() + ".description", locale).format(null);
    }
    default Component description() {
        return Component.translatable("secret-life.tasks." + getTaskKey() + ".description");
    }
    default Component lore() {
        return name().style(Style.style(NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC)).hoverEvent(description());
    }
    Optional<ConfigurationSection> getConfigurationSection();
    Player getTaskOwner();
    TaskDifficulty getDifficulty();
    ItemStack createTaskBook();
    ConfigurationSection saveTask(ConfigurationSection taskSection);
    String getTaskKey();
    default void acceptGuess() { fail(); }
    default void givePlayerTaskBook(Player player) { player.give(createTaskBook()); }
    default Component taskProgressExplanation() { return Component.text("No explanation provided"); }

    Builder<? extends Builder<?>> builderProvider();
    default Builder<? extends Builder<?>> createBuilder() {
        return builderProvider().difficulty(getDifficulty()).player(getTaskOwner());
    }
    public abstract static class Builder<T extends Builder<T>> {
        protected final Random random = new Random();
        protected TaskDifficulty assignedDifficulty = null;
        protected Player owningPlayer;
        protected Consumer<PlayerTask> onTaskCompletion;
        // Implementation notes:
        // argument `players` will be provided by a higher level in the tree. The only required additional parameters
        // (if any) are specific to your task. A literal for your task name does not need to be included.
        public LiteralArgumentBuilder<CommandSourceStack> builderCommand(LifeSeries p, LiteralArgumentBuilder<CommandSourceStack> root, Consumer<PlayerTask> onTaskAdded, Consumer<PlayerTask> onTaskCompletion) {
            return root.executes(ctx -> {
                TaskDifficulty difficulty = TaskDifficulty.difficultyResolver(ctx, "difficulty");
                ctx.getArgument("players", PlayerSelectorArgumentResolver.class)
                        .resolve(ctx.getSource()).forEach((player) -> onTaskAdded.accept(
                                this.player(player).onCompletion(onTaskCompletion).difficulty(difficulty).build(p)
                        ));
                return 1;
            });
        }
        public T player(Player owningPlayer) { this.owningPlayer = owningPlayer; return (T) this; }
        public T onCompletion(Consumer<PlayerTask> taskConsumer) {
            this.onTaskCompletion = taskConsumer;
            return (T) this;
        }
        public T difficulty(TaskDifficulty difficulty) {
            this.assignedDifficulty = difficulty;
            return (T) this;
        }

        public abstract PlayerTask build(LifeSeries p);
    }
}
