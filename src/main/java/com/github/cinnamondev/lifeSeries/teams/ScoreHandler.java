package com.github.cinnamondev.lifeSeries.teams;

import com.github.cinnamondev.lifeSeries.util.ColourConverter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ScoreHandler {
    private final Plugin p;
    private final ScoreboardManager manager;
    private final Scoreboard scoreboard;

    private final TeamMeta spectatorTeam;
    private final SortedSet<TeamMeta> rankedTeams;

    private int untrackedPlayerScore;
    private final HashMap<UUID, Integer> playerScores;

    public ScoreHandler(Plugin p, HashMap<UUID,Integer> playerScores, int defaultScore) {
        this.p = p;
        this.manager = p.getServer().getScoreboardManager();
        this.scoreboard = manager.getMainScoreboard();
        this.rankedTeams = getSortedMetaFromConfig();
        this.playerScores = playerScores;
        Team spectatorTeam = scoreboard.getTeam("spectator");
        if (spectatorTeam == null) { spectatorTeam = scoreboard.registerNewTeam("spectator"); }
        this.spectatorTeam = new TeamMeta(
                spectatorTeam,
                "Dead",
                NamedTextColor.GRAY,
                Collections.emptyList(), // spectators cannot kill anyone and be rewarded time.
                -1
        );
    }
    public ScoreHandler(Plugin p, int defaultScore) { this(p, new HashMap<>(), defaultScore); }

    private SortedSet<TeamMeta> getSortedMetaFromConfig() {
        ConfigurationSection categories = p.getConfig().getConfigurationSection("categories");
        return categories.getKeys(false).stream()
                .map(name -> {
                    var teamConfig = categories.getConfigurationSection(name);
                    // if getstring yields `null`, then something really weird is going on anyway.
                    NamedTextColor colour = ColourConverter.tryNamedColourFromString(teamConfig.getString("colour"))
                            .orElseGet(() -> {
                                p.getLogger().warning("Couldn't interpret colour string for team " + name);
                                return NamedTextColor.WHITE;
                            });

                    // register team
                    Team team = scoreboard.getTeam(name);
                    if (team == null) {
                        team = scoreboard.registerNewTeam(name);
                    }

                    team.color(colour);
                    team.setAllowFriendlyFire(true);
                    team.setCanSeeFriendlyInvisibles(false);

                    return new TeamMeta(
                            team,
                            name,
                            colour,
                            teamConfig.getStringList("can-kill"),
                            teamConfig.getInt("lives")
                    );
                })
                .collect(Collectors.toCollection(() ->
                        new TreeSet<TeamMeta>(Comparator.comparingInt(TeamMeta::getMininumScore).reversed())
                ));
    }

    public HashMap<UUID, Integer> getScores() { return playerScores; }
    public int getScore(UUID uuid) {
        Integer score = playerScores.get(uuid);
        if (score == null) {
            playerScores.put(uuid, untrackedPlayerScore);
            score = untrackedPlayerScore;
        }
        return score;
    }
    public int getScore(OfflinePlayer player) { return getScore(player.getUniqueId()); }
    /// update player (identified by uuid)'s score with an absolute value >=0. if enabled, when player team is updated
    /// and that player is on 0 time, the function will return true (to allow specific gamemode implementation to do
    /// something (usually kill) with that player)
    public boolean setScore(UUID uuid, int score, boolean updateTeamWhenOnline) {
        var oldTeam = getTeam(uuid);
        playerScores.put(uuid, Math.max(score,0));
        var newTeam = getTeam(uuid);
        if (!newTeam.equals(oldTeam) && updateTeamWhenOnline) {
            Player player = p.getServer().getPlayer(uuid);
            if (player != null) {
                oldTeam.getScoreboardTeam().removePlayer(player);
                newTeam.getScoreboardTeam().addPlayer(player);
                if (getScore(uuid) == 0) { return true; } // player should be put into a 'just died' state!
            }
        }
        return false;
    }
    public boolean setScore(OfflinePlayer player, int score, boolean updateTeamWhenOnline) { return setScore(player.getUniqueId(), score, updateTeamWhenOnline); }
    public boolean addScore(UUID uuid, int score, boolean updateTeamWhenOnline) { return setScore(uuid, getScore(uuid) + score, updateTeamWhenOnline); }
    public boolean addScore(OfflinePlayer player, int score, boolean updateTeamWhenOnline) { return addScore(player.getUniqueId(), score, updateTeamWhenOnline); }

    public int addUntrackedPlayerScore(int value) {
        return untrackedPlayerScore = Math.max(untrackedPlayerScore + value,0);
    }
    public void setUntrackedPlayerScore(int defaultScore) { untrackedPlayerScore = defaultScore; }
    public int getUntrackedPlayerScore() { return untrackedPlayerScore; }

    /// get player's team according to THEIR GAME SCORE. if player's actual current team needs to be considered
    /// (in this case assuming you are setting scores with `updateTeamWhenOnline`=`false`), get their SB team from
    /// minecraft.
    public TeamMeta getTeam(OfflinePlayer player) {
        return getTeam(player.getUniqueId());
    }
    public TeamMeta getTeam(UUID uuid) { return getTeam(getScore(uuid)); }
    public TeamMeta getTeam(int score) {
        return rankedTeams.stream()
                .filter(team -> score > team.getMininumScore()) // should not be >=, players on boundry
                .findFirst()                                                // will be downgraded to next tier.
                .orElse(this.spectatorTeam); // player has 0 time left!
    }
    /// update all scores according to a user defined function (functionally identical to `replaceAll`). if
    /// players belong to a new team afterwards, their scoreboard team will be updated.
    /// any uuids which have CHANGED into a 'Dead' state (lives/score/time = 0) as a result, and they are online, they
    /// will be returned by this function as Players (typically so they can be killed, but a particular implementation
    /// could do something fancy if they so chose). Therefore, players who join having 0 time (having >0 time prior)
    /// will be handled the next time this is called. if you need to modify a specific score, use `<set/add>Score`
    public Collection<Player> updateAllTrackedScoresAndTeams(BiFunction<UUID, Integer, Integer> updater) {
        ArrayList<Player> deadPlayers = new ArrayList<>();
        playerScores.replaceAll((uuid, score) -> {
            TeamMeta oldTeam = getTeam(uuid);
            int newScore = Math.max(updater.apply(uuid, score),0);
            TeamMeta newTeam = getTeam(newScore);
            if (!oldTeam.equals(newTeam)) { // player team has changed, register to new team
                Player player = p.getServer().getPlayer(uuid);
                if (player != null) { // player is online
                    oldTeam.getScoreboardTeam().removePlayer(player);
                    newTeam.getScoreboardTeam().addPlayer(player);
                    if (score == 0) { // player JUST died (not was already dead i.e. already in Spectator team)
                        deadPlayers.add(player); // we don't know if they're online or not...
                    }
                }

            }
            return newScore;
        });
        return deadPlayers;
    }

    public Optional<TeamMeta> tryForTeam(String teamName) {
        for (var team : rankedTeams) {
            if (team.getName().equalsIgnoreCase(teamName)) { return Optional.of(team); }
        }
        return Optional.empty();
    }

    /// sync player team and scoreboard team according to tracked score and return the new team
    public Optional<TeamMeta> updateTeamGetChange(OfflinePlayer p) {
        Team oldTeam = scoreboard.getPlayerTeam(p);
        TeamMeta teamMeta = getTeam(p);
        if (oldTeam != null) {
            if (!oldTeam.equals(teamMeta.getScoreboardTeam())) {
                reassignPlayerTeam(p, teamMeta);
                return Optional.of(teamMeta);
            }
        } else {
            return Optional.of(teamMeta);
        }
        return Optional.empty();
    }

    public TeamMeta getSpectatorTeam() { return this.spectatorTeam; }

    private void reassignPlayerTeam(OfflinePlayer player, TeamMeta newTeamMeta) {
        reassignPlayerScoreboardTeam(player, newTeamMeta.getScoreboardTeam());
    }

    private void reassignPlayerScoreboardTeam(OfflinePlayer player, Team newTeam) {
        Team currentTeam = scoreboard.getPlayerTeam(player);
        if (currentTeam != null) { currentTeam.removePlayer(player); }

        newTeam.addPlayer(player);
    }


    public Component getDecoratedPlayerName(OfflinePlayer player) {
        String name = player.getName();
        if (name == null) { name = ""; }
        p.getLogger().warning("couldnt get offlineplayer name :(");
        return Component.text(name).style(getTeam(player).style());
    }

    public Style getTeamStyle(TeamMeta teamMeta) {
        return Style.style(teamMeta.getColor(), TextDecoration.BOLD);
    }
    public Style getTeamStyle(int score) {
        return getTeamStyle(getTeam(score));
    }
    public Style getTeamStyle(OfflinePlayer player) {
        return getTeamStyle(getScore(player));
    }
}
