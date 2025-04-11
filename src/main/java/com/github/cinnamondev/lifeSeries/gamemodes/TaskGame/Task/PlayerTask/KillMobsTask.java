package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class KillMobsTask extends AbstractPlayerTask implements Listener, GenericThreshold, SessionLongTask {
    protected int hits = 0;
    private Component explanation = Component.text("None killed yet.");
    public KillMobsTask(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
    }

    protected abstract Collection<EntityType> candidateEntitys();

    @EventHandler
    public void onMobDeath(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        Player killer = entity.getKiller();
        if (!Objects.equals(killer, owningPlayer)) { return; }
        EntityType type = entity.getType();
        if (candidateEntitys().contains(type)) {
            explanation = Component.text(hits + "/" + getThreshold() + " killed");
            hits+=1;
            if (hits == getThreshold()) { complete(); }
        }
    }

    @Override
    public int fallbackThreshold() {
        return 5;
    }

    @Override
    public boolean isTaskGuessable() {
        return true;
    }

    @Override
    public Component taskProgressExplanation() {
        return explanation;
    }
}
