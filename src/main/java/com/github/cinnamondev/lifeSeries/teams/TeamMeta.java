package com.github.cinnamondev.lifeSeries.teams;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.List;

public class TeamMeta {
    private final String name;
    private final NamedTextColor colour;
    private final int minimumTime;
    private final GameMode gameMode;
    private final Team team;
    private final List<String> canKill;

    public TeamMeta(Team team, String name, NamedTextColor colour, int minimumTime, List<String> canKill, GameMode gameMode) {
        this.name = name;
        this.colour = colour;
        this.minimumTime = minimumTime;
        this.gameMode = gameMode;
        this.team = team;
        this.canKill = canKill;
        team.setCanSeeFriendlyInvisibles(false);
        team.color(colour);

    }
    public TeamMeta(Team team, String name, NamedTextColor colour, int minimumTime, List<String> canKill) {
        this(team, name, colour, minimumTime, canKill, GameMode.SURVIVAL);
    }
    public int getMinimumTime() { return minimumTime; }
    public void applyGameMode(Player p) {
        p.setGameMode(gameMode);
    }
    public String getName() { return name; }
    public NamedTextColor getColour() { return colour; }
    public boolean canKill(String teamName) {
        return canKill.contains(teamName);
    }
    public List<String> killableTeams() { return canKill; }

}
