package com.github.cinnamondev.lifeSeries.gamemodes;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface CommandContainer {
    default Collection<FilledLiteralCommand> gameCommands(LifeSeries p) { return Collections.emptyList(); }
    default  Collection<LiteralArgumentBuilder<CommandSourceStack>> adminSubCommands(LifeSeries p) { return Collections.emptyList(); }

    interface FilledLiteralCommand {
        List<String> getAliases();
        String getDescription();
        LiteralCommandNode<CommandSourceStack> command();
    }
}
