package com.github.cinnamondev.lifeSeries.gamemodes.CatLife.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CatLife.CatLife;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;

import java.util.Arrays;

public class CatDisguiseSubCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> command(LifeSeries p, CatLife game) {
        return Commands.literal("meowmeowmeowmeow").requires(src -> src.getSender().hasPermission("life.admin.game")).then(Commands.argument("players", ArgumentTypes.players())
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
                .then(DisguiseMeAsACat.disguiserCommandTree(Commands.literal("customize"),p,game, true)));
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
