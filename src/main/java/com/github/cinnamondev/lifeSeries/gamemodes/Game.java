package com.github.cinnamondev.lifeSeries.gamemodes;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;

public interface Game extends Runnable, CommandContainer {
    default void onGameStart() {}
    default void onGameStop() {}
    void restoreStateFromSave();
    void clearSaveData();
    default void onServerDisable() {}
    default boolean onKilled(LifeSeries p, Player killed, int punishment) {
        AtomicBoolean isFinalDeath = new AtomicBoolean(false);
        p.getScoreHandler().updatePlayerScoreAndTeam(killed, (uuid,score) -> score - punishment, (player, newTeam) -> {
            isFinalDeath.set(p.getScoreHandler().isPlayerSpectator(player)); // only on team change.
        });
        return isFinalDeath.get();
    }
    default void rewardKiller(LifeSeries p, Player killer, int reward) {
        p.getScoreHandler().updatePlayerScoreAndTeam(killer, (uuid,score) -> score + reward);
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

}