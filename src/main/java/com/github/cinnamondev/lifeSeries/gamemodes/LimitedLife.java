package com.github.cinnamondev.lifeSeries.gamemodes;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.Boogeyman.AbstractBoogeyman;
import com.github.cinnamondev.lifeSeries.gamemodes.Timed.TimeSubCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.Timed.Timed;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

public class LimitedLife extends AbstractBoogeyman implements Game {
    private final ArrayList<UUID> boogeymen = new ArrayList<>();
    public LimitedLife(LifeSeries p) {
        super(p);
    }


    @Override
    public boolean onKilled(LifeSeries p, Player killed, Player killer) {
        if (isBoogeyman(killer)) {
            cureBoogeyman(killer);
            killer.sendMessage("You have been cured!");
            return super.onKilled(p,
                    killed, p.getConfig().getInt("options.punishment.boogey-death"),
                    killer, p.getConfig().getInt("options.rewards.boogey-kill")
            );
        } else {
            return super.onKilled(p, killed, killer); // let default implementation handle it
        }
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