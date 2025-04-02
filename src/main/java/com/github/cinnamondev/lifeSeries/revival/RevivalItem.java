package com.github.cinnamondev.lifeSeries.revival;

import com.github.cinnamondev.lifeSeries.CustomRecipe;
import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import java.util.UUID;

public class RevivalItem extends CustomRecipe implements Listener {
    private final HashMap<UUID, RevivalMenu> menus = new HashMap<>();
    protected final LifeSeries p;
    @Inject
    public RevivalItem(LifeSeries p) {
        super(p, new NamespacedKey(p, "revival-item"), p.getConfig().getConfigurationSection("revival.item"));
        this.p = p;
    }

    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public ItemStack getItem() {
        ItemStack item = super.getItem();
        if (item == null) { return null; }

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) { return null; }

        itemMeta.displayName(Component.text(recipeConfig.getString("name", "Revival Item"))
                .color(NamedTextColor.LIGHT_PURPLE)
        );
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.setUnbreakable(true);
        itemMeta.setEnchantmentGlintOverride(true);
        itemMeta.setMaxStackSize(1);
        String itemModelString = recipeConfig.getString("model", null);
        if (itemModelString != null) {
            itemMeta.setItemModel(NamespacedKey.fromString(itemModelString));
        }
        item.setItemMeta(itemMeta);
        return item;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void openRevivalMenuOnItemInteract(PlayerInteractEvent e) {
        if (e.getItem() == null) { return; }
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) { return; }
        if (e.getItem().getItemMeta().getPersistentDataContainer().getOrDefault(key, PersistentDataType.BOOLEAN, false)) {
            // it is the revival item
            e.getPlayer().openInventory(
                    getPlayersMenu(e.getPlayer()).getInventory()
            );
        }
    }

    public RevivalMenu getPlayersMenu(Player player) {
        return menus.computeIfAbsent(player.getUniqueId(), (_uuid) -> new RevivalMenu(p, getItem(), player.locale()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void menuInteraction(InventoryClickEvent e) {
        Inventory inv = e.getClickedInventory();
        if (inv == null) { return; }

        if (inv.getHolder(false) instanceof RevivalMenu menu) {
            e.setCancelled(true);
            if (!e.getClick().isLeftClick()) { return; }
            menu.getButton(e.getSlot()).ifPresent(btn -> btn.click(e.getWhoClicked()));
        }
    }
}
