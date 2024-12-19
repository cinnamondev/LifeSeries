package com.github.cinnamondev.lifeSeries.gamemodes;

import com.github.cinnamondev.lifeSeries.Boogeyman;
import com.github.cinnamondev.lifeSeries.Game;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class Timed implements Game, Boogeyman {
    private int unregisteredPlayerTime; // players who have not joined the game yet
    private HashMap<UUID, Integer> tracked_players;
    private final Plugin p;

    public Timed(Plugin p) {
        this.p = p;
    }

    @Override
    public void pause() {

    }

    @Override
    public void onKilled(Player killed) {

    }

    @Override
    public void onKilled(Player killed, Player killer) {

    }

    @Override
    public void accept(Runnable runnable) {
        unregisteredPlayerTime =- 1;

        for (Integer time : tracked_players.values()) {
            time -= 1;
        }
        // tick savegame player time by 1 tick
        this.p.getServer().getOnlinePlayers()
                .forEach(player -> tracked_players.getOrDefault(player.getUniqueId(), unregisteredPlayerTime));
    }

    @Override
    public @NotNull Consumer<Runnable> andThen(@NotNull Consumer<? super Runnable> after) {
        return Game.super.andThen(after);
    }

    @Override
    public List<Player> roll(int min, int max) {
        return List.of();
    }

    @Override
    public boolean isBoogeyman(OfflinePlayer offlinePlayer) {
        return false;
    }
}
