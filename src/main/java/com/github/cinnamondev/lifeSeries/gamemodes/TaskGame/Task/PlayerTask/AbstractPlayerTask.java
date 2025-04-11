package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.SecretLife;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractPlayerTask implements PlayerTask {
    protected final LifeSeries p;
    protected final Player owningPlayer;
    protected TaskStatus status = TaskStatus.IN_PROGRESS;
    protected final Consumer<PlayerTask> taskConsumer;
    protected final TaskDifficulty difficulty;
    public AbstractPlayerTask(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        this.p = p;
        this.owningPlayer = owningPlayer;
        this.taskConsumer = onTaskCompletion;
        this.difficulty = difficulty;
    }

    @Override
    public void complete() {
        cleanup();
        this.status = TaskStatus.COMPLETE;
        if (taskConsumer != null) { taskConsumer.accept(this); }
    }

    @Override
    public void fail() {
        cleanup();
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
    public TaskDifficulty getDifficulty() {
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
    public Optional<ConfigurationSection> getConfigurationSection() {
        return Optional.ofNullable(p.getConfig().getConfigurationSection("options.secret-life.task-configs." + getTaskKey()));
    }

}
