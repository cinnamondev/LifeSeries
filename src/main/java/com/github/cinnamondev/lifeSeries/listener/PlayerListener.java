package com.github.cinnamondev.lifeSeries.listener;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
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

        boolean isFinalDeath;
        if (killer != null && !killed.equals(killer)) {
            isFinalDeath = p.getGame().onKilled(p, killed, killer);
        } else {
            isFinalDeath = p.getGame().onKilled(p, killed);
        }

        // TODO: final death actions
        if (isFinalDeath) { // final death

            Title deathTitle;
            if (killer != null && !killed.equals(killer)) {
                deathTitle = Title.title(
                        Component.text(killed.getName() + " ran out of time!").color(NamedTextColor.RED),
                        Component.text("... after being killed by ").color(NamedTextColor.RED).append(
                                Component.text(killer.getName())
                                        .style(Style.style(
                                                p.getScoreHandler().getTeam(killer).getColor(),
                                                TextDecoration.BOLD
                                        ))
                        )
                );
            } else {
                deathTitle = Title.title(
                        Component.text(killed.getName() + " ran out of time!").color(NamedTextColor.RED),
                        Component.text("... by succumbing to nature.").color(NamedTextColor.RED)
                );
            }
            p.getServer().showTitle(deathTitle);

            killed.getWorld().strikeLightningEffect(killed.getLocation());
        } else if (p.getConfig().getBoolean("options.final-death.keepinv-till-final")) { // not final death
            e.getDrops().clear();
            e.setKeepInventory(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void PlayerRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();

        if (p.getScoreHandler().getTeam(player).equals(p.getScoreHandler().getSpectatorTeam()) && player.getGameMode() != GameMode.CREATIVE) {
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

}
