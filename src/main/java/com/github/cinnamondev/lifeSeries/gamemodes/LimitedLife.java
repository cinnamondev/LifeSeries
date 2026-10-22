package com.github.cinnamondev.lifeSeries.gamemodes;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.Boogeyman.AbstractBoogeyman;
import com.github.cinnamondev.lifeSeries.gamemodes.Timed.TimeSubCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.Timed.Timed;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import com.github.cinnamondev.lifeSeries.util.UtilityComponents;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class LimitedLife extends AbstractBoogeyman implements Game {
    private final ArrayList<UUID> boogeymen = new ArrayList<>();
    public LimitedLife(LifeSeries p) {
        super(p);
    }

    private Map<UUID,Integer> initialScores = null;
    private int initialUntracked = -1;

    @Override
    public void onGameStart() {
        super.onGameStart();
        initialUntracked = p.getScoreHandler().getUntrackedScore();
        initialScores = p.getScoreHandler().getAllTrackedScores();
    }

    @Override
    public void onGameStop() {
        super.onGameStop();
        if (initialScores != null) {
            var finalScores = p.getScoreHandler().getAllTrackedScores();

            finalScores.forEach((uuid, score) -> {
                TeamMeta team = p.getScoreHandler().getTeam(uuid);
                int playerStartScore = initialScores.getOrDefault(uuid, p.getScoreHandler().getUntrackedScore());
                p.getComponentLogger().info(
                        Component.text("").append(
                                Component.text(Objects.requireNonNullElse(p.getServer().getOfflinePlayer(uuid).getName(), uuid.toString()))
                                        .style(p.getScoreHandler().getTeam(uuid).style())
                        ).append(Component.text(" has a time of ")).append(
                                UtilityComponents.playerTime(
                                        score,
                                        TimeUnit.SECONDS,
                                        team.getColor()
                                )
                        ).append(Component.text(" ("))
                                .append(UtilityComponents.playerTimeChange(score - playerStartScore, TimeUnit.SECONDS))
                                .append(Component.text(")"))
                );
            });
        }
    }

    @Override
    public boolean onKilled(LifeSeries p, Player killed, int punishment) {
        boolean isFinalDeath = super.onKilled(p, killed, punishment);
        killed.showTitle(Title.title(
                UtilityComponents.playerTimeChange(-1 * punishment, TimeUnit.SECONDS),
                Component.empty()
        ));
        return isFinalDeath;
    }

    @Override
    public void rewardKiller(LifeSeries p, Player killer, int reward) {
        super.rewardKiller(p, killer, reward);
        killer.showTitle(Title.title(
                UtilityComponents.playerTimeChange(reward, TimeUnit.SECONDS),
                Component.empty()
        ));
    }

    @Override
    public Collection<LiteralArgumentBuilder<CommandSourceStack>> adminSubCommands(LifeSeries p) {
        return Stream.of(Collections.singletonList(TimeSubCommand.command(p)), super.adminSubCommands(p))
                .flatMap(Collection::stream).toList();
    }

    @Override
    public void run() {
        p.getScoreHandler().updateTrackableScoresAndTeams((uuid, score) -> score - 1);
        p.getScoreHandler().addUntrackedScore(-1);
        p.getServer().getOnlinePlayers().forEach(player -> Timed.displayPlayerTime(p, player));
    }
}