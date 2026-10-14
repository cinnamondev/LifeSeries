package com.github.cinnamondev.lifeSeries;

import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;

import java.util.EnumSet;

public class Snail {
    // 3 modes
    // 1. chase (normal)
    // 2. burrow (plays animation, player too far.)
    // 3. jump (player within reach
    // IF Player killed, should burrow afterward and reappear N ticks later near the respawned player.
    public static class ChasePlayerGoal implements Goal<Silverfish> {
        private final Player trackingPlayer;
        private final Mob mob;
        private int unreachableTicksTime = 0;
        int radius = 20;
        public ChasePlayerGoal(Player trackingPlayer, Mob mob) {
            this.trackingPlayer = trackingPlayer;
            this.mob = mob;
        }

        @Override
        public boolean shouldActivate() {
            return false;
        }

        @Override
        public void start() {
            Goal.super.start();
        }

        @Override
        public void stop() {
            Goal.super.stop();
        }

        @Override
        public void tick() {
            mob.setTarget(trackingPlayer);
            mob.getPathfinder().moveTo(trackingPlayer);
            if (mob.getPathfinder().getCurrentPath() == null ||
                    !mob.getPathfinder().getCurrentPath().canReachFinalPoint()) { unreachableTicksTime+=1; }

            if (unreachableTicksTime >= 100) {
                ChunkSnapshot chunk = trackingPlayer.getChunk().getChunkSnapshot();
                boolean noLocationFound = true;
                for (int x= -radius; x <= radius; x++) {
                    for (int z= -radius; z <= radius; z++) {
                        for (int y = -radius; y <= radius; y++) {
                            if (x == trackingPlayer.getLocation().x() || z == trackingPlayer.getLocation().z()) {
                                continue;
                            }
                            BlockData blockData = chunk.getBlockData(x,y-1,z);
                            if (!blockData.getMaterial().equals(Material.AIR)) {
                                // we found the floor! try path finding
                                mob.teleport(new Location(trackingPlayer.getWorld(), x, y, z));
                                Pathfinder.PathResult result = mob.getPathfinder().findPath(trackingPlayer);
                                if (result != null && result.canReachFinalPoint()) {
                                    noLocationFound = false;
                                    mob.getPathfinder().moveTo(trackingPlayer);
                                }
                            }
                        }
                    }
                }
                if (noLocationFound) { // we have no choice but to teleport them to the player
                    mob.teleport(trackingPlayer);
                    mob.getPathfinder().moveTo(trackingPlayer);
                }
                unreachableTicksTime = 0;
            }
        }

        @Override
        public GoalKey<Silverfish> getKey() {
            return null;
        }

        @Override
        public EnumSet<GoalType> getTypes() {
            return null;
        }
    }
}
