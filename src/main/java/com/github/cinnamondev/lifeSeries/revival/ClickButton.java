package com.github.cinnamondev.lifeSeries.revival;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ClickButton implements Button<Void> {
    private final Inventory inventory;
    private final int inventorySlot;
    private final ItemStack item;
    private Consumer<HumanEntity> onClicked = (player) -> {};

    public ClickButton(ItemStack item, Inventory owningInventory, int buttonSlot) {
        this.item = item;
        this.inventory = owningInventory;
        this.inventorySlot = buttonSlot;
    }
    public ClickButton(ItemStack item, Inventory owningInventory, int buttonSlot, Consumer<HumanEntity> onClicked) {
        this(item, owningInventory, buttonSlot);
        this.onClicked = onClicked;
    }

    public void onClick(BiConsumer<HumanEntity, Void> onClicked) {
        this.onClicked = (player) -> onClicked.accept(player, null);
    }
    public void onClick(Consumer<HumanEntity> onClicked) {
        this.onClicked = onClicked;
    }

    public void updateDisplayBlock() {
        inventory.setItem(inventorySlot, item);
    }

    @Override
    public void click(HumanEntity player) { onClicked.accept(player); }
}
