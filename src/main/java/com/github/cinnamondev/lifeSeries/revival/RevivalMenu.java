package com.github.cinnamondev.lifeSeries.revival;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.google.common.collect.Lists;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DeathProtection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RevivalMenu implements InventoryHolder, Listener {
    private final LifeSeries p;
    private final Inventory inventory;
    private final ItemStack item;
    private final NamespacedKey revivalItemKey;
    private final UUID owningPlayer;
    private final Button<?>[] buttons = new Button[27*2];

    private boolean teleportToPlayer = true;
    private int page = 0;

    public RevivalMenu(LifeSeries p, NamespacedKey revivalItemKey, ItemStack item, UUID player) {
        this.p = p;
        this.revivalItemKey = revivalItemKey;
        this.item = item;
        this.inventory = p.getServer().createInventory(this, 27*2, Component.text("Revival Menu", NamedTextColor.AQUA));
        this.owningPlayer = player;

        buttons[4] = buttonToggleRespawnLocation(4);
        buttons[0] = buttonNextPage(0,
                GlobalTranslator.translator()
                        .translate(Component.translatable("revival-item.buttons.prev-page"), p.getServerLocale()),
                -1
        );
        buttons[8] = buttonNextPage(8,
                GlobalTranslator.translator()
                        .translate(Component.translatable("revival-item.buttons.next-page"), p.getServerLocale()),
                1
        );
        //fillEmptySlots(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE,1));
    }

    public Optional<Button<?>> getButton(int slot) {
        if (slot < 0 || slot >= buttons.length) { return Optional.empty(); }
        return Optional.ofNullable(buttons[slot]);
    }
    public void erasePlayerHeadButtons() {
        for (int i = 18; i < buttons.length; i++) {
            buttons[i] = null;
            inventory.getContents()[i] = null;
        }
    }

    public void fillPage(int newPage) {
        erasePlayerHeadButtons();

        // split online dead players into lists of size (27*2)-18 ( 1 dub - 2 rows)
        List<List<Player>> playerPages = Lists.partition(
                p.getServer().getOnlinePlayers().stream()
                        .filter(player -> p.getScoreHandler().getTeam(player).equals(p.getScoreHandler().getSpectatorTeam()))
                        .filter(player -> player.hasPermission("life.revival"))
                        .map(player -> (Player) player)
                        .toList(),
                1//(27*2)-18
        );
        if (playerPages.isEmpty()) { return; }
        this.page = newPage < playerPages.size() ? newPage : 0;
        List<Player> currentPage = playerPages.get(this.page);

        for (int i = 18; i < buttons.length && (i-18) < currentPage.size(); i++) { // note. currentPage should always be <=
            buttons[i] = revivePlayerButton(i, currentPage.get(i-18)); // to remaining buttons capacity! :)
            buttons[i].updateDisplayBlock();
        }
    }
    private ClickButton buttonNextPage(int slot, Component displayName, int pageOffset) {
        ItemStack item = ItemStack.of(Material.ARROW,1);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(displayName);
        item.setItemMeta(meta);

        ClickButton button = new ClickButton(item, inventory, slot, (player) -> {
            this.page = Math.max(this.page + pageOffset, 0);
            fillPage(this.page);
        });
        button.updateDisplayBlock();
        return button;
    }
    private ToggleButton buttonToggleRespawnLocation(int slot) {
        ItemStack itemOn = ItemStack.of(Material.TOTEM_OF_UNDYING,1);
        ItemMeta meta = itemOn.getItemMeta();

        meta.displayName(GlobalTranslator.translator().translate(
                Component.translatable("revival-item.buttons.location-player"),  p.getServerLocale()
        ));
        itemOn.setItemMeta(meta);

        ItemStack itemOff = ItemStack.of(Material.ENCHANTING_TABLE,1);
        meta = itemOff.getItemMeta();
        meta.displayName(GlobalTranslator.translator().translate(
                Component.translatable("revival-item.buttons.location-spawn"),  p.getServerLocale()
        ));
        itemOff.setItemMeta(meta);

        ToggleButton button = new ToggleButton(itemOn, itemOff,true, inventory, slot, (player, state) ->
                teleportToPlayer = state
        );
        button.updateDisplayBlock();
        return button;
    }
    private ClickButton revivePlayerButton(int slot, Player player) {
        ItemStack item = playerHead(player);
        ClickButton button = new ClickButton(item, inventory, slot, (reviver) -> {
            reviver.getInventory().removeItemAnySlot(this.item);
            reviver.closeInventory();

            p.getScoreHandler().updatePlayerScoreAndTeam(player.getUniqueId(),
                    (_uuid, currentScore) -> p.getConfig().getInt("revival.added-score", 0),
                    (revived, newTeam) -> {
                        String respawnWorld = p.getConfig().getString("options.respawn-world", "auto");
                        if (teleportToPlayer) {
                            revived.teleport(reviver.getLocation());
                        } else if (respawnWorld.equalsIgnoreCase("auto")) {
                            p.getServer().getWorlds().stream()
                                    .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
                                    .findFirst()
                                    .ifPresentOrElse((world) -> revived.teleport(world.getSpawnLocation()), () ->
                                            p.getLogger().warning("Couldn't find a world to teleport revived to!")
                                    );
                        } else {
                            World world = p.getServer().getWorld(respawnWorld);
                            if (world != null) {
                                revived.teleport(world.getSpawnLocation());
                            }
                        }
                        revived.setGameMode(GameMode.SURVIVAL);
                        announceRevival(revived);
                    });
        });
        button.updateDisplayBlock();
        return button;
    }

    private void summonParticleBeam(Player player) {
        Location playerLocation = player.getLocation();
        for (int i = playerLocation.getBlockY() +2; i < playerLocation.getWorld().getMaxHeight(); i++) { // get FIRST highest block thats sky accessible.
            playerLocation.setY(i);
            Block block = playerLocation.getBlock();
            if (block.isEmpty() && block.getLightFromSky() == 15) {
                p.getLogger().info("found free block at " + i);
                block.setType(Material.END_GATEWAY, false);
                p.getServer().getScheduler().runTaskLater(p, () -> block.setType(Material.AIR), 220);
                break;
            }
        }
    }

    private void announceRevival(Player player) {
        if (p.getConfig().getBoolean("revival.announce.title.enabled", true)) {
            if (!p.getConfig().getBoolean("revival.announce.title.totem", false)) {
                p.getServer().showTitle(Title.title(
                        Component.translatable("revival-item.announce-revival")
                                .style(Style.style(NamedTextColor.GOLD, TextDecoration.BOLD))
                                .arguments(player.displayName()),
                        Component.empty()
                ));
                // show players title
            } else {
                p.getServer().getOnlinePlayers().forEach(onlinePlayer -> {
                    ItemStack head = playerHead(player);
                    head.setData(DataComponentTypes.DEATH_PROTECTION, DeathProtection.deathProtection().build());

                    // sendEquipmentChange doesnt actually change the equipment, it just tells the client it's changed.
                    ItemStack currentOffHand = onlinePlayer.getEquipment().getItemInOffHand();
                    onlinePlayer.sendEquipmentChange(onlinePlayer, EquipmentSlot.OFF_HAND, head);
                    p.getServer().getScheduler().runTaskLater(p, () -> {
                        onlinePlayer.playEffect(EntityEffect.TOTEM_RESURRECT);
                        onlinePlayer.sendEquipmentChange(onlinePlayer, EquipmentSlot.OFF_HAND, currentOffHand);
                    },2);

                });
                // show players totem :o
            }
        }
        if (p.getConfig().getBoolean("revival.announce.beam.enabled", true)) {
            summonParticleBeam(player);
        }
    }
    @Override
    public @NotNull Inventory getInventory() {
        this.page = 0; // reset page
        fillPage(this.page);
        return this.inventory;
    }

    private ItemStack playerHead(Player player) {
        ItemStack item = ItemStack.of(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) { return item; }
        meta.setOwningPlayer(player);
        meta.displayName(
                Component.translatable("revival-item.buttons.revive-player")
                        .arguments(player.displayName())
        );
        item.setItemMeta(meta);

        return item;
    }


}
