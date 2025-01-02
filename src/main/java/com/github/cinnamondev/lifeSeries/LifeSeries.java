package com.github.cinnamondev.lifeSeries;

import com.github.cinnamondev.lifeSeries.commands.GameControlCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.Game;
import com.github.cinnamondev.lifeSeries.gamemodes.Timed;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.util.Tick;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class LifeSeries extends JavaPlugin {
    private File saveFile;
    private FileConfiguration saveFileCfg;
    private Game game = null;

    private final ScheduledExecutorService asyncScheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> gameTask;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // if doesnt exist
        FileConfiguration config = getConfig();
        String gamemode = config.getString("mode");

        switch (gamemode) {
            case "limitedlife":
            case "timed":
                game = new Timed(this, 300);
                break;
            case null:
            default:
                getLogger().warning("invalid gamemode " + gamemode + ", cannot continue!!! ");
                break;
        }

        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();

        manager.registerEventHandler(LifecycleEvents.COMMANDS, e -> {
            Commands commands = e.registrar();
            // /life <session/lives/time>
            commands.register(
                    GameControlCommand.command(this),
                    GameControlCommand.description,
                    GameControlCommand.aliases
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

    public void saveGame() {}

    public void startSession() {
        gameTask = asyncScheduler.scheduleAtFixedRate(game, 1,1, TimeUnit.SECONDS);
    }
    public void stopSession() {
        if (!gameTask.isDone()) { gameTask.cancel(false); }
    }
    public void pauseSession() {
        if (!gameTask.isDone()) { gameTask.cancel(false); }
    }

    public Game getGame() { return this.game; }

}
