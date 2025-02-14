package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

public abstract class AbstractPlayerTask implements PlayerTask {
    protected final LifeSeries p;
    protected final Player owningPlayer;
    protected TaskStatus status = TaskStatus.IN_PROGRESS;

    public AbstractPlayerTask(LifeSeries p, Player owningPlayer) {
        this.p = p;
        this.owningPlayer = owningPlayer;
    }

    @Override
    public void complete() {
        this.status = TaskStatus.COMPLETE;
    }

    @Override
    public void fail() {
        this.status = TaskStatus.FAILED;
    }

    @Override
    public TaskStatus getTaskProgress() {
        return this.status;
    }

    @Override
    public Player getTaskOwner() {
        return this.owningPlayer;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> ass
}
