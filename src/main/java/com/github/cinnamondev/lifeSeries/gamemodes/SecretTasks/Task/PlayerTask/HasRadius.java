package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

import org.bukkit.Location;

public interface HasRadius extends PlayerTask {
    int fallbackRadius();
    default int getRadius() {
        return getConfigurationSection().map(c -> c.getInt("radius", -1)).filter(n -> n!= -1).orElse(fallbackRadius());
    }
    default boolean withinRadius(Location loc1, Location loc2) {
        if (loc1.getWorld() != loc2.getWorld()) { return false; }
        return loc1.distanceSquared(loc2) <= getRadius();
    }

}
