package com.angelsmp.angel.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

/** Thin wrapper over the Bukkit particle pipeline (rename-proof lookups). */
public final class Particles {

    private Particles() {
    }

    public static Particle resolve(String key) {
        return Compat.particle(key);
    }

    public static void spawn(World world, String key, Location loc, int count,
                             double ox, double oy, double oz, double speed) {
        Particle particle = resolve(key);
        if (particle == null || world == null) {
            return;
        }
        try {
            world.spawnParticle(particle, loc, count, ox, oy, oz, speed);
        } catch (Throwable ignored) {
            // particle data mismatch on this version - skip silently
        }
    }

    public static void spawnBlock(World world, String key, Location loc, int count,
                                  double ox, double oy, double oz, BlockData data) {
        Particle particle = resolve(key);
        if (particle == null || world == null) {
            return;
        }
        try {
            world.spawnParticle(particle, loc, count, ox, oy, oz, 0.0, data);
        } catch (Throwable ignored) {
            // particle does not accept block data on this version - skip silently
        }
    }

    public static void spawnItem(World world, String key, Location loc, int count,
                                 double ox, double oy, double oz, ItemStack data) {
        Particle particle = resolve(key);
        if (particle == null || world == null) {
            return;
        }
        try {
            world.spawnParticle(particle, loc, count, ox, oy, oz, 0.0, data);
        } catch (Throwable ignored) {
            // particle does not accept item data on this version - skip silently
        }
    }
}
