package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.*;
import com.github.cinnamondev.lifeSeries.util.PlayerHead;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ChestHeadTask extends AbstractPlayerTask implements Listener, SessionLongTask, HasRadius {
    private ArmorStand armorStand; // TODO: this task should have a 'grace period'
    private final Inventory inventory;
    private final NamespacedKey ballLauncherKey;
    private final AtomicBoolean gracePeriodOver = new AtomicBoolean(false);
    public ChestHeadTask(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
        this.ballLauncherKey = new NamespacedKey(p, "ball-launcher");
        // create a list of lootable items. this is a bit of a stream abuse, sorry.
        var lootables =  p.getConfig().getMapList("options.secret-life.task-configs.chest-head.loot").stream()
                .map(map -> (Map<String, Object>) map)
                .flatMap(map -> {
                    String name = (String) map.get("name");
                    int quantity = (Integer) map.getOrDefault("quantity", 1);
                    if (name == null) { return Stream.empty(); }

                    if (name.equalsIgnoreCase("revival-item")) {
                        return Stream.of(p.getRevivalItem().item().asQuantity(quantity));
                    } else {
                        Material material = Material.getMaterial(name.toUpperCase());
                        if (material == null) {
                            return Stream.empty();
                        }
                        return Stream.of(ItemStack.of(material, quantity));
                    }
                }).toList();

        if (lootables.isEmpty()) { throw new IllegalStateException("chest head task loot item loot list is empty"); }
        // get random loot item from the lootables list.
        this.inventory = p.getServer().createInventory(owningPlayer, 27,
                Component.translatable("secret-life.tasks.chest-head.briefcase-title"));
        this.inventory.setItem(13, lootables.get((int) (lootables.size() * Math.random())));

        int grace = getConfigurationSection()
                .map(c -> c.getInt("grace", -1))
                .filter(c -> c <= 0)
                .orElse(0) * 20;

        if (grace != 0) { // tell player about the grace period
            owningPlayer.sendMessage(
                    Component.translatable("secret-life.tasks.grace-period", String.valueOf(grace))
            );
        } else { grace = 1; } // next tick if there is no grace period.

        p.getServer().getScheduler().runTaskLater(p, () -> {
            gracePeriodOver.set(true);
            spawnCartOnPlayer();
            owningPlayer.give(ballLauncher(1));
        }, grace);
    }

    @Override
    public void cleanup() {
        if (armorStand != null) { armorStand.remove(); }
    }

    @Override
    public void fail() {
        super.fail();
        cleanup();
    }


    @Override
    public void complete() {
        super.complete();
        cleanup();
    }

    private void spawnCartOnPlayer() {
        double scale = Objects.requireNonNull(owningPlayer.getAttribute(Attribute.SCALE)).getValue();
        this.armorStand = (ArmorStand) owningPlayer.getWorld()
                .spawnEntity(owningPlayer.getLocation().add(0,0.1f * scale, 0), EntityType.ARMOR_STAND);

        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        armorStand.setCollidable(false);
        armorStand.setVisible(false);

        try {
            ItemStack head = PlayerHead.fromUrl(1, URI.create(getConfigurationSection()
                            .map(section -> section.getString("head-url")) // whatever the user said
                            .orElse("http://textures.minecraft.net/texture/275bcff2e74deed37a319a1f404e70d06a5f360cacee99c71346f38560cbd72a") // chest head
                    ).toURL());
            armorStand.getEquipment().setHelmet(head);
        } catch (MalformedURLException e) {
            throw new RuntimeException("should just work?" + e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!e.getPlayer().equals(owningPlayer) || !gracePeriodOver.get()) { return; }
        // entity needs to follow the player
        double scale = Objects.requireNonNull(owningPlayer.getAttribute(Attribute.SCALE)).getValue();
        armorStand.teleport(owningPlayer.getLocation().add(0, 0.1f * scale, 0));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent e) {
        if (!e.getPlayer().equals(owningPlayer) || !gracePeriodOver.get()) { return; }
        // get rid of entity
        cleanup();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!e.getPlayer().equals(owningPlayer) || !gracePeriodOver.get()) { return; }
        // respawn entity
        spawnCartOnPlayer();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void armorStandHeadClick(PlayerArmorStandManipulateEvent e) {
        if (e.getRightClicked().equals(armorStand)) { e.setCancelled(true); }
        if (e.getSlot() != EquipmentSlot.HEAD || e.getPlayer().equals(owningPlayer)) { return; }

        e.getPlayer().openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void lootTakenEvent(InventoryClickEvent e) {
        if (!e.getInventory().equals(inventory)) { return; }
        if (e.getSlot() == 13) {
            if (withinRadius(e.getWhoClicked().getLocation(), owningPlayer.getLocation())) {
                // player is close enough
                fail();
            } else {
                // if theyre too far away just close the inventory when they try to take it.
                e.setCancelled(true);
            }
            // must be next tick or it screws up delivering the loot to the player
            p.getServer().getScheduler().runTaskLater(p, () -> e.getInventory().close(), 1);

        } else { e.setCancelled(true); }
    }

    public ItemStack ballLauncher(int n) {
        ItemStack launcher = ItemStack.of(Material.WIND_CHARGE, n);

        ItemMeta meta = launcher.getItemMeta();
        meta.displayName(Component.text(
                GlobalTranslator.translator().translate("secret-life.tasks.chest-head.ball-launcher-name", owningPlayer.locale())
                        .format(null)
        ));
        meta.getPersistentDataContainer().set(ballLauncherKey, PersistentDataType.BOOLEAN, true);
        NamespacedKey model = getConfigurationSection()
                .map(c -> NamespacedKey.fromString(c.getString("model", "minecraft:wind_charge")))
                .orElse(NamespacedKey.fromString("minecraft:wind_charge"));
        meta.setItemModel(model);
        launcher.setItemMeta(meta);
        return launcher;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void preventBallLauncherPickup(EntityPickupItemEvent e) {
        if (e.getEntity().equals(owningPlayer)
                || !getConfigurationSection().map(c -> c.getBoolean("prevent-sharing", false)).orElse(false)) {
            return;
        } // we dont care about owning player or if prevent-sharing is off.

        if (e.getItem().getItemStack().getPersistentDataContainer().get(ballLauncherKey, PersistentDataType.BOOLEAN) != null) {
                e.setCancelled(true);
        }
    }
    @EventHandler(priority = EventPriority.NORMAL)
    public void ballLauncherFire(PlayerLaunchProjectileEvent e) {
        if (e.getItemStack().getPersistentDataContainer().get(ballLauncherKey, PersistentDataType.BOOLEAN) != null) {
            e.setShouldConsume(false); // dont consume ball launcher
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void closeInventoryWhenPlayerHit(EntityDamageByEntityEvent e) {
        if (!Objects.equals(e.getDamageSource().getCausingEntity(), owningPlayer)) { return; }
        if (e.getEntity() instanceof Player damaged) {
            if (damaged.getOpenInventory().getTopInventory().equals(this.inventory)) {
                damaged.closeInventory();
            }
        }
    }

    @Override
    public boolean isTaskGuessable() {
        return false;
    }

    @Override
    public String getTaskKey() { return "chest-head"; }

    @Override
    public Builder builderProvider() {
        return new Builder();
    }

    public static class Builder extends PlayerTask.Builder<Builder> {
        @Override
        public AbstractPlayerTask build(LifeSeries p) {
            return new ChestHeadTask(p, owningPlayer, onTaskCompletion, assignedDifficulty);
        }
    }
}
