package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.CatLife;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.AbstractTaskGame;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.CatLife.Commands.*;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.SecretLife;
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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class CatLife extends AbstractTaskGame implements Listener {
    private final MeowCommand meowCommand;
    private final HashMap<UUID, CatWatcher> disguiseManager = new HashMap<>();

    public record CatDisguise(Cat.Type type, DyeColor color) {}

    public CatLife(LifeSeries p) {
        super(p);
        this.meowCommand = new MeowCommand(p);
    }

    @Override
    public void run() {
        super.run();
    }

    public void applyCatDisguise(Player player, CatDisguise disguise, boolean save) {
        UUID uuid = player.getUniqueId();
        removeCatDisguise(uuid); // try to remove any cat disguise IF it was there.
        if (save) {
            p.getSave().set("players." + uuid.toString() + ".cat.type", disguise.type.getKey().toString());
            p.getSave().set("players." + uuid.toString() + ".cat.collar", disguise.color.toString());
        }
        var catWatcher = CatLife.applyDisguiseToPlayer(player, disguise);
        disguiseManager.put(uuid, catWatcher);
    }
    public void removeCatDisguise(UUID uuid) {
        disguiseManager.computeIfPresent(uuid, (_uuid, watcher) -> {
            watcher.getDisguise().removeDisguise();
            return null;
        });
    }
    public void removeCatDisguise(OfflinePlayer player) { removeCatDisguise(player.getUniqueId()); }
    public Optional<CatWatcher> getCatWatcher(UUID uuid) {
        return Optional.ofNullable(disguiseManager.get(uuid));
    }
    public Optional<CatWatcher> getCatWatcher(OfflinePlayer player) { return getCatWatcher(player.getUniqueId()); }

    public MeowCommand getMeowCommand() { return this.meowCommand; }

    @Override
    public Collection<FilledLiteralCommand> gameCommands(LifeSeries p) {
        return Stream.concat(super.gameCommands(p).stream(),
                Stream.of(
                        meowCommand,
                        new CatSitCommand( this),
                        new CatVisibilityCommand(this),
                        new DisguiseMeAsACat(this)
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
        applyBestCatDisguise(e.getPlayer());
    }

    public boolean applyBestCatDisguise(Player player) {
        var dis = getDisguiseFor(player);
        if (dis == null) {
            applyCatDisguise(player, TABBY_DISGUISE, false);
            return false;
        }
        applyCatDisguise(player, dis, true);
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        // ensure that cat disguise is moved
        getCatWatcher(e.getPlayer())
                .ifPresent(d -> {
                    d.getDisguise().stopDisguise();
                    d.getDisguise().startDisguise();
                });
    }

    public static final CatDisguise TABBY_DISGUISE = new CatDisguise(Cat.Type.TABBY, DyeColor.RED);
    /// try to get a disguise for a player uuid, if it fails (due to no entry or malformed entry) it will return null.
    public @Nullable CatDisguise getDisguiseFor(UUID uuid) {
        ConfigurationSection section = p.getSave().getConfigurationSection("players." + uuid.toString() + ".cat");
        if (section == null) { return null; }
        Cat.Type type; {
            String str = section.getString("type");
            if (str == null || str.isEmpty()) { // unexpected path, just log and cry
                p.getLogger().info("no cat type found for player " + uuid);
                return null;
            }
            try {
                type = RegistryAccess.registryAccess().getRegistry(RegistryKey.CAT_VARIANT)
                        .get(Key.key(str));
            } catch (NoSuchElementException e) {
                p.getLogger().warning("a cat type was found for player " + uuid + " but it is malformed?");
                return null;
            }
        }
        DyeColor colour; {
            String str = section.getString("collar");
            if (str == null || str.isEmpty()) {
                p.getLogger().info("no cat collar saved for player " + uuid);
                return null;
            }
            try {
                colour = DyeColor.valueOf(str.toUpperCase());
            } catch (IllegalArgumentException e) {
                p.getLogger().warning("invalid cat collar saved for player " + uuid);
                return null;
            }
        }
        return new CatDisguise(type, colour);
    }
    public @Nullable CatDisguise getDisguiseFor(OfflinePlayer player) { return getDisguiseFor(player.getUniqueId()); }

    private static CatWatcher applyDisguiseToPlayer(Player player, CatDisguise disguise) {
        //AttributeInstance scale = player.getAttribute(Attribute.SCALE);
        //if (scale == null) { return null; }
        //scale.setBaseValue(0.5);

        MobDisguise mobDisguise = new MobDisguise(DisguiseType.CAT);
        mobDisguise.setSelfDisguiseVisible(false);
        mobDisguise.setEntity(player);
        mobDisguise.setReplaceSounds(true);
        mobDisguise.setScalePlayerToDisguise(true);

        CatWatcher watcher = (CatWatcher) mobDisguise.getWatcher();
        watcher.setTamed(true);
        watcher.setCollarColor(disguise.color);
        watcher.setType(disguise.type);
        watcher.setCustomNameVisible(true);
        watcher.setCustomName(player.getName());

        mobDisguise.startDisguise();
        return watcher;
    }
}
