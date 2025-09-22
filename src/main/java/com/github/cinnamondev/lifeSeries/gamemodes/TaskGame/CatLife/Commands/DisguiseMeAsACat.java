package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.CatLife.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.CatLife.CatLife;
import com.github.cinnamondev.lifeSeries.util.UtilityComponents;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
    private final CatLife game;
    public DisguiseMeAsACat(CatLife game) {
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
                game,
                false
        ).build();
    }

    public static LiteralArgumentBuilder<CommandSourceStack> disguiserCommandTree(LiteralArgumentBuilder<CommandSourceStack> root, CatLife game, boolean playersArgPresent) {
        return root
                .then(Commands.argument("type", ArgumentTypes.resource(RegistryKey.CAT_VARIANT))
                        .then(Commands.argument("collar", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (DyeColor color : DyeColor.values()) {
                                        builder.suggest(color.toString());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> commandExecutor(ctx, game, playersArgPresent))
                        )
                )
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage(UtilityComponents.dyeList());
                    return 0;
                });
    }

    private static int commandExecutor(CommandContext<CommandSourceStack> ctx, CatLife game, boolean playersArgPresent) throws CommandSyntaxException {
        Cat.Type type = ctx.getArgument("type", Cat.Type.class);
        DyeColor dye;
        try {
            dye = DyeColor.valueOf(StringArgumentType.getString(ctx, "collar").trim().toUpperCase());
        } catch (IllegalArgumentException e) { // send dye list instead...
            ctx.getSource().getSender().sendMessage(UtilityComponents.dyeList());
            return 1;
        }
        CatLife.CatDisguise catDisguise = new CatLife.CatDisguise(type, dye);

        if (playersArgPresent) { // this part of the tree is reused elsewhere... so double check!
            ctx.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource())
                    .forEach(player -> game.applyCatDisguise(player, catDisguise, true));
        } else if (ctx.getSource().getSender() instanceof Player player) {
            game.applyCatDisguise(player, catDisguise, true);
        }
        return 1;
    }

    public static Component disguiseTutorial() {
        return Component.translatable("cat-life.uncustomized-cat-prompt");
    }
}
