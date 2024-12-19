package com.github.cinnamondev.lifeSeries.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerListener implements Listener {
    @EventHandler
    public void PlayerDeath(PlayerDeathEvent e) {
        Player player = e.getPlayer();
        Player killer = player.getKiller();

        if (killer != null) {

        }



    }
}
