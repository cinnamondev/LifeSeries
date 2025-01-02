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
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void PlayerRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
    }

}
