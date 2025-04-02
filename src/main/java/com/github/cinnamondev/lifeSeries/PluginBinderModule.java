package com.github.cinnamondev.lifeSeries;

import com.github.cinnamondev.lifeSeries.gamemodes.Game;
import com.github.cinnamondev.lifeSeries.teams.ScoreHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.bukkit.plugin.Plugin;

public class PluginBinderModule extends AbstractModule {
    private final LifeSeries lifeSeries;

    public PluginBinderModule(LifeSeries lifeSeries) {
        this.lifeSeries = lifeSeries;
    }

    public Injector createInjector() {
        return Guice.createInjector(this);
    }

    @Override
    protected void configure() {
        this.bind(LifeSeries.class).toInstance(this.lifeSeries);
        this.bind(Plugin.class).toInstance(this.lifeSeries);
        this.bind(Game.class).toInstance(this.lifeSeries.getGame());
        this.bind(ScoreHandler.class).toInstance(this.lifeSeries.getScoreHandler());
    }
}
