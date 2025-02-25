package com.github.cinnamondev.lifeSeries.gamemodes.CatLife.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.github.cinnamondev.lifeSeries.gamemodes.CatLife.CatLife;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

public class CatSitCommand implements CommandContainer.FilledLiteralCommand {
    private final LifeSeries p;
    private final CatLife game;

    public CatSitCommand(LifeSeries p, CatLife catGame) {
        this.p = p;
        this.game = catGame;
    }

    @Override
    public List<String> getAliases() {
        return List.of("sit", "gsit", "catsit", "nyanyauwu");
    }

    @Override
    public String getDescription() {
        return "kbity sit!!!!";
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("csit")
                .requires(src -> src.getSender() instanceof Player)
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    game.getCatWatcher(player).ifPresentOrElse((watcher) -> {
                        watcher.setSitting(!watcher.isSitting()); // toggle
                    }, () -> player.sendMessage(Component.text(
                            "i was lazy and didnt want to implement all of gsit just to include kbity. soz."
                    )));
                    return 1;
                })
                .build();
    }
}
