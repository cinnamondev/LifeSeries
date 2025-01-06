package com.github.cinnamondev.lifeSeries.listener;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryNerfer implements Listener {
    private final Plugin p;
    List<NamespacedKey> bannedItems = new ArrayList<>();
    public InventoryNerfer(Plugin p) {
        this.p = p;
        var bannedItemList = p.getConfig().getStringList("banned-items");
        bannedItemList.stream().map(String::toLowerCase).forEach(string -> {
            try {
                NamespacedKey key = NamespacedKey.minecraft(string);
                bannedItems.add(key);
                p.getLogger().info("Found item key in config: " + key.asString());
            } catch (Exception e) {
                p.getLogger().warning("Couldn't make namespace key for item. skipping. item: " + string);
                p.getLogger().throwing("InventoryNerfer", "InventoryNerfer", e);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void nerfEntityDrops(EntityDeathEvent e) {
        if (e.getEntity() instanceof Player) { return; } // dont try and nerf players
        e.getDrops().removeIf(item -> bannedItems.contains(item.getType().getKey()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void nerfLootDrops(LootGenerateEvent e) {
        if (e.getEntity() instanceof Player p && p.hasPermission("life.bypass.banned-items")) { return; }
        e.getLoot().removeIf(item -> bannedItems.contains(item.getType().getKey()));
    }

    public void nerfOnlinePlayersItems() {
        p.getServer().getOnlinePlayers().forEach(player -> {
            if (player.hasPermission("life.bypass.banned-items")) { return; }
            var inventory = player.getInventory().getContents();
            for (int i=0; i < inventory.length; i++) {
                if (inventory[i] != null) {
                    if (bannedItems.contains(inventory[i].getType().getKey())) {
                        player.sendMessage(
                                Component.text("Removed blacklisted item ").append(inventory[i].displayName())
                                        .color(NamedTextColor.GRAY)
                        );
                        player.getInventory().setItem(i, null);
                    }
                }
            }
        });
    }
}
