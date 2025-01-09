package com.github.cinnamondev.lifeSeries.revival;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import io.papermc.paper.datacomponent.DataComponentBuilder;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DeathProtection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RevivalMenu implements InventoryHolder, Listener {
    private final LifeSeries p;
    private final Inventory inventory;
    private final UUID owningPlayer;
    private Button[] buttons = new Button[27*2];
    private HashMap<Integer, Button> buttonMap = new HashMap<>();

    private boolean teleportToPlayer = true;
    private int page = 0;

    public RevivalMenu(LifeSeries p, UUID player) {
        this.p = p;
        this.inventory = p.getServer().createInventory(this, 27*2, Component.text("Revival Menu", NamedTextColor.AQUA));
        this.owningPlayer = player;

        buttons[4] = buttonToggleRespawnLocation(4);
        buttons[0] = buttonNextPage(0, Component.text("Previous page"), -1);
        buttons[8] = buttonNextPage(8, Component.text("Next page"), 1);
        //fillEmptySlots(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE,1));
    }

    public Optional<Button> getButton(int slot) {
        if (slot < 0 || slot >= buttons.length) { return Optional.empty(); }
        return Optional.ofNullable(buttons[slot]);
    }
    public void erasePlayerHeadButtons() {
        for (int i = 18; i < buttons.length; i++) {
            buttons[i] = null;
        }
    }

    public void fillPage(int newPage) {
        ArrayList<Button> headButtons = new ArrayList<>();
        erasePlayerHeadButtons();

        List<List<Player>> playerPages = Lists.partition(
                p.getServer().getOnlinePlayers().stream()
                        .filter(player -> p.getScoreHandler().getTeam(player).equals(p.getScoreHandler().getSpectatorTeam()))
                        .filter(player -> player.hasPermission("life.revival"))
                        .map(player -> (Player) player)
                        .toList(),
                1//(27*2)-18
        );
        List<Player> currentPage = Collections.emptyList();
        if (playerPages.isEmpty()) { p.getLogger().info("no online dead players"); return; }
        this.page = newPage < playerPages.size() ? newPage : 0;
        p.getLogger().info("page is"  + page);
        currentPage = playerPages.get(page);

        for (int i = 18; i < buttons.length && (i-18) < currentPage.size(); i++) { // note. currentPage should always be <=
            buttons[i] = revivePlayerButton(i, currentPage.get(i-18).getUniqueId()); // to remaining buttons capacity! :)
            buttons[i].updateDisplayBlock();
        }
    }
    private ClickButton buttonNextPage(int slot, Component displayName, int pageOffset) {
        ItemStack item = new ItemStack(Material.ARROW,1);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(displayName);
        item.setItemMeta(meta);

        ClickButton button = new ClickButton(item, inventory, slot, (player) -> {
            page = Math.max(page + pageOffset, 0);
            player.sendMessage(displayName);
            player.sendMessage(Component.text("page is " + page));
            fillPage(page);
        });
        button.updateDisplayBlock();
        return button;
    }
    private ToggleButton buttonToggleRespawnLocation(int slot) {
        ItemStack itemOn = new ItemStack(Material.TOTEM_OF_UNDYING,1);
        ItemMeta meta = itemOn.getItemMeta();
        meta.displayName(Component.text("Teleport revived to me"));
        itemOn.setItemMeta(meta);

        ItemStack itemOff = new ItemStack(Material.ENCHANTING_TABLE,1);
        meta = itemOff.getItemMeta();
        meta.displayName(Component.text("Teleport revived to spawn"));
        itemOff.setItemMeta(meta);

        ToggleButton button = new ToggleButton(itemOn, itemOff,true, inventory, slot, (player, state) -> {
            player.sendMessage(Component.text(state ? "Player will now teleport to you on revival"
                    : " Player will now teleport to spawn on revival"));
            teleportToPlayer = state;
        });
        button.updateDisplayBlock();
        return button;
    }
    private ClickButton revivePlayerButton(int slot, UUID uuid) {
        ItemStack item = playerHead(p.getServer().getOfflinePlayer(uuid));
        ClickButton button = new ClickButton(item, inventory, slot, (player) ->
            p.getScoreHandler().updatePlayerScoreAndTeam(uuid,
                    (_uuid, currentScore) -> p.getConfig().getInt("revival.added-score",0),
                    (revived, newTeam) -> {
                        String respawnWorld = p.getConfig().getString("options.respawn-world", "auto");
                        if (teleportToPlayer) {
                            Player reviver = p.getServer().getPlayer(owningPlayer);
                            if (reviver != null) {
                                revived.teleport(reviver.getLocation());
                            }
                        } else if (respawnWorld.equalsIgnoreCase("auto")) {
                            p.getServer().getWorlds().stream()
                                    .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
                                    .findFirst()
                                    .ifPresentOrElse((world) -> revived.teleport(world.getSpawnLocation()), () ->
                                            p.getLogger().warning("Couldn't find a world to teleport revived to!")
                                    );
                        } else {
                            World world = p.getServer().getWorld(respawnWorld);
                            if (world != null) { revived.teleport(world.getSpawnLocation()); }
                        }
                        revived.setGameMode(GameMode.SURVIVAL);
                        announceRevival(revived);
                    }));
        button.updateDisplayBlock();
        return button;
    }

    private void summonParticleBeam(Player player) {
        Location playerLocation = player.getLocation();
        int playerY = playerLocation.getBlockY();
        int maxHeight = playerLocation.getWorld().getMaxHeight();
        for (int i = playerY+2; i < playerLocation.getWorld().getMaxHeight(); i++) { // get FIRST highest block thats sky accessible.
            playerLocation.setY(i);
            Block block = playerLocation.getBlock();
            if (block.isEmpty() && block.getLightFromSky() == 15) {
                p.getLogger().info("found free block at " + i);
                block.setType(Material.END_GATEWAY, false);
                p.getServer().getScheduler().runTaskLater(p, () -> {
                    block.setType(Material.AIR);
                }, 220);
                break;
            }
        }
    }

    private void announceRevival(Player player) {
        if (p.getConfig().getBoolean("revival.announce.title.enabled", true)) {
            if (!p.getConfig().getBoolean("revival.announce.title.totem", false)) {
                p.getServer().showTitle(Title.title(
                        Component.text(player.getName() + " has been revived!")
                                .style(Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)),
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
        page = 0; // reset page
        fillPage(page);
        return this.inventory;
    }

    private ItemStack playerHead(OfflinePlayer player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) { return item; }
        meta.setOwningPlayer(player);
        String username = player.getName();
        meta.displayName(Component.text("Revive " + (username != null ? username : "USERNAME BROKE")));
        item.setItemMeta(meta);

        return item;
    }


}
