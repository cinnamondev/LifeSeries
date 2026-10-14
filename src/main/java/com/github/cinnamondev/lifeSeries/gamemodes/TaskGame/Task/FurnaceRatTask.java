package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.KillSpecialMobTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.TaskDifficulty;
import io.papermc.paper.event.entity.EntityMoveEvent;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.ItemDisplayWatcher;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import javax.naming.Name;
import java.util.function.Consumer;
import java.util.function.Function;

public class FurnaceRatTask extends KillSpecialMobTask<Shulker> implements Listener {
    private boolean runawaySpawned = false;
    private Ocelot ocelot = null;
    public FurnaceRatTask(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayer, onTaskCompletion, difficulty, loc -> {
            Shulker shulker = (Shulker) loc.getWorld().spawnEntity(loc, EntityType.SHULKER);
            shulker.setRemoveWhenFarAway(false);
            shulker.setAI(false);
            shulker.setInvisible(true);
            shulker.setCollidable(false);
            return shulker;
        });


        super.afterDeathAction(e -> { // give the player a diamond + a couple more as a reward
            e.getDrops().clear();
            e.getDrops().add(ItemStack.of(Material.DIAMOND, 3));
        });

    }


    @EventHandler
    public void onFurnaceFuelInsert(InventoryClickEvent e) {
        if (e.getSlotType().equals(InventoryType.SlotType.FUEL)
                && e.getClickedInventory().getType().equals(InventoryType.FURNACE)
                && owningPlayer.equals(e.getWhoClicked())
                && !runawaySpawned) {
            runawaySpawned = true;
            Location furanceLoc = e.getClickedInventory().getLocation();
            // get block infront of the furnace
            var spawnLoc = ((Directional) furanceLoc.getBlock().getBlockData())
                    .getFacing()
                    .getDirection()
                    .toLocation(e.getWhoClicked().getWorld());

            Particle.SMOKE.builder().location(spawnLoc).allPlayers()
                    .count(20)
                    .count(14)
                    .offset(0.3, 0.3, 0.3)
                    .extra(0.2)
                    .spawn();
            Particle.FLAME.builder().location(spawnLoc).allPlayers()
                    .count(3)
                    .offset(0.1, 0.1, 0.1)
                    .extra(0.1)
                    .spawn();
            Particle.CAMPFIRE_SIGNAL_SMOKE.builder()
                    .location(spawnLoc)
                    .allPlayers()
                    .offset(0, -0.3, 0)
                    .extra(0.2)
                    .spawn();

            p.getServer().getScheduler().runTaskLater(p, () -> {
                this.ocelot = (Ocelot) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.OCELOT);

                ocelot.setTrusting(false);
                ocelot.addPotionEffect(PotionEffectType.SPEED.createEffect(20 * 6000, 1));
                ocelot.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
                ocelot.setHealth(20);

                MiscDisguise disguise = new MiscDisguise(DisguiseType.ITEM_DISPLAY)
                        .setEntity(ocelot)
                        .setReplaceSounds(true);

                ItemDisplayWatcher watcher = (ItemDisplayWatcher) disguise.getWatcher();
                watcher.setItemStack(ItemStack.of(Material.DIAMOND));
                watcher.setGlowing(true);
                disguise.setWatcher(watcher);

                disguise.startDisguise();
                spawnMob(spawnLoc);
            },1);
            owningPlayer.sendMessage(Component.text("catch that rat!"));
        }

    }

    @Override
    public void cleanup() {
        super.cleanup();
        ocelot.remove();
    }

    @EventHandler
    public void teleportShulkerBox(EntityMoveEvent e) {
        if (e.getEntity().equals(ocelot)) {
            getMob().teleport(ocelot.getLocation());
        }
    }

    @Override
    public boolean isTaskGuessable() {
        return false;
    }

    @Override
    public String getTaskKey() {
        return "furnace-rat";
    }

    @Override
    public Builder builderProvider() {
        return new Builder();
    }

    public static class Builder extends PlayerTask.Builder<Builder> {
        @Override
        public PlayerTask build(LifeSeries p) {
            return new RunawayDiamond(p, owningPlayer, onTaskCompletion, assignedDifficulty);
        }
    }
}
