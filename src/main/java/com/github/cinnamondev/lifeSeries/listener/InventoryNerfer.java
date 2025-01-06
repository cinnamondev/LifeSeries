package com.github.cinnamondev.lifeSeries.listener;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.Bukkit;
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
        var bannedConfig = p.getConfig().getConfigurationSection("banned-items");
        if (bannedConfig != null) {
            bannedConfig.getKeys(false).stream().map(NamespacedKey::minecraft)
                    .forEach(key -> {
                        Recipe recipe = Bukkit.getRecipe(key);
                        if (recipe == null) { return; }
                        bannedItems.add(key);
                        Bukkit.removeRecipe(key, true); // prevent crafting!
                    });
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void nerfEntityDrops(EntityDeathEvent e) {
        if (e.getEntity() instanceof Player) { return; }
        e.getDrops().removeIf(item -> bannedItems.contains(item.getType().getKey()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void nerfLootTableDrops(LootGenerateEvent e) {
        e.getLoot().removeIf(item -> bannedItems.contains(item.getType().getKey()));
    }

    public void nerfOnlinePlayersItems() {
        p.getServer().getOnlinePlayers().forEach(player -> {
            if (player.hasPermission("life.bypass.banned-items")) { return; }

            var inventory = player.getInventory().getContents();
            for (int i=0; i < inventory.length; i++) {
                if (bannedItems.contains(inventory[i].getType().getKey())) {
                    player.getInventory().setItem(i, null);
                }
            }
        });
    }
}
