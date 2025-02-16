package com.github.cinnamondev.lifeSeries.gamemodes.SecretLife;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Commands.MeowCommand;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class CatLife extends SecretLife {
    private final MeowCommand meowCommand;

    public CatLife(LifeSeries p) {
        super(p);
        this.meowCommand = new MeowCommand(p);
    }

    @Override
    public Collection<FilledLiteralCommand> gameCommands(LifeSeries p) {
        ArrayList<FilledLiteralCommand> commands = new ArrayList<>(super.gameCommands(p));
        commands.add(meowCommand);
        return commands;
    }

    @Override
    public Collection<LiteralArgumentBuilder<CommandSourceStack>> adminSubCommands(LifeSeries p) {
        var commands = new ArrayList<>(super.adminSubCommands(p));
        return commands;
    }

    private static void disguisePlayerAsCat(Player player) {

        // ARGGg!!!!!!!]

        // TODO: cat type randomizer or command to customize your cat.
    }
}
