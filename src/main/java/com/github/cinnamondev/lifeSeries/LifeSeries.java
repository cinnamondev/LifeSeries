package com.github.cinnamondev.lifeSeries;

import com.github.cinnamondev.lifeSeries.commands.AmITheBoogeyMan;
import com.github.cinnamondev.lifeSeries.commands.GameControlCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.BoogeymanGame;
import com.github.cinnamondev.lifeSeries.gamemodes.Game;
import com.github.cinnamondev.lifeSeries.gamemodes.Timed;
import com.github.cinnamondev.lifeSeries.listener.EnchantmentNerfer;
import com.github.cinnamondev.lifeSeries.listener.InventoryNerfer;
import com.github.cinnamondev.lifeSeries.listener.PlayerListener;
import com.github.cinnamondev.lifeSeries.revival.RevivalItem;
import com.github.cinnamondev.lifeSeries.teams.ScoreHandler;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    private RevivalItem revivalItem;

    private ArrayList<NamespacedKey> registeredRecipes = new ArrayList<>();

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
        revivalItem = new RevivalItem(this, new NamespacedKey(this, "revival-item"));
        Bukkit.addRecipe(revivalItem.getRecipe());


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
        Bukkit.getPluginManager().registerEvents(inventoryNerfer, this);
        Bukkit.getPluginManager().registerEvents(revivalItem, this);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            saveFileCfg.set("paused", true); // if the server crashes unnaturally, game should be able to resume.
            saveGame();
            enchantmentNerfer.nerfOnlinePlayersItems();
            inventoryNerfer.nerfOnlinePlayersItems();
            getServer().getOnlinePlayers().forEach(player -> { // force players to know custom recipes
                player.discoverRecipes(registeredRecipes);
            });
        }, 300,300);
        // Plugin startup logic

        registeredRecipes.addAll(discoverAndAddCustomRecipes());
        registeredRecipes.add(new NamespacedKey(this, "revival-item"));

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

    private List<NamespacedKey> discoverAndAddCustomRecipes() {
        var section =this.getConfig().getConfigurationSection("custom-recipes");
        if (section == null) { return Collections.emptyList(); }

        return section.getKeys(false).stream()
                .map(keyString -> new NamespacedKey(this, keyString))
                .peek(key -> {
                    CustomRecipe recipe = new CustomRecipe(
                            this,
                            key,
                            section.getConfigurationSection(key.getKey())
                    );
                    getServer().getConsoleSender().sendMessage(recipe.getRecipeMessage());
                    Bukkit.addRecipe(recipe.getRecipe());
                })
                .toList();

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
