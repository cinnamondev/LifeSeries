package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public interface PlayerTask {
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
    Component getTaskName();
    Component getTaskDescription();
    Player getTaskOwner();
    // implementation notes:
    // available arguments from brigadier commands
    static LiteralArgumentBuilder<CommandSourceStack> assignPlayerTaskSubCommand() {
        return null;
    }
    default ItemStack createTaskBook() {
        ItemStack book =ItemStack.of(Material.WRITTEN_BOOK,1);
        book.setItemMeta(((BookMeta) book.getItemMeta()).toBuilder()
                .author(Component.text("God UwU"))
                .title(Component.text("Your task."))
                .addPage(getTaskName().appendNewline().append(getTaskDescription()))
                .build()
        );
        return book;
    }
    default void givePlayerTaskBook(Player player) { player.give(createTaskBook()); }
}
