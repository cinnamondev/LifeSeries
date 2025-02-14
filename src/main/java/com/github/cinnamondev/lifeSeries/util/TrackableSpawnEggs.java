package com.github.cinnamondev.lifeSeries.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

public class TrackableSpawnEggs {
    public static boolean isEntityFromTrackedEgg(Plugin p, Entity entity) {
        return entity.getPersistentDataContainer()
                .get(new NamespacedKey(p, "trackable-egg"), PersistentDataType.BOOLEAN) != null;
    }
    public static Optional<ItemStack> tryGetTrackableSpawnEgg(Plugin p, EntityType type, int n) {
        Material eggMaterial = p.getServer().getItemFactory().getSpawnEgg(type);
        if (eggMaterial == null) { return Optional.empty(); }

        ItemStack egg = new ItemStack(eggMaterial, n);
        SpawnEggMeta meta = (SpawnEggMeta) egg.getItemMeta();

        Entity entity = meta.getSpawnedEntity().createEntity(p.getServer().getWorlds().getFirst());
        entity.getPersistentDataContainer()
                .set(new NamespacedKey(p, "trackable-egg"), PersistentDataType.BOOLEAN, true);
        meta.setSpawnedEntity(entity.createSnapshot());
        egg.setItemMeta(meta);

        return Optional.of(egg);
    }
}
