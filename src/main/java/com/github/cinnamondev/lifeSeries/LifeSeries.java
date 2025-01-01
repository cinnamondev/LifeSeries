package com.github.cinnamondev.lifeSeries;

import com.github.cinnamondev.lifeSeries.commands.AmITheBoogeyMan;
import com.github.cinnamondev.lifeSeries.commands.GameControlCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.Game;
import com.github.cinnamondev.lifeSeries.gamemodes.Timed;
import com.github.cinnamondev.lifeSeries.teams.ScoreHandler;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class LifeSeries extends JavaPlugin {
    private File saveFile;
    private FileConfiguration saveFileCfg;

    private ScoreHandler scoreboardHandler;
    private Game game = null;
    private BukkitTask task;
    @Override
    public void onEnable() {
        saveDefaultConfig(); // if doesnt exist
        FileConfiguration config = getConfig();
        String gamemode = config.getString("mode");

        switch (gamemode) {
            case "limitedlife":
            case "timed":
                game = new Timed(this,20);
                break;
            case null:
            default:
                getLogger().warning("invalid gamemode " + gamemode + ", cannot continue!!! ");
                break;
        }

        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();

        loadSave();

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
                    AmITheBoogeyMan.command(this),
                    AmITheBoogeyMan.description,
                    AmITheBoogeyMan.aliases
            );
        });
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public FileConfiguration getSaveFileCfg() { return saveFileCfg; }
    public File getSaveFile() { return saveFile; }

    public void saveGame() {
        getConfig().set("untrackedPlayerScore", scoreboardHandler.getUntrackedPlayerScore());
        scoreboardHandler.getScores().forEach((uuid, score) -> {
           saveFileCfg.set(uuid.toString() + ".score", score);
        });
        try {
            saveFileCfg.save(saveFile);
        } catch (IOException e) {
            getLogger().warning("couldnt save??");
            getServer().getOnlinePlayers().stream().filter(player -> player.hasPermission("life.admin")).forEach(player -> {
                player.sendMessage(Component.text("help!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"));
            });
        }

    }
    private void loadSave() {
        saveFile = new File(getDataFolder(), "save.yml");
        if (!saveFile.exists()) {
            saveFile.getParentFile().mkdirs();
            saveResource("save.yml", false);
        }

        saveFileCfg = YamlConfiguration.loadConfiguration(saveFile);

        if (saveFileCfg.getInt("untrackedPlayerScore", -1 ) == -1) {
            // reset
            saveFileCfg.set("untrackedPlayerScore", getConfig().getInt("starting-score"));
        }
        ConfigurationSection playerList = saveFileCfg.getConfigurationSection("players") != null ?
                getConfig().getConfigurationSection("players")
                : getConfig().createSection("players");

        HashMap<UUID, Integer> scoreList = new HashMap<>();
        playerList.getKeys(false).stream().forEach(uuid -> {
           int score = playerList.getConfigurationSection(uuid).getInt("score", -1);
           if (score != -1) { scoreList.put(UUID.fromString(uuid), score); }
        });
        scoreboardHandler = new ScoreHandler(this, scoreList, getConfig().getInt("untrackedPlayerScore"));
        if (saveFileCfg.getBoolean("paused", false)) {
            saveFileCfg.set("paused", false); // unset
            game.configureFromResumedSave(playerList);
        } else {
            game.resetPerSessionData(playerList);
        }
    }
    public void startSession() {
        task = getServer().getScheduler().runTaskTimer(
            this,
            game,
            10,
            20);
    }
    public void stopSession() {
        saveFileCfg.set("paused", false);
        saveGame();
        if (!task.isCancelled()) { task.cancel(); }
    }
    public void pauseSession() {
        saveFileCfg.set("paused", true);
        saveGame();
        if (!task.isCancelled()) { task.cancel(); }
    }

    public ScoreHandler getScoreboardHandler() { return this.scoreboardHandler; }
    public Game getGame() { return this.game; }

}
