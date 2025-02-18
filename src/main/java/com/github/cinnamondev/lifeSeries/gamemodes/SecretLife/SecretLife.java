package com.github.cinnamondev.lifeSeries.gamemodes.SecretLife;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.Game;
import com.github.cinnamondev.lifeSeries.gamemodes.Lives;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Task.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretLife.Task.PlayerTask;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class SecretLife extends Lives implements SecretTasks {
    HashMap<UUID, PlayerTask> tasks = new HashMap<>();

    public SecretLife(LifeSeries p) {
        super(p);
    }
    @Override
    public AbstractPlayerTask.Builder getRandomSecretTask() {
        return null;
    }

    @Override
    public AbstractPlayerTask.Builder getRandomSecretTask(TaskDifficulty difficulty) {
        return null;
    }

    @Override
    public void addSecretTask(PlayerTask secretTask) {
        removeSecretTask(secretTask.getTaskOwner()); // ensure everythings cleaned up.
        Bukkit.getPluginManager().registerEvents(secretTask, p);
        tasks.put(secretTask.getTaskOwner().getUniqueId(), secretTask);
    }

    public void onTaskCompletion(PlayerTask secretTask) {
        removeSecretTask(secretTask.getTaskOwner());
    }

    @Override
    public void onTaskSuccess(PlayerTask secretTask) {
        secretTask.getTaskOwner().sendMessage(Component.text("success"));

    }

    @Override
    public void onTaskFailure(PlayerTask secretTask) {
        secretTask.getTaskOwner().sendMessage(Component.text("fail"));
    }

    @Override
    public void removeSecretTask(OfflinePlayer taskOwner) {
        tasks.computeIfPresent(taskOwner.getUniqueId(), (_uuid, listener) -> {
            HandlerList.unregisterAll(listener);
            return null;
        });
    }

    @Override
    public Optional<PlayerTask> getSecretTask(OfflinePlayer taskOwner) {
        return Optional.ofNullable(tasks.get(taskOwner.getUniqueId()));
    }

    @Override
    public boolean teamCanGuessTasks(String teamString) {
        return p.getConfig().getStringList("options.secret-life.can-guess-tasks").stream()
                .anyMatch(str -> str.equalsIgnoreCase(teamString));
    }

    @Override
    public boolean playerCanGuessTasks(OfflinePlayer guesser) {
        return teamCanGuessTasks(p.getScoreHandler().getTeam(guesser).getScoreboardTeam().toString());
    }
}
