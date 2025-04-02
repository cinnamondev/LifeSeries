package com.github.cinnamondev.lifeSeries.revival;

import org.bukkit.entity.HumanEntity;

import java.util.function.BiConsumer;

public interface Button<T> {
    public void onClick(BiConsumer<HumanEntity, T> onClicked);
    public void updateDisplayBlock();
    public void click(HumanEntity humanEntity);
}
