package com.github.cinnamondev.lifeSeries.revival;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

public class ToggleButton implements Button<Boolean> {
    private final Inventory inventory;
    private final int inventorySlot;
    private final ItemStack onItem;
    private final ItemStack offItem;
    private boolean on = false;
    private BiConsumer<HumanEntity, Boolean> onClicked = (player, click) -> {};
    public ToggleButton(ItemStack onItem, ItemStack offItem, boolean initialState,
                        Inventory owningInventory, int buttonSlot) {
        this.onItem = onItem;
        this.offItem = offItem;
        this.on = initialState;
        this.inventory = owningInventory;
        this.inventorySlot = buttonSlot;
    }
    public ToggleButton(ItemStack onItem, ItemStack offItem, boolean initialState,
                        Inventory owningInventory, int buttonSlot,
                        BiConsumer<HumanEntity, Boolean> onClicked) {
        this(onItem, offItem, initialState, owningInventory, buttonSlot);
        this.onClicked = onClicked;
    }


    public void onClick(BiConsumer<HumanEntity, Boolean> onClicked) {
        this.onClicked = onClicked;
    }

    public void updateDisplayBlock() {
        inventory.setItem(inventorySlot, on ? onItem : offItem);
    }
    public void click(HumanEntity human) { on = !on; updateDisplayBlock(); onClicked.accept(human, on); }
}
