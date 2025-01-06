package com.github.cinnamondev.lifeSeries;

import com.github.cinnamondev.lifeSeries.commands.AmITheBoogeyMan;
import com.github.cinnamondev.lifeSeries.commands.GameControlCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.BoogeymanGame;
import com.github.cinnamondev.lifeSeries.gamemodes.Game;
import com.github.cinnamondev.lifeSeries.gamemodes.Timed;
import com.github.cinnamondev.lifeSeries.listener.EnchantmentNerfer;
import com.github.cinnamondev.lifeSeries.listener.InventoryNerfer;
import com.github.cinnamondev.lifeSeries.listener.PlayerListener;
import com.github.cinnamondev.lifeSeries.teams.ScoreHandler;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.util.Tick;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class LifeSeries extends JavaPlugin {
    private File saveFile;
    private YamlConfiguration saveFileCfg;
    private ScoreHandler scoreHandler;
    private EnchantmentNerfer enchantmentNerfer;
    private InventoryNerfer inventoryNerfer;
    private Game game = null;

    private final ScheduledExecutorService asyncScheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> gameTask;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // if doesnt exist
        reloadConfig();

        saveFile = new File(getDataFolder(), "save.yml");
        if (!saveFile.exists()) {
            saveFile.getParentFile().mkdirs();
            saveResource("save.yml", false);
        }
        saveFileCfg = YamlConfiguration.loadConfiguration(saveFile);

        getLogger().warning("starting score is" + getConfig().getInt("starting-score"));

        scoreHandler = new ScoreHandler(this);
        enchantmentNerfer = new EnchantmentNerfer(this);
        inventoryNerfer = new InventoryNerfer(this);

        String gamemode = getConfig().getString("mode");

        switch (gamemode) {
            case "limitedlife":
            case "timed":
                game = new Timed(this);
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

            if (game instanceof BoogeymanGame boogeymanGame) {
                commands.register(
                        AmITheBoogeyMan.command(this, boogeymanGame),
                        "Are you the boogeyman?",
                        Arrays.asList("aib", "boogey")
                );
            }
        });

        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(enchantmentNerfer, this);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            saveFileCfg.set("paused", true); // if the server crashes unnaturally, game should be able to resume.
            saveGame();
            enchantmentNerfer.nerfOnlinePlayersItems();
            inventoryNerfer.nerfOnlinePlayersItems();
        }, 300,300);
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (getConfig().getBoolean("options.pause-on-server-stop", false)) {
            pauseSession();
        } else {
            stopSession();
        }
    }


    public FileConfiguration getSave() { return saveFileCfg; }
    public File getSaveFile() { return saveFile; }

    public void saveGame() {
        try {
            saveFileCfg.save(saveFile);
        } catch (IOException e) {
            getLogger().warning("couldnt save???");
        }
    }

    public void startSession() {
        //getServer().getScheduler().scheduleSyncRepeatingTask(this, game, 20,20);
        gameTask = asyncScheduler.scheduleAtFixedRate(game, 1,1, TimeUnit.SECONDS);
    }
    public void stopSession() {
        if (!gameTask.isDone()) {
            gameTask.cancel(false);
        }
        saveFileCfg.set("paused", false);
        saveGame();
    }
    public void pauseSession() {
        if (!gameTask.isDone()) {
            gameTask.cancel(false);
        }
        saveFileCfg.set("paused", true);
        saveGame();
    }

    public Game getGame() { return this.game; }
    public ScoreHandler getScoreHandler() { return this.scoreHandler; }
}
