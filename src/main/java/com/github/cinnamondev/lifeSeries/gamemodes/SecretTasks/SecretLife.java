package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.Lives;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.TaskLookup;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.Immutable;
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
import java.util.stream.Collectors;

public class SecretLife extends Lives implements SecretTasks {
    HashMap<UUID, PlayerTask> tasks = new HashMap<>();

    public SecretLife(LifeSeries p) {
        super(p);
    }

    @Override
    public void run() {
        p.getScoreHandler().updateAllTrackedScoresAndTeams((_uuid, score) -> score, (player, newTeam) -> {
            if (p.getScoreHandler().isPlayerSpectator(player)) {
                p.getServer().getScheduler().runTask(p, () -> player.setHealth(0));
            } else if (canGuessTask(newTeam)) {
                player.sendMessage(Component.translatable("secret-life.can-guess-tasks").color(NamedTextColor.YELLOW));
            }
        });
        p.getServer().getOnlinePlayers().forEach(player -> {
            TeamMeta team = p.getScoreHandler().getTeam(player);
            if (canGuessTask(player)) {
                player.sendActionBar(Component.text("You can guess tasks"));
            }
        });
    }

    @Override
    public void onGameStart() {
        if (p.getConfig().getBoolean("options.secret-life.auto-roll", false)) {
            p.getServer().getOnlinePlayers().forEach(player -> this.rollTask(
                    player,
                    p.getScoreHandler().getTeam(player),
                    true,
                    true
            ).givePlayerTaskBook(player));
        }
    }

    @Override
    public void onGameStop() {
        tasks.forEach((uuid, task) -> task.endOfSession());
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
    public Collection<String> getTasksOfDifficulty(SecretTasks.TaskDifficulty difficulty) {
        ConfigurationSection section = p.getConfig().getConfigurationSection("options.secret-life.task-deck");
        if (section == null) return Collections.emptyList();
        return switch (difficulty) {
            case SecretTasks.TaskDifficulty.EASY -> section.getStringList("easy.deck");
            case SecretTasks.TaskDifficulty.MEDIUM -> section.getStringList("normal.deck");
            case SecretTasks.TaskDifficulty.HARD -> section.getStringList("hard.deck");
            default -> Collections.emptyList();
        };
    }
    @Override
    public PlayerTask rollTasks(Player owningPlayer, Collection<String> taskList, boolean respectLimits, boolean add) throws RuntimeException{
        Collection<String> filteredTaskList;
        if (respectLimits) {
            filteredTaskList = taskList.stream()
                    .filter(taskName -> TaskLookup.getTaskAssignmentLimit(p, taskName)
                            .filter(limit -> searchForTaskByKey(taskName).size() < limit)
                            .isPresent()
                    ).toList();
        } else {
            filteredTaskList = taskList;
        }

        var task = TaskLookup.getTaskBuilderByKey(
                filteredTaskList.stream()
                        .skip((int) (filteredTaskList.size() * Math.random())).findFirst()
                        .orElseThrow(() -> new IllegalStateException("No task found")) // this shouldnt be a possible
        )
                .player(owningPlayer)
                .onCompletion(this::onTaskCompletion)
                .buildWithAnySettings(p, this);
        if (add) { addSecretTask(task); }
        return task;
    }


    @Override
    public void addSecretTask(PlayerTask secretTask) {
        removeSecretTask(secretTask.getTaskOwner()); // ensure everythings cleaned up.
        Bukkit.getPluginManager().registerEvents(secretTask, p);
        tasks.put(secretTask.getTaskOwner().getUniqueId(), secretTask);
        //secretTask.givePlayerTaskBook(secretTask.getTaskOwner());
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
        secretTask.getTaskOwner().sendMessage(Component.text("fail"));
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
        return p.getConfig().getStringList("options.secret-life.can-infinite-roll").stream()
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
