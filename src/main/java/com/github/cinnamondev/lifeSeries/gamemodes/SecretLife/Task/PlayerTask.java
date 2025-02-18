package com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Task;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.function.Consumer;

public interface PlayerTask extends Listener {
    public enum TaskStatus {
        COMPLETE,
        IN_PROGRESS,
        ODD_STATE, // i.e. StayTogetherTask when target is offline.
        FAILED
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
    TranslatableComponent getTaskName();
    TranslatableComponent getTaskDescription();
    Player getTaskOwner();
    default ItemStack createTaskBook() {
        ItemStack book =ItemStack.of(Material.WRITTEN_BOOK,1);
        book.setItemMeta(((BookMeta) book.getItemMeta()).toBuilder()
                .author(Component.text("God"))
                .title(Component.text("Your task."))
                .addPage(getTaskName().appendNewline().append(getTaskDescription()))
                .build()
        );
        return book;
    }
    default void givePlayerTaskBook(Player player) { player.give(createTaskBook()); }
    default Component componentWithLore() {
        return getTaskName().style(Style.style(NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD)).hoverEvent(getTaskDescription());
    }
}
