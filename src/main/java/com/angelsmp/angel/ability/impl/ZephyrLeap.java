package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.Ability;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/**
 * 💨 Wind Angel - Active: Zephyr Leap.
 * Launches the caster forward and upward at 45 degrees for an instant 15-block
 * dash; on landing, a smoke shockwave applies Knockback III within 4 blocks.
 * Cooldown 8s.
 */
public class ZephyrLeap implements Ability {

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerProfile profile) {
        Vector look = player.getLocation().getDirection();
        // 45-degree launch: horizontal components + a matched vertical component.
        Vector horizontal = new Vector(look.getX(), 0, look.getZ());
        if (horizontal.lengthSquared() < 0.01) {
            horizontal = new Vector(0, 0, 1);
        }
        horizontal.normalize();
        Vector velocity = horizontal.clone().add(new Vector(0, 1, 0)).normalize().multiply(1.6);
        player.setVelocity(velocity);

        Sounds.playAt(player.getLocation(), "entity.ender_dragon.flap", 1.0f, 1.0f);
        plugin.getControlManager().beginGlide(player.getUniqueId());

        // Detect the landing frame -> smoke shockwave + Knockback III within 4 blocks.
        final int[] ticks = {0};
        final BukkitTask[] holder = new BukkitTask[1];
        holder[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || ticks[0]++ > 120) {
                holder[0].cancel();
                plugin.getControlManager().endGlide(player.getUniqueId());
                return;
            }
            Particles.spawn(player.getWorld(), "cloud", player.getLocation(), 4, 0.2, 0.1, 0.2, 0.02);
            if (player.isOnGround() && ticks[0] > 4) {
                landing(plugin, player);
                holder[0].cancel();
                plugin.getControlManager().endGlide(player.getUniqueId());
            }
        }, 1L, 1L);
    }

    private void landing(AngelPlugin plugin, Player player) {
        Location center = player.getLocation();
        Particles.spawn(center.getWorld(), "large_smoke", center, 40, 2.0, 0.3, 2.0, 0.1);
        Sounds.playAt(center, "entity.generic.explode", 1.0f, 0.8f);
        for (Entity entity : center.getWorld().getNearbyEntities(center, 4.0, 4.0, 4.0)) {
            if (!(entity instanceof LivingEntity living) || entity.equals(player)) {
                continue;
            }
            Vector away = living.getLocation().toVector().subtract(center.toVector());
            if (away.lengthSquared() < 0.01) {
                away = new Vector(0, 0, 1);
            }
            // Knockback III.
            Vector push = away.normalize().multiply(2.2);
            push.setY(0.6);
            living.setVelocity(push);
        }
    }
}
