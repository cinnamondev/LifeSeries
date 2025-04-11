package com.github.cinnamondev.lifeSeries.gamemodes.Boogeyman;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.Boogeyman.Commands.AmITheBoogeyMan;
import com.github.cinnamondev.lifeSeries.gamemodes.Boogeyman.Commands.BoogeymanSubCommand;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.OfflinePlayer;

import java.util.*;

public interface Boogeyman extends CommandContainer {
    ArrayList<UUID> getBoogeyList();
    default void setBoogeys(Collection<UUID> boogeys) {
        getBoogeyList().clear();
        getBoogeyList().addAll(boogeys);
    }

    boolean rollBoogeyman(int min, int max);

    default boolean isBoogeyman(UUID uuid) { return getBoogeyList().contains(uuid); }
    default boolean isBoogeyman(OfflinePlayer offlinePlayer) { return isBoogeyman(offlinePlayer.getUniqueId()); }
    default void cureBoogeyman(UUID uuid) { getBoogeyList().remove(uuid); }
    default void addBoogeyman(UUID uuid) { getBoogeyList().add(uuid); }
    default void addBoogeyman(OfflinePlayer player) { addBoogeyman(player.getUniqueId()); }
    /// remove player from list apply reward
    default void cureBoogeyman(OfflinePlayer offlinePlayer) { cureBoogeyman(offlinePlayer.getUniqueId()); }
    void punishBoogeymen();

    @Override
    default Collection<FilledLiteralCommand> gameCommands(LifeSeries p) {
        return List.of(new AmITheBoogeyMan(this, p));
    }
    @Override
    default Collection<LiteralArgumentBuilder<CommandSourceStack>> adminSubCommands(LifeSeries p) {
        return Collections.singletonList(BoogeymanSubCommand.boogeyman(p, this));
    }
}
