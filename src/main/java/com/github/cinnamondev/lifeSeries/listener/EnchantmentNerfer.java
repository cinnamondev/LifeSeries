package com.github.cinnamondev.lifeSeries.listener;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnchantmentNerfer implements Listener {
    LifeSeries p;
    final Registry<Enchantment> enchantmentRegistry = RegistryAccess
            .registryAccess()
            .getRegistry(RegistryKey.ENCHANTMENT);
    List<Enchantment> blacklistedEnchants = new ArrayList<>();
    public EnchantmentNerfer(LifeSeries p) {
        this.p = p;
        var bannedEnchants = p.getConfig().getConfigurationSection("banned-enchantments.blocklist");
        if (bannedEnchants != null) {
            bannedEnchants.getKeys(false).forEach(enchantmentName -> {
                Enchantment enc = enchantmentRegistry.get(Key.key("minecraft:" + enchantmentName.toLowerCase()));
                if (enc != null) { blacklistedEnchants.add(enc); }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void nerfEnchanting(PrepareItemEnchantEvent e) {
        int maxLevel = p.getConfig().getInt("banned-enchantments.max-level",0);
        if (maxLevel == -1) { return; }
        for (EnchantmentOffer offer : e.getOffers()) {
            if (maxLevel == 0) {
                offer = null;
            } else if (offer != null) {
                if (offer.getEnchantmentLevel() > maxLevel) { offer.setEnchantmentLevel(maxLevel); }
                if (blacklistedEnchants.contains(offer.getEnchantment())) {
                    var newEntry = rollValidEnchants(e.getItem());
                    offer.setEnchantment(newEntry.getKey());
                    offer.setEnchantmentLevel(newEntry.getValue());
                    offer.setCost(1);
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void nerfAnvil(PrepareAnvilEvent e) {
        if (p.getConfig().getInt("banned-enchantments.max-level",0) == -1) { return; }
        ItemStack result = e.getResult();
        if (result == null) { return; }
        nerfEnchants(result, e.getViewers());
        e.setResult(result);
    }

    private Map<Enchantment, Integer> nerfEnchants(Map<Enchantment, Integer> enchantments, List<HumanEntity> viewers) {
        return enchantments.entrySet().stream().flatMap(enc -> {
            if (blacklistedEnchants.contains(enc.getKey())) {
                viewers.forEach(viewer -> viewer.sendMessage(enc.getKey().displayName(enc.getValue())
                        .appendSpace()
                        .append(Component.text("is blacklisted!"))
                ));
                return Stream.empty(); // remove enchantment
            } else {
                int maxLevel = p.getConfig().getInt("banned-enchantments.max-level",0);
                Integer currentLevel = enc.getValue();
                if (currentLevel > maxLevel) {
                    viewers.forEach(viewer -> viewer.sendMessage(
                            enc.getKey().displayName(enc.getValue())
                                    .appendSpace()
                                    .append(Component.text("has been nerfed to"))
                                    .appendSpace()
                                    .append(enc.getKey().displayName(maxLevel))
                    ));
                    currentLevel = maxLevel;
                }
                return Stream.of(new AbstractMap.SimpleEntry<>(
                        enc.getKey(),
                        currentLevel
                ));
            }
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    private Map<Enchantment, Integer> nerfEnchants(Map<Enchantment, Integer> enchantments) {
        return nerfEnchants(enchantments, Collections.emptyList());
    }
    private void nerfEnchants(ItemStack item, List<HumanEntity> viewers) {
        ItemMeta resultMeta = item.getItemMeta();
        if (resultMeta == null) { return; }
        Map<Enchantment, Integer> enchantments;
        if (item.getType() == Material.ENCHANTED_BOOK && resultMeta instanceof EnchantmentStorageMeta encMeta) {
            //enchantments = item.getEnchantments();
            enchantments = encMeta.getStoredEnchants();
        } else {
            enchantments = item.getEnchantments();
        }
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
    private void nerfEnchants(ItemStack item) {
        nerfEnchants(item, Collections.emptyList());
    }

    public void nerfOnlinePlayersItems() {
        p.getServer().getOnlinePlayers().forEach(player -> {
            for (var item : player.getInventory().getContents()) {
                if (item == null) { continue; }
                nerfEnchants(item, Collections.singletonList(player));
            }
        });
    }

    public List<Enchantment> getValidEnchantsForItem(ItemStack item) {
        return enchantmentRegistry.stream()
                .filter(blacklistedEnchants::contains)
                .filter(enc -> enc.canEnchantItem(item))
                .toList();
    }
    public Map.Entry<Enchantment, Integer> rollValidEnchants(ItemStack item) {
        Random random = new Random();
        List<Enchantment> validEnchants = getValidEnchantsForItem(item);
        var enchant = validEnchants.get(random.nextInt(validEnchants.size()));
        return new AbstractMap.SimpleEntry<>(
                validEnchants.get(random.nextInt(validEnchants.size())),
                random.nextInt(p.getConfig().getInt("banned-enchantments.max-level",0)+1)
        );
    }

}
