package com.github.cinnamondev.lifeSeries.teams;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class ScoreHandler {
    private Plugin p;
    private YamlConfiguration save;
    private ConfigurationSection playerData;
    public ScoreHandler(Plugin p) {
        this.p = p;
    }

    public int getScore(OfflinePlayer player) { return 100;}
    public int setScore(OfflinePlayer player) {
        return 110;
    }
}
