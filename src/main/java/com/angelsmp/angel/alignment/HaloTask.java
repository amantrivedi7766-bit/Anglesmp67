package com.angelsmp.angel.alignment;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.Alignment;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Compat;
import com.angelsmp.angel.util.Particles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

/**
 * Phase 3 - The Head Sign System.
 *
 * <p>Renders a permanent floating Angel Halo (or Devil Horns) exactly 0.5 blocks
 * above the player's head using client-side particle arrays. It follows the head,
 * crouches with the player and vanishes while the player is invisible.</p>
 */
public class HaloTask {

    private final AngelPlugin plugin;
    private BukkitTask task;
    private double angle;

    public HaloTask(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task != null) {
            return;
        }
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        int density = Math.max(4, plugin.getConfigManager().haloParticleDensity());
        double spin = plugin.getConfigManager().haloSpinSpeed();
        angle += spin * 0.05;
        PotionEffectType invisibility = Compat.effect("INVISIBILITY");

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
            if (profile == null || !profile.hasAlignment() || profile.isSoulLocked()) {
                continue; // no halo while the soul is fractured (Phase 6)
            }
            if (invisibility != null && player.hasPotionEffect(invisibility)) {
                continue; // disappears while invisible
            }
            // Exactly 0.5 blocks above the head.
            Location head = player.getLocation().add(0, 2.35, 0);
            Alignment alignment = profile.getAlignment();

            if (alignment.isDevil()) {
                if (plugin.getConfigManager().enableDevilHorns()) {
                    renderHorns(head, alignment);
                }
            } else {
                renderHalo(head, alignment, density);
            }
        }
    }

    private void renderHalo(Location head, Alignment alignment, int density) {
        for (int i = 0; i < density; i++) {
            double theta = angle + (i * (Math.PI * 2) / density);
            Location point = head.clone().add(Math.cos(theta) * 0.5, 0.0, Math.sin(theta) * 0.5);
            switch (alignment) {
                case FIRE -> {
                    Particles.spawn(head.getWorld(), "flame", point, 1, 0, 0, 0, 0);
                    Particles.spawn(head.getWorld(), "lava", point, 1, 0, 0, 0, 0);
                }
                case ICE -> {
                    Particles.spawn(head.getWorld(), "snowflake", point, 1, 0, 0, 0, 0);
                    Particles.spawn(head.getWorld(), "snowball", point, 1, 0, 0, 0, 0);
                }
                case LIGHTNING -> {
                    Particles.spawn(head.getWorld(), "electric_spark", point, 1, 0, 0, 0, 0);
                    if (i % 4 == 0) {
                        Particles.spawn(head.getWorld(), "end_rod", point, 1, 0, 0, 0, 0);
                    }
                }
                case WIND -> Particles.spawn(head.getWorld(), "cloud", point, 1, 0, 0, 0, 0);
                case EARTH -> Particles.spawnBlock(head.getWorld(), "block_marker", point, 1, 0, 0, 0,
                        org.bukkit.Material.DIRT.createBlockData());
                case LIGHT -> {
                    Particles.spawn(head.getWorld(), "glow", point, 1, 0, 0, 0, 0);
                    Particles.spawn(head.getWorld(), "end_rod", point, 1, 0, 0, 0, 0);
                }
                default -> Particles.spawn(head.getWorld(), "end_rod", point, 1, 0, 0, 0, 0);
            }
        }
    }

    private void renderHorns(Location head, Alignment alignment) {
        // Two distinct points of deep purple / dark red portal particles forming sharp horns.
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < 4; i++) {
                double lift = i * 0.12;
                Location point = head.clone().add(side * (0.22 + lift * 0.15), lift, 0.0);
                Particles.spawn(head.getWorld(), "portal", point, 1, 0, 0, 0, 0);
                if (i % 2 == 0) {
                    Particles.spawn(head.getWorld(), "dust", point, 1, 0, 0, 0, 0);
                }
            }
        }
    }
}
