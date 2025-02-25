package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.Lives;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.TaskLookup;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.*;

public class SecretLife extends Lives implements SecretTasks {
    HashMap<UUID, com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask> tasks = new HashMap<>();

    public SecretLife(LifeSeries p) {
        super(p);
    }

    @Override
    public void run() {
        p.getServer().getOnlinePlayers().forEach(player -> {
            TeamMeta team = p.getScoreHandler().getTeam(player);
            if (canGuessTask(player)) {
                player.sendActionBar(
                        Component.text("You can guess tasks")
                );
            }
        });
    }

    @Override
    public Collection<String> getAllTasksForTeam(TeamMeta teamMeta) {
        ArrayList<String> tasks = new ArrayList<>();
        // search through task-deck and filter teams
        ConfigurationSection section = p.getConfig().getConfigurationSection("options.secret-life.task-deck");
        if (section == null) { return Collections.emptyList(); }

        section.getKeys(false).forEach(k -> { // for each difficulty
            section.getStringList(k + ".teams").stream()
                    .filter(str -> str.equalsIgnoreCase(teamMeta.getScoreboardTeam().getName()))
                    .findFirst().ifPresent(_str -> { // if team is in "difficulty.team"
                        tasks.addAll(section.getStringList(k + ".deck")); // add all tasks in deck.
                    });
        });
        return List.of();
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
    public PlayerTask rollTasks(Player owningPlayer, Collection<String> taskList) {
        return TaskLookup.getTaskBuilderByKey(
                taskList.stream().skip((int) (taskList.size() * Math.random())).findFirst()
                        .orElseThrow(() -> new IllegalStateException("No task found")) // this shouldnt be a possible
        )
                .player(owningPlayer)
                .onCompletion(this::onTaskCompletion)
                .buildWithAnySettings(p, this);
    }


    @Override
    public void addSecretTask(com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask secretTask) {
        removeSecretTask(secretTask.getTaskOwner()); // ensure everythings cleaned up.
        Bukkit.getPluginManager().registerEvents(secretTask, p);
        tasks.put(secretTask.getTaskOwner().getUniqueId(), secretTask);
        secretTask.givePlayerTaskBook(secretTask.getTaskOwner());
    }

    @Override
    public void onTaskSuccess(com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask secretTask) {
        secretTask.getTaskOwner().sendMessage(Component.text("success"));
        int reward = p.getConfig().getInt("options.secret-life.rewards.task-success", 0);
        if (reward > 0) {
            p.getScoreHandler().updatePlayerScoreAndTeam(secretTask.getTaskOwner(), (_uuid, score) ->
                    score + reward
            );
        }

    }

    @Override
    public void onTaskFailure(com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask secretTask) {
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
    public Optional<com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask> getSecretTask(OfflinePlayer taskOwner) {
        return Optional.ofNullable(tasks.get(taskOwner.getUniqueId()));
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
}
