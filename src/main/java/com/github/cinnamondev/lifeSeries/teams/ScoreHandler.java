package com.github.cinnamondev.lifeSeries.teams;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.util.ColourConverter;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScoreHandler {
    protected LifeSeries p;
    private final Scoreboard scoreboard;
    private final FileConfiguration save;
    private ConfigurationSection playerData;
    private final SortedSet<TeamMeta> rankedTeams;
    private final TeamMeta spectatorTeam;

    public ScoreHandler(LifeSeries p) {
        this.p = p;
        this.save = p.getSave();
        this.scoreboard = p.getServer().getScoreboardManager().getMainScoreboard();
        this.playerData = save.getConfigurationSection("players");
        if (playerData == null) { this.playerData = save.createSection("players"); }

        this.rankedTeams = loadTeamsFromConfig(
                Objects.requireNonNull(p.getConfig().getConfigurationSection("categories"))
        );
        Team team = scoreboard.getTeam("Dead");
        if (team != null) { team.unregister(); }
        team = scoreboard.registerNewTeam("Dead");
        team.color(NamedTextColor.GRAY);
        team.setAllowFriendlyFire(true);
        team.setCanSeeFriendlyInvisibles(true);

        this.spectatorTeam = new TeamMeta(team, Collections.emptyList(), -1);
    }

    private SortedSet<TeamMeta> loadTeamsFromConfig(ConfigurationSection categories) {
        return categories.getKeys(false).stream().map(name -> {
            scoreboard.getTeams().stream().filter(team -> team.getName().equals(name))
                    .forEach(Team::unregister); // unregister any hangers
           ConfigurationSection config = categories.getConfigurationSection(name);

            NamedTextColor colour = ColourConverter.tryNamedColourFromString(config.getString("colour"))
                    .orElse(NamedTextColor.LIGHT_PURPLE);

            Team team = scoreboard.registerNewTeam(name);

            team.color(colour);
            team.setAllowFriendlyFire(true);
            team.setCanSeeFriendlyInvisibles(false);
            return new TeamMeta(team, config.getStringList("can-kill"), config.getInt("score"));
        }).collect(Collectors.toCollection(() -> new TreeSet<>(
                Comparator.comparingInt(TeamMeta::getMininumScore).reversed()
        )));
    }

    public int getUntrackedScore() {
        int score = save.getInt("untrackedScore",-1);
        if (score == -1) {
            score = p.getConfig().getInt("starting-score");
            save.set("untrackedScore", score);
        }
        return score;
    }
    public void setUntrackedScore(int untrackedScore) { save.set("untrackedScore", untrackedScore); }
    public int addUntrackedScore(int score) {
        int newScore = Math.max(getUntrackedScore() + score,0);
        setUntrackedScore(newScore);
        return newScore;
    }

    /**
     * returns the player score. if the player is not already tracked, they will have a default score assigned.
     * @param uuid uuid of player
     * @return score
     */
    public int getScore(UUID uuid) {
        int score = playerData.getInt(uuid.toString() + ".score", -1);
        if (score == -1) {
            score = getUntrackedScore();
            playerData.set(uuid + ".score", score);
        }
        return score;
    }
    /// {@link ScoreHandler#getScore(OfflinePlayer)}
    public int getScore(OfflinePlayer player) {  return getScore(player.getUniqueId()); }
    protected void setScore(UUID uuid, int score) { playerData.set(uuid.toString() + ".score", score); }
    protected int addScore(UUID uuid, int score) {
        int newScore = Math.max(getScore(uuid) + score, 0);
        setScore(uuid, newScore);
        return newScore;
    }

    public Optional<TeamMeta> tryForTeam(String teamName) {
        return rankedTeams.stream().filter(teamMeta -> teamMeta.getScoreboardTeam().getName().equals(teamName)).findFirst();
    }
    ///  get team corresponding to provided score
    public TeamMeta getTeam(int score) {
        return rankedTeams.stream().filter(_team -> score >= _team.getMininumScore()).findFirst().orElse(spectatorTeam);
    }
    ///  get team corresponding to uuid (like all of these methods, the 'default' score will be used where it doesnt exist)
    public TeamMeta getTeam(UUID uuid) { return getTeam(getScore(uuid)); }
    ///  {@link ScoreHandler#getTeam(UUID)}
    public TeamMeta getTeam(OfflinePlayer player) { return getTeam(getScore(player.getUniqueId())); }
    public TeamMeta getSpectatorTeam() { return this.spectatorTeam; }
    public boolean isPlayerSpectator(UUID uuid) { return getTeam(uuid).equals(spectatorTeam); }
    public boolean isPlayerSpectator(OfflinePlayer player) { return getTeam(player).equals(spectatorTeam); }

    public SortedSet<TeamMeta> getRankedTeams() { return new TreeSet<>(this.rankedTeams); }
    /**
     * Updates a player score according to the result of `updater`. Works for any UUID, falls back to default score if
     * they were not already tracked.
     *
     * If player is online *and* their team changes score is below minimum, `onTeamChange` will be called. The players
     * team will always be updated.
     *
     * NOTE: This function does not ensure players who 'transition' to a dead team are killed. Generally you should
     * be looking at {@link ScoreHandler#updatePlayerScoreAndTeam(UUID, BiFunction)}
     * instead. If you call this function, you should generally make sure that spectators are handled if appropriate.
     * (excluding i.e. revival clock)
     *
     * @param uuid UUID of player (online or offline, tracked or untracked)
     * @param updater
     * @param onTeamChange
     */
    public void updatePlayerScoreAndTeam(UUID uuid,
                                         BiFunction<UUID, Integer, Integer> updater,
                                         BiConsumer<Player, TeamMeta> onTeamChange) {
        TeamMeta oldTeam = getTeam(uuid);
        setScore(uuid, Math.max(updater.apply(uuid, getScore(uuid)),0));
        TeamMeta newTeam = getTeam(uuid);
        Player player = p.getServer().getPlayer(uuid);
        if (player != null) {
            Team scoreboardTeam = scoreboard.getPlayerTeam(player); // attempt to update
            Team newScoreboardTeam = newTeam.getScoreboardTeam();
            if (scoreboardTeam != null) {
                scoreboardTeam.removePlayer(player);
            }
            newScoreboardTeam.addPlayer(player);
            if (!oldTeam.equals(newTeam)) { // changed team
                onTeamChange.accept(player, newTeam);
            }
        }
    }
    /// {@link ScoreHandler#updatePlayerScoreAndTeam(OfflinePlayer, BiFunction, BiConsumer)}
    public void updatePlayerScoreAndTeam(OfflinePlayer player,
                                         BiFunction<UUID, Integer, Integer> updater,
                                         BiConsumer<Player, TeamMeta> onlinePlayerTeamHasChanged) {
        updatePlayerScoreAndTeam(player.getUniqueId(), updater, onlinePlayerTeamHasChanged);
    }

    /**
     * Update a player score according to `updater`. Works for any player UUID, even if it was not being tracked before.
     * Will use default score value if it is not set prior. If the player is online and runs out of score, they will
     * change team to spectatorTeam and be killed.
     * @param uuid UUID of player (online or offline, tracked or untracked)
     * @param updater
     */
    public void updatePlayerScoreAndTeam(UUID uuid,  BiFunction<UUID, Integer, Integer> updater) {
        updatePlayerScoreAndTeam(uuid, updater, (player, team) -> {
            if (isPlayerSpectator(uuid)) { p.getServer().getScheduler().runTask(p, () -> player.setHealth(0)); }
        });
    }
    
    /// {@link ScoreHandler#updatePlayerScoreAndTeam(UUID, BiFunction)}
    public void updatePlayerScoreAndTeam(OfflinePlayer player,  BiFunction<UUID, Integer, Integer> updater) {
        updatePlayerScoreAndTeam(player.getUniqueId(), updater);
    }

    /// update all tracked players score according to the returned value of `updater`. works for any player uuid,
    /// if they are not already tracked, they will be assigned the default score before `updater` is called.
    /// if the player is online, and they change team category (including running out of time),
    /// `onlinePlayerTeamHasChanged` will be called.
    public void updateAllTrackedScoresAndTeams(BiFunction<UUID, Integer, Integer> updater,
                                               BiConsumer<Player,TeamMeta> onlinePlayerTeamHasChanged) {
        List<UUID> deadPlayers = new ArrayList<>();
        playerData.getKeys(false).forEach(uuidString -> {
            UUID uuid = UUID.fromString(uuidString);
            updatePlayerScoreAndTeam(uuid, updater, onlinePlayerTeamHasChanged);
        });
    }

    /// update all tracked players score according to the returned value of `updater`. works for any player uuid,
    /// if they are not already tracked, they will be assigned the default score before `updater` is called.
    /// if the player is online, and they run out of time (player has changed team to spectatorTeam), the player will be
    /// killed.
    public void updateAllTrackedScoresAndTeams(BiFunction<UUID, Integer, Integer> updater) {
        List<UUID> deadPlayers = new ArrayList<>();
        playerData.getKeys(false).forEach(uuidString -> {
            UUID uuid = UUID.fromString(uuidString);
            updatePlayerScoreAndTeam(uuid, updater);
        });
    }

    /// Similar to `updateAllTrackedScoresAndTeams`, except it will ensure all online players are currently being
    /// tracked.
    public void updateTrackableScoresAndTeams(BiFunction<UUID, Integer, Integer> updater) {
        p.getServer().getOnlinePlayers().forEach(this::getScore);
        updateAllTrackedScoresAndTeams(updater);
    }

    public List<Player> getAllAliveOnlinePlayers() {
        return p.getServer().getOnlinePlayers().stream()
                .filter(player -> !isPlayerSpectator(player))
                .map(player -> (Player) player) // cast away from ? extends Player
                .toList();
    }

    public Stream<UUID> listTrackedUUIDs() {
        return playerData.getKeys(false).stream().map(UUID::fromString);
    }

    public Stream<OfflinePlayer> listTrackedPlayers() {
        return playerData.getKeys(false).stream()
                .map(uuidString -> p.getServer().getOfflinePlayer(UUID.fromString(uuidString)));
    }
}
