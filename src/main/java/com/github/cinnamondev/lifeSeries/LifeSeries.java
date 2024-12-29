package com.github.cinnamondev.lifeSeries;

import com.github.cinnamondev.lifeSeries.commands.BoogeymanCommand;
import com.github.cinnamondev.lifeSeries.commands.GameControlCommand;
import com.github.cinnamondev.lifeSeries.teams.ScoreHandler;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class LifeSeries extends JavaPlugin {
    private File saveFile;
    private FileConfiguration saveFileCfg;

    private ScoreHandler scoreboardHandler;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // if doesnt exist
        FileConfiguration config = getConfig();
        String gamemode = config.getString("mode");


        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();

        manager.registerEventHandler(LifecycleEvents.COMMANDS, e -> {
            Commands commands = e.registrar();
            // /life <session/lives/time>
            commands.register(
                    GameControlCommand.command(this),
                    GameControlCommand.description,
                    GameControlCommand.aliases
            );
            // /amitheboogeyman
            commands.register(
                    BoogeymanCommand.command(),
                    BoogeymanCommand.description,
                    BoogeymanCommand.aliases
            );
        });
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void loadSave() {
        saveFile = new File(getDataFolder(), "save.yml");
        if (!saveFile.exists()) {
            saveFile.getParentFile().mkdirs();
            p.saveResource("save.yml", false);
        }

        saveFileCfg = YamlConfiguration.loadConfiguration(saveFile);
    }

    private void saveGame() throws IOException {
        scoreboardHandler.getPlayerScores().forEach((k,v) -> {
            ConfigurationSection section = saveFileCfg.getConfigurationSection(k.toString());
            section.set("score", v);
            ConfigurationSection usefulStats = saveFileCfg.createSection(k.toString());
            section.set("currentTeam", scoreboardHandler.getTeam(k));
        });

        saveFileCfg.save(saveFile);
    }

    public Map<UUID, Integer> forEachPlayer(BiConsumer<UUID, Integer> action) {
        ConfigurationSection players = saveFileCfg.getConfigurationSection("players");
        for (var key : players.getKeys(false)) {
            action.accept(
                    UUID.fromString(key),
                    players.getInt(key.toString() + ".score")
            );
        }
    }
    public ScoreHandler getScoreboardHandler() { return this.scoreboardHandler; }

    /// save all entries to the section for playerdata
    private void savePlayerData() {
        playerData.forEach((k,v) -> {
            ConfigurationSection playerDataSection = saveFileCfg.createSection("players");
            ConfigurationSection playerSection = playerDataSection.createSection(k.toString());
            playerSection.set("name", p.getServer().getOfflinePlayer(k).getName());
            playerSection.set("score", v);
        });
    }
}
