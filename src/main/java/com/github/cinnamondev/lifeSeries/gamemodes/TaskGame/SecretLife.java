package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame;

import com.github.cinnamondev.lifeSeries.LifeSeries;

public class SecretLife extends AbstractTaskGame {
    public SecretLife(LifeSeries p) {
        super(p);
    }

    @Override
    public void run() {
        p.getScoreHandler().updateAllTrackedScoresAndTeams((_uuid, score) -> score);
        super.run();
    }

}
