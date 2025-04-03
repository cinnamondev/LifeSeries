package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.Game;
import com.github.cinnamondev.lifeSeries.gamemodes.Lives;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.AbstractTargetedPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.TargetedPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.TaskDifficulty;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.TaskLookup;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import com.google.common.collect.ImmutableMap;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.*;

public abstract class AbstractSecretTasks implements SecretTasks, Game {
    protected final LifeSeries p;
    protected HashMap<UUID, PlayerTask> tasks = new HashMap<>();
    private boolean gameStarted = false; // used as an indicator for auto roll listener.
    protected AbstractSecretTasks(LifeSeries p) {
        this.p = p;
    }

    @Override
    public void onServerDisable() {
        tasks.values().forEach(PlayerTask::cleanup);
    }

    @Override
    public void run() {
        p.getServer().getOnlinePlayers().forEach(player -> {
            TeamMeta team = p.getScoreHandler().getTeam(player);
            if (canGuessTask(player)) {
                player.sendActionBar(Component.text("You can guess tasks"));
            }
        });
    }
    // TODO: save task progress to save file.
    @Override
    public void restoreStateFromSave() {

    }

    @Override
    public void clearSaveData() {

    }

    @Override
    public void onGameStart() {
        gameStarted = true;
        if (p.getConfig().getBoolean("options.secret-life.auto-roll", false)) {
            p.getScoreHandler().getAllAliveOnlinePlayers().forEach(player -> this.rollTask(
                    player,
                    p.getScoreHandler().getTeam(player),
                    true,
                    true
            ));
        }
    }

    @Override
    public void onGameStop() {
        gameStarted = false;
        tasks.forEach((uuid, task) -> task.endOfSession());
        tasks.clear();
    }

    @Override
    public Collection<String> getAllTasksForTeam(TeamMeta teamMeta) {
        // search through task-deck and filter teams
        ConfigurationSection section = p.getConfig().getConfigurationSection("options.secret-life.task-deck");
        if (section == null) { return Collections.emptyList(); }

        return section.getKeys(false).stream().filter(deckName ->
                        section.getStringList(deckName + ".teams").stream()
                                .anyMatch(str -> str.equalsIgnoreCase(teamMeta.getScoreboardTeam().getName()))
                ).flatMap(deckName -> section.getStringList(deckName + ".deck").stream())
                .toList();
    }

    @Override
    public Collection<String> getTasksOfDifficulty(TaskDifficulty difficulty) {
        ConfigurationSection section = p.getConfig().getConfigurationSection("options.secret-life.task-deck");
        if (section == null) return Collections.emptyList();
        return switch (difficulty) {
            case TaskDifficulty.EASY -> section.getStringList("easy.deck");
            case TaskDifficulty.MEDIUM -> section.getStringList("normal.deck");
            case TaskDifficulty.HARD -> section.getStringList("hard.deck");
            default -> Collections.emptyList();
        };
    }
    @Override
    public PlayerTask rollTasks(Player owningPlayer, Collection<String> taskList, boolean respectLimits, boolean add) throws RuntimeException{
        Collection<String> filteredTaskList;
        if (respectLimits) {
            filteredTaskList = taskList.stream()
                    .filter(taskName ->
                            searchForTaskByKey(taskName).size() < TaskLookup.getTaskAssignmentLimit(p, taskName))
                    .toList();
        } else {
            filteredTaskList = taskList;
        }

        var taskBuilder = TaskLookup.getTaskBuilderByKey(
                filteredTaskList.stream()
                        .skip((int) (filteredTaskList.size() * Math.random())).findFirst()
                        .orElseThrow(() -> new IllegalStateException("No task found")));

        if (taskBuilder instanceof TargetedPlayerTask.Builder<?> targetTaskBuilder) {
            taskBuilder = targetTaskBuilder.randomTarget(p);
        }
        var task = taskBuilder
                .onCompletion(this::onTaskCompletion)
                .player(owningPlayer)
                .build(p);
        if (add) { addSecretTask(task); }
        return task;
    }


    @Override
    public void addSecretTask(PlayerTask secretTask) {
        removeSecretTask(secretTask.getTaskOwner()); // ensure everythings cleaned up.
        Bukkit.getPluginManager().registerEvents(secretTask, p);
        tasks.put(secretTask.getTaskOwner().getUniqueId(), secretTask);
        secretTask.givePlayerTaskBook(secretTask.getTaskOwner());
    }

    @Override
    public void onTaskSuccess(PlayerTask secretTask) {
        secretTask.getTaskOwner().sendMessage(Component.translatable("secret-life.task-completed.message"));
        int reward = p.getConfig().getInt("options.secret-life.rewards.task-success", 0);
        if (reward > 0) {
            p.getScoreHandler().updatePlayerScoreAndTeam(secretTask.getTaskOwner(), (_uuid, score) ->
                    score + reward
            );
        }

    }

    @Override
    public void onTaskFailure(PlayerTask secretTask) {
        secretTask.getTaskOwner().sendMessage(Component.translatable("secret-life.failed-task"));
        int punishment = p.getConfig().getInt("options.punishment.task-failure", 0);
        if (punishment > 0) {
            p.getScoreHandler().updatePlayerScoreAndTeam(secretTask.getTaskOwner(), (_uuid, score) ->
                    score - punishment
            );
        }

    }

    @Override
    public void removeSecretTask(OfflinePlayer taskOwner) {
        tasks.computeIfPresent(taskOwner.getUniqueId(), (_uuid, listener) -> {
            HandlerList.unregisterAll(listener);
            return null;
        });
    }

    @Override
    public Optional<PlayerTask> getSecretTask(OfflinePlayer taskOwner) {
        return Optional.ofNullable(tasks.get(taskOwner.getUniqueId()));
    }

    @Override
    public Collection<PlayerTask> getAllSecretTasks() {
        return this.tasks.values();
    }

    @Override
    public boolean canGuessTask(TeamMeta team) {
        return p.getConfig().getStringList("options.secret-life.can-guess-tasks").stream()
                .anyMatch(str -> str.equalsIgnoreCase(team.getScoreboardTeam().getName()));
    }

    @Override
    public boolean canGuessTask(OfflinePlayer guesser) {
        if (guesser instanceof Player onlineGuesser) {
            return canGuessTask(p.getScoreHandler().getTeam(onlineGuesser)) || onlineGuesser.hasPermission("life.can-always-guess-tasks");
        } else {
            return canGuessTask(p.getScoreHandler().getTeam(guesser));
        }
    }
    public boolean canRerollTask(TeamMeta reroller) {
        return p.getConfig().getStringList("options.secret-life.can-reroll.teams").stream()
                .anyMatch(str -> str.equalsIgnoreCase(reroller.getScoreboardTeam().getName()));
    }
    public boolean canRerollTask(OfflinePlayer reroller) {
        if (reroller instanceof Player onlineReroller) {
            return canRerollTask(p.getScoreHandler().getTeam(reroller)) || onlineReroller.hasPermission("life.can-infinite-roll");
        } else {
            return canRerollTask(p.getScoreHandler().getTeam(reroller));
        }
    }

    public static ItemStack baseBook(Plugin p, int n) {
        ItemStack item = ItemStack.of(Material.WRITTEN_BOOK,n);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(p, "taskBook"), PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }
}
