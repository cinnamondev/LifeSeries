package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public interface PlayerTask extends Listener {
    public enum TaskStatus {
        COMPLETE,
        IN_PROGRESS,
        ODD_STATE, // i.e. StayTogetherTask when target is offline.
        FAILED;

        public Component asComponent() {
            return switch(this) {
                case COMPLETE -> Component.translatable("task-status.complete").color(NamedTextColor.GREEN);
                case IN_PROGRESS -> Component.translatable("task-status.in-progress").color(NamedTextColor.YELLOW);
                case ODD_STATE -> Component.translatable("task-status.odd-state").color(NamedTextColor.YELLOW);
                case FAILED -> Component.translatable("task-status.failed").color(NamedTextColor.RED);
            };
        }
    }
    void complete();
    void fail();
    default boolean endOfSession() {
        switch (getTaskProgress()) {
            case COMPLETE:
                return true;
            case IN_PROGRESS:
            case ODD_STATE:
                fail();
            case FAILED:
                return false;
        }
        return false;
    }
    TaskStatus getTaskProgress();
    boolean isTaskGuessable();
    default Component name() {
        return Component.translatable("secret-life.tasks." + getTaskKey() + ".name");
    }
    default Component description() {
        return Component.translatable("secret-life.tasks." + getTaskKey() + ".description");
    }
    default Component lore() {
        return name().style(Style.style(NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC)).hoverEvent(description());
    }
    Optional<ConfigurationSection> getConfigurationSection();
    Player getTaskOwner();
    TaskDifficulty getDifficulty();
    ItemStack createTaskBook();
    ConfigurationSection saveTask(ConfigurationSection taskSection);
    String getTaskKey();
    default void acceptGuess() { fail(); }
    default void givePlayerTaskBook(Player player) { player.give(createTaskBook()); }

}
