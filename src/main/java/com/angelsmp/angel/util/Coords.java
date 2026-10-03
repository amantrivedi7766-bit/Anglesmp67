package com.angelsmp.angel.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/** Module 2B - the anti-suffocation safety scan. */
public final class Coords {

    private Coords() {
    }

    /**
     * Shifts the target location upward step-by-step (+1 Y) until a clear
     * 2-block-high pocket of open air is found above a non-hazardous floor.
     *
     * @return a safe location (never inside solid blocks or lava).
     */
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

            if (!feet.getType().isSolid()
                    && !head.getType().isSolid()
                    && !isHazardous(floor.getType())) {
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
