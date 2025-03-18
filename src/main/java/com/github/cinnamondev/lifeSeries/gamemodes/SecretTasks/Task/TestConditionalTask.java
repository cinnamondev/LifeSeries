package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.SelfCompletableTask;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.function.Consumer;

public class TestConditionalTask extends AbstractPlayerTask implements SelfCompletableTask {
    public TestConditionalTask(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, SecretTasks.TaskDifficulty difficulty) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
    }

    @Override
    public boolean conditionalCompleteTask() {
        return true;
    }

    @Override
    public boolean requireVerification() {
        return false;
    }

    @Override
    public boolean isTaskGuessable() {
        return false;
    }

    @Override
    public Component name() {
        return Component.text("Test Conditional Task");
    }

    @Override
    public Component description() {
        return Component.text("ConditionalTaskDescription");
    }

    @Override
    public String getTaskKey() {
        return "conditional-test-task";
    }

    public static class Builder<T extends AbstractPlayerTask.Builder<T>> extends AbstractPlayerTask.Builder<T> {
        public AbstractPlayerTask build(LifeSeries p) {
            return new TestConditionalTask(p, this.owningPlayer, this.onTaskCompletion, this.assignedDifficulty);
        }
        public AbstractPlayerTask buildWithAnySettings(LifeSeries p, SecretTasks game) {
            return new TestConditionalTask(p, this.owningPlayer, this.onTaskCompletion, this.assignedDifficulty);
        }
    }
}
