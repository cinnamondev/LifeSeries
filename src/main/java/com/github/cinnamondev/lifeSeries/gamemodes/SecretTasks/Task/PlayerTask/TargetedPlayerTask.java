package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface TargetedPlayerTask extends PlayerTask {
    OfflinePlayer getTargetedPlayer();
}
