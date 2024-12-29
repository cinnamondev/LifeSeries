package com.github.cinnamondev.lifeSeries.teams;

import com.github.cinnamondev.lifeSeries.util.ColourConverter;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.stream.Collectors;

public class ScoreHandler {
    private final Plugin p;
    private final ScoreboardManager manager;
    private final Scoreboard scoreboard;

    private final TeamMeta spectatorTeam;
    private final SortedSet<TeamMeta> rankedTeams;

    private int untrackedPlayerScore;
    private HashMap<UUID, Integer> playerScores = new HashMap<>();

    public ScoreHandler(Plugin p, int defaultScore) {
        this.p = p;
        this.manager = p.getServer().getScoreboardManager();
        this.scoreboard = manager.getMainScoreboard();
        this.rankedTeams = getSortedMetaFromConfig();

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

    public int decrementUntrackedPlayerScore(int decrementValue) { untrackedPlayerScore -= decrementValue; return untrackedPlayerScore; }
    public void setUntrackedPlayerScore(int defaultScore) { untrackedPlayerScore = defaultScore; }
    public HashMap<UUID, Integer> getPlayerScores() { return playerScores; }
    private Integer getOrUseDefaultScore(OfflinePlayer player) {
        return getOrUseDefaultScore(player.getUniqueId());
    }
    private Integer getOrUseDefaultScore(UUID uuid) {
        return playerScores.getOrDefault(uuid, untrackedPlayerScore);
    }
    public TeamMeta getTeam(OfflinePlayer player) {
        return getTeam(player.getUniqueId());
    }
    public TeamMeta getTeam(UUID uuid) {
        int score = getOrUseDefaultScore(uuid);
        return rankedTeams.stream()
                .filter(team -> score > team.getMininumScore()) // should not be >=, players on boundry
                .findFirst()                                                // will be downgraded to next tier.
                .orElse(this.spectatorTeam); // player has 0 time left!
    }

    private TeamMeta updatePlayerTeam(OfflinePlayer p) {
        Team oldTeam = scoreboard.getPlayerTeam(p);
        TeamMeta teamMeta = getTeam(p);
        if (oldTeam != null) {
            if (!oldTeam.equals(teamMeta.getScoreboardTeam())) { return teamMeta; }

            reassignPlayerTeam(p, teamMeta);
            if (p instanceof Player player) { // update players gamemode, if they are online (is a Player)
                teamMeta.setPlayerGameMode(player);
            }
        }
        return teamMeta;
    }

    private void reassignPlayerTeam(OfflinePlayer player, TeamMeta newTeamMeta) {
        reassignPlayerScoreboardTeam(player, newTeamMeta.getScoreboardTeam());
    }

    private void reassignPlayerScoreboardTeam(OfflinePlayer player, Team newTeam) {
        Team currentTeam = scoreboard.getPlayerTeam(player);
        if (currentTeam != null) { currentTeam.removePlayer(player); }

        newTeam.addPlayer(player);
    }


}
