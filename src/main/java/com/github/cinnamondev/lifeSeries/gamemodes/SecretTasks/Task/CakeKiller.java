package com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.AbstractPlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.PlayerTask;
import com.github.cinnamondev.lifeSeries.gamemodes.SecretTasks.Task.PlayerTask.TaskDifficulty;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.BlockDisplayWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.CatWatcher;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;

import java.util.function.Consumer;

public class CakeKiller extends AbstractPlayerTask {
    public CakeKiller(LifeSeries p, Player owningPlayer, Consumer<PlayerTask> onTaskCompletion, TaskDifficulty difficulty) {
        super(p, owningPlayer, onTaskCompletion, difficulty);
    }

    // TODO: Find and KILL a cake
    // make a cake then inceneate it
    // place a cake and we make it into a ocelot (untameable) with armor stand holding cake? then you have to kill the cake or something
    // maybe??
    private void spawnCake(Location loc) {
        Ocelot ocelot = (Ocelot) loc.getWorld().spawnEntity(loc, EntityType.OCELOT);

        ocelot.setTrusting(false);
        ocelot.setBreed(false);
        ocelot.setLoveModeTicks(0);
        ocelot.getPersistentDataContainer().set();
        MiscDisguise disguise = new MiscDisguise(DisguiseType.BLOCK_DISPLAY);
        disguise.setEntity(ocelot);
        disguise.setReplaceSounds(true);

        BlockDisplayWatcher watcher = (BlockDisplayWatcher) disguise.getWatcher();
        watcher.setBlock(Material.CAKE);
        watcher.setGridLocked(false);
        disguise.setWatcher(watcher);

        disguise.startDisguise();

    }

    @Override
    public boolean isTaskGuessable() {
        return false;
    }

    @Override
    public String getTaskKey() {
        return "";
    }

    @Override
    public Builder<? extends Builder<?>> builderProvider() {
        return null;
    }
}
