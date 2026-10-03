package com.angelsmp.angel.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

/**
 * Thin wrapper over the Bukkit particle pipeline.
 *
 * <p>Particles are resolved from their {@code minecraft:} key at runtime (see
 * {@link Compat#particle(String)}) so the plugin keeps working across server
 * versions even if a specific particle is renamed or absent &mdash; a missing
 * particle simply resolves to {@code null} and is skipped rather than crashing
 * the ability.</p>
 */
public final class Particles {

    private Particles() {
    }

    /** Resolves a particle key such as {@code "flame"} or {@code "minecraft:flame"}. */
    public static Particle resolve(String key) {
        return Compat.particle(key);
    }

    /** Spawns a simple (non-data) particle. */
    public static void spawn(World world, String key, Location loc, int count,
                             double ox, double oy, double oz, double speed) {
        Particle particle = resolve(key);
        if (particle == null) {
            return;
        }
        try {
            world.spawnParticle(particle, loc, count, ox, oy, oz, speed);
        } catch (Throwable ignored) {
            // Particle data mismatch on this version - skip silently.
        }
    }

    /** Spawns a block-data particle (e.g. {@code block_marker}, {@code block_crumble}). */
    public static void spawnBlock(World world, String key, Location loc, int count,
                                  double ox, double oy, double oz, BlockData data) {
        Particle particle = resolve(key);
        if (particle == null) {
            return;
        }
        try {
            world.spawnParticle(particle, loc, count, ox, oy, oz, 0.0, data);
        } catch (Throwable ignored) {
            // Particle does not accept block data on this version - skip silently.
        }
    }

    /** Spawns an item particle (e.g. {@code item_snowball}, {@code item}). */
    public static void spawnItem(World world, String key, Location loc, int count,
                                 double ox, double oy, double oz, ItemStack data) {
        Particle particle = resolve(key);
        if (particle == null) {
            return;
        }
        try {
            world.spawnParticle(particle, loc, count, ox, oy, oz, 0.0, data);
        } catch (Throwable ignored) {
            // Particle does not accept item data on this version - skip silently.
        }
    }

    /** Returns true if the particle key is recognised by this server. */
    public static boolean exists(String key) {
        return resolve(key) != null;
    }
}
