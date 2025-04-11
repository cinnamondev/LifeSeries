package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

import org.bukkit.Location;

import java.util.Objects;

public interface HasRadius extends PlayerTask {
    default int fallbackRadius() {
        return getConfigurationSection()
                .map(c -> Objects.requireNonNull(c.getParent()).getInt("default.generic-radius-threshold", -1))
                .orElse(32);
    }
    default int getRadius() {
        return getConfigurationSection().map(c -> c.getInt("radius", -1)).filter(n -> n!= -1).orElse(fallbackRadius());
    }
    default boolean withinRadius(Location loc1, Location loc2) {
        if (loc1.getWorld() != loc2.getWorld()) { return false; }
        return loc1.distanceSquared(loc2) <= getRadius();
    }

}
