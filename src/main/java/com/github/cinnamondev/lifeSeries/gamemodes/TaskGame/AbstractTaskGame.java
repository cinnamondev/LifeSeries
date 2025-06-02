package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.Game;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.*;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import net.kyori.adventure.text.Component;
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

public abstract class AbstractTaskGame implements TaskGame, Game {
    protected final LifeSeries p;
    protected HashMap<UUID, PlayerTask> tasks = new HashMap<>();
    private boolean gameStarted = false; // used as an indicator for auto roll listener.
    protected AbstractTaskGame(LifeSeries p) {
        this.p = p;
    }

    @Override
    public void onServerDisable() {
        tasks.values().forEach(PlayerTask::cleanup);
    }

    @Override
    public Map<UUID, PlayerTask> assignedTasks() {
        return Collections.unmodifiableMap(tasks);
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
            rollTasks(
                    p.getScoreHandler().getAllAliveOnlinePlayers().stream()
                            .filter(player -> !player.hasPermission("life.gamemaster"))
                            .toList(),
                    RollMode.TEAM,
                    true,
                    true
            );
        }
    }

    @Override
    public void onGameStop() {
        gameStarted = false;
        tasks.forEach((uuid, task) -> task.endOfSession());
        tasks.clear();
    }

    @Override
    public List<TaskMeta> getConfiguredTasks(TaskDifficulty difficulty) {
        ConfigurationSection section = p.getConfig().getConfigurationSection("options.secret-life.task-deck");
        if (section == null) return Collections.emptyList();
        List<String> keys = switch (difficulty) {
            case TaskDifficulty.EASY -> section.getStringList("easy.deck");
            case TaskDifficulty.MEDIUM -> section.getStringList("normal.deck");
            case TaskDifficulty.HARD -> section.getStringList("hard.deck");
            default -> Collections.emptyList();
        };
        return keys.stream().map(k ->
            new TaskMeta(
                    k,
                    difficulty,
                    TaskLookup.getTaskAssignmentMinimum(p, k),
                    TaskLookup.getTaskAssignmentLimit(p,k),
                    searchForTaskByKey(k).size()
            )
        ).toList();
    }

    @Override
    public List<TaskDifficulty> availableDifficultiesForTeam(TeamMeta teamMeta) {
        // search through task-deck and filter teams
        ConfigurationSection section = p.getConfig().getConfigurationSection("options.secret-life.task-deck");
        if (section == null) { return Collections.emptyList(); }

        return section.getKeys(false).stream().filter(deckName ->
                        section.getStringList(deckName + ".teams").stream()
                                .anyMatch(str -> str.equalsIgnoreCase(teamMeta.getScoreboardTeam().getName()))
                )
                .flatMap(str -> TaskDifficulty.tryFromString(str).stream())
                .toList();
    }

    @Override
    public List<PlayerTask> rollTasks(List<Player> players, RollMode rollMode, boolean respectLimits, boolean add) {
        //  TODO: this will need bettering later.  this is kinda scrappy ? (the whole function, really)
        var rollableTasks = switch (rollMode) {
            case EASY -> getConfiguredTasks(TaskDifficulty.EASY);
            case MEDIUM -> getConfiguredTasks(TaskDifficulty.MEDIUM);
            case HARD -> getConfiguredTasks(TaskDifficulty.HARD);
            case TEAM, ANY -> getAllTasks();
        };

        ArrayList<TaskMeta> potentialTasks = new ArrayList<>(rollableTasks);
        ArrayList<TaskMeta> encounteredTasks = new ArrayList<>(potentialTasks.size());

        ArrayList<Map.Entry<Player, TeamMeta>> remainingCandidates = players.stream().map(player ->
                Map.entry(player, rollMode == RollMode.TEAM ? p.getScoreHandler().getTeam(player)
                        : p.getScoreHandler().getSpectatorTeam()) // calling getTeam can have unintended side effects so
                                                                  // if we don't specify team mode, we should just use a
                                                                  // fallback value.
        ).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<PlayerTask> finalTasks = new ArrayList<>(players.size());


        while ((!remainingCandidates.isEmpty() && !potentialTasks.isEmpty()) || (potentialTasks.isEmpty() && !encounteredTasks.isEmpty())) {
            if (respectLimits) { // filter out tasks by assignment limit
                potentialTasks = potentialTasks.stream().filter(meta ->
                        // this may not make sense immediately, but pretty much we want to keep tasks where the
                        // number of assigned players is less than the configured max, and any tasks where there is no
                        // minimum, and if there is a minimum, we need to only keep tasks where there are less assigned
                        // than the minimum.
                    meta.assigned() < meta.max()
                            && (meta.min() == -1 || meta.assigned() <= meta.min())
                            && remainingCandidates.size() >= meta.min()
                ).collect(Collectors.toCollection(ArrayList::new));
            }

            // rotate in encounteredTasks if its not empty and potentialTasks is. potential tasks will be whittled down
            if (potentialTasks.isEmpty() && !encounteredTasks.isEmpty()) {
                potentialTasks.addAll(encounteredTasks);  // rotate in the 'used task' list and shuffle it, and try again
                encounteredTasks.clear();
                Collections.shuffle(potentialTasks);
                continue;
            } else if (potentialTasks.isEmpty()) { break; } // if potential tasks is empty we cant go further anyway.

            // select random player
            var candidate = remainingCandidates.get((int) (remainingCandidates.size() * Math.random()));

            // filter down available tasks by what the candidate can do (if roll mode is appropriate)
            ArrayList<TaskMeta> candidateTasks;
            if (rollMode == RollMode.TEAM) {
                var teamDecks = availableDifficultiesForTeam(p.getScoreHandler().getTeam(candidate.getKey()));
                candidateTasks = potentialTasks.stream()
                        .filter(meta -> teamDecks.contains(meta.difficulty()))
                        .collect(Collectors.toCollection(ArrayList::new));
            } else { candidateTasks = potentialTasks; }
            // select random task
            // TODO: make it less likely players overlap their tasks. maybe use `Collections.shuffle` then iterate through it.
            // once we're  at the end of the list shuffle again until we're out of tasks
            if (candidateTasks.isEmpty()) {
                p.getLogger().warning("No candidate tasks found for " + candidate.getKey().getName());
                remainingCandidates.remove(candidate);
                continue;
            }
            var task = candidateTasks.get((int) (candidateTasks.size() * Math.random()));

            encounteredTasks.add(task); // task is now 'visited', roll them into encountered tasks.
            potentialTasks.remove(task);

            var builder = task.builder();

            // this is the worst part! i am the list juggler i love lists i love lists i love lists.
            // luckily you only encounter this for group tasks
            if (builder instanceof AbstractSharedGroupTask.Builder<?> sharedBuilder) {
                ArrayList<Map.Entry<Player, TeamMeta>> additionalCandidates;
                if (rollMode == RollMode.TEAM) {
                    additionalCandidates = remainingCandidates.stream()
                            .filter(e -> e.getValue().equals(candidate.getValue()))
                            .filter(e -> e.equals(candidate))
                            .collect(Collectors.toCollection(ArrayList::new));
                } else { additionalCandidates = remainingCandidates; }

                // clamp ranges of configuration values if they are weird for a group task or unassigned..
                int min = respectLimits ? Math.max(task.assigned(), task.min()) : task.min();
                if (min == -1) { min = 1; } // if unassigned

                int max = task.max();
                // if we dont respect limits let them go ham otherwise we should try to fallback to a nicer number.
                if (max == Integer.MAX_VALUE && respectLimits) {
                    max = p.getConfig().getInt("options.secret-life.group-tasks.default-assignment-limit", 8);
                }
                // n-1 (to accomodate candidateplayer) chosen randomly to fill.
                int nToAssign = min + (int) (Math.random() * (max - min))-1;
                Collections.shuffle(additionalCandidates);

                var finalCandidates = additionalCandidates.subList(0, nToAssign);
                finalCandidates.add(candidate);

                builder = sharedBuilder.players(finalCandidates.stream().map(Map.Entry::getKey).toList());
                finalCandidates.forEach(remainingCandidates::remove);
            } else {
                // and here is the nice normal part where we just set the candidate and call it a day
                builder = builder.player(candidate.getKey());
                remainingCandidates.remove(candidate);
            }

            // select random target (we dont care if its in the pool)
            if (builder instanceof TargetedPlayerTask.Builder<?> targetedBuilder) {
                builder = targetedBuilder.randomTarget();
            }

            var playerTask = builder.difficulty(task.difficulty()).onCompletion(this::onTaskCompletion).build(p);

            if (playerTask instanceof AbstractSharedGroupTask sharedTask) { // turn a sharedTask immediately into
                finalTasks.addAll(sharedTask.getGluedTasks().values());     // the player's GroupTask's.
            } else {
                finalTasks.add(playerTask);
            }
        }

        if (add) { finalTasks.forEach(this::addSecretTask); }
        return finalTasks;
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
        tasks.computeIfPresent(taskOwner.getUniqueId(), (uuid, task) -> {
            // if we remove a grouptask it should also be removed from its common task
            if (task instanceof GroupTask groupTask) { groupTask.getSharedTask().getGluedTasks().remove(uuid); }
            HandlerList.unregisterAll(task);
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
