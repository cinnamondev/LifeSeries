package com.github.cinnamondev.lifeSeries.listener;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.util.TrackableSpawnEggs;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.material.SpawnEgg;
import org.bukkit.persistence.PersistentDataType;

import javax.naming.Name;
import java.util.Objects;
import java.util.Random;

public class DroppableSpawnEggs implements Listener {
    private final LifeSeries p;
    private final Random rand = new Random();
    public DroppableSpawnEggs(LifeSeries p) {
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
