package com.github.cinnamondev.lifeSeries.listener;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * listens for common game events (PlayerDeath) and handles spectator respawn (ensure item drop)
 */
public class PlayerListener implements Listener {
    private final LifeSeries p;


    public PlayerListener(LifeSeries p) {
        this.p = p;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerKilled(PlayerDeathEvent e) {
        Player killed = e.getPlayer();
        Player killer = killed.getKiller();

        boolean isFinalDeath;
        if (killer != null && !killed.equals(killer)) {
            isFinalDeath = p.getGame().onKilled(p, killed, killer);

            if (isFinalDeath && p.getConfig().getBoolean("options.final-death.announce", true)) {
                p.getServer().showTitle(Title.title(
                        Component.translatable("final-death.title")
                                .arguments(killed.displayName())
                                .color(NamedTextColor.RED),
                        Component.translatable("final-death.cause-player")
                                .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD))
                                .arguments(killer.displayName())
                ));
            }
        } else {
            isFinalDeath = p.getGame().onKilled(p, killed);
            if (isFinalDeath && p.getConfig().getBoolean("options.final-death.announce", true)) {
                p.getServer().showTitle(Title.title(
                        Component.translatable("final-death.title")
                                .arguments(killed.displayName())
                                .color(NamedTextColor.RED),
                        Component.translatable("final-death.cause-other").color(NamedTextColor.RED)
                ));
            }
        }

        // TODO: final death actions
        if (isFinalDeath) { // final death
            if (p.getConfig().getBoolean("options.final-death.lightning-strike", true)) {
                killed.getWorld().strikeLightningEffect(killed.getLocation());
            }

            if (p.getConfig().getBoolean("options.final-death.respawn-after-final", false)) {
                p.getServer().getScheduler().runTaskLater(p, () -> killed.spigot().respawn(), 2);
            }

            if (p.getConfig().getBoolean("options.final-death.drop-head", true)) {
                ItemStack head = ItemStack.of(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.setOwningPlayer(killed);
                head.setItemMeta(meta);
                e.getDrops().add(head);
            }
        } else if (p.getConfig().getBoolean("options.final-death.keepinv-till-final", false)) { // not final death
            e.getDrops().clear();
            e.setKeepInventory(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();

        if (p.getScoreHandler().isPlayerSpectator(player) && player.getGameMode() != GameMode.CREATIVE) {
            player.setGameMode(GameMode.SPECTATOR);
        }
    }
}
