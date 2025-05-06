package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.commands.SpecialArguments.GreedyPlayerSelector;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.apache.commons.lang3.ClassUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.ObjectInputFilter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractSharedGroupTask extends AbstractPlayerTask {
    protected Map<UUID, GroupTask> tasks;
    protected UUID taskUUID = UUID.randomUUID();

    public AbstractSharedGroupTask(LifeSeries p, Collection<Player> owningPlayers, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayers.stream().findFirst().orElseThrow(), onTaskCompletion, difficulty);
        tasks = owningPlayers.stream()
                .map(player -> Map.entry(player.getUniqueId(), new GroupTask.Builder().task(this).player(player).onCompletion(onTaskCompletion).build(p)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    public UUID getTaskUUID() { return this.taskUUID; }
    public Map<UUID, GroupTask> getGluedTasks() { return this.tasks; }

    @Override
    public void complete() {
        cleanup();
        this.status = PlayerTask.TaskStatus.COMPLETE;
        tasks.values().forEach(groupTask -> groupTask.getTaskConsumer().accept(this));
    }

    @Override
    public void fail() {
        cleanup();
        this.status = PlayerTask.TaskStatus.FAILED;
        tasks.values().forEach(groupTask -> groupTask.getTaskConsumer().accept(this));
    }

    @Override
    public ConfigurationSection saveTask() {
        // groupTasks
        //   task-key
        //     uuid
        //       ...
        ConfigurationSection save = p.getSave().getConfigurationSection("groupTasks");
        if (save == null) { save = p.getSave().createSection("groupTasks"); }

        ConfigurationSection taskCategory = save.getConfigurationSection(getTaskKey());
        if (taskCategory == null) { taskCategory = save.createSection(getTaskKey()); }

        ConfigurationSection taskSection = taskCategory.createSection(getTaskUUID().toString());
        taskSection.set("progress", getTaskProgress());

        return taskSection;
    }

    public abstract static class Builder<T extends Builder<T>> extends PlayerTask.Builder<T> {
        protected Collection<Player> players;
        protected UUID taskUUID = UUID.randomUUID();

        protected List<Player> resolveAllPlayers(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
            return Stream.concat(
                    ctx.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).stream(),
                    ((List<Player>) ctx.getArgument("additionalPlayers", List.class)).stream()
            ).toList();
        }

        public LiteralArgumentBuilder<CommandSourceStack> builderCommand(LifeSeries p, LiteralArgumentBuilder<CommandSourceStack> root, Consumer<PlayerTask> onTaskAdded, Consumer<PlayerTask> onTaskCompletion) {
            Command<CommandSourceStack> executor = ctx -> {
                TaskDifficulty difficulty = ctx.getArgument("difficulty", TaskDifficulty.class);;
                var players = resolveAllPlayers(ctx);
                this.players(players).difficulty(difficulty).onCompletion(onTaskCompletion).build(p)
                        .getGluedTasks().values().forEach(onTaskAdded);
                return 1;
            };

            return root
                    .then(Commands.argument("additionalPlayers", new GreedyPlayerSelector()).executes(executor))
                    .executes(executor);
        }

        public T players(Player... players) { this.players = Arrays.stream(players).toList(); return (T) this; }
        public T players(Collection<Player> players) { this.players = players; return (T) this; }
        /// optional argument to set the uuid of the group task so we can restore.
        public T uuid(UUID taskUUID) { this.taskUUID = taskUUID; return (T) this; }
        @Override
        public T player(Player owningPlayer) { return this.players(owningPlayer); }

        @Override public abstract AbstractSharedGroupTask build(LifeSeries p);
    }
}
