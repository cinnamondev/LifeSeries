package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.CatLife.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.CatLife.CatLife;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

public class CatVisibilityCommand implements CommandContainer.FilledLiteralCommand {
    private final CatLife game;

    public CatVisibilityCommand(CatLife game) {
        this.game = game;
    }

    @Override
    public List<String> getAliases() {
        return List.of("catvisibility");
    }

    @Override
    public String getDescription() {
        return "toggle visibility of your own cat.";
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("meowermeoweronthewall")
                .requires(src -> src.getSender().hasPermission("life.player.cat-customize"))
                .requires(src -> src.getSender() instanceof Player)
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    game.getCatWatcher(player).ifPresentOrElse(cat -> {
                        boolean selfDisguiseVisible = cat.getDisguise().isSelfDisguiseVisible();
                        if (!selfDisguiseVisible) {
                            player.sendMessage(Component.translatable("cat-life.disguise-visibility"));
                        }
                        cat.getDisguise().setSelfDisguiseVisible(!selfDisguiseVisible);

                    }, () -> player.sendMessage(Component.translatable("cat-life.not-disguised")));
                    return 1;
                })
                .build();
    }
}
