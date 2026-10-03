package com.angelsmp.angel.cooldown;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Tracks per-player ability cooldown frames as wall-clock deadlines. */
public class CooldownManager {

    private final Map<UUID, Long> readyAt = new ConcurrentHashMap<>();
    private final Map<UUID, Long> totals = new ConcurrentHashMap<>();

    /** Starts a cooldown of the given length in server ticks (20 ticks = 1s). */
    public void start(UUID uuid, long ticks) {
        startSeconds(uuid, ticks / 20.0, (long) Math.ceil(ticks / 20.0));
    }

    public void startSeconds(UUID uuid, double seconds) {
        startSeconds(uuid, seconds, (long) Math.ceil(seconds));
    }

    public void startSeconds(UUID uuid, double seconds, long totalForBar) {
        readyAt.put(uuid, System.currentTimeMillis() + (long) (seconds * 1000.0));
        totals.put(uuid, totalForBar);
    }

    public boolean isReady(UUID uuid) {
        Long deadline = readyAt.get(uuid);
        return deadline == null || System.currentTimeMillis() >= deadline;
    }

    public long remainingMillis(UUID uuid) {
        Long deadline = readyAt.get(uuid);
        if (deadline == null) {
            return 0L;
        }
        return Math.max(0L, deadline - System.currentTimeMillis());
    }

    public long remainingSeconds(UUID uuid) {
        return (long) Math.ceil(remainingMillis(uuid) / 1000.0);
    }

    public boolean isCoolingDown(UUID uuid) {
        return remainingMillis(uuid) > 0L;
    }

    /** @return the total cooldown length in seconds last applied (for the HUD bar ratio). */
    public long totalSeconds(UUID uuid) {
        Long total = totals.get(uuid);
        return total == null ? 0L : total;
    }

    /** @return fraction remaining in the range 0.0 (ready) .. 1.0 (just started). */
    public double progress(UUID uuid) {
        long total = totalSeconds(uuid);
        if (total <= 0L) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, remainingMillis(uuid) / (total * 1000.0)));
    }

    public void clear(UUID uuid) {
        readyAt.remove(uuid);
        totals.remove(uuid);
    }

    public void clearAll() {
        readyAt.clear();
        totals.clear();
    }
}
