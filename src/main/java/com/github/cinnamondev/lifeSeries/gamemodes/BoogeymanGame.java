package com.github.cinnamondev.lifeSeries.gamemodes;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public abstract class BoogeymanGame implements Game {
    private final LifeSeries p;

    private List<UUID> boogeymen;
    public BoogeymanGame(LifeSeries p) {
        this.p = p;
    }
    public Collection<UUID> getBoogeymen() { return boogeymen; }
    /// rolled using specified values.
    public boolean roll(int min, int max) { return false;}
    public void setBoogeys(List<UUID> boogeymen) {
        for (var uuid: boogeymen) {
            p.getConfig().getConfigurationSection("players").getKeys(false).forEach(savedUUID -> {

            });
            p.getSave().set("players." + uuid.toString() + ".is-boogey", "yes");
        }
        this.boogeymen = boogeymen;
    }

    public boolean isBoogeyman(OfflinePlayer offlinePlayer) {
        return isBoogeyman(offlinePlayer.getUniqueId());
    }
    public boolean isBoogeyman(UUID uuid) {
        return boogeymen.contains(uuid);
    }
    /// remove player from list - does NOT apply reward.
    public void cure(UUID uuid) {
        p.getSave().set("players." + uuid.toString() + ".is-boogey", "cured");
        boogeymen.remove(uuid);
    }
    /// remove player from list apply reward
    public void cure(OfflinePlayer offlinePlayer) {
        cure(offlinePlayer.getUniqueId());
    }
    @Override
    public boolean onKilled(LifeSeries p, Player killed, Player killer) {
        if (isBoogeyman(killer)) {
            cure(killer);
            return onKilled(p,
                    killed, p.getConfig().getInt("options.punishment.boogey-death"),
                    killer, p.getConfig().getInt("options.reward.boogey-kill")
            );
        } else {
            return Game.super.onKilled(p, killed, killer); // let default implementation handle it
        }
    }

}
