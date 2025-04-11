package com.github.cinnamondev.lifeSeries.revival;

import org.bukkit.entity.HumanEntity;

import java.util.function.BiConsumer;

public interface Button<T> {
    void onClick(BiConsumer<HumanEntity, T> onClicked);
    void updateDisplayBlock();
    void click(HumanEntity humanEntity);
}
