package com.angelsmp.angel.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/** Safe-spot scanning used by the death/respawn and altar systems. */
public final class Coords {

    private Coords() {
    }

    /** Shifts the location upward until a clear 2-block-high pocket is found. */
    public static Location findSafeSpot(Location origin) {
        World world = origin.getWorld();
        if (world == null) {
            return origin;
        }
        double x = origin.getX();
        double z = origin.getZ();
        int y = Math.max(world.getMinHeight() + 1, origin.getBlockY());
        int maxY = world.getMaxHeight() - 3;
        while (y < maxY) {
            Block feet = world.getBlockAt((int) Math.floor(x), y, (int) Math.floor(z));
            Block head = world.getBlockAt((int) Math.floor(x), y + 1, (int) Math.floor(z));
            Block floor = world.getBlockAt((int) Math.floor(x), y - 1, (int) Math.floor(z));
            if (!feet.getType().isSolid() && !head.getType().isSolid() && !isHazardous(floor.getType())) {
                Location safe = origin.clone();
                safe.setY(y);
                return safe;
            }
            y++;
        }
        Location safe = origin.clone();
        safe.setY(Math.min(maxY, y));
        return safe;
    }

    private static boolean isHazardous(Material material) {
        return material == Material.LAVA
                || material == Material.MAGMA_BLOCK
                || material == Material.CAMPFIRE
                || material == Material.FIRE
                || material == Material.SOUL_FIRE
                || material == Material.CACTUS
                || material == Material.POWDER_SNOW;
    }
}
