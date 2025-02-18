package com.github.cinnamondev.lifeSeries.gamemodes.SecretLife;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.CatWatcher;
import org.bukkit.DyeColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.jar.Attributes;

public class CatDisguiser {
    public static CatWatcher catDisguise(Player player, Cat.Type catType, DyeColor dyeColor) {
        //AttributeInstance scale = player.getAttribute(Attribute.SCALE);
        //if (scale == null) { return null; }
        //scale.setBaseValue(0.5);

        MobDisguise disguise = new MobDisguise(DisguiseType.CAT, true);
        disguise.setSelfDisguiseVisible(false);
        disguise.setEntity(player);
        disguise.setReplaceSounds(true);
        disguise.setScalePlayerToDisguise(true);

        CatWatcher watcher = (CatWatcher) disguise.getWatcher();
        watcher.setTamed(true);
        watcher.setCollarColor(dyeColor);
        watcher.setType(catType);

        disguise.startDisguise();
        return watcher;
    }
}
