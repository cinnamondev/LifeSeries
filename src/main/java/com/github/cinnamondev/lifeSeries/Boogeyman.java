package com.github.cinnamondev.lifeSeries;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public interface Boogeyman {
    public List<Player> roll(int min, int max);
    public boolean isBoogeyman(OfflinePlayer offlinePlayer);
}
