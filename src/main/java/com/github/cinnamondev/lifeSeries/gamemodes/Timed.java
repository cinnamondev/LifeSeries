package com.github.cinnamondev.lifeSeries.gamemodes;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.entity.Player;

public class Timed extends BasicBoogeymanGameImpl {
    private final LifeSeries p;

    public Timed(LifeSeries p) {
        super(p);
        this.p = p;
    }

    /// tick the game.
    @Override
    public void run() {
        p.getScoreHandler().updateTrackableScoresAndTeams((uuid, score) -> score - 1);
        p.getScoreHandler().addUntrackedScore(-1);

        p.getServer().getOnlinePlayers().forEach(this::displayPlayerTime);

    }

    private void displayPlayerTime(Player player) {
        String time =DurationFormatUtils.formatDuration(
                p.getScoreHandler().getScore(player) * 1000,
                "HH':'mm':'ss",
                true);
        player.sendActionBar(Component.text(time).decorate(TextDecoration.BOLD).color(
                p.getScoreHandler().getTeam(player).getScoreboardTeam().color()
        ));
    }

}