package com.github.cinnamondev.lifeSeries.gamemodes.Boogeyman;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.Game;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractBoogeyman implements Boogeyman, Game {
    protected final LifeSeries p;
    protected ArrayList<UUID> boogeymen = new ArrayList<>();

    public AbstractBoogeyman(LifeSeries p) {
        this.p = p;
    }

    @Override
    public void punishBoogeymen() {
        for (UUID uuid : getBoogeyList()) {
            TeamMeta playerTeam = p.getScoreHandler().getTeam(uuid);
            boolean playerTeamNotPunishable = p.getConfig()
                    .getStringList("options.boogeyman.failure.do-not-punish-teams")
                    .stream().map(str -> p.getScoreHandler().tryForTeam(str))
                    .<TeamMeta>mapMulti(Optional::ifPresent)
                    .anyMatch(playerTeam::equals);
            if (playerTeamNotPunishable || p.getScoreHandler().isPlayerSpectator(uuid)) { continue; }
            if (p.getConfig().getBoolean("options.boogeyman.demote-to-next-team", false)) {
                int teamMinScore = playerTeam.getMininumScore();
                p.getScoreHandler().updatePlayerScoreAndTeam(uuid, (_uuid, _score) -> teamMinScore);
            } else {
                int demotionScore = p.getConfig().getInt("options.boogeyman.failure.demotion-score", 0);
                p.getScoreHandler().updatePlayerScoreAndTeam(uuid, (_uuid, score) -> score - demotionScore);
            }
        }
    }

    @Override
    public boolean rollBoogeyman(int min, int max) {
        Random random = new Random();

        // filter down what teams we can select from.
        ArrayList<String> candidateTeams = p.getConfig().getStringList("options.boogeyman.allowed-teams")
                .stream().filter(name -> p.getScoreHandler().tryForTeam(name).isPresent())
                .collect(Collectors.toCollection(ArrayList::new));

        if (candidateTeams.isEmpty()) { p.getLogger().info("no configured teams to select from"); return false; }
        // select players from those teams
        getBoogeyList().clear();
        p.getServer().getOnlinePlayers().stream()
                .filter(player ->
                        candidateTeams.contains(p.getScoreHandler().getTeam(player).getScoreboardTeam().getName())
                ).map(Entity::getUniqueId)
                .forEach(getBoogeyList()::add);
        if (min > getBoogeyList().size()) {
            getBoogeyList().clear();
            p.getLogger().info("couldnt find enough candidates!");
            return false;
        }

        // bound min >= n >= max, where max will be bound by the number of available candidates.
        var numberCandidates = Math.min(random.nextInt(max - min + 1) + min, getBoogeyList().size());
        // whittle down candidates until we have
        while (getBoogeyList().size() > numberCandidates) {
            var removeIndex = random.nextInt(getBoogeyList().size()-1);
            getBoogeyList().remove(removeIndex); // keep removing candidates until we have reached the specified amount
        }

        for (UUID uuid : getBoogeyList()) {
            p.getSave().set("players." + uuid.toString() + ".is-boogey", "yes");
        }

        return true;
    }

    @Override
    public void restoreStateFromSave() {
        Objects.requireNonNull(p.getSave().getConfigurationSection("players")).getKeys(false).stream()
                .filter(uuid -> p.getSave().getBoolean("players." + uuid + ".is-boogey", false))
                .map(UUID::fromString)
                .forEach(boogeymen::add);
    }

    @Override
    public void clearSaveData() {
        Objects.requireNonNull(p.getSave().getConfigurationSection("players")).getKeys(false)
                .forEach(uuidStr -> p.getSave().set("players." + uuidStr + ".is-boogey", false));
    }

    @Override
    public ArrayList<UUID> getBoogeyList() {
        return boogeymen;
    }

    @Override
    public boolean onKilled(LifeSeries p, Player killed, Player killer) {
        if (isBoogeyman(killer)) {
            cureBoogeyman(killer);
            killer.sendMessage(Component.translatable("boogeyman.cured"));
            return onKilled(p,
                    killed, p.getConfig().getInt("options.punishment.boogey-death"),
                    killer, p.getConfig().getInt("options.rewards.boogey-kill")
            );
        } else {
            return Game.super.onKilled(p, killed, killer); // let default implementation handle it
        }
    }
}
