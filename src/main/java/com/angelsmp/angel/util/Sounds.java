package com.angelsmp.angel.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Sound helper built on namespaced sound keys (e.g. {@code entity.wither.spawn}).
 * Using keys rather than the {@code Sound} enum keeps every audio trigger working
 * across server versions and on Paper / Spigot / Purpur alike.
 */
public final class Sounds {

    private Sounds() {
    }

    public static void playAt(Location location, String key, float volume, float pitch) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        location.getWorld().playSound(location, key, volume, pitch);
    }

    public static void playTo(Player player, String key, float volume, float pitch) {
        if (player == null) {
            return;
        }
        player.playSound(player.getLocation(), key, volume, pitch);
    }

    /** Plays a sound only for players inside a radius of the origin. */
    public static void playNearby(Location origin, double radius, String key, float volume, float pitch) {
        if (origin == null || origin.getWorld() == null) {
            return;
        }
        World world = origin.getWorld();
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distanceSquared(origin) <= radius * radius) {
                player.playSound(origin, key, volume, pitch);
            }
        }
    }
}
