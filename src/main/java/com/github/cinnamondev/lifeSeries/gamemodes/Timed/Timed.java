package com.github.cinnamondev.lifeSeries.gamemodes.Timed;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.Game;
import com.github.cinnamondev.lifeSeries.util.UtilityComponents;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class Timed implements Game {
    protected final LifeSeries p;

    public Timed(LifeSeries p) {
        this.p = p;
    }

    /// tick the game.
    @Override
    public void run() {
        p.getScoreHandler().updateTrackableScoresAndTeams((uuid, score) -> score - 1);
        p.getScoreHandler().addUntrackedScore(-1);
        p.getServer().getOnlinePlayers().forEach(player -> Timed.displayPlayerTime(p, player));

    }

    public static void displayPlayerTime(LifeSeries p, Player player) {
        player.sendActionBar(UtilityComponents.playerTime(
                p.getScoreHandler().getScore(player),
                TimeUnit.SECONDS,
                p.getScoreHandler().getTeam(player).getColor()
        ));

    }

    @Override
    public Collection<LiteralArgumentBuilder<CommandSourceStack>> adminSubCommands(LifeSeries p) {
        return Collections.singletonList(TimeSubCommand.command(p));
    }

    @Override
    public void restoreStateFromSave() {

    }

    @Override
    public void clearSaveData() {

    }

    @Override
    public boolean onKilled(LifeSeries p, Player killed, int punishment) {
        boolean isFinalDeath = Game.super.onKilled(p, killed, punishment);
        killed.showTitle(Title.title(
                UtilityComponents.playerTimeChange(-1 * punishment, TimeUnit.SECONDS),
                Component.empty()
        ));
        return isFinalDeath;
    }

    @Override
    public void rewardKiller(LifeSeries p, Player killer, int reward) {
        Game.super.rewardKiller(p, killer, reward);
        killer.showTitle(Title.title(
                UtilityComponents.playerTimeChange(reward, TimeUnit.SECONDS),
                Component.empty()
        ));
    }
}