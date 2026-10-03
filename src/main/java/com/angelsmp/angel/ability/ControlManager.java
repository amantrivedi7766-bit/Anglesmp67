package com.angelsmp.angel.ability;

import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase 4 - short-lived combat control states: the Ice freeze (movement + jump
 * lock) and the Lightning camera/mouse lock.
 */
public class ControlManager {

    /** Players whose horizontal movement and jump are locked. */
    private final Map<UUID, Long> frozen = new ConcurrentHashMap<>();
    /** Players whose camera (yaw/pitch) is locked, with the locked angles. */
    private final Map<UUID, float[]> cameraLocked = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cameraLockedUntil = new ConcurrentHashMap<>();
    /** Players currently mid Wind Leap (for the landing shockwave). */
    private final Map<UUID, Long> gliding = new ConcurrentHashMap<>();

    // ---- Freeze --------------------------------------------------------

    public void freeze(UUID uuid, long ticks) {
        frozen.put(uuid, System.currentTimeMillis() + ticks * 50L);
    }

    public boolean isFrozen(UUID uuid) {
        Long until = frozen.get(uuid);
        return until != null && until > System.currentTimeMillis();
    }

    // ---- Camera lock ---------------------------------------------------

    public void lockCamera(UUID uuid, float yaw, float pitch, long ticks) {
        cameraLocked.put(uuid, new float[]{yaw, pitch});
        cameraLockedUntil.put(uuid, System.currentTimeMillis() + ticks * 50L);
    }

    public boolean isCameraLocked(UUID uuid) {
        Long until = cameraLockedUntil.get(uuid);
        return until != null && until > System.currentTimeMillis();
    }

    public float[] getLockedAngles(UUID uuid) {
        return cameraLocked.get(uuid);
    }

    // ---- Wind glide ----------------------------------------------------

    public void beginGlide(UUID uuid) {
        gliding.put(uuid, System.currentTimeMillis() + 6000L);
    }

    public boolean isGliding(UUID uuid) {
        Long until = gliding.get(uuid);
        return until != null && until > System.currentTimeMillis();
    }

    public void endGlide(UUID uuid) {
        gliding.remove(uuid);
    }

    /** Applies a horizontal-only movement lock to a from/to vector pair. */
    public static Vector horizontalOnly(Vector velocity) {
        return new Vector(0.0, Math.min(velocity.getY(), 0.0), 0.0);
    }
}
