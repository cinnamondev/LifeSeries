package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.KillSpecialMobTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.TaskDifficulty;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.BlockDisplayWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ItemDisplayWatcher;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;
import java.util.function.Consumer;

public class CakeKiller extends KillSpecialMobTask<Shulker> {
    private final boolean runawaySpawned = false;
    private Ocelot ocelot = null;
    public CakeKiller(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayer, onTaskCompletion, difficulty, (loc) -> {
            Shulker shulker = (Shulker) loc.getWorld().spawnEntity(loc, EntityType.SHULKER);
            shulker.setRemoveWhenFarAway(false);
            shulker.setAI(false);
            shulker.setInvisible(true);
            shulker.setCollidable(false);
            return shulker;
        });
    }

    @EventHandler
    public void placeBlockEvent(BlockPlaceEvent e) {
        if (!e.getPlayer().equals(owningPlayer)
                || !e.getBlock().getBlockData().getMaterial().equals(Material.CAKE)) { return; }

        owningPlayer.sendMessage(Component.translatable("secret-life.tasks.cake-killer.hunt-that-cake"));

        p.getServer().getScheduler().runTaskLater(p, () -> {
            Location loc = e.getBlock().getLocation();

            this.ocelot = (Ocelot) loc.getWorld().spawnEntity(loc, EntityType.OCELOT);

            ocelot.setTrusting(false);
            ocelot.addPotionEffect(PotionEffectType.SPEED.createEffect(20 * 6000, 1));
            ocelot.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
            ocelot.setHealth(20);

            MiscDisguise disguise = new MiscDisguise(DisguiseType.BLOCK_DISPLAY)
                    .setEntity(ocelot)
                    .setReplaceSounds(true);

            BlockDisplayWatcher watcher = (BlockDisplayWatcher) disguise.getWatcher();
            watcher.setBlock(Material.CAKE.createBlockData());
            watcher.setGlowing(true);
            disguise.setWatcher(watcher);

            disguise.startDisguise();
            e.getBlock().setType(Material.AIR);
            spawnMob(e.getBlock().getLocation());
        },1);

    }

    @Override
    public boolean isTaskGuessable() {
        return true;
    }

    @Override
    public String getTaskKey() {
        return "cake-killer";
    }

    @Override
    public Builder builderProvider() {
        return new Builder();
    }

    public static class Builder extends PlayerTask.Builder<Builder> {
        @Override
        public AbstractPlayerTask build(LifeSeries p) {
            return new CakeKiller(p, owningPlayer, onTaskCompletion, assignedDifficulty);
        }
    }
}
