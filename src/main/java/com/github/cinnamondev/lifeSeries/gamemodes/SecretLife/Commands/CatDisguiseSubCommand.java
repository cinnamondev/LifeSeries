package com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.CatDisguiser;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.CatLife;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Task.PlayerTask;
import com.github.cinnamondev.lifeSeries.util.UtilityComponents;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.registry.RegistryKey;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.CatWatcher;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class CatDisguiseSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command(LifeSeries p, CatLife game) {
        return Commands.literal("meowmeowmeowmeow").then(Commands.argument("players", ArgumentTypes.players())
                .then(Commands.literal("enable")
                        .executes(ctx -> {
                            // attempt to pull players disguise from configuration, failing that set them to an
                            // orange tabby or something.
                            ctx.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource())
                                    .forEach(game::tryDisguiseFromConfigOrUseTabby);
                            return 1;
                        })
                )
                .then(Commands.literal("disable")
                        .executes(ctx -> {
                            ctx.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource())
                                    .forEach(game::removePlayerDisguise);
                            return 1;
                        })
                )
                .then(Commands.literal("customize").then(Commands.argument("type", ArgumentTypes.resource(RegistryKey.CAT_VARIANT))
                        .then(Commands.argument("collar", StringArgumentType.word())
                                .executes(ctx -> {
                                    var optDye = Arrays.stream(DyeColor.values())
                                            .filter(dye -> ctx.getArgument("collar", String.class).trim()
                                                    .equalsIgnoreCase(dye.toString()))
                                            .findFirst();
                                    if (optDye.isPresent()) {
                                        ctx.getArgument("players", PlayerSelectorArgumentResolver.class)
                                                .resolve(ctx.getSource()).forEach(player -> {
                                                    game.addPlayerDisguise(player, CatDisguiser.catDisguise(
                                                            player,
                                                            ctx.getArgument("type", Cat.Type.class),
                                                            optDye.get()
                                                    ));
                                                });
                                    } else {
                                        ctx.getSource().getSender().sendMessage(UtilityComponents.dyeList());
                                    }
                                    return 1;
                                })
                        )
                        .executes(ctx -> {
                            ctx.getSource().getSender().sendMessage(UtilityComponents.dyeList());
                            return 0;
                        })
                )));
    }


    public static void sendDyeColourList(Audience audience) {
        Component dyeList = Arrays.stream(DyeColor.values()).map(DyeColor::toString)
                .map(Component::text)
                .reduce(Component.text(""), (acc, text) ->
                        (net.kyori.adventure.text.TextComponent) acc.append(text).appendNewline()
                );

        audience.sendMessage(
                Component.text("This command requires a dye color. These are the following options:")
                        .appendNewline()
                        .append(dyeList)
        );
    }
}
