package com.github.cinnamondev.lifeSeries.gamemodes;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

public interface BoogeymanGame extends Game {
    default public List<Player> roll(LifeSeries plugin, int min, int max, List<TeamMeta> allowedTeams) {
        Random random = new Random();
        ArrayList<Player> candidates = plugin.getServer().getOnlinePlayers().stream()
                .filter(p -> allowedTeams.contains(plugin.getScoreboardHandler().getTeam(p)))
                .collect(Collectors.toCollection(ArrayList::new));

        var numberCandidates = random.nextInt(max - min) + min;
        if (numberCandidates > candidates.size()) { numberCandidates = candidates.size(); }

        while (candidates.size() > numberCandidates) {
            var removeIndex = random.nextInt(candidates.size()-1);
            candidates.remove(removeIndex); // keep removing candidates until we have reached the specified amount
        }

        return candidates;
    }
    public List<OfflinePlayer> getBoogeys();
    default public boolean playerIsBoogeyman(OfflinePlayer offlinePlayer) {
        return playerIsBoogeyman(offlinePlayer.getUniqueId());
    }
    public boolean playerIsBoogeyman(UUID uuid);
    /// remove player from list apply reward
    public void cure(OfflinePlayer offlinePlayer);
    /// apply player punishment score remove.
    public void punish(OfflinePlayer offlinePlayer);

}
