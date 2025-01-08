package com.github.cinnamondev.lifeSeries.revival;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Button<T> {
    public void onClick(BiConsumer<HumanEntity, T> onClicked);
    public void updateDisplayBlock();
    public void click(HumanEntity humanEntity);
}
