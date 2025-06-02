package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.CatLife;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.SecretLife;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.CatLife.Commands.CatDisguiseSubCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.CatLife.Commands.CatSitCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.CatLife.Commands.MeowCommand;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.CatWatcher;
import net.kyori.adventure.key.Key;
import org.bukkit.DyeColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;
import java.util.stream.Stream;

public class CatLife extends SecretLife implements Listener {
    private final MeowCommand meowCommand;
    private final HashMap<UUID, CatWatcher> disguiseManager = new HashMap<>();

    public CatLife(LifeSeries p) {
        super(p);
        this.meowCommand = new MeowCommand(p);
    }

    @Override
    public void run() {
        super.run();
        savePlayerDisguises();
    }

    public void savePlayerDisguises() {
        disguiseManager.entrySet().forEach(entry -> {
            p.getSave().set("players." + entry.getKey() + ".cat.type", entry.getValue().getType().getKey().toString());
            p.getSave().set("players." + entry.getKey() + ".cat.collar", entry.getValue().getCollarColor().toString());
        });
    }

    public boolean tryPlayerDisguiseFromConfig(Player player) {
        String keyString = p.getSave().getString("players." + player.getUniqueId() + ".cat.type");
        if (keyString == null) {
            p.getLogger().info("no catsguise type saved for player: " + player.getName());
            return false;
        }
        Cat.Type type = RegistryAccess.registryAccess().getRegistry(RegistryKey.CAT_VARIANT)
                .get(Key.key(p.getSave().getString("players." + player.getUniqueId() + ".cat.type","brokey")));
        if (type == null) {
            p.getLogger().warning("invalid cat type key in save for player " + player.getName());
            return false;
        }

        String dyeString = p.getSave().getString("players." + player.getUniqueId() + ".cat.collar");
        if (dyeString == null) {
            p.getLogger().info("no catsguise collar saved for player: " + player.getName());
            return false;
        }

        DyeColor colour;
        try {
            colour = DyeColor.valueOf(dyeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            p.getLogger().warning("invalid cat collar saved for player: " + player.getName());
            return false;
        }

        addPlayerDisguise(player.getUniqueId(), CatLife.catDisguise(player, type, colour));
        return true;
    }
    public void addPlayerDisguise(UUID uuid, CatWatcher catWatcher) {
        removePlayerDisguise(uuid);
        disguiseManager.put(uuid, catWatcher);
    }
    public void addPlayerDisguise(OfflinePlayer player, CatWatcher catWatcher) {
        addPlayerDisguise(player.getUniqueId(), catWatcher);
    }
    public void removePlayerDisguise(UUID uuid) {
        disguiseManager.computeIfPresent(uuid, (_uuid, watcher) -> {
            watcher.getDisguise().removeDisguise();
            return null;
        });
    }
    public Optional<CatWatcher> getCatWatcher(UUID uuid) {
        return Optional.ofNullable(disguiseManager.get(uuid));
    }
    public Optional<CatWatcher> getCatWatcher(OfflinePlayer player) { return getCatWatcher(player.getUniqueId()); }

    public MeowCommand getMeowCommand() { return this.meowCommand; }

    public void removePlayerDisguise(OfflinePlayer player) {
        removePlayerDisguise(player.getUniqueId());
    }

    @Override
    public Collection<FilledLiteralCommand> gameCommands(LifeSeries p) {
        return Stream.concat(super.gameCommands(p).stream(),
                Stream.of(
                        meowCommand,
                        new CatSitCommand(p, this)
                )
        ).toList();
    }

    @Override
    public Collection<LiteralArgumentBuilder<CommandSourceStack>> adminSubCommands(LifeSeries p) {
        var commands = new ArrayList<>(super.adminSubCommands(p));
        commands.add(CatDisguiseSubCommand.command(p, this));
        return commands;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        tryDisguiseFromConfigOrUseTabby(e.getPlayer());
    }

    public void tryDisguiseFromConfigOrUseTabby(Player player) {
        boolean success = tryPlayerDisguiseFromConfig(player);
        if (!success) {
            CatLife.catDisguise(player,
                    Cat.Type.TABBY,
                    DyeColor.RED
            );

        }
    }

    public static CatWatcher catDisguise(Player player, Cat.Type catType, DyeColor dyeColor) {
        //AttributeInstance scale = player.getAttribute(Attribute.SCALE);
        //if (scale == null) { return null; }
        //scale.setBaseValue(0.5);

        MobDisguise disguise = new MobDisguise(DisguiseType.CAT);
        disguise.setSelfDisguiseVisible(false);
        disguise.setEntity(player);
        disguise.setReplaceSounds(true);
        disguise.setScalePlayerToDisguise(true);

        CatWatcher watcher = (CatWatcher) disguise.getWatcher();
        watcher.setTamed(true);
        watcher.setCollarColor(dyeColor);
        watcher.setType(catType);
        watcher.setCustomNameVisible(false);

        disguise.startDisguise();
        return watcher;
    }
}
