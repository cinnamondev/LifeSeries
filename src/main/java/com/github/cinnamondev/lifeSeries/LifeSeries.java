package com.github.cinnamondev.lifeSeries;

import com.github.cinnamondev.lifeSeries.customRecipe.ConfigRecipe;
import com.github.cinnamondev.lifeSeries.commands.AdminCommand;
import com.github.cinnamondev.lifeSeries.customRecipe.ShapedConfigRecipe;
import com.github.cinnamondev.lifeSeries.customRecipe.ShapelessConfigRecipe;
import com.github.cinnamondev.lifeSeries.gamemodes.Game;
import com.github.cinnamondev.lifeSeries.gamemodes.LimitedLife;
import com.github.cinnamondev.lifeSeries.gamemodes.Lives;
import com.github.cinnamondev.lifeSeries.gamemodes.CatLife.CatLife;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.SecretLife;
import com.github.cinnamondev.lifeSeries.gamemodes.Timed.Timed;
import com.github.cinnamondev.lifeSeries.listener.EnchantmentNerfer;
import com.github.cinnamondev.lifeSeries.listener.ItemNerfer;
import com.github.cinnamondev.lifeSeries.listener.PlayerListener;
import com.github.cinnamondev.lifeSeries.revival.RevivalItem;
import com.github.cinnamondev.lifeSeries.teams.ScoreHandler;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
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
    private ScoreHandler scoreHandler;

    // listeners
    @Inject private PlayerListener playerListener;
    @Inject private EnchantmentNerfer enchantmentNerfer;
    @Inject private ItemNerfer itemNerfer;
    @Inject private RevivalItem revivalItem;

    private Game game = null;
    // Game runs in a seperate thread
    private final ScheduledExecutorService asyncScheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> gameTask;

    private final ArrayList<NamespacedKey> registeredRecipes = new ArrayList<>();
    //private int scoreHandlerUpdaterTaskID;
    private boolean doAutosave = true;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // if doesnt exist
        reloadConfig();

        saveFile = new File(getDataFolder(), "save.yml");
        if (!saveFile.exists()) {
            if (!saveFile.getParentFile().mkdirs()) {
                throw new IllegalStateException("Unable to create save directory :(");
            }
            saveResource("save.yml", false);
        }
        saveFileCfg = YamlConfiguration.loadConfiguration(saveFile);

        scoreHandler = new ScoreHandler(this);
        game = switch(getConfig().getString("mode", null)) {
            case "cat-life" -> new CatLife(this);
            case "secret-life" -> new SecretLife(this);
            case "limited-life" -> new LimitedLife(this);
            case "timed" -> new Timed(this);
            case "lives" -> new Lives(this);
            case null -> throw new RuntimeException("Null-y game, we're screwed!");
            default -> throw new RuntimeException("Unexpected value: " + getConfig().getString("mode", null));
        };
        getLogger().info("Gamemode is" + game.toString());

        // injector for plugin and common dependencies
        var binder = new PluginBinderModule(this);
        Injector injector = binder.createInjector();

        // translation
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

        // listener bring up
        Bukkit.getPluginManager().registerEvents(playerListener, this);
        Bukkit.getPluginManager().registerEvents(enchantmentNerfer, this);
        Bukkit.getPluginManager().registerEvents(itemNerfer, this);
        Bukkit.getPluginManager().registerEvents(revivalItem, this);
        if (game instanceof Listener gameListener) {
            Bukkit.getPluginManager().registerEvents(gameListener, this);
        }

        // TODO: determine whether to restore game state from save file or simply ignore everything except score
        if (saveFileCfg.getBoolean("paused", false)) {
            game.restoreStateFromSave();
            // game coming back from paused
        }

        // register custom recipes
        registeredRecipes.addAll(discoverAndAddCustomRecipes());
        // add revival item if its enabled
        if (getConfig().getBoolean("revival.enabled", false)) {
            Bukkit.addRecipe(revivalItem.recipe());
            registeredRecipes.add(new NamespacedKey(this, "revival-item"));
        }

        registerCommands();

        // autosave task
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::autosaveTicker, 300,300);
    }

    public RevivalItem getRevivalItem() { return this.revivalItem; }

    @Override
    public void onDisable() {
        game.onServerDisable();
        // Plugin shutdown logic
        if (getConfig().getBoolean("options.pause-on-server-stop", false)) {
            pauseSession();
        } else {
            stopSession();
        }
    }

    public Game getGame() { return this.game; }
    public ScoreHandler getScoreHandler() { return this.scoreHandler; }

    private void registerCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, e -> {
            Commands commands = e.registrar();
            // /life <session/lives/time>
            commands.register(
                    AdminCommand.command(this),
                    AdminCommand.description,
                    AdminCommand.aliases
            );

            game.gameCommands(this).forEach(command -> commands.register(
                    command.command(),
                    command.getDescription(),
                    command.getAliases()
            ));
        });
    }

    private List<NamespacedKey> discoverAndAddCustomRecipes() {
        var recipeSection =this.getConfig().getConfigurationSection("custom-recipes");
        if (recipeSection == null) { return Collections.emptyList(); }

        return recipeSection.getKeys(false).stream()
                .map(keyString -> new NamespacedKey(this, keyString))
                .peek(key -> {
                    ConfigurationSection recipeConfig = recipeSection.getConfigurationSection(key.getKey());
                    if (recipeConfig == null) { return; }
                    ConfigRecipe<?> recipe;
                    if (recipeConfig.getBoolean("shapeless", false)) {
                        recipe = new ShapelessConfigRecipe(this, key, recipeConfig);
                    } else {
                        recipe = new ShapedConfigRecipe(this, key, recipeConfig);
                    }
                    getServer().getConsoleSender().sendMessage(recipe.explainRecipe());
                    Bukkit.addRecipe(recipe.recipe());
                })
                .toList();

    }

    // runs periodically
    private void autosaveTicker() {
        if (doAutosave) {
            saveFileCfg.set("paused", true); // if the server crashes unnaturally, game should be able to resume.
            saveGame();
        }
        enchantmentNerfer.nerfOnlinePlayersItems();
        itemNerfer.nerfOnlinePlayersItems();
        getServer().getOnlinePlayers().forEach(player -> { // force players to know custom recipes
            player.discoverRecipes(registeredRecipes);
        });
    }

    public FileConfiguration getSave() { return saveFileCfg; }

    public void saveGame() {
        try {
            saveFileCfg.save(saveFile);
        } catch (IOException e) {
            getLogger().warning("couldnt save???");
        }
    }

    /// start ticking the game runner. note that the Game runnable runs in a separate thread to the rest of the game so
    /// it runs at 1t/s, so any action in its run path that can trigger the Bukkit API / change world state should
    /// be deferred to the bukkit scheduler
    public void startSession() {
        game.onGameStart();
        //getServer().getScheduler().scheduleSyncRepeatingTask(this, game, 20,20);
        doAutosave = true;
        //scoreHandlerUpdaterTaskID = getServer().getScheduler().scheduleSyncRepeatingTask(
        //        this,
        //       () -> getScoreHandler().updateTrackableScoresAndTeams((_uuid, score) -> score),
        //        20,
        //        20
        //);

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
        game.onGameStop();
    }

}
