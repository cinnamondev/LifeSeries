package com.github.cinnamondev.lifeSeries.gamemodes;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryBuilderFactory;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.event.RegistryComposeEvent;
import io.papermc.paper.registry.set.RegistryKeySet;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Game extends Runnable, CommandContainer {
    static TypedKey<Dialog> PRIMARY_DIALOG_KEY = TypedKey.create(RegistryKey.DIALOG, Key.key("subot:game_dialog"));
    default void onGameStart() {}
    default void onGameStop() {}
    void restoreStateFromSave();
    void clearSaveData();
    default void onServerDisable() {}
    default boolean onKilled(LifeSeries p, Player killed, int punishment) {
        AtomicBoolean isFinalDeath = new AtomicBoolean(false);
        p.getScoreHandler().updatePlayerScoreAndTeam(killed, (uuid,score) -> score - punishment, (player, newTeam) -> {
            isFinalDeath.set(p.getScoreHandler().isPlayerSpectator(player)); // only on team change.
        });
        return isFinalDeath.get();
    }
    default void rewardKiller(LifeSeries p, Player killer, int reward) {
        p.getScoreHandler().updatePlayerScoreAndTeam(killer, (uuid,score) -> score + reward);
    }
    default boolean onKilled(LifeSeries p, Player killed) {
        return onKilled(p, killed, p.getConfig().getInt("options.punishment.death"));
    }
    default boolean onKilled(LifeSeries p, Player killed, int punishment, Player killer, int reward) {
        rewardKiller(p, killer, reward);
        return onKilled(p, killed, punishment);
    }
    default boolean onKilled(LifeSeries p, Player killed, Player killer) {
        TeamMeta killerTeam = p.getScoreHandler().getTeam(killer);
        TeamMeta killedTeam = p.getScoreHandler().getTeam(killed);

        if (!killerTeam.equals(killedTeam)) {
            return onKilled(p,
                    killed, p.getConfig().getInt("options.punishment.death"),
                    killer, p.getConfig().getInt("options.rewards.kill")
            );
        } else {
            if (!p.getConfig().getBoolean("options.rewards.disable-on-disallowed-team")) {
                rewardKiller(p, killed, p.getConfig().getInt("options.rewards.kill"));
            }
            if (!p.getConfig().getBoolean("options.punishment.disable-on-disallowed-team")) {
                return onKilled(p, killed);
            }
        }
        return false;
    }


    default Dialog getDialog() {
        Dialog d = RegistryAccess.registryAccess().getRegistry(RegistryKey.DIALOG).get(dialogKey());
        if (d == null) {
            throw new IllegalStateException("shouldnt have no dialog here...");
        }
        return d;
    }
    default Key dialogKey() { return Key.key("subot:game_dialog"); }
    /// Called in BOOTSTRAP context! :)
    default DialogRegistryEntry.Builder buildMainDialog(RegistryComposeEvent<Dialog, ? extends DialogRegistryEntry.Builder> b) {
        return b.empty().type(DialogType.notice())
                    .base(DialogBase.builder(Component.text("Game settings"))
                            .body(Collections.singletonList(DialogBody.plainMessage(
                                    Component.text("There are no settings available here (yet...)")
                            ))).build()
                    );
    }
    default List<Function<RegistryComposeEvent<Dialog, ? extends DialogRegistryEntry.Builder>, DialogRegistryEntry.Builder>> secondaryDialogs() { return Collections.emptyList(); }
}