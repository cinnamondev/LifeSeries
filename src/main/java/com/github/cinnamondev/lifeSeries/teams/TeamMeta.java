package com.github.cinnamondev.lifeSeries.teams;

import com.github.cinnamondev.lifeSeries.util.ColourConverter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TeamMeta {
    private final List<String> canKill;
    private final int mininumScore;
    private final Team scoreboardTeam;
    private final GameMode gamemode;

    public TeamMeta(final Team team, final List<String> canKill, final int mininumScore, final GameMode gamemode) {
        this.scoreboardTeam = team;
        this.canKill = canKill;
        this.mininumScore = mininumScore;
        this.gamemode = gamemode;
    }
    public TeamMeta(final Team team, final List<String> canKill, final int mininumScore) {
        this(
                team,
                canKill,
                mininumScore,
                GameMode.SURVIVAL
        );
    }

    public List<String> getCanKill() { return canKill; }
    private boolean canKillTeam(final String targetTeamName) { return canKill.contains(targetTeamName); }
    public boolean canKillTeam(TeamMeta team) { return canKill.contains(team.getScoreboardTeam().getName()); }
    public int getMininumScore() { return mininumScore; }
    public Team getScoreboardTeam() { return scoreboardTeam; }
    public GameMode getGameMode() { return gamemode; }
    public void setPlayerGameMode(Player p) {
        p.setGameMode(gamemode);
    }
    public TextColor getColor() { return scoreboardTeam.color(); }
    public Style style() {
        return Style.style(scoreboardTeam.color(), TextDecoration.BOLD);
    }
    public Component decoratedString(String string) {
        return Component.text(string).style(style());
    }
}
