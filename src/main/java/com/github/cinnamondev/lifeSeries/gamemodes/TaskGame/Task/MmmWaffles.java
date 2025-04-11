package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task;

import com.github.cinnamondev.lifeSeries.customRecipe.ConfigRecipe;
import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.customRecipe.RecipeProvider;
import com.github.cinnamondev.lifeSeries.customRecipe.ShapedConfigRecipe;
import com.github.cinnamondev.lifeSeries.customRecipe.ShapelessConfigRecipe;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.KillSpecialMobTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.TaskDifficulty;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class MmmWaffles extends KillSpecialMobTask<Wither> implements RecipeProvider<Recipe> {
    private ArmorStand armorStand = null;
    private final ConfigRecipe<?> recipe;
    private Sound spawnWaffleSound = null;
    private Sound deathWaffleSound = null;
    private List<Sound> ambientWaffleSounds = Collections.emptyList();
    // its like cake killer, we will use a custom item that looks like cake. THIS IS A FUN EASTER EGG THAT REQUIRES
    // A CUSTOM MODEL ITEM FROM A RESOURCE PACK, POTENTIALLY WITH SOUNDS?

    // waffles recipe should be to make bread w custom model,
    // IF the player owns the task, turn it into cake with custom model data.
    public MmmWaffles(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        // Wither Spawner Thing
        super(p, owningPlayer, onTaskCompletion, difficulty, (loc) -> {
            Wither wither = (Wither) loc.getWorld().spawnEntity(loc, EntityType.WITHER);
            wither.enterInvulnerabilityPhase();
            wither.customName(Component.text("Waffle"));
            return wither;
        });

        super.afterDeathAction(e -> {
            if (deathWaffleSound != null) {e.getEntity().playSound(deathWaffleSound);}
        });

        // CREATE RECIPE
        ConfigurationSection section = getConfigurationSection()
                .flatMap(c -> Optional.ofNullable(c.getConfigurationSection("item")))
                .orElseThrow();
        if (section.getBoolean("shapeless", false)) {
            this.recipe = new ShapelessConfigRecipe(p, key, section, this::makeWaffleItem);
        } else {
            this.recipe = new ShapedConfigRecipe(p, key, section, this::makeWaffleItem);
        }

        Bukkit.addRecipe(recipe.recipe());
        owningPlayer.discoverRecipe(recipe.recipeKey());
        soundsFromConfig();
    }

    private ItemStack makeWaffleItem(ItemStack inputItem) {
        ItemMeta meta = inputItem.getItemMeta();
        meta.displayName(Component.text("Waffle", NamedTextColor.AQUA));
        meta.setItemModel(
                NamespacedKey.fromString(
                        getConfigurationSection()
                                .flatMap(c -> Optional.ofNullable(c.getString("item.model", null)))
                                .orElse("minecraft:cake"),
                        p
                )
        );
        inputItem.setItemMeta(meta);
        return inputItem;
    }

    @Override
    public void cleanup() {
        Bukkit.removeRecipe(recipe.recipeKey(), true);
        if (armorStand != null) {armorStand.remove();}
    }

    @Override
    public void complete() {
        super.complete();
    }

    @Override
    public void fail() {
        super.fail();
    }

    private void soundsFromConfig() {
        // CREATE SOUNDS
        this.spawnWaffleSound = getConfigurationSection()
                .flatMap(c -> Optional.ofNullable(c.getString("sounds.spawn", null)))
                .map(Key::key)
                .map(k -> Sound.sound(k, Sound.Source.HOSTILE, 1, 1))
                .orElse(null);

        this.deathWaffleSound = getConfigurationSection()
                .flatMap(c -> Optional.ofNullable(c.getString("sounds.death", null)))
                .map(Key::key)
                .map(k -> Sound.sound(k, Sound.Source.HOSTILE, 1, 1))
                .orElse(null);

        this.ambientWaffleSounds = getConfigurationSection().stream()
                .flatMap(c -> c.getStringList("sounds.ambient").stream().map(Key::key)
                        .map( k -> Sound.sound(k, Sound.Source.HOSTILE, 1, 1))
                ).toList();
    }

    @EventHandler
    public void placeBlockEvent(BlockPlaceEvent e) {
        if (!e.getBlock().getBlockData().getMaterial().equals(Material.CAKE)) { return; }

        owningPlayer.sendMessage(Component.text("Destroy the waffle.")
                .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD)));
        if (spawnWaffleSound != null) { p.getServer().playSound(this.spawnWaffleSound, Sound.Emitter.self()); }

        p.getServer().getScheduler().runTaskLater(p, () -> e.getBlock().setType(Material.AIR),1);
        p.getServer().getScheduler().runTaskLater(p, () -> explosionFun(e.getBlock().getLocation()),2);
        p.getServer().getScheduler().runTaskLater(p, () -> {
            spawnMob(e.getBlock().getLocation());
            Wither wither = getMob();
            Location loc = wither.getLocation();
            armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);

            wither.addPassenger(armorStand);

            armorStand.setVisible(false);
            armorStand.setInvulnerable(true);
            armorStand.setCollidable(false);
            ItemStack waffle = ItemStack.of(Material.WIND_CHARGE,1);
            ItemMeta meta = waffle.getItemMeta();
            meta.setItemModel(new NamespacedKey(p, "waffle"));
            waffle.setItemMeta(meta);

            armorStand.setItem(EquipmentSlot.HEAD, new ItemStack(waffle));
        },10);
    }

    // big explosion effect
    private void explosionFun(Location loc) {
        //loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 10);
        Location finalLoc = loc.add(0,1,0);
        loc.getNearbyPlayers(16).forEach(player -> {
            p.getLogger().info(player.getName());
            Vector mp = player.getLocation().toVector().subtract(finalLoc.toVector());
            WindCharge charge = (WindCharge) finalLoc.getWorld()
                    .spawnEntity(player.getLocation().subtract(mp.normalize().multiply(1)), EntityType.WIND_CHARGE);
            charge.setVelocity(mp.normalize().multiply(3));
        });
    }


    public void craftItemEvent(CraftItemEvent e) {
        if (this.recipe.recipe().equals(e.getRecipe()) && !e.getWhoClicked().equals(owningPlayer)) {
            e.setResult(Event.Result.DENY);
            e.setCancelled(true); // prevent crafting
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void warnOnCrafting(PrepareItemCraftEvent e) {
        // we cant stop the user here but we can tell them to say the magic words
        if (this.recipe.recipe().equals(e.getRecipe()) && !e.getViewers().contains(owningPlayer)) {
            e.getViewers().forEach(player -> player.sendMessage(
                    Component.translatable("block-nerf.crafting-banned")
                            .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD))
            ));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void nerfCrafting(CraftItemEvent e) {
        if (!this.recipe.recipe().equals(e.getRecipe())) { return; }
        if (!e.getViewers().contains(owningPlayer)) {
            ItemStack substitute = ItemStack.of(Material.BARRIER);
            ItemMeta meta = substitute.getItemMeta();
            meta.displayName(Component.translatable("block-nerf.crafting-banned")
                    .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD)));
            substitute.setItemMeta(meta);
            e.setCurrentItem(substitute);
            e.setCancelled(true);
        } else {
            e.setCurrentItem(this.recipe.item()); // force the item to match
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void witherMove(EntityMoveEvent e) { // ensure passengers (armor stand) is aligned to wither.
        if (e.getEntity() instanceof Wither w && doesMobBelongToTask(w)) {
            w.getPassengers().forEach(entity -> {
                entity.getLocation().setYaw(w.getLocation().getYaw());
                entity.getLocation().setPitch(0f);
            });
        }
    }

    @Override
    public boolean isTaskGuessable() {
        return false;
    }

    @Override
    public String getTaskKey() {
        return "waffle-monster";
    }

    @Override
    public Recipe recipe() {
        return this.recipe.recipe();
    }

    @Override
    public Builder builderProvider() {
        return new Builder();
    }

    public static class Builder extends PlayerTask.Builder<Builder> {
        @Override
        public AbstractPlayerTask build(LifeSeries p) {
            return new MmmWaffles(p, owningPlayer, onTaskCompletion, assignedDifficulty);
        }
    }

}
