package com.github.cinnamondev.lifeSeries;

import com.github.cinnamondev.lifeSeries.gamemodes.BoogeymanFeature.AmITheBoogeyMan;
import com.github.cinnamondev.lifeSeries.commands.AdminCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.BoogeymanFeature.Boogeyman;
import com.github.cinnamondev.lifeSeries.gamemodes.Game;
import com.github.cinnamondev.lifeSeries.gamemodes.LimitedLife;
import com.github.cinnamondev.lifeSeries.gamemodes.Timed.Timed;
import com.github.cinnamondev.lifeSeries.listener.EnchantmentNerfer;
import com.github.cinnamondev.lifeSeries.listener.ItemNerfer;
import com.github.cinnamondev.lifeSeries.listener.PlayerListener;
import com.github.cinnamondev.lifeSeries.revival.RevivalItem;
import com.github.cinnamondev.lifeSeries.teams.ScoreHandler;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.apache.commons.lang3.LocaleUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class LifeSeries extends JavaPlugin {
    private File saveFile;
    private YamlConfiguration saveFileCfg;

    private Locale serverLocale;

    private ScoreHandler scoreHandler;
    private EnchantmentNerfer enchantmentNerfer;
    private ItemNerfer itemNerfer;
    private RevivalItem revivalItem;
    private Game game = null;

    private final ArrayList<NamespacedKey> registeredRecipes = new ArrayList<>();

    private final ScheduledExecutorService asyncScheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> gameTask;
    private boolean doAutosave = true;

    @Override
    public void onEnable() {
        TranslationRegistry registry = TranslationRegistry.create(new NamespacedKey(this, "translations"));
        ResourceBundle bundle = ResourceBundle.getBundle(
                "bundles.Bundle",
                Locale.UK,
                UTF8ResourceBundleControl.get()
        );
        registry.defaultLocale(Locale.UK);
        registry.registerAll(Locale.UK, bundle, true);
        registry.registerAll(Locale.US, bundle, true);
        GlobalTranslator.translator().addSource(registry);
        // used for items and stuff.
        try {
            serverLocale = LocaleUtils.toLocale(getConfig().getString("server-locale", "en-US"));
        } catch (IllegalArgumentException e) {
            getLogger().warning("couldn't get locale, defaulting to en-US");
            serverLocale = Locale.US;
        }

        getServer().getMessenger()
                .registerOutgoingPluginChannel(this, "BungeeCord");
        saveDefaultConfig(); // if doesnt exist
        reloadConfig();

        saveFile = new File(getDataFolder(), "save.yml");
        if (!saveFile.exists()) {
            saveFile.getParentFile().mkdirs();
            saveResource("save.yml", false);
        }
        saveFileCfg = YamlConfiguration.loadConfiguration(saveFile);

        if (saveFileCfg.getBoolean("paused", false)) {
            // game coming back from paused
        } else {
            // game coming back a fresh session
        }
        getLogger().warning("starting score is" + getConfig().getInt("starting-score"));

        switch (getConfig().getString("mode", null)) {
            case "limited-life":
            case "limitedlife":
                game = new LimitedLife(this);
            case "timed":
                game = new Timed(this);
                break;
            case null:
            default:
                getLogger().warning("invalid gamemode, cannot continue!!! ");
                break;
        }

        scoreHandler = new ScoreHandler(this);
        enchantmentNerfer = new EnchantmentNerfer(this);
        itemNerfer = new ItemNerfer(this);
        revivalItem = new RevivalItem(this, new NamespacedKey(this, "revival-item"));
        Bukkit.addRecipe(revivalItem.getRecipe());

        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(enchantmentNerfer, this);
        Bukkit.getPluginManager().registerEvents(itemNerfer, this);
        Bukkit.getPluginManager().registerEvents(revivalItem, this);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (doAutosave) {
                saveFileCfg.set("paused", true); // if the server crashes unnaturally, game should be able to resume.
                saveGame();
            }
            enchantmentNerfer.nerfOnlinePlayersItems();
            itemNerfer.nerfOnlinePlayersItems();
            getServer().getOnlinePlayers().forEach(player -> { // force players to know custom recipes
                player.discoverRecipes(registeredRecipes);
            });
        }, 300,300);
        // Plugin startup logic

        registeredRecipes.addAll(discoverAndAddCustomRecipes());
        registeredRecipes.add(new NamespacedKey(this, "revival-item"));

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, e -> {
            Commands commands = e.registrar();
            // /life <session/lives/time>
            commands.register(
                    AdminCommand.command(this),
                    AdminCommand.description,
                    AdminCommand.aliases
            );

            game.gameCommands(this).forEach(command -> {
                commands.register(
                        command.command(),
                        command.getDescription(),
                        command.getAliases()
                );
            });
        });
    }

    public @Nullable RevivalItem getRevivalItem() { return this.revivalItem; }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (getConfig().getBoolean("options.pause-on-server-stop", false)) {
            pauseSession();
        } else {
            stopSession();
        }

        getServer().getMessenger()
                .unregisterOutgoingPluginChannel(this, "Bungeecord");
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

    public void saveGame() {
        try {
            saveFileCfg.save(saveFile);
        } catch (IOException e) {
            getLogger().warning("couldnt save???");
        }
    }

    public Locale getServerLocale() { return this.serverLocale; }
    /// start ticking the game runner. note that the Game runnable runs in a separate thread to the rest of the game so
    /// it runs at 1t/s, so any action in its run path that can trigger the Bukkit API / change world state should
    /// be deferred to the bukkit scheduler
    public void startSession() {
        //getServer().getScheduler().scheduleSyncRepeatingTask(this, game, 20,20);
        doAutosave = true;
        gameTask = asyncScheduler.scheduleAtFixedRate(game, 1,1, TimeUnit.SECONDS);
    }
    public void stopSession() {
        doAutosave = false;
        saveFileCfg.set("paused", false);
        saveGame();
        if (gameTask == null) { return; }
        if (!gameTask.isDone()) {
            gameTask.cancel(false);
        }

    }
    public void pauseSession() {
        doAutosave = true;
        saveFileCfg.set("paused", true);
        saveGame();
        if (gameTask == null) { return; }
        if (!gameTask.isDone()) {
            gameTask.cancel(false);
        }
    }

    public void endOfSession() {
        stopSession();
        gameTask.cancel(false);
        game.onGameStop(this);
    }

    public void trySendAllToServer() {
        getServer().getOnlinePlayers().forEach(player -> {});
    }
    public Game getGame() { return this.game; }
    public ScoreHandler getScoreHandler() { return this.scoreHandler; }
}
