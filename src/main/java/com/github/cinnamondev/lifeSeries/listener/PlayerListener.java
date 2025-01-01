package com.github.cinnamondev.lifeSeries.listener;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import net.kyori.adventure.text.Component;
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

import java.util.ArrayList;
import java.util.List;

public class PlayerListener implements Listener {
    private final LifeSeries p;
    public PlayerListener(LifeSeries p) {
        this.p = p;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void PlayerDeath(PlayerDeathEvent e) {
        Player killed = e.getPlayer();
        Player killer = killed.getKiller();

        if (killer == null) {
            p.getGame().onKilled(p,killed);
        } else {
            p.getGame().onKilled(p, killed,killer);
        }

        // only for final deaths. Team should be updated after penalties were dealt to players.
        if (p.getScoreboardHandler().getTeam(killed).getName().equalsIgnoreCase("Dead")) {
            // make the announcement
            if (p.getConfig().getBoolean("options.final-death.drop-head")) {
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD,1);
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                meta.setOwningPlayer(killer);
                skull.setItemMeta(meta);

                e.getDrops().add(skull);
            }
            if (p.getConfig().getBoolean("options.final-death.lightning-strike")) {
                killed.getWorld().strikeLightningEffect(killed.getLocation());
            }
            // respawn player after a couple ticks. gamemode etc is handled when player respawns.
            if (p.getConfig().getBoolean("options.final-death.respawn-after-final")) { // gamemode etc is handled
                killed.setRespawnLocation(killed.getLocation()); // by `PlayerListener::PlayerRespawn`
                p.getServer().getScheduler().runTaskLater(p, () -> killed.spigot().respawn(), 2);
            }
        } else if (p.getConfig().getBoolean("options.final-death.keepinv-till-final")) { // enforce keepinventory
            e.setKeepInventory(true);                                                         // for non-final deaths
            e.getDrops().clear();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void PlayerRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) { return; }

        TeamMeta team = p.getScoreboardHandler().getTeam(player);
        team.setPlayerGameMode(player); // respawn player in appropriate gamemode
    }

}
