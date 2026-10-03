package com.angelsmp.angel.ability;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the ability projectiles that need special impact handling
 * (currently the Blaze Fireball's anti-grief explosion safeguard).
 */
public class ProjectileTracker {

    private final Set<UUID> blazeFireballs = ConcurrentHashMap.newKeySet();

    public void trackBlazeFireball(UUID projectileId) {
        blazeFireballs.add(projectileId);
    }

    public boolean isBlazeFireball(UUID projectileId) {
        return blazeFireballs.contains(projectileId);
    }

    public void forget(UUID projectileId) {
        blazeFireballs.remove(projectileId);
    }
}
