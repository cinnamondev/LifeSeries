package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import net.kyori.adventure.text.Component;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.concurrent.TimeUnit;

public class ExplosiveTrapTask extends AbstractPlayerTask implements Listener, SelfCompletableTask {

    public ExplosiveTrapTask(LifeSeries p, Player owningPlayer) {
        super(p, owningPlayer);
    }
    private int recentExplosionDeaths = 0;
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerBlewUp(PlayerDeathEvent e) {
        DamageSource source = e.getDamageSource();
        DamageType damageType = e.getDamageSource().getDamageType();
        if (!(damageType.equals(DamageType.EXPLOSION) || damageType.equals(DamageType.PLAYER_EXPLOSION))) { return; }
        recentExplosionDeaths += 1;
        p.getServer().getScheduler()
                .runTaskLater(p,
                        () -> recentExplosionDeaths = Math.max(recentExplosionDeaths-1,0),
                        TimeUnit.MINUTES.toSeconds(3) * 20);
    }


    @Override
    public boolean conditionalCompleteTask() {
        if (recentExplosionDeaths > 0) {
            complete();
        } else {
            getTaskOwner().sendMessage(Component.translatable("secret-life.conditional-task.didnt-complete"));
        }
    }

    @Override
    public boolean endOfSession() {
        if (status.equals(TaskStatus.IN_PROGRESS) || status.equals(TaskStatus.ODD_STATE)) { fail(); return false; }
        return true;
    }

    @Override
    public TaskStatus getTaskProgress() {
        return status;
    }

    @Override
    public boolean isTaskGuessable() {
        return true;
    }

    @Override
    public Component getTaskName() {
        return null;
    }

    @Override
    public Component getTaskDescription() {
        return null;
    }

    @Override
    public Player getTaskOwner() {
        return pla
    }
}
