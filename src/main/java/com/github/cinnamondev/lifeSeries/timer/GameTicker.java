package com.github.cinnamondev.lifeSeries.timer;

import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class GameTicker implements Consumer<BukkitTask> {
    @Override
    public void accept(BukkitTask bukkitTask) {

    }

    @Override
    public @NotNull Consumer<BukkitTask> andThen(@NotNull Consumer<? super BukkitTask> after) {
        return Consumer.super.andThen(after);
    }
}
