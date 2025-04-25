package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.SecretLife;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Generic assignable task that wraps around a 'glue task' (aka `AbstractSharedGroupTask`, implementation required).
 * If a group of players is assigned a task, irregardless of the tasks content, the type of their task is always
 * `GroupTask`. Internally, the specifc task implementation will be derived from `T`.
 * @param <T>
 */
public final class GroupTask implements PlayerTask {
    //    T getCommon();
    //    Consumer<PlayerTask> getTaskConsumer();
    //    abstract class Builder<T extends Builder<T>> extends PlayerTask.Builder<T> {
    //        protected AbstractSharedGroupTask taskGlue;
    //        T backingTask(AbstractSharedGroupTask taskGlue) {
    //            this.taskGlue = taskGlue;
    //            return (T) this;
    //        }
    //    }
    protected final LifeSeries p;
    protected final Player owningPlayer;
    protected final TaskDifficulty difficulty;
    protected final AbstractSharedGroupTask<?> groupTask;
    protected final Consumer<PlayerTask> taskConsumer;

    public GroupTask(LifeSeries p, Player owningPlayer, TaskDifficulty difficulty, Consumer<PlayerTask> taskConsumer, AbstractSharedGroupTask<?> groupTask) {
        this.p = p;
        this.owningPlayer = owningPlayer;
        this.difficulty = difficulty;
        this.taskConsumer = taskConsumer;
        this.groupTask = groupTask;
    }

    public Collection<Player> getInvolvedPlayers() {
        return groupTask.getGluedTasks().stream().map(GroupTask::getTaskOwner).collect(Collectors.toList());
    }

    @Override
    public boolean isTaskRerollable() {
        return groupTask.isTaskRerollable();
    }

    @Override
    public boolean isTaskGuessable() {
        return groupTask.isTaskGuessable();
    }

    @Override
    public String getTaskKey() {
        return groupTask.getTaskKey();
    }

    public Consumer<PlayerTask> getTaskConsumer() {
        return this.taskConsumer;
    }

    @Override
    public Player getTaskOwner() {
        return this.owningPlayer;
    }

    @Override
    public ItemStack createTaskBook() {
        ItemStack book = SecretLife.baseBook(p, 1);

        Component name = GlobalTranslator.render(name(), owningPlayer.locale());
        ArrayList<Component> pages = descriptionServerTranslate(owningPlayer.locale())
                .lines()
                .map(Component::text)
                .map(TextComponent::asComponent)
                .collect(Collectors.toCollection(ArrayList::new));

        pages.set(0, name.appendNewline().appendNewline().append(pages.getFirst()));
        //Component description = GlobalTranslator.render(description(), owningPlayer.locale());
        book.setItemMeta(((BookMeta) book.getItemMeta()).toBuilder()
                .author(Component.text("God"))
                .title(Component.text("Your task."))
                .pages(pages)
                .build()
        );
        return book;
    }


    @Override
    public Component taskProgressExplanation() {
        return groupTask.taskProgressExplanation();
    }

    @Override
    public void complete() {
        groupTask.complete();
    }

    @Override
    public void fail() {
        groupTask.fail();
    }

    @Override
    public void cleanup() {
        groupTask.cleanup();
    }

    @Override
    public void endOfSession() {
        groupTask.endOfSession();
    }

    @Override
    public TaskStatus getTaskProgress() {
        return groupTask.getTaskProgress();
    }

    @Override
    public Component lore() {
        return groupTask.lore();
    }

    @Override
    public Component description() {
        return groupTask.description();
    }

    @Override
    public String descriptionServerTranslate(Locale locale) {
        return groupTask.descriptionServerTranslate(locale);
    }

    @Override
    public Component name() {
        return groupTask.name();
    }

    @Override
    public String nameServerTranslate(Locale locale) {
        return groupTask.nameServerTranslate(locale);
    }

    @Override
    public Optional<ConfigurationSection> getConfigurationSection() {
        return groupTask.getConfigurationSection();
    }

    @Override
    public TaskDifficulty getDifficulty() {
        return this.difficulty;
    }


    @Override
    public ConfigurationSection saveTask() {
        var playerSection = p.getSave().getConfigurationSection("players");
        if (playerSection == null) {
            playerSection = p.getSave().createSection("players");
        }

        var task = playerSection.createSection(this.getTaskKey());
        task.set("uuid", groupTask.getTaskUUID().toString());
        return task;
    }

    @Override
    public void acceptGuess() {
        groupTask.acceptGuess();
    }


    @Override
    public Builder builderProvider() {
        return new Builder();
    }

    public static class Builder extends PlayerTask.Builder<Builder> {
        protected AbstractSharedGroupTask<?> groupTask = null;
        public Builder task(AbstractSharedGroupTask<?> task) { this.groupTask = task; return this; }
        @Override
        public GroupTask build(LifeSeries p) {
            if (groupTask == null) { throw new RuntimeException("grouptask cannot exist without a glue"); }
            var task = new GroupTask(p, owningPlayer, assignedDifficulty, onTaskCompletion, groupTask);
            groupTask.getGluedTasks().add(task);
            return task;
        }
    }
}
