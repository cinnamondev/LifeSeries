package com.github.cinnamondev.lifeSeries.gamemodes;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import org.bukkit.entity.Player;

public class Lives implements Game {
    protected final LifeSeries p;

    public Lives(LifeSeries p) {
        this.p = p;
    }

    @Override
    public void run() {

    }

    @Override
    public void onGameStart(LifeSeries p) {
        Game.super.onGameStart(p);
    }

    @Override
    public void onGameStop(LifeSeries p) {
        Game.super.onGameStop(p);
    }

    @Override
    public boolean onKilled(LifeSeries p, Player killed, int punishment) {
        return Game.super.onKilled(p, killed, punishment);
    }

    @Override
    public boolean onKilled(LifeSeries p, Player killed, int punishment, Player killer, int reward) {
        return Game.super.onKilled(p, killed, punishment, killer, reward);
    }
}
