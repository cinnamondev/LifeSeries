package com.github.cinnamondev.lifeSeries.commands.SpecialArguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class GreedyPlayerSelector implements CustomArgumentType<List<Player>, String> {
    private static final DynamicCommandExceptionType ERROR_NOT_PLAYER = new DynamicCommandExceptionType(name ->
            MessageComponentSerializer.message().serialize(Component.text(name + " is not online or is not a player."))
    );

    @Override
    public List<Player> parse(StringReader reader) throws CommandSyntaxException {
        ArrayList<Player> players = new ArrayList<>();
        for (String s : reader.readString().split(" ")) {
            if (s.isEmpty()) { continue; }
            Player player = Bukkit.getPlayerExact(s);
            if (player == null) { throw ERROR_NOT_PLAYER.create(s); }
            players.add(player);
        }
        return Collections.unmodifiableList(players);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String latestArgument = StringUtils.substringAfterLast(builder.getRemainingLowerCase(), ' ')
                .toLowerCase();
        Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(latestArgument))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    @Override
    public ArgumentType<String> getNativeType() { return StringArgumentType.greedyString(); }
}
