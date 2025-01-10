package com.github.cinnamondev.lifeSeries.gamemodes;

import org.bukkit.OfflinePlayer;

import java.util.Collection;
import java.util.UUID;

public interface Boogeyman {
    public boolean roll(int min, int max);
    public boolean isBoogeyman(OfflinePlayer offlinePlayer);
    public boolean isBoogeyman(UUID uuid);
    public void cure(UUID uuid);
    public void addBoogey(UUID uuid);
    public void addBoogey(OfflinePlayer offlinePlayer);
    /// remove player from list apply reward
    public void cure(OfflinePlayer offlinePlayer);
    public Collection<UUID> getBoogeymen();

}
