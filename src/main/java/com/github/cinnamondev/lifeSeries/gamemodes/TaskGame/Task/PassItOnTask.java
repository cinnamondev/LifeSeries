package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.TaskGame;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.SessionLongTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.TaskDifficulty;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class PassItOnTask extends AbstractPlayerTask implements SessionLongTask, Listener {
    private final NamespacedKey bookKey;
    protected final TaskGame game;
    protected PlayerTask.Builder<?> previousTaskBuilder = null;
    public PassItOnTask(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, TaskGame game, PlayerTask.Builder<?> previousTask, TaskDifficulty difficulty) {
        // we require secrettasks as a dependency here, so that we can assign the next player a task.
        super(p, owningPlayer, onTaskCompletion, difficulty);
        this.bookKey = new NamespacedKey(p, "pass-it-on");
        this.game = game;
        boolean saveOldTask = getConfigurationSection()
                .map(c -> c.getBoolean("save-old-task", false))
                .orElse(false);
        if (previousTask != null && saveOldTask) {
            this.previousTaskBuilder = previousTask;
            owningPlayer.sendMessage(Component.translatable("secret-life.tasks.if-saves-previous"));
        }
    }

    @Override
    public void cleanup() {
        p.getServer().getOnlinePlayers().forEach(player -> // go hog wild
                player.getInventory().removeItemAnySlot(createTaskBook().asQuantity(Integer.MAX_VALUE))
        );
    }

    @Override
    public void complete() {
        super.complete();
        if (previousTaskBuilder != null) {
            game.addSecretTask(previousTaskBuilder.onCompletion(taskConsumer).build(p));
        }
    }

    @Override
    public boolean isTaskGuessable() {
        return false;
    }

    @Override
    public String getTaskKey() {
        return "pass-it-on";
    }

    @Override
    public ItemStack createTaskBook() {
        ItemStack book = super.createTaskBook();
        ItemMeta meta = book.getItemMeta();
        meta.getPersistentDataContainer().set(bookKey, PersistentDataType.STRING, owningPlayer.getUniqueId().toString());
        book.setItemMeta(meta);
        return book;
    }

    @EventHandler
    public void bookPickupEvent(EntityPickupItemEvent e) {
        String uuidStr = e.getItem().getItemStack()
                .getPersistentDataContainer().get(bookKey, PersistentDataType.STRING);
        if (uuidStr == null) { return; }
        // player picks up item, link it back to this specific task.
        if (e.getEntity() instanceof Player picker && UUID.fromString(uuidStr).equals(owningPlayer.getUniqueId())) {
            explanation = Component.text("Book was picked up by player ").append(picker.displayName());
            p.getServer().getScheduler().runTaskLater(p, () -> { // in next tick, get rid of the old book and give a new one.
                complete(); // complete current players task

                // now we need to make a new task for the player who picked the book up.
                boolean saveOldTask = getConfigurationSection()
                        .map(c -> c.getBoolean("save-old-task", false))
                        .orElse(false);
                Optional<PlayerTask> oOldTask = game.getSecretTask(picker);
                Builder builder = (Builder) this.createBuilder()
                        .player(picker)
                        .onCompletion(taskConsumer); // create same task with new player
                if (oOldTask.isPresent()) { // if old task exists, make it a builder
                    oOldTask.get().cleanup();
                    if (saveOldTask) { builder = builder.previousTask(oOldTask.get().createBuilder()); }
                }
                game.addSecretTask(builder.build(p));
            }, 1);
        } else { e.setCancelled(true); } // prevent non human pickup.

    }
    private Component explanation = Component.text("book has not been passed on");
    @Override
    public Component taskProgressExplanation() {
        return explanation;
    }

    @Override
    public Builder builderProvider() {
        return new Builder();
    }
    public static class Builder extends PlayerTask.Builder<Builder> {
        protected PlayerTask.Builder<?> previousTaskBuilder = null;
        public Builder previousTask(PlayerTask.Builder<?> previousTaskBuilder) { // not required typically.
            this.previousTaskBuilder = previousTaskBuilder;
            return this;
        }
        @Override
        public AbstractPlayerTask build(LifeSeries p) {
            if (p.getGame() instanceof TaskGame game) {
                return new PassItOnTask(p, owningPlayer, onTaskCompletion, game, previousTaskBuilder, assignedDifficulty);
            } else { throw new RuntimeException("PassItOnTask requires SecretTasks"); }
        }
    }
}
