package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.Group;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.*;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringGroupTask extends AbstractSharedGroupTask implements SelfCompletableTask, SessionLongTask, PlayerTask {
    private final boolean isGuessable;


    public StringGroupTask(LifeSeries p, Collection<Player> owningPlayers, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayers, onTaskCompletion, difficulty);
        isGuessable = getConfigurationSection().map(c -> c.getBoolean("guessable", false)).orElse(false);

    }

    @Override
    public boolean conditionalCompleteTask() {
        return false;
    }

    @Override
    public boolean isTaskGuessable() {
        return isGuessable;
    }

    @Override
    public String getTaskKey() {
        return "custom-group-task";
    }

    @Override
    public PlayerTask.Builder<? extends PlayerTask.Builder<?>> builderProvider() {
        return null;
    }

    public class Builder extends PlayerTask.Builder<Builder> {
        protected String taskString = null;

        protected static Stream<ConfigurationSection> getAllStrings(Plugin p) {
            p.getConfig().getValues(true).entrySet()
                    .stream()
                    .filter(e -> e.getValue() instanceof ConfigurationSection);
            var section = p.getConfig()
                    .getConfigurationSection("options.secret-life.task-configs.custom-group-task");
            if (section == null) {
                p.getLogger().warning("no custom-group-task section found");
                return Collections.emptySet();
            }
            return section.getKeys(false);
        }
        protected static Stream<String> getAllowedStringsFor(Plugin p, TeamMeta team) {
            return getAllStrings(p).stream()
                    .filter(s -> {
                        var section = p.getConfig().getConfigurationSection(
                                "options.secret-life.task-configs.custom-group-task." + s
                        );
                        if (section == null) {
                            p.getLogger().warning("no group string task section found?");
                            return false;
                        }
                        return section.getStringList("allowed-teams").contains(team.getScoreboardTeam().getName());
                    });
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
