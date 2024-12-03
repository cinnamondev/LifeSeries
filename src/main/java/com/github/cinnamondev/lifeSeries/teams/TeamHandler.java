package com.github.cinnamondev.lifeSeries.teams;

import com.github.cinnamondev.lifeSeries.util.ColourConverter;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.module.Configuration;
import java.util.*;
import java.util.stream.Collectors;

public class TeamHandler {
   private final Plugin p;
   private final TeamMeta spectatorTeam;
   private final Scoreboard scoreboard;
   private HashMap<String, TeamMeta> metaLookup = new HashMap<>();
   private ArrayList<TeamMeta> rankedMeta;

   public TeamHandler(Plugin p) {
      this.p = p;
      this.scoreboard = p.getServer().getScoreboardManager().getMainScoreboard();
      spectatorTeam = new TeamMeta(
              registerTeamOverriding(scoreboard, "Dead"),
              "Dead",
              NamedTextColor.GRAY,
              -1,
              Collections.emptyList(),
              GameMode.SPECTATOR);

      // load teams from configuration
      ConfigurationSection categories = p.getConfig().getConfigurationSection("categories");
      metaLookup = categories.getKeys(false).stream()
              .map(name -> {
                 var team = categories.getConfigurationSection(name);
                 // if getstring yields `null`, then something really weird is going on anyway.
                 NamedTextColor colour = ColourConverter.tryNamedColourFromString(team.getString("colour"))
                         .orElseGet(() -> {
                            p.getLogger().warning("Couldn't interpret colour string for server " + name);
                            return NamedTextColor.WHITE;
                         });
                 return new TeamMeta(
                         registerTeamOverriding(scoreboard, name),
                         name,
                         colour,
                         team.getInt("lives"),
                         team.getStringList("can-kill")
                 );
              })
              .sorted((lhs,rhs) -> Integer.compare(rhs.getMinimumTime(), lhs.getMinimumTime())) // reversed order.
              .reduce(new HashMap<String, TeamMeta>(), (map, element) -> {
                    map.put(element.getName(), element);
                    return map;
              }, (m,m2) -> {
                  m.putAll(m2);
                  return m;
              });

      rankedMeta = metaLookup.entrySet().stream()
              .sorted((lhs,rhs) -> Integer.compare(
                      rhs.getValue().getMinimumTime(),
                      lhs.getValue().getMinimumTime()
              )).map(Map.Entry::getValue)
              .collect(Collectors.toCollection(ArrayList::new));

      scoreboard.registerNewTeam("Dead");
   }

   private TeamMeta getTeamByPlayerScore(int playerScore) {
       for (TeamMeta meta : rankedMeta) {
           // rankedmeta is ranked high time to low time.
           // first time to exceed player time will be player category
           // time is kept non-exclusive so players on 0 score will become spectator.
           if (playerScore > meta.getMinimumTime()) { return meta; }
       }
       return spectatorTeam; // if a team isnt found then the player is considered dead.
   }

   private boolean doesKillReward(TeamMeta team, TeamMeta killerTeam) {
       return killerTeam.canKill(team.getName());
   }

   private static Team registerTeamOverriding(Scoreboard s, String name) {
      Team t = s.getTeam(name);
      if (t != null) {
         t.unregister();
      }
      t = s.registerNewTeam(name);
      return t;
   }
}
