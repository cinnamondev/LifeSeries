package com.github.cinnamondev.lifeSeries.gamemodes;

import org.bukkit.entity.Player;

import java.util.function.Consumer;

public interface Game extends Consumer<Runnable> {
    public void pause();
    public void onKilled(Player killed);
    public void onKilled(Player killed, Player killer);

}