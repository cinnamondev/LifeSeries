package com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Commands;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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

public class MeowCommand implements CommandContainer.FilledLiteralCommand {
    private final LifeSeries p;

    public MeowCommand(LifeSeries p) {
        this.p = p;
    }
    public MeowCommand(LifeSeries p, Collection<Consumer<Player>> listeners) {
        this.p = p;
        this.meowListeners.addAll(listeners);
    }
    public MeowCommand(LifeSeries p, Consumer<Player>... listeners) {
        this(p, Arrays.asList(listeners));
    }

    private final ArrayList<Consumer<Player>> meowListeners = new ArrayList<>();
    public void addMeowListener(Consumer<Player> listener) { this.meowListeners.add(listener); }
    public void removeMeowListener(Consumer<Player> listener) { this.meowListeners.remove(listener); }

    @Override
    public List<String> getAliases() {
        return List.of("westerly");
    }

    @Override
    public String getDescription() {
        return "meow meow mrrp mrrp :3";
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("meow")
                .requires(src -> src.getSender() instanceof Player)
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    p.getServer().playSound(
                            Sound.sound(Key.key("minecraft:entity.cat.ambient"), Sound.Source.VOICE, 1,1),
                            player
                    );
                    return 1;
                })
                .build();
    }
}
