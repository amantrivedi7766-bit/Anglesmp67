package com.angelsmp.angel.progression;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.util.Compat;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Module 7 - The Complete Level-Up Upgrade Animation Sequence.
 *
 * <p>Runs the movement freeze, the three layered sound bursts, the double-helix
 * sine/cosine particle swirl and the client screen title card.</p>
 */
public class LevelUpAnimation {

    private static final int FREEZE_TICKS = 20;

    private final AngelPlugin plugin;
    private final Set<UUID> frozen = ConcurrentHashMap.newKeySet();

    public LevelUpAnimation(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    /** @return true while the player's horizontal movement is frozen. */
    public boolean isFrozen(UUID uuid) {
        return frozen.contains(uuid);
    }

    /** Fires the whole sequence for the given player. */
    public void play(Player player) {
        Location origin = player.getLocation();

        // 1) Input Vector Freeze.
        frozen.add(player.getUniqueId());

        // 2) Sound Packet Waves (all nearby players within a 20-block radius).
        Sounds.playNearby(origin, 20.0, "item.totem.use", 1.0f, 1.0f);
        Sounds.playNearby(origin, 20.0, "ui.toast.challenge_complete", 1.0f, 1.2f);
        Sounds.playNearby(origin, 20.0, "entity.lightning_bolt.thunder", 1.0f, 0.6f);

        // 3) The Double-Helix Particle Swirl + 5) Unfreeze after 20 ticks.
        final int[] step = {0};
        final BukkitTask[] holder = new BukkitTask[1];
        holder[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                frozen.remove(player.getUniqueId());
                holder[0].cancel();
                return;
            }
            double height = step[0] * 0.15; // 0.0 -> ~3.0 over 20 ticks
            Location base = player.getLocation();
            double x1 = Math.cos(height * 5);
            double z1 = Math.sin(height * 5);
            Particles.spawn(base.getWorld(), "totem_of_undying",
                    base.clone().add(x1, height, z1), 1, 0.0, 0.0, 0.0, 0.0);
            Particles.spawn(base.getWorld(), "glow",
                    base.clone().add(-x1, height, -z1), 1, 0.0, 0.0, 0.0, 0.0);

            step[0]++;
            if (step[0] > FREEZE_TICKS) {
                frozen.remove(player.getUniqueId());
                holder[0].cancel();
            }
        }, 0L, 1L);

        // 4) The Client Screen Title Overlay (fadeIn 10, stay 40, fadeOut 10).
        Compat.sendTitle(player, "§e§l★ LEVEL UP ★", "§7Your elemental strength has expanded!", 10, 40, 10);
    }
}
