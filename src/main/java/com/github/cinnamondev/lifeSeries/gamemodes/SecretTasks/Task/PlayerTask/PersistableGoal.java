package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

import io.papermc.paper.persistence.PersistentDataViewHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.Optional;

public interface PersistableGoal<T extends PersistentDataViewHolder, V> {
    NamespacedKey goalKey();
    PersistentDataType<?, V> goalType();
    default Optional<V> tryGetGoal(T container) {
        return Optional.ofNullable(container.getPersistentDataContainer().get(goalKey(), goalType()));
    }
}
