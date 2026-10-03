package com.angelsmp.angel.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Sound helper built on namespaced sound keys (e.g. {@code entity.blaze.shoot}),
 * so every audio trigger works across versions and on Paper / Spigot / Purpur.
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

    /** Plays a sound to everyone within a radius of the origin. */
    public static void playNearby(Location origin, double radius, String key, float volume, float pitch) {
        if (origin == null || origin.getWorld() == null) {
            return;
        }
        for (Player player : origin.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(origin) <= radius * radius) {
                player.playSound(origin, key, volume, pitch);
            }
        }
    }
}
