package com.github.cinnamondev.lifeSeries.gamemodes.Boogeyman;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.Game;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class AbstractBoogeyman implements Boogeyman, Game {
    private final LifeSeries p;
    private ArrayList<UUID> boogeymen = new ArrayList<>();

    public AbstractBoogeyman(LifeSeries p) {
        this.p = p;
    }

    @Override
    public ArrayList<UUID> getBoogeyList() {
        return boogeymen;
    }

    @Override
    public boolean onKilled(LifeSeries p, Player killed, Player killer) {
        if (isBoogeyman(killer)) {
            cureBoogeyman(killer);
            killer.sendMessage(Component.translatable("boogeyman.cured"));
            return Game.super.onKilled(p,
                    killed, p.getConfig().getInt("options.punishment.boogey-death"),
                    killer, p.getConfig().getInt("options.rewards.boogey-kill")
            );
        } else {
            return Game.super.onKilled(p, killed, killer); // let default implementation handle it
        }
    }
}
