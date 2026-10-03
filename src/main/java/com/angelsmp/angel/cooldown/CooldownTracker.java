package com.angelsmp.angel.cooldown;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.Element;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Compat;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Module 4A - Non-Lagging Actionbar Cooldown Tracker.
 *
 * <p>A single master task runs every 2 game ticks (0.1s). Ability cooldowns are
 * stored as millisecond epoch expirations on the profile; this task only reads
 * them and renders the action-bar bar.</p>
 */
public class CooldownTracker {

    private final AngelPlugin plugin;
    private final Set<UUID> wasCooling = ConcurrentHashMap.newKeySet();
    private BukkitTask task;

    public CooldownTracker(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task != null) {
            return;
        }
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 2L, 2L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        wasCooling.clear();
    }

    private void tick() {
        long now = System.currentTimeMillis();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
            if (profile == null || !profile.hasElement()) {
                continue;
            }
            Element element = profile.getElement();
            long activeRemaining = Math.max(0L,
                    profile.getActiveAbilityTimestamp() + Cooldowns.active(element) * 1000L - now);
            long ultimateRemaining = Math.max(0L,
                    profile.getUltimateAbilityTimestamp() + Cooldowns.ultimate(element) * 1000L - now);

            if (activeRemaining > 0L) {
                wasCooling.add(player.getUniqueId());
                Compat.sendActionBar(player, bar("Ability", activeRemaining, Cooldowns.active(element)));
            } else if (ultimateRemaining > 0L) {
                wasCooling.add(player.getUniqueId());
                Compat.sendActionBar(player, bar("Ultimate", ultimateRemaining, Cooldowns.ultimate(element)));
            } else {
                Compat.sendActionBar(player, "§a§l● ABILITY READY ●");
                if (wasCooling.remove(player.getUniqueId())) {
                    // The exact millisecond the timer hits zero.
                    Sounds.playTo(player, "block.note_block.bell", 1.0f, 1.5f);
                }
            }
        }
    }

    /** Builds e.g. §cAbility Cooldown: 4.5s [§a████§c██████§7]. */
    private String bar(String label, long remainingMillis, int totalSeconds) {
        double remainingSeconds = remainingMillis / 1000.0;
        int total = Math.max(1, totalSeconds);
        double fraction = Math.max(0.0, Math.min(1.0, remainingSeconds / total));
        int green = (int) Math.floor(fraction * 10);
        int red = 10 - green;
        return "§c" + label + " Cooldown: " + String.format(java.util.Locale.ROOT, "%.1f", remainingSeconds) + "s [§a"
                + "█".repeat(green) + "§c" + "█".repeat(red) + "§7]";
    }
}
