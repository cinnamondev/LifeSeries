package com.github.cinnamondev.lifeSeries.gamemodes;

import com.github.cinnamondev.lifeSeries.LifeSeries;

public class Lives implements Game {
    protected final LifeSeries p;

    public Lives(LifeSeries p) {
        this.p = p;
    }

    @Override
    public void run() {
        // default behaviour: player dies on no eligible team
        p.getScoreHandler().updateTrackableScoresAndTeams((uuid, lives) -> lives);
    }

    @Override
    public void restoreStateFromSave() {} // no action required.

    @Override
    public void clearSaveData() {} // no action required
}
