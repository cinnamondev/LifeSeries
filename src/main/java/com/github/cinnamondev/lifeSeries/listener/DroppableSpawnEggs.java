package com.github.cinnamondev.lifeSeries.listener;

import com.github.cinnamondev.lifeSeries.util.TrackableSpawnEggs;
import com.google.inject.Inject;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

import java.util.Random;

public class DroppableSpawnEggs implements Listener {
    protected final Plugin p;
    protected final Random rand = new Random();

    @Inject public DroppableSpawnEggs(Plugin p) {
        this.p = p;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void entityDeathEvent(EntityDeathEvent e) {
        if (e.getEntity() instanceof Player) { return; }
        if (rand.nextInt(100) <= p.getConfig().getInt("options.spawn-eggs.chance")) {
            TrackableSpawnEggs.tryGetTrackableSpawnEgg(p, e.getEntity().getType(), 1)
                    .ifPresent((egg) -> e.getDrops().add(egg));
        }
    }
}
