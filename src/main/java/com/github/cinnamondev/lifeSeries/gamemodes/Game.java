package com.github.cinnamondev.lifeSeries.gamemodes;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.entity.Player;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public interface Game extends Runnable {
    default void onGameStart() {}
    default void onGameStop() {}
    default boolean onKilled(LifeSeries p, Player killed, int punishment) {
        AtomicBoolean isFinalDeath = new AtomicBoolean(false);
        p.getScoreHandler().updatePlayerScoreAndTeam(killed, (uuid,score) -> {
            String time = DurationFormatUtils.formatDuration(
                    punishment * 1000L,
                    "'-'HH':'mm':'ss",
                    true);
            killed.showTitle(Title.title(
                    Component.text(time).style(Style.style(NamedTextColor.RED, TextDecoration.BOLD)),
                    Component.empty()
            ));
            TeamMeta newTeam = p.getScoreHandler().getTeam(score-punishment);
            if (newTeam.equals(p.getScoreHandler().getSpectatorTeam())) { isFinalDeath.set(true); }
            return score - punishment;
        });
        return isFinalDeath.get();
    }
    default boolean onKilled(LifeSeries p, Player killed) {
        return onKilled(p, killed, p.getConfig().getInt("options.punishment.death"));
    }
    default boolean onKilled(LifeSeries p, Player killed, int punishment, Player killer, int reward) {
        rewardKiller(p, killer, reward);
        return onKilled(p, killed, punishment);
    }
    default boolean onKilled(LifeSeries p, Player killed, Player killer) {
        TeamMeta killerTeam = p.getScoreHandler().getTeam(killer);
        TeamMeta killedTeam = p.getScoreHandler().getTeam(killed);

        if (!killerTeam.equals(killedTeam)) {
            return onKilled(p,
                    killed, p.getConfig().getInt("options.punishment.death"),
                    killer, p.getConfig().getInt("options.rewards.kill")
            );
        } else {
            if (!p.getConfig().getBoolean("options.rewards.disable-on-disallowed-team")) {
                rewardKiller(p, killed, p.getConfig().getInt("options.rewards.kill"));
            }
            if (!p.getConfig().getBoolean("options.punishment.disable-on-disallowed-team")) {
                return onKilled(p, killed);
            }
        }
        return false;
    }
    private void rewardKiller(LifeSeries p, Player killer, int reward) {
        p.getScoreHandler().updatePlayerScoreAndTeam(killer, (uuid,score) -> {
            String time = DurationFormatUtils.formatDuration(
                    reward * 1000L,
                    "'+'HH':'mm':'ss",
                    true);
            killer.showTitle(Title.title(
                    Component.text(time).style(Style.style(NamedTextColor.GREEN, TextDecoration.BOLD)),
                    Component.empty()
            ));
            return score + reward;
        });
    }
}