package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks;

import com.github.cinnamondev.lifeSeries.LifeSeries;

public class SecretLife extends AbstractSecretTasks {
    public SecretLife(LifeSeries p) {
        super(p);
    }

    @Override
    public void run() {
        p.getScoreHandler().updateAllTrackedScoresAndTeams((_uuid, score) -> score);
        super.run();
    }

}
