package com.github.cinnamondev.lifeSeries;

import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;

public interface Game extends Consumer<Runnable> {
    public void pause();
    public void onKilled(Player killed);
    public void onKilled(Player killed, Player killer);

}