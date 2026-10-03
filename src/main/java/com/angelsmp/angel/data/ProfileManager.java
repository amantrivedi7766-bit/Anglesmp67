package com.angelsmp.angel.data;

import com.angelsmp.angel.AngelPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Phase 1 - asynchronous, memory-buffered profile lifecycle.
 * Keyed strictly by UUID; DB work runs on a background thread pool.
 */
public class ProfileManager {

    private final AngelPlugin plugin;
    private final Storage storage;
    private final ExecutorService dbPool = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "AngelSMP-DB");
        thread.setDaemon(true);
        return thread;
    });

    private final Map<UUID, PlayerProfile> active = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerProfile> pending = new ConcurrentHashMap<>();

    public ProfileManager(AngelPlugin plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    /** Login Pre-Check Phase - runs on the async pre-login thread. */
    public void preLoad(UUID uuid) {
        try {
            PlayerProfile profile = storage.load(uuid);
            if (profile == null) {
                profile = new PlayerProfile(uuid); // baseline: NONE / tier 1 / 0 / 0
                storage.insertBaseline(profile);
            }
            pending.put(uuid, profile);
        } catch (Exception ex) {
            plugin.getLogger().severe("Failed to pre-load profile for " + uuid + ": " + ex.getMessage());
            pending.put(uuid, new PlayerProfile(uuid));
        }
    }

    /** Join Map Injection Phase. */
    public PlayerProfile join(UUID uuid) {
        PlayerProfile profile = pending.remove(uuid);
        if (profile == null) {
            try {
                profile = storage.load(uuid);
            } catch (Exception ex) {
                plugin.getLogger().severe("Failed to load profile on join for " + uuid + ": " + ex.getMessage());
            }
            if (profile == null) {
                profile = new PlayerProfile(uuid);
                saveAsync(profile);
            }
        }
        active.put(uuid, profile);
        return profile;
    }

    /** Safe Exit Phase. */
    public void quit(UUID uuid) {
        PlayerProfile profile = active.remove(uuid);
        pending.remove(uuid);
        if (profile != null) {
            saveAsync(profile);
        }
    }

    public PlayerProfile get(UUID uuid) {
        PlayerProfile profile = active.get(uuid);
        return profile != null ? profile : pending.get(uuid);
    }

    public boolean isLoaded(UUID uuid) {
        return active.containsKey(uuid);
    }

    public void saveAsync(PlayerProfile profile) {
        dbPool.execute(() -> {
            try {
                storage.save(profile);
            } catch (Exception ex) {
                plugin.getLogger().severe("Async save failed for " + profile.getUuid() + ": " + ex.getMessage());
            }
        });
    }

    /** Periodic flush (config: server-settings.save-interval-minutes). */
    public void saveAll() {
        for (PlayerProfile profile : active.values()) {
            try {
                storage.save(profile);
            } catch (Exception ex) {
                plugin.getLogger().severe("Save failed for " + profile.getUuid() + ": " + ex.getMessage());
            }
        }
    }

    public void shutdown() {
        dbPool.shutdown();
        storage.close();
    }
}
