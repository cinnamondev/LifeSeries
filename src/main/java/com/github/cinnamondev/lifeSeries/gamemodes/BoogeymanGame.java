package com.github.cinnamondev.lifeSeries.gamemodes;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public abstract class BoogeymanGame implements Game {
    private final LifeSeries p;

    private List<UUID> boogeymen = new ArrayList<>();
    public BoogeymanGame(LifeSeries p) {
        this.p = p;
    }
    public Collection<UUID> getBoogeymen() { return boogeymen; }
    /// rolled using specified values.
    public boolean roll(int min, int max) {
        Random random = new Random();

        // filter down what teams we can select from.
        ArrayList<String> candidateTeams = p.getConfig().getStringList("options.boogeyman.allowed-teams")
                .stream().filter(name -> p.getScoreHandler().tryForTeam(name).isPresent())
                .collect(Collectors.toCollection(ArrayList::new));
        if (candidateTeams.isEmpty()) { p.getLogger().info("no configured teams to select from"); return false; }
        // select players from those teams
        boogeymen = p.getServer().getOnlinePlayers().stream()
                .filter(player ->
                        candidateTeams.contains(p.getScoreHandler().getTeam(player).getScoreboardTeam().getName())
                ).map(Entity::getUniqueId)
                .collect(Collectors.toCollection(ArrayList::new));
        if (min > boogeymen.size()) {
            boogeymen = new ArrayList<>(); // empty again
            p.getLogger().info("couldnt find enough candidates!");
            return false;
        }

        // bound min >= n >= max, where max will be bound by the number of available candidates.
        var numberCandidates = Math.max(random.nextInt(max - min) + min, boogeymen.size());
        // whittle down candidates until we have
        while (boogeymen.size() > numberCandidates) {
            var removeIndex = random.nextInt(boogeymen.size()-1);
            boogeymen.remove(removeIndex); // keep removing candidates until we have reached the specified amount
        }

        for (UUID uuid : boogeymen) {
            p.getSave().set("players." + uuid.toString() + ".is-boogey", "yes");
        }


        return true;
    }

    public boolean isBoogeyman(OfflinePlayer offlinePlayer) {
        return isBoogeyman(offlinePlayer.getUniqueId());
    }
    public boolean isBoogeyman(UUID uuid) {
        return boogeymen.contains(uuid);
    }
    /// remove player from list - does NOT apply reward.
    public void cure(UUID uuid) {
        p.getSave().set("players." + uuid.toString() + ".is-boogey", "cured");
        boogeymen.remove(uuid);
    }
    public void addBoogey(UUID uuid) {
        p.getSave().set("players." + uuid.toString() + ".is-boogey", "yes");
        boogeymen.add(uuid);}
    public void addBoogey(OfflinePlayer offlinePlayer) { addBoogey(offlinePlayer.getUniqueId());}
    /// remove player from list apply reward
    public void cure(OfflinePlayer offlinePlayer) {
        cure(offlinePlayer.getUniqueId());
    }
    @Override
    public boolean onKilled(LifeSeries p, Player killed, Player killer) {
        if (isBoogeyman(killer)) {
            cure(killer);
            killer.sendMessage("You have been cured!");
            return onKilled(p,
                    killed, p.getConfig().getInt("options.punishment.boogey-death"),
                    killer, p.getConfig().getInt("options.reward.boogey-kill")
            );
        } else {
            return Game.super.onKilled(p, killed, killer); // let default implementation handle it
        }
    }

}
