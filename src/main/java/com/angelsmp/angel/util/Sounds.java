package com.angelsmp.angel.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Sound helper built on namespaced sound keys (e.g. {@code item.firecharge.use}).
 *
 * <p>Using string keys rather than the {@code Sound} enum keeps every audio
 * trigger in the specification working across server versions, because the
 * client-side sound registry is looked up by key at playback time.</p>
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

    /** Plays a sound at a location for a single listener only. */
    public static void playAtFor(Player listener, Location location, String key, float volume, float pitch) {
        if (listener == null || location == null || location.getWorld() == null) {
            return;
        }
        listener.playSound(location, key, volume, pitch);
    }
}
