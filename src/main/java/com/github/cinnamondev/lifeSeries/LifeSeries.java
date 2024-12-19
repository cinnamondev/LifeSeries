package com.github.cinnamondev.lifeSeries;

import com.github.cinnamondev.lifeSeries.commands.BoogeymanCommand;
import com.github.cinnamondev.lifeSeries.commands.GameControlCommand;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class LifeSeries extends JavaPlugin {


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
}
