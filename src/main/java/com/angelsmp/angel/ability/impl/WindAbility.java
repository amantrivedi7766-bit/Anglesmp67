package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.Ability;
import com.angelsmp.angel.ability.ProjectileManager;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.element.TierData;
import com.angelsmp.angel.player.PlayerData;
import com.angelsmp.angel.util.Compat;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/**
 * 🌀 Wind Angel (The Zephyr).
 *
 * <p>Extracts the player's looking vector, applies an acceleration multiplier
 * and injects it into the velocity channel for a massive Forward Leap, then
 * applies an area-of-effect knockback push.</p>
 *
 * <p>Note: for the Wind archetype the "duration" column of the metrics table
 * carries the AoE splash radius (4.0b / 6.0b / 8.0b), so
 * {@link TierData#getDurationSeconds()} is read as that radius here.</p>
 */
public class WindAbility implements Ability {

    private static final double VELOCITY_MULTIPLIER = 1.8;
    private static final double KNOCKBACK_FORCE = 1.2;

    @Override
    public AngelElement element() {
        return AngelElement.WIND;
    }

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerData data, TierData tier) {
        int level = tier.getTier();
        double radius = tier.getDurationSeconds(); // 4.0 / 6.0 / 8.0 blocks

        // Exact audio trigger: launch blast.
        Sounds.playAt(player.getLocation(), "item.trident.riptide", 1.0f, 1.0f);
        Sounds.playAt(player.getLocation(), "entity.phantom.flap", 1.0f, 1.2f);

        // Launch centre footprint under the player's feet.
        Location feet = player.getLocation();
        Particles.spawn(feet.getWorld(), "cloud", feet, 35, 0.6, 0.1, 0.6, 0.05);
        Particles.spawn(feet.getWorld(), "poof", feet, 15, 0.6, 0.1, 0.6, 0.05);

        // Forward leap: look vector * acceleration multiplier.
        Vector direction = player.getLocation().getDirection().normalize();
        Vector velocity = direction.clone().multiply(VELOCITY_MULTIPLIER);
        velocity.setY(Math.max(velocity.getY(), 0.4) + 0.25);
        player.setVelocity(velocity);

        // AoE knockback push.
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius)) {
            if (!(entity instanceof LivingEntity living) || entity.equals(player)) {
                continue;
            }
            Vector away = living.getLocation().toVector().subtract(player.getLocation().toVector());
            if (away.lengthSquared() < 0.01) {
                away = direction.clone();
            }
            ProjectileManager.applyKnockback(living, away, KNOCKBACK_FORCE);
            if (tier.getDamage() > 0.0) {
                living.damage(tier.getDamage(), player);
            }
        }

        // Flight trajectory: crit sparks marking the air path.
        renderFlightTrail(plugin, player);

        if (level >= 2) {
            grantLandingBuff(plugin, player);
        }
        if (level >= 3) {
            // Sonic Boom: nullifies all personal fall damage.
            plugin.getStatusManager().setFallImmune(player, 20L * 10L);
        }
    }

    private void renderFlightTrail(AngelPlugin plugin, Player player) {
        final int[] ticks = {0};
        final BukkitTask[] holder = new BukkitTask[1];
        holder[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (ticks[0]++ > 15 || !player.isOnline()) {
                holder[0].cancel();
                return;
            }
            Particles.spawn(player.getWorld(), "crit", player.getLocation(), 10, 0.15, 0.15, 0.15, 0.0);
        }, 0L, 1L);
    }

    private void grantLandingBuff(AngelPlugin plugin, Player player) {
        // Detect the landing frame, then apply Speed II for 3 seconds.
        final int[] ticks = {0};
        final BukkitTask[] holder = new BukkitTask[1];
        holder[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (ticks[0]++ > 100 || !player.isOnline()) {
                holder[0].cancel();
                return;
            }
            if (player.isOnGround()) {
                PotionEffectType speed = Compat.effect("SPEED");
                if (speed != null) {
                    player.addPotionEffect(new PotionEffect(speed, 60, 1, false, true, true));
                }
                Sounds.playTo(player, "entity.breeze.land", 1.0f, 1.0f);
                holder[0].cancel();
            }
        }, 1L, 1L);
    }
}
