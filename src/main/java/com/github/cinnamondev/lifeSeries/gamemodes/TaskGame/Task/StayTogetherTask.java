package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

public class StayTogetherTask extends AbstractTargetedWatchdogTask implements Listener, SessionLongTask, RequiresOnlineTarget {

    private final double maxDistance = 10;
    private final int timeoutMinutes = 10;
    private int strikes = 0;
    public StayTogetherTask(LifeSeries p, Player owningPlayer, int watchdogInterval, OfflinePlayer target, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayer, watchdogInterval, target, difficulty, onTaskCompletion);
    }

    @Override
    public void bark() {
        strikes += 1;
        if (strikes > 5) {
            fail();
        } else {
            owningPlayer.sendMessage(
                    Component.translatable("secret-life.tasks.follow-another-player.reminder", targetedPlayer.getName())
                            .style(Style.style(NamedTextColor.RED, TextDecoration.BOLD))
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTaskPlayerMove(PlayerMoveEvent e) {
        if (!e.getPlayer().equals(owningPlayer) || targetedPlayer == null) { return; }

        Player onlineTarget = targetedPlayer.getPlayer();
        if (onlineTarget == null) { feed(); return; }

        Location playerLocation = owningPlayer.getLocation();
        World playerWorld = owningPlayer.getWorld();
        Location targetLocation = onlineTarget.getLocation();
        World targetWorld = onlineTarget.getWorld();

        // if player is out of range of other player
        if (!playerWorld.equals(targetWorld)) { return; }
        if (playerLocation.distanceSquared(targetLocation) <= maxDistance) {
            feed();
        }
    }

    @Override
    public boolean isTaskGuessable() {
        return true;
    }

    @Override
    public String descriptionServerTranslate(Locale locale) {
        Object[] args = { maxDistance, targetedPlayer.getName(), timeoutMinutes, strikes };
        return GlobalTranslator.translator().translate("secret-life.tasks." + getTaskKey() + ".description", locale)
                .format(args);

    }

    @Override
    public TranslatableComponent description() {
        return Component.translatable("secret-life.tasks.follow-another-player.description")
                .arguments(
                        Component.text(maxDistance),
                        Component.text(Objects.requireNonNull(targetedPlayer.getName())),
                        Component.text(timeoutMinutes),
                        Component.text(strikes)
                );
    }

    @Override
    public Component taskProgressExplanation() {
        return super.taskProgressExplanation()
                .appendNewline()
                .append(RequiresOnlineTarget.super.taskProgressExplanation());
    }
    @Override
    public String getTaskKey() {
        return "follow-another-player";
    }

    @Override
    public StayTogetherTask.Builder builderProvider() {
        return new StayTogetherTask.Builder();
    }
    public static class Builder extends TargetedPlayerTask.Builder<Builder> {
        @Override
        public AbstractPlayerTask build(LifeSeries p) {
            return new StayTogetherTask(p, owningPlayer, 200, targetPlayer, onTaskCompletion, assignedDifficulty);
        }
    }



}
