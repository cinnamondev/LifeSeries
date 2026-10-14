package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.Group;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.*;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

public class StringTask implements SelfCompletableTask, SessionLongTask, PlayerTask {

    public record Configuration(String taskName, boolean verification, boolean guessable, List<TeamMeta> allowedTeams) {
        public static Configuration fromConfigurationSection(LifeSeries p, ConfigurationSection section) {
            var name = section.getString("name");
            if (name == null || name.isEmpty()) { throw new IllegalArgumentException("no name provided for task"); }
            var verify =  section.getBoolean("verify", true);
            var guessable = section.getBoolean("guessable", false);
            var allowedTeams = section.getStringList("allowedTeams").stream()
                    .flatMap(str -> p.getScoreHandler().tryForTeam(str).stream()).toList();
            if (allowedTeams.isEmpty()) { throw new IllegalArgumentException("no allowedTeams provided for task / malformed"); }
            return new Configuration(name, verify, guessable, allowedTeams);
        }
    }

    @Override
    public boolean conditionalCompleteTask() {
        return false;
    }

    @Override
    public void complete() {

    }

    @Override
    public void fail() {

    }

    @Override
    public TaskStatus getTaskProgress() {
        return null;
    }

    @Override
    public boolean isTaskGuessable() {
        return false;
    }

    @Override
    public Optional<ConfigurationSection> getConfigurationSection() {
        return Optional.empty();
    }

    @Override
    public Player getTaskOwner() {
        return null;
    }

    @Override
    public TaskDifficulty getDifficulty() {
        return null;
    }

    @Override
    public ItemStack createTaskBook(Locale locale) {
        return null;
    }

    @Override
    public ConfigurationSection saveTask() {
        return null;
    }

    @Override
    public String getTaskKey() {
        return "";
    }

    @Override
    public PlayerTask.Builder<? extends PlayerTask.Builder<?>> builderProvider() {
        return null;
    }

    public class Builder extends PlayerTask.Builder<Builder> {
        protected String taskString = null;

        protected Set<String> getAllStrings(Plugin p) {
            var section = p.getConfig()
                    .getConfigurationSection("options.secret-life.task-configs.arbitrary-string-task");
            if (section == null) {
                p.getLogger().warning("no arbitrary-string-task section found");
                return Collections.emptySet();
            }
            return section.getKeys(false);
        }
        protected Set<String> getAllowedStringsFor(Plugin p, TeamMeta team) {
            return getAllStrings(p).stream()
                    .filter(s -> {
                        var section = p.getConfig().getConfigurationSection(
                                "options.secret-life.task-configs.arbitrary-string-task." + s
                        );
                        if (section == null) {
                            p.getLogger().warning("no specific string task section found?");
                            return false;
                        }
                        return section.getStringList("allowed-teams").contains(team.getScoreboardTeam().getName());
                    })
                    .collect(Collectors.toSet());
        }

        Builder string(String string) { this.taskString = string; return this; }

        Builder randomString(Plugin p) {
            var set = getAllStrings(p);
            this.taskString = set.stream().skip((int) (Math.random() * set.size())).findFirst().orElse(null);
            return this;
        }

        public Builder randomString(Plugin p, TeamMeta team) {
            var set = getAllowedStringsFor(p, team);
            this.taskString = set.stream().skip((int) (Math.random() * set.size())).findFirst().orElse(null);
            return this;
        }


        @Override
        public PlayerTask build(LifeSeries p) {
            return null;
        }
    }
}
