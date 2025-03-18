package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretTasks;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.revival.RevivalItem;
import com.github.cinnamondev.lifeSeries.revival.RevivalMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.http.WebSocket;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ChestHeadTask extends AbstractPlayerTask implements Listener {
    private final ItemStack loot;
    private StorageMinecart minecart;

    public ChestHeadTask(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, SecretTasks.TaskDifficulty difficulty) {
        super(p, owningPlayer, onTaskCompletion, difficulty);

        // create a list of lootable items. this is a bit of a stream abuse, sorry.
        var lootables = p.getConfig().getMapList("options.secret-life.task-configs.chest-head.loot").stream().map(map -> {
            MemoryConfiguration mc = new MemoryConfiguration();
            mc.addDefaults((Map<String, Object>) map);
            return mc;
        }).flatMap(mc -> {
            String name = mc.getString("name", null);
            int quantity = mc.getInt("quantity", 1);
            if (name == null) {
                p.getLogger().warning("chest head task loot item wo name ignoring");
                return Stream.empty();
            }
            RevivalItem revivalItem = p.getRevivalItem();
            if (name.equalsIgnoreCase("revival-item") && revivalItem != null) {
                return Stream.of(revivalItem.getItem().asQuantity(quantity));
            }
            Material material = Material.getMaterial(name.toUpperCase());
            if (material == null) {
                p.getLogger().warning("chest head task loot item with invalid material, ignoring");
                return Stream.empty();
            }
            return Stream.of(ItemStack.of(material,quantity));
        }).toList();

        if (lootables.isEmpty()) { throw new IllegalStateException("chest head task loot item loot list is empty"); }
        // get random loot item from the lootables list.
        this.loot = lootables.get((int) (lootables.size() * Math.random()));


        spawnCartOnPlayer();
    }

    @Override
    public void fail() {
        super.fail();
        if (minecart != null) { minecart.remove(); }
    }

    @Override
    public void complete() {
        super.complete();
        if (minecart != null) { minecart.remove(); }
    }

    private void spawnCartOnPlayer() {
        this.minecart = (StorageMinecart) owningPlayer.getWorld()
                .spawnEntity(owningPlayer.getLocation().add(0f,1f,0f), EntityType.CHEST_MINECART);

        minecart.setVisibleByDefault(false);
        minecart.getInventory().setItem(15, this.loot);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!e.getPlayer().equals(owningPlayer)) { return; }
        if (getTaskProgress().equals(TaskStatus.COMPLETE) && getTaskProgress().equals(TaskStatus.FAILED)) { return; }
        // entity needs to follow the player
        minecart.teleport(e.getPlayer().getLocation().add(0,1,0));
    }

    public void onPlayerLeave(PlayerQuitEvent e) {
        if (!e.getPlayer().equals(owningPlayer)) { return; }
        if (getTaskProgress().equals(TaskStatus.COMPLETE) && getTaskProgress().equals(TaskStatus.FAILED)) { return; }
        // get rid of entity
        minecart.remove();
    }

    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!e.getPlayer().equals(owningPlayer)) { return; }
        if (getTaskProgress().equals(TaskStatus.COMPLETE) && getTaskProgress().equals(TaskStatus.FAILED)) { return; }
        // respawn entity
        spawnCartOnPlayer();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void storageMinecartClick(InventoryClickEvent e) {
        Inventory inv = e.getClickedInventory();
        if (getTaskProgress().equals(TaskStatus.COMPLETE) && getTaskProgress().equals(TaskStatus.FAILED)) { return; }
        if (inv == null) { return; }

        if (Objects.equals(inv.getHolder(false), minecart)) {
            if (!e.getWhoClicked().equals(owningPlayer)) { return; }
            if (!e.getClick().isLeftClick()) { return; }

            if (e.getSlot() == 15) {
                // got loot item
                inv.close();
                fail();
            } else {
                e.setCancelled(true);
            }
        }
    }
    @Override
    public boolean isTaskGuessable() {
        return false;
    }

    @Override
    public String getTaskKey() {
        return "chest-head";
    }
    // TODO!

    public static class Builder extends AbstractPlayerTask.Builder<ChestHeadTask.Builder> {
        @Override
        public AbstractPlayerTask build(LifeSeries p) {
            return new ChestHeadTask(p, owningPlayer, onTaskCompletion, assignedDifficulty);
        }

        @Override
        public AbstractPlayerTask buildWithAnySettings(LifeSeries p, SecretTasks game) {
            return build(p);
        }
    }
}
