package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.concurrent.TimeUnit;

public interface RequiresOnlineTarget extends TargetedPlayerTask {
    @Override
    default Component taskProgressExplanation() {
        if (getTargetedPlayer().getPlayer() == null) {
            return Component.text("Task is in an odd state, it requires the target player is online.");
        }
        return Component.empty();
    }

}
