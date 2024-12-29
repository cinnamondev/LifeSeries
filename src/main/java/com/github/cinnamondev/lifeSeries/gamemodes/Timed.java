package com.github.cinnamondev.lifeSeries.gamemodes;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.teams.ScoreHandler;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.ScoreboardManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class Timed implements Game, BoogeymanGame {
    private File saveFile;
    private FileConfiguration saveFileCfg;

    private final int timerDecrementQuantity;

    private int unregisteredPlayerTime; // players who have not joined the game yet
    private HashMap<UUID, Integer> tracked_players;
    private List<OfflinePlayer> boogeys;
    private final LifeSeries p;
    private final ScoreHandler scoreHandler;
    public Timed(LifeSeries p, int callIntervalTicks) {
        timerDecrementQuantity = callIntervalTicks / 20; // number of seconds to decremnt by (expect this is to be n*20)
        this.p = p;
        this.scoreHandler = p.getScoreboardHandler();
    }


    /// tick the game.
    @Override
    public void accept(Runnable runnable) {
        // decrement def
        scoreHandler.decrementUntrackedPlayerScore(timerDecrementQuantity);
        scoreHandler.getPlayerScores().replaceAll((k,v) -> Math.max(v - timerDecrementQuantity, 0));
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
}