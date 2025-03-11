package com.github.cinnamondev.lifeSeries.gamemodes;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class DoubleLife implements Game, Listener {
    private LifeSeries p;

    public DoubleLife(LifeSeries p) {
        this.p = p;
    }

    BidiMap<UUID,UUID> linkedPlayers = new DualHashBidiMap<UUID,UUID>();
    public void unlinkPlayer(UUID player) {
        linkedPlayers.remove(player);
        linkedPlayers.removeValue(player);
    }

    public void linkPlayers(UUID player1, UUID player2) {
        unlinkPlayer(player1); // ensure both players arent linked
        unlinkPlayer(player2);

        linkedPlayers.put(player1, player2);
    }

    public Optional<UUID> tryGetLinkedPlayer(UUID player) {
        UUID uuid = linkedPlayers.get(player);
        return Optional.ofNullable(linkedPlayers.get(player))
                .or(() -> Optional.ofNullable(linkedPlayers.getKey(player)));
    }

    @Override
    public void run() {

    }

    @Override
    public boolean onKilled(LifeSeries p, Player killed, int punishment) {
        return Game.super.onKilled(p, killed, punishment/2); // /2 as it will be applied twice.
    }

    @Override
    public boolean onKilled(LifeSeries p, Player killed, int punishment, Player killer, int reward) {
        return Game.super.onKilled(p, killed, punishment, killer, reward/2);
    }

    @Override
    public Collection<FilledLiteralCommand> gameCommands(LifeSeries p) {
        return Game.super.gameCommands(p);
    }

    @Override
    public Collection<LiteralArgumentBuilder<CommandSourceStack>> adminSubCommands(LifeSeries p) {
        return Game.super.adminSubCommands(p);
    }
}
