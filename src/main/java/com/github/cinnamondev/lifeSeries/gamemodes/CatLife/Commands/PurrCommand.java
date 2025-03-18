package com.github.cinnamondev.lifeSeries.gamemodes.CatLife.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

// because of course there is
public class PurrCommand implements CommandContainer.FilledLiteralCommand {
    private final LifeSeries p;

    public PurrCommand(LifeSeries p) {
        this.p = p;
    }

    @Override
    public List<String> getAliases() {
        return List.of("pspsps");
    }

    @Override
    public String getDescription() {
        return "meow meow mrrp mrrp :3";
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("purr")
                .requires(src -> src.getSender() instanceof Player)
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    Key sound = (Math.random()*2) <= 1.5 ? Key.key("minecraft:entity.cat.purr")
                            : Key.key("minecraft:entity.cat.purreow"); // sometimes change the noise. idk!
                    p.getServer().playSound(Sound.sound(sound, Sound.Source.VOICE, 1,1), player);
                    return 1;
                })
                .build();
    }
}