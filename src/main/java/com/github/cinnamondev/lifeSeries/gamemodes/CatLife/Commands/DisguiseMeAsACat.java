package com.github.cinnamondev.lifeSeries.gamemodes.CatLife.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.github.cinnamondev.lifeSeries.gamemodes.CatLife.CatLife;
import com.github.cinnamondev.lifeSeries.util.UtilityComponents;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class DisguiseMeAsACat implements CommandContainer.FilledLiteralCommand {
    private final LifeSeries p;
    private final CatLife game;
    public DisguiseMeAsACat(LifeSeries p, CatLife game) {
        this.p = p;
        this.game = game;
    }

    @Override
    public List<String> getAliases() {
        return List.of("catguise", "nyyananananacatman", "customizeLook");
    }

    @Override
    public String getDescription() {
        return "change yourself into the most fashionable cat this side of the craft";
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return disguiserCommandTree(
                Commands.literal("catsumize")
                        .requires(src -> src.getSender() instanceof Player)
                        .requires(src -> src.getSender().hasPermission("life.player.cat-customize")),
                p,
                game,
                false
        ).build();
    }

    public static LiteralArgumentBuilder<CommandSourceStack> disguiserCommandTree(LiteralArgumentBuilder<CommandSourceStack> root, LifeSeries p, CatLife game, boolean playersArgPresent) {
        return root
                .then(Commands.argument("type", ArgumentTypes.resource(RegistryKey.CAT_VARIANT))
                        .then(Commands.argument("collar", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (DyeColor color : DyeColor.values()) {
                                        builder.suggest(color.toString());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    var optDye = Arrays.stream(DyeColor.values())
                                            .filter(dye -> ctx.getArgument("collar", String.class).trim()
                                                    .equalsIgnoreCase(dye.toString()))
                                            .findFirst();
                                    if (optDye.isPresent()) {
                                        if (playersArgPresent) {
                                            ctx.getArgument("players", PlayerSelectorArgumentResolver.class)
                                                    .resolve(ctx.getSource()).forEach(player -> game.addPlayerDisguise(player,
                                                            CatLife.catDisguise(
                                                            player,
                                                            ctx.getArgument("type", Cat.Type.class),
                                                            optDye.get()
                                                    )));
                                        } else if (ctx.getSource().getSender() instanceof Player player) {
                                            game.addPlayerDisguise(player, CatLife.catDisguise(
                                                    player,
                                                    ctx.getArgument("type", Cat.Type.class),
                                                    optDye.get()
                                            ));
                                        }

                                    } else {
                                        ctx.getSource().getSender().sendMessage(UtilityComponents.dyeList());
                                    }
                                    return 1;
                                })
                        )
                )
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage(UtilityComponents.dyeList());
                    return 0;
                });
    }

    public static Component disguiseTutorial() {
        return Component.translatable("cat-life.uncustomized-cat-prompt");
    }
}
