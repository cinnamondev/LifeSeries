package com.github.cinnamondev.lifeSeries;

import io.papermc.paper.registry.RegistryAccess;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RevivalItem implements Listener {
    private LifeSeries p;
    private final NamespacedKey revivalItemKey;
    public RevivalItem(LifeSeries p) {
        this.p = p;
        this.revivalItemKey = new NamespacedKey(p, "revival-item");

        Bukkit.addRecipe(shapedRecipe());
    }

    public ItemStack getItem(int n) {
        ItemStack item;
        {
            String keyString = p.getConfig().getString("revival.item.block");
            if (keyString == null) { return null; }
            Material material = Material.getMaterial(keyString);
            if (material == null) { return null; }
            ItemType type =  material.asItemType();
            if (type == null) { return null; }
            item = type.createItemStack(n);
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) { return null; }

        itemMeta.displayName(Component.text(p.getConfig().getString("revival.item.name", "Revival Item"))
                .color(NamedTextColor.LIGHT_PURPLE)
        );
        itemMeta.getPersistentDataContainer().set(revivalItemKey, PersistentDataType.BOOLEAN, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.setUnbreakable(true);
        itemMeta.setEnchantmentGlintOverride(true);
        itemMeta.setMaxStackSize(1);
        itemMeta.setEnchantable(0);
        itemMeta.setCustomModelData(p.getConfig().getInt("revival.item.model"));

        return item;
    }
    public ItemStack getItem() { return getItem(1); }

    public ShapedRecipe shapedRecipe() {
        String[] recipe = new String[9];
        List<String> configRecipe = p.getConfig().getStringList("revival.item.recipe");
        for (int i=0; i < 9; i++) {
            if (i < configRecipe.size()) { recipe[i] = configRecipe.get(i).toUpperCase(); } else { recipe[i] = null; }
        }

        return new ShapedRecipe(revivalItemKey, getItem(1)).shape(recipe);
    }

    public void openRevivalMenu(PlayerInteractEvent e) {
        if (e.getItem() == null) { return; }
        if (Boolean.TRUE.equals(e.getItem().getItemMeta().getPersistentDataContainer().get(revivalItemKey, PersistentDataType.BOOLEAN))) {
            // it is the revival item
        }
    }
}
