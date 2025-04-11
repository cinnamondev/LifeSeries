package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

import net.kyori.adventure.text.Component;

public interface RequiresOnlineTarget extends TargetedPlayerTask {
    @Override
    default Component taskProgressExplanation() {
        if (getTargetedPlayer().getPlayer() == null) {
            return Component.text("Task is in an odd state, it requires the target player is online.");
        }
        return Component.empty();
    }

}
