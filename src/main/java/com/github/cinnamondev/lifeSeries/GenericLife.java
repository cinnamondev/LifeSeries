package com.github.cinnamondev.lifeSeries;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public abstract class GenericLife {
    protected Plugin p;
    private File file;
    private final YamlConfiguration saveGame;

    private boolean timerPaused = false;
    private ArrayList<String> deaths = new ArrayList<String>();


    protected GenericLife(Plugin p) {
        this.file = new File(p.getDataFolder(), "save.yml");
        this.saveGame = YamlConfiguration.loadConfiguration(this.file);
    }

    public void punishPlayer(Player p, int punishmentQuantity) {

    }


    private final void timedEvent() {
        if (timerPaused) { return; }

        onTimer();
    }

    protected abstract void onTimer();

    private void gameCountDown() {
    }
    public void start() {

    }

    public void stop() {
        // generate summary
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd--HH-mm");
        df.setTimeZone(TimeZone.getDefault());
        String dateTime = df.format(new Date());
        File f = new File(p.getDataFolder(), "summary-" + dateTime +".txt");
    }

    public void pause() {

    }

    public void reset() {

    }




}
