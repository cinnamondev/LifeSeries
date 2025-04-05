package com.github.cinnamondev.lifeSeries.revival;

import com.github.cinnamondev.lifeSeries.customRecipe.ConfigRecipe;
import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.customRecipe.RecipeProvider;
import com.github.cinnamondev.lifeSeries.customRecipe.ShapedConfigRecipe;
import com.github.cinnamondev.lifeSeries.customRecipe.ShapelessConfigRecipe;
import com.github.cinnamondev.lifeSeries.util.UtilityComponents;
import com.google.inject.Inject;
import jdk.jshell.execution.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.NamespacedKey;
import org.bukkit.Utility;
import org.bukkit.configuration.ConfigurationSection;
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
import java.util.Locale;
import java.util.UUID;

public class RevivalItem implements Listener, RecipeProvider<Recipe> {
    private final HashMap<UUID, RevivalMenu> menus = new HashMap<>();
    private final ConfigRecipe<?> recipe;
    protected final LifeSeries p;
    protected final NamespacedKey key;
    protected final ConfigurationSection config;
    @Inject
    public RevivalItem(LifeSeries p) {
        this.p = p;

        this.key = new NamespacedKey(p, "revival-item");
        this.config = p.getConfig().getConfigurationSection("revival.item");
        if (p.getConfig().getBoolean("revival.item.shapeless", false)) {
            this.recipe = new ShapelessConfigRecipe(p, key, config, this::makeItemRevivalItem);
        } else {
            this.recipe = new ShapedConfigRecipe(p, key, config, this::makeItemRevivalItem);
        }
    }

    protected ItemStack makeItemRevivalItem(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.displayName(Component.text(config.getString("name", "Revival Item"))
                .color(NamedTextColor.LIGHT_PURPLE));

        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.setUnbreakable(true);
        itemMeta.setEnchantmentGlintOverride(true);
        itemMeta.setMaxStackSize(1);
        itemMeta.setItemModel(NamespacedKey.fromString(config.getString("model", "minecraft:clock")));

        item.setItemMeta(itemMeta);
        return item;
    }

    public ItemStack item() {
        return recipe.item();
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
        return menus.computeIfAbsent(player.getUniqueId(), (_uuid) -> new RevivalMenu(p, recipe.item(), player.locale()));
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

    @Override
    public Recipe recipe() {
        return recipe.recipe();
    }
}
