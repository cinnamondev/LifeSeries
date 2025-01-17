package com.github.cinnamondev.lifeSeries.gamemodes;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class LimitedLife extends Timed implements Boogeyman {
    private final LifeSeries p;
    private final ArrayList<UUID> boogeymen = new ArrayList<>();
    public LimitedLife(LifeSeries p) {
        super(p);
        this.p = p;
    }

    @Override
    public ArrayList<UUID> getBoogeyList() {
        return this.boogeymen;
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
    public void onGameStop() {
        punishBoogeymen(p);
    }
}