package com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Commands;

import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.CatDisguiser;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.CatLife;
import com.github.cinnamondev.lifeSeries.util.UtilityComponents;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.registry.RegistryKey;
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
        return List.of("catguise", "nyyananananacatman");
    }

    @Override
    public String getDescription() {
        return "turn into a cat!!!!!!!!!!!!!!!!!!!!!!!!!!!";
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("catsumize")
                .requires(src -> src.getSender() instanceof Player)
                .requires(src -> src.getSender().hasPermission("lf.cat-life.customize"))
                .then(Commands.argument("type", ArgumentTypes.resource(RegistryKey.CAT_VARIANT))
                        .then(Commands.argument("collar", StringArgumentType.word())
                                .executes(ctx -> {
                                    Player player = (Player) ctx.getSource().getSender();
                                    Arrays.stream(DyeColor.values())
                                            .filter(dye -> ctx.getArgument("collar", String.class).equals(dye.toString()))
                                            .findFirst()
                                            .ifPresentOrElse((dye) -> {
                                                game.addPlayerDisguise(player, CatDisguiser.catDisguise(
                                                        player,
                                                        ctx.getArgument("type", Cat.Type.class),
                                                        dye
                                                ));
                                            }, () -> {
                                                ctx.getSource().getSender().sendMessage(UtilityComponents.dyeList());
                                            });
                                    return 1;
                                })
                        )
                        .executes(ctx -> {
                            ctx.getSource().getSender().sendMessage(UtilityComponents.dyeList());
                            return 0;
                        })
                )
                .build();
    }
}
