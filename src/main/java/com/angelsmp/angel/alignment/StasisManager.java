package com.angelsmp.angel.alignment;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase 2 - the Welcome / Stasis state.
 * A brand-new player cannot walk, break blocks or take damage until they pick
 * their destiny in the Selection GUI.
 */
public class StasisManager {

    private final Set<UUID> stasis = ConcurrentHashMap.newKeySet();

    public void begin(UUID uuid) {
        stasis.add(uuid);
    }

    public void end(UUID uuid) {
        stasis.remove(uuid);
    }

    public boolean isInStasis(UUID uuid) {
        return stasis.contains(uuid);
    }
}
