package com.github.cinnamondev.lifeSeries.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

///  TODO. Idea is to add a summary of kills/deaths to each player that can then be used to generate a session summary
///  things to output: player time left categorized by player team (hmm. maybe a summary couldnt be made if a clean stop isnt done)
///
public class PlayerStatsListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        Player killer = dead.getKiller();

        if (killer != null) {
            // do something with killer
        }

    }

}
