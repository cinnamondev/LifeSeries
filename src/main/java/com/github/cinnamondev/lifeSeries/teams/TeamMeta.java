package com.github.cinnamondev.lifeSeries.teams;

import com.github.cinnamondev.lifeSeries.util.ColourConverter;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Optional;

public class TeamMeta {
    private final String name;
    private final NamedTextColor color;
    private final List<String> canKill;
    private final int livesRequirement;
    private final Team scoreboardTeam;
    private final GameMode gamemode;

    public TeamMeta(final Team team, final String name, final NamedTextColor color, final List<String> canKill, final int livesRequirement, final GameMode gamemode) {
        this.scoreboardTeam = team;
        this.name = name;
        this.color = color;
        this.canKill = canKill;
        this.livesRequirement = livesRequirement;
        this.gamemode = gamemode;
    }
    public TeamMeta(final Team team, final String name, final NamedTextColor color, final List<String> canKill, final int livesRequirement) {
        this(
                team,
                name,
                color,
                canKill,
                livesRequirement,
                GameMode.SURVIVAL
        );
    }
    public TeamMeta(final Team team, final String name, final String color, final List<String> canKill, final int livesRequirement) {
        this(
                team,
                name,
                ColourConverter.tryNamedColourFromString(color)
                        .orElseThrow(() -> new IllegalArgumentException("aaargh")),
                canKill,
                livesRequirement
        );
    }

    public String getName() { return name; }
    public NamedTextColor getColor() { return color; }
    public List<String> getCanKill() { return canKill; }
    public boolean canKillTeam(final OfflinePlayer player) { return canKill.contains(player.getName()); }
    public int getLivesRequirement() { return livesRequirement; }
    public Team getScoreboardTeam() { return scoreboardTeam; }
    public GameMode getGameMode() { return gamemode; }
    public void setPlayerGameMode(Player p) {
        p.setGameMode(gamemode);
    }
}
