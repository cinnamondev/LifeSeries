package com.github.cinnamondev.lifeSeries.gamemodes.BoogeymanFeature;

import com.github.cinnamondev.lifeSeries.LifeSeries;
import com.github.cinnamondev.lifeSeries.gamemodes.CommandContainer;
import com.github.cinnamondev.lifeSeries.teams.TeamMeta;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;

import java.util.*;
import java.util.stream.Collectors;

public interface Boogeyman extends CommandContainer {
    public ArrayList<UUID> getBoogeyList();

    default boolean rollBoogeyman(LifeSeries p, int min, int max) {
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

    default boolean isBoogeyman(UUID uuid) { return getBoogeyList().contains(uuid); }
    default boolean isBoogeyman(OfflinePlayer offlinePlayer) { return isBoogeyman(offlinePlayer.getUniqueId()); }
    default void cureBoogeyman(UUID uuid) { getBoogeyList().remove(uuid); }
    /// remove player from list apply reward
    default void cureBoogeyman(OfflinePlayer offlinePlayer) { cureBoogeyman(offlinePlayer.getUniqueId()); }
    default void addBoogeyman(UUID uuid) { getBoogeyList().add(uuid); }
    default void addBoogeyman(OfflinePlayer offlinePlayer) { addBoogeyman(offlinePlayer.getUniqueId()); }
    default void punishBoogeymen(LifeSeries p) {
        for (UUID uuid : getBoogeyList()) {
            TeamMeta playerTeam = p.getScoreHandler().getTeam(uuid);
            if (p.getScoreHandler().getRankedTeams().getLast().equals(playerTeam)
                    && p.getConfig().getBoolean("options.boogeyman.failure.punish-lowest-team", false)) {
                continue; // do not punish this player
            }
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
    default Collection<FilledLiteralCommand> gameCommands(LifeSeries p) {
        return List.of(new AmITheBoogeyMan(this, p));
    }
    @Override
    default Collection<LiteralArgumentBuilder<CommandSourceStack>> adminSubCommands(LifeSeries p) {
        return Collections.singletonList(BoogeymanSubCommand.boogeyman(p, this));
    }
}
