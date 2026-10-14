package com.github.cinnamondev.lifeSeries;

import com.github.cinnamondev.lifeSeries.gamemodes.Game;
import com.github.cinnamondev.lifeSeries.gamemodes.LimitedLife;
import com.github.cinnamondev.lifeSeries.gamemodes.Lives;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.CatLife.CatLife;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.SecretLife;
import com.github.cinnamondev.lifeSeries.gamemodes.Timed.Timed;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.registry.event.RegistryEvents;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.Level;

public class Bootstrap implements PluginBootstrap {
    YamlConfiguration config = null;
    File configFile;
    LifeSeries plugin;

    public ConfigurationSection getConfig() {
        if (config == null) {
            reloadConfig();
        }

        return config;
    }
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);

        final InputStream defConfigStream = getResource("config.yml");
        if (defConfigStream == null) {
            return;
        }

        config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
    }


    public @Nullable InputStream getResource(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        try {
            URL url = getClass().getClassLoader().getResource(filename);
            if (url == null) {
                return null;
            }
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }
    public void saveDefault(BootstrapContext ctx, String resourcePath, boolean replace) {
        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource("config.yml");
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + file);
        }

        File outFile = ctx.getDataDirectory().resolve(resourcePath).toFile();
        File outDir = outFile.getParentFile();

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } else {
                ctx.getLogger().info("Could not save {} to {} because {} already exists.", outFile.getName(), outFile, outFile.getName());
            }
        } catch (IOException ex) {
            ctx.getLogger().warn("Could not save {} to {}", outFile.getName(), outFile, ex);
        }
    }

    @Override
    public void bootstrap(BootstrapContext ctx) {
        plugin = new LifeSeries();

        Game game;
        game = switch(getConfig().getString("mode", null)) {
            case "cat-life" -> {
                // check if we have LibsDiguises...
                try {
                    Class<?> clazz = Class.forName("me.libraryaddict.disguise.disguisetypes.watchers.CatWatcher");
                } catch (ClassNotFoundException e) {
                    ctx.getLogger().error("LibsDisguises is not present!!!!!!! REQUIRED for cat life.");
                    throw new RuntimeException(e);
                }
                yield new CatLife(plugin);
            }
            case "secret-life" -> new SecretLife(plugin);
            case "limited-life" -> new LimitedLife(plugin);
            case "timed" -> new Timed(plugin);
            case "lives" -> new Lives(plugin);
            case null -> throw new RuntimeException("Null-y game, we're screwed!");
            default -> throw new RuntimeException("Unexpected value: " + getConfig().getString("mode", null));
        };

        ctx.getLifecycleManager()
                .registerEventHandler(RegistryEvents.DIALOG.compose()
                .newHandler(game::buildMainDialog));

        for (var consumer : game.secondaryDialogs()) {
            ctx.getLifecycleManager()
                    .registerEventHandler(RegistryEvents.DIALOG.compose()
                            .newHandler(consumer::apply));
        }

        plugin.withGame(game);

        configFile = ctx.getDataDirectory().resolve("config.yml").toFile();
        saveDefault(ctx, "config.yml", false);
        reloadConfig();

    }

    @Override
    public JavaPlugin createPlugin(PluginProviderContext context) {
        return plugin;
    }

    private void registerDialogs(BootstrapContext ctx) {

    }
}
