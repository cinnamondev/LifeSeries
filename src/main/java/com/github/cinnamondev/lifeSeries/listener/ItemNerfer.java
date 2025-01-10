package com.github.cinnamondev.lifeSeries.listener;

import io.papermc.paper.event.player.PlayerPickItemEvent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Crafter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ItemNerfer implements Listener {
    private final Plugin p;
    private final List<NamespacedKey> bannedItems;
    public ItemNerfer(Plugin p) {
        this.p = p;
        List<String> bannedNames = p.getConfig().getStringList("banned-items");
        this.bannedItems = bannedNames.stream().map(String::toLowerCase)
                .flatMap(string -> {
                    try {
                        NamespacedKey key = NamespacedKey.minecraft(string);
                        p.getLogger().info("Found item key in config: " + key.asString());
                        return Stream.of(key);
                    } catch (Exception e) {
                        p.getLogger().warning("Couldn't make namespace key for item. skipping. item: " + string);
                        p.getLogger().throwing("InventoryNerfer", "InventoryNerfer", e);
                        return Stream.empty();
                    }
                }).toList();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void nerfEntityDrops(EntityDeathEvent e) {
        if (e.getEntity() instanceof Player) { return; } // dont try and nerf players
        e.getDrops().removeIf(item -> bannedItems.contains(item.getType().getKey()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void nerfLootDrops(LootGenerateEvent e) {
        if (e.getEntity() instanceof Player player && player.hasPermission("life.bypass.banned-items")) { return; }
        e.getLoot().removeIf(item -> bannedItems.contains(item.getType().getKey()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void nerfBlockPlacing(BlockPlaceEvent e) {
        if (e.getPlayer().hasPermission("life.bypass.banned-items")) { return; }
        if (bannedItems.contains(e.getBlockPlaced().getType().getKey())) { e.setBuild(false); }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void nerfBlockBreaking(BlockBreakEvent e) {
        if (e.getPlayer().hasPermission("life.bypass.banned-items")) { return; }
        if (bannedItems.contains(e.getBlock().getType().getKey())) { e.setCancelled(true); e.setDropItems(false); }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void nerfItemPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) { return; }
        if (player.hasPermission("life.bypass.banned-items")) { return; }
        if (bannedItems.contains(e.getItem().getItemStack().getType().getKey())) { e.setCancelled(true); }

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void nerfAutocrafter(CrafterCraftEvent e) {
        if (bannedItems.contains(e.getResult().getType().getKey())) {
            e.setResult(ItemStack.empty());
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void warnOnCrafting(PrepareItemCraftEvent e) {
        // we cant stop the user here but we can tell them to say the magic words
        if (e.getViewers().stream().anyMatch(player -> player.hasPermission("life.bypass.banned-items"))) { return; }
        Recipe recipe = e.getRecipe();
        if (recipe == null) { return; }
        if (bannedItems.contains(recipe.getResult().getType().getKey())) {
            e.getViewers().forEach(player -> player.sendMessage(
                    Component.text("You are not allowed to craft this item")
                            .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD))
            ));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void nerfCrafting(CraftItemEvent e) {
        if (e.getViewers().stream().anyMatch(player -> player.hasPermission("life.bypass.banned-items"))) { return; }
        if (bannedItems.contains(e.getRecipe().getResult().getType().getKey())) {
            ItemStack substitute = ItemStack.of(Material.BARRIER);
            ItemMeta meta = substitute.getItemMeta();
            meta.displayName(Component.text("You are not allowed to craft this item")
                    .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD)));
            substitute.setItemMeta(meta);
            e.setCurrentItem(substitute);
            e.setCancelled(true);
        }
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
