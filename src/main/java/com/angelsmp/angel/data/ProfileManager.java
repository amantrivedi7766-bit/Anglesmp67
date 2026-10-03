package com.angelsmp.angel.data;

import com.angelsmp.angel.AngelPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Module 1 - the asynchronous memory-buffered lifecycle.
 *
 * <pre>
 * [Player Connects] --&gt; Async DB Fetch --&gt; Cache Data to Profile Map
 * [Player Quits]    &lt;-- Async DB Save  &lt;-- Remove from Memory Cache
 * </pre>
 *
 * <p>Profiles are keyed strictly by UUID. The heavy DB work happens on a
 * dedicated background thread pool so the main thread never blocks.</p>
 */
public class ProfileManager {

    private final AngelPlugin plugin;
    private final Storage storage;
    private final ExecutorService dbPool = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "AngelSMP-DB");
        thread.setDaemon(true);
        return thread;
    });

    /** Live RAM registry map (post-join). */
    private final Map<UUID, PlayerProfile> active = new ConcurrentHashMap<>();
    /** Profiles pre-loaded during the login phase, not yet injected into the live map. */
    private final Map<UUID, PlayerProfile> pending = new ConcurrentHashMap<>();

    public ProfileManager(AngelPlugin plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public Storage getStorage() {
        return storage;
    }

    // ---- Login Pre-Check Phase (AsyncPlayerPreLoginEvent) --------------

    /**
     * Runs on the async pre-login thread: fetches the profile from the database
     * (inserting a fresh baseline row if none exists) and stages it in memory.
     */
    public void preLoad(UUID uuid) {
        try {
            PlayerProfile profile = storage.load(uuid);
            if (profile == null) {
                profile = new PlayerProfile(uuid); // baseline: ANGEL / NONE / 0 / 0 / 0
                storage.insertBaseline(profile);
                plugin.getLogger().info("Inserted baseline profile for " + uuid);
            }
            pending.put(uuid, profile);
        } catch (Exception ex) {
            plugin.getLogger().severe("Failed to pre-load profile for " + uuid + ": " + ex.getMessage());
            // Fail-safe: stage a baseline so the player is never left without a profile.
            pending.put(uuid, new PlayerProfile(uuid));
        }
    }

    // ---- Join Map Injection Phase (PlayerJoinEvent) --------------------

    /** Moves the pre-loaded profile into the live RAM registry. */
    public PlayerProfile join(UUID uuid) {
        PlayerProfile profile = pending.remove(uuid);
        if (profile == null) {
            // The pre-login phase was missed (reload, plugin hot-install, ...): load now.
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

    // ---- Safe Exit Phase (PlayerQuitEvent) -----------------------------

    /** Copies the data out of live memory, drops the UUID, and saves asynchronously. */
    public void quit(UUID uuid) {
        PlayerProfile profile = active.remove(uuid);
        pending.remove(uuid);
        if (profile != null) {
            saveAsync(profile);
        }
    }

    // ---- Access --------------------------------------------------------

    public PlayerProfile get(UUID uuid) {
        PlayerProfile profile = active.get(uuid);
        if (profile == null) {
            profile = pending.get(uuid);
        }
        return profile;
    }

    public boolean isLoaded(UUID uuid) {
        return active.containsKey(uuid);
    }

    // ---- Async persistence --------------------------------------------

    public void saveAsync(PlayerProfile profile) {
        dbPool.execute(() -> {
            try {
                storage.save(profile);
            } catch (Exception ex) {
                plugin.getLogger().severe("Async save failed for " + profile.getUuid() + ": " + ex.getMessage());
            }
        });
    }

    /** Flushes every live profile to disk (used on plugin disable). */
    public void saveAll() {
        for (PlayerProfile profile : active.values()) {
            try {
                storage.save(profile);
            } catch (Exception ex) {
                plugin.getLogger().severe("Final save failed for " + profile.getUuid() + ": " + ex.getMessage());
            }
        }
    }

    public void shutdown() {
        dbPool.shutdown();
        storage.close();
    }
}
