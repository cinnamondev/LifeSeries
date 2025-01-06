package com.github.cinnamondev.lifeSeries.listener;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnchantmentNerfer implements Listener {
    Plugin p;
    final Registry<Enchantment> enchantmentRegistry = RegistryAccess
            .registryAccess()
            .getRegistry(RegistryKey.ENCHANTMENT);
    List<NamespacedKey> blacklistedEnchants = new ArrayList<>();
    public EnchantmentNerfer(Plugin p) {
        this.p = p;
        var bannedEnchants = p.getConfig().getStringList("banned-enchantments.blocklist");
        bannedEnchants.stream().map(String::toLowerCase).forEach(string -> {
            try {
                NamespacedKey key = NamespacedKey.minecraft(string);
                blacklistedEnchants.add(key);
                p.getLogger().info("Found enchant key in config: " + key.asString());
            } catch (Exception e) {
                p.getLogger().warning("Couldn't make namespace key for enchantment " + string);
                p.getLogger().throwing("EnchantmentNerfer", "EnchantmentNerfer", e);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void nerfEnchantingHint(PrepareItemEnchantEvent e) {
        // note: you could probably get a high level enchant from a secondary effect. but they should be nerfed and or
        // removed in a listener.
        if (enchantBypassIsEnabledFor(e.getViewers())) { return; }
        int maxLevel = p.getConfig().getInt("banned-enchantments.max-level",0);
        var offers = e.getOffers();
        for (int i=0; i < offers.length; i++) {
            if (maxLevel == 0) {
                offers[i] = null;
            } else if (offers[i] != null) {
                if (blacklistedEnchants.contains(offers[i].getEnchantment().getKey())) {
                    offers[i].setEnchantment(Enchantment.UNBREAKING);
                    offers[i].setEnchantmentLevel(1);
                }
                if (offers[i].getEnchantmentLevel() > maxLevel) {
                    offers[i].setEnchantmentLevel(maxLevel);
                }
            }
        }
    }

    // seems to be when the event touches an item it becomes unanvilable or untouchable otherwise...
    @EventHandler(priority = EventPriority.HIGH)
    public void nerfAnvil(PrepareAnvilEvent e) {
        if (enchantBypassIsEnabledFor(e.getViewers())) { return; }
        ItemStack result = e.getResult();
        if (result == null) { return; }
        nerfEnchantedItem(result);
    }

    private Optional<Map.Entry<Enchantment, Integer>> nerfEnchant(Map.Entry<Enchantment, Integer> enchantment, List<HumanEntity> viewers) {
        if (blacklistedEnchants.contains(enchantment.getKey().getKey())) {
            viewers.forEach(viewer -> viewer.sendMessage(enchantment.getKey().displayName(enchantment.getValue())
                    .appendSpace()
                    .append(Component.text("is blacklisted!"))
            ));
            return Optional.empty(); // remove enchantment
        } else {
            int maxLevel = p.getConfig().getInt("banned-enchantments.max-level", 0);
            Integer currentLevel = enchantment.getValue();
            if (currentLevel > maxLevel) {
                viewers.forEach(viewer -> viewer.sendMessage(
                        enchantment.getKey().displayName(enchantment.getValue())
                                .appendSpace()
                                .append(Component.text("has been nerfed to"))
                                .appendSpace()
                                .append(enchantment.getKey().displayName(maxLevel))
                ));
                currentLevel = maxLevel;
            }
            return Optional.of(new AbstractMap.SimpleEntry<>(enchantment.getKey(), currentLevel));
        }
    }
    private Map<Enchantment, Integer> nerfEnchants(Map<Enchantment, Integer> enchantments, List<HumanEntity> viewers) {
        return enchantments.entrySet().stream()
                .map(enc -> nerfEnchant(enc, viewers))
                .<Map.Entry<Enchantment, Integer>>mapMulti(Optional::ifPresent)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    private Map<Enchantment, Integer> nerfEnchants(Map<Enchantment, Integer> enchantments) {
        return nerfEnchants(enchantments, Collections.emptyList());
    }
    private void nerfEnchantedItem(ItemStack item, List<HumanEntity> viewers) {
        ItemMeta resultMeta = item.getItemMeta();
        if (resultMeta == null) { return; }
        Map<Enchantment, Integer> enchantments;
        if (item.getType() == Material.ENCHANTED_BOOK && resultMeta instanceof EnchantmentStorageMeta encMeta) {
            //enchantments = item.getEnchantments();
            enchantments = encMeta.getStoredEnchants();
        } else {
            enchantments = item.getEnchantments();
        }

        if (enchantments.isEmpty()) { return; }
        var newEnchantments = nerfEnchants(enchantments, viewers);

        if (item.getType() == Material.ENCHANTED_BOOK && resultMeta instanceof EnchantmentStorageMeta encMeta) {
            //enchantments = item.getEnchantments();
            enchantments.forEach((enc, level) -> encMeta.removeStoredEnchant(enc)); // this feels stupid!
            newEnchantments.forEach((enc,level) -> encMeta.addStoredEnchant(enc, level, true));
            item.setItemMeta(encMeta);
        } else {
            item.removeEnchantments();
            item.addUnsafeEnchantments(newEnchantments);
        }

    }
    private void nerfEnchantedItem(ItemStack item) {
        nerfEnchantedItem(item, Collections.emptyList());
    }

    public void nerfOnlinePlayersItems() {
        p.getServer().getOnlinePlayers().forEach(player -> {
            if (enchantBypassIsEnabledFor(player)) { return; }
            for (var item : player.getInventory().getContents()) {
                if (item != null) {
                    nerfEnchantedItem(item, Collections.singletonList(player));
                }
            }
        });
    }

    private boolean enchantBypassIsEnabledFor(List<HumanEntity> players) {
        return (players.stream().anyMatch(v -> v.hasPermission("life.bypass.banned-enchantments")))
                || (p.getConfig().getInt("banned-enchantments.max-level",0) == -1);
    }
    private boolean enchantBypassIsEnabledFor(Player player) {
        return enchantBypassIsEnabledFor(Collections.singletonList(player));
    }

}
