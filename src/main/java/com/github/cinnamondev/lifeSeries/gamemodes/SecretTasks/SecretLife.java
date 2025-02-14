package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.Game;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class SecretLife implements Game {
    private final LifeSeries p;
    private HashMap<UUID, PlayerTask> assignedTasks = new HashMap<>();

    public SecretLife(LifeSeries p) {
        this.p = p;

        p.getConfig().
    }

    @Override
    public void run() {

    }

    @Override
    public void onGameStart(LifeSeries p) {
        Game.super.onGameStart(p);
    }

    @Override
    public void onGameStop(LifeSeries p) {
        Game.super.onGameStop(p);
        assignedTasks.forEach();
    }

    @Override
    public Collection<FilledLiteralCommand> gameCommands(LifeSeries p) {
        return Game.super.gameCommands(p);
    }

    @Override
    public Collection<LiteralArgumentBuilder<CommandSourceStack>> adminSubCommands(LifeSeries p) {
        return Game.super.adminSubCommands(p).add(
                Commands.literal("")
        );
    }
}
