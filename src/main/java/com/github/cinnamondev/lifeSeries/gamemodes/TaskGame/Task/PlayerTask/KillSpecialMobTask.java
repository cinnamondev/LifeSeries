package com.github.cinnamondev.lifeSeries.gamemodes.TaskGame.Task.PlayerTask;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class KillSpecialMobTask<T extends Damageable> extends AbstractPlayerTask implements Listener {
    private final Function<Location, T> mobSpawner;
    private Consumer<EntityDeathEvent> onDeath = e -> {};
    protected T mob = null;
    protected final NamespacedKey key;
    public KillSpecialMobTask(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty, Function<Location, T> mobSpawner) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
        this.key = new NamespacedKey(p, getTaskKey());
        this.mobSpawner = mobSpawner;
    }

    public void afterDeathAction(Consumer<EntityDeathEvent> onDeath) {
        this.onDeath = onDeath;
    }

    @Override
    public void cleanup() {
        if (mob != null) { mob.remove(); }
    }

    protected void spawnMob(Location location) {
        mob = mobSpawner.apply(location);
        mob.getPersistentDataContainer().set(key, PersistentDataType.STRING, owningPlayer.getUniqueId().toString());
    }

    protected T getMob() {
        return mob;
    }
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        if (doesMobBelongToTask(e.getEntity())) {
            boolean mustBeByOwnersHand = getConfigurationSection()
                    .map(c -> c.getBoolean("owner-must-kill", false))
                    .orElse(false);
            if (!mustBeByOwnersHand || owningPlayer.equals(entity.getKiller())) {
                onDeath.accept(e); // this is okay. if a key with the uuid matches here then we know its
                complete();                        // of type T
            } else {
                return;
            }
        }
    }

    protected boolean doesMobBelongToTask(Entity entity) {
        return owningPlayer.getUniqueId().toString()
                .equals(entity.getPersistentDataContainer().get(key, PersistentDataType.STRING));
    }
}
