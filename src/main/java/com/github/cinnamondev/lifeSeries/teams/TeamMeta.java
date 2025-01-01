package com.github.cinnamondev.lifeSeries.teams;

import com.github.cinnamondev.lifeSeries.util.ColourConverter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
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
    private final int mininumScore;
    private final Team scoreboardTeam;
    private final GameMode gamemode;

    public TeamMeta(final Team team, final String name, final NamedTextColor color, final List<String> canKill, final int mininumScore, final GameMode gamemode) {
        this.scoreboardTeam = team;
        this.name = name;
        this.color = color;
        this.canKill = canKill;
        this.mininumScore = mininumScore;
        this.gamemode = gamemode;
    }
    public TeamMeta(final Team team, final String name, final NamedTextColor color, final List<String> canKill, final int mininumScore) {
        this(
                team,
                name,
                color,
                canKill,
                mininumScore,
                GameMode.SURVIVAL
        );
    }
    public TeamMeta(final Team team, final String name, final String color, final List<String> canKill, final int mininumScore) {
        this(
                team,
                name,
                ColourConverter.tryNamedColourFromString(color)
                        .orElseThrow(() -> new IllegalArgumentException("aaargh")),
                canKill,
                mininumScore
        );
    }

    public String getName() { return name; }
    public List<String> getCanKill() { return canKill; }
    public boolean canKillTeam(final String targetTeamName) { return canKill.contains(targetTeamName); }
    public int getMininumScore() { return mininumScore; }
    public Team getScoreboardTeam() { return scoreboardTeam; }
    public GameMode getGameMode() { return gamemode; }
    public void setPlayerGameMode(Player p) {
        p.setGameMode(gamemode);
    }
    public NamedTextColor getColor() { return color; }
    public Style style() {
        return Style.style(color, TextDecoration.BOLD);
    }
    public Component decoratedString(String string) {
        return Component.text(string).style(style());
    }
}
