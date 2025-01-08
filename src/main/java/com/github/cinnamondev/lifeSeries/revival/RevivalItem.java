package com.github.cinnamondev.lifeSeries.revival;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RevivalItem implements Listener {
    private LifeSeries p;
    private final HashMap<UUID, RevivalMenu> menus = new HashMap<>();
    private final NamespacedKey revivalItemKey;

    public RevivalItem(LifeSeries p, NamespacedKey revivalItemKey) {
        this.p = p;
        this.revivalItemKey = revivalItemKey;

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
        itemMeta.setCustomModelData(p.getConfig().getInt("revival.item.model"));

        item.setItemMeta(itemMeta);
        return item;
    }
    public ItemStack getItem() { return getItem(1); }
    public ShapedRecipe shapedRecipe() {
        StringBuilder slots = new StringBuilder();
        List<String> configRecipe = p.getConfig().getStringList("revival.item.recipe");
        HashMap<String, Character> ingredientMap = new HashMap<>();

        int charOffset = 0;
        for (var string : configRecipe) {
            p.getLogger().info(string);
            if (ingredientMap.containsKey(string)) { continue; }
            if (string.equals("EMPTY")) {
                ingredientMap.put(string.toUpperCase(), ' ');
            } else {
                ingredientMap.put(string.toUpperCase(), (char) (33 + charOffset));
                charOffset+=1;
            }
        }


        for (int i=0; i < 9; i++) {
            if (i < configRecipe.size()) {
                slots.append(ingredientMap.get(configRecipe.get(i)));
            } else {
                slots.append(ingredientMap.get("EMPTY"));
            }
        }

        for (Map.Entry<String, Character> stringCharacterEntry : ingredientMap.entrySet()) {
            p.getLogger().warning("key: " + stringCharacterEntry.getKey() + " value: " + stringCharacterEntry.getValue());
        }
        ShapedRecipe recipe = new ShapedRecipe(revivalItemKey, getItem(1)).shape(
                slots.substring(0,3),
                slots.substring(3,6),
                slots.substring(6,9)
        );

        p.getLogger().warning("TOP " + slots.substring(0,3));
        p.getLogger().warning("MED " + slots.substring(3,6));
        p.getLogger().warning("BOT " + slots.substring(6,9));
        for (int i=0; i < 9; i++) {
            String string = configRecipe.get(i);
            if (!string.equals("EMPTY")) {
                Material material = Material.getMaterial(string.toUpperCase());
                if (material != null) {
                    recipe.setIngredient(ingredientMap.get(string), material);
                } else {
                    p.getLogger().warning("couldnt get material " + string + ". cant build revival item recipe!");
                    return null;
                }

            }
        }
        return recipe;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void openRevivalMenu(PlayerInteractEvent e) {
        if (e.getItem() == null) { return; }
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) { return; }
        if (Boolean.TRUE.equals(e.getItem().getItemMeta().getPersistentDataContainer().get(revivalItemKey, PersistentDataType.BOOLEAN))) {
            // it is the revival item
            RevivalMenu menu = menus.computeIfAbsent(e.getPlayer().getUniqueId(), (uuid) -> new RevivalMenu(p, uuid));
            e.getPlayer().openInventory(menu.getInventory());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void menuInteraction(InventoryClickEvent e) {
        Inventory inv = e.getClickedInventory();
        if (inv == null || !e.getClick().isLeftClick()) { return; }

        if (inv.getHolder(false) instanceof RevivalMenu menu) {
            menu.getButton(e.getSlot()).ifPresent(btn -> btn.click(e.getWhoClicked()));
            e.setCancelled(true);
        }
    }
}
