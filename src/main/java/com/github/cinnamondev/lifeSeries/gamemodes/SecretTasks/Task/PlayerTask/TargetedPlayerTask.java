package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

import org.bukkit.OfflinePlayer;

public interface TargetedPlayerTask extends PlayerTask {
    OfflinePlayer getTargetedPlayer();
}
