package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.Ability;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Damage;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/**
 * 🔥 Fire Angel - Active: Inferno Blast.
 * A fast-moving projectile stream of 15 flame particles; 4 hearts of true
 * damage on collision and a 6-second ignite. Cooldown 10s.
 */
public class InfernoBlast implements Ability {

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerProfile profile) {
        Sounds.playAt(player.getLocation(), "entity.blaze.shoot", 1.0f, 1.0f);

        Vector direction = player.getEyeLocation().getDirection().normalize();
        Location position = player.getEyeLocation().add(direction.clone().multiply(0.6));
        double damage = plugin.getConfigManager().fireDamage()
                + plugin.getAbilityManager().tierDamageBonus(profile.getTier());
        int burnTicks = plugin.getConfigManager().fireBurnSeconds() * 20;

        final int[] steps = {0};
        final BukkitTask[] holder = new BukkitTask[1];
        holder[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (steps[0]++ > 60 || !player.isOnline()) {
                holder[0].cancel();
                return;
            }
            position.add(direction.clone().multiply(1.2));

            // The projectile stream: 15 flame particles in the look direction.
            Particles.spawn(position.getWorld(), "flame", position, 15, 0.08, 0.08, 0.08, 0.01);

            if (position.getBlock().getType().isSolid()) {
                impact(position);
                holder[0].cancel();
                return;
            }
            for (Entity entity : position.getWorld().getNearbyEntities(position, 1.3, 1.3, 1.3)) {
                if (entity instanceof LivingEntity living && !entity.equals(player)) {
                    Damage.trueDamage(living, damage);
                    living.setFireTicks(Math.max(living.getFireTicks(), burnTicks));
                    impact(living.getLocation());
                    holder[0].cancel();
                    return;
                }
            }
        }, 0L, 1L);
    }

    private void impact(Location location) {
        Sounds.playAt(location, "entity.generic.explode", 1.0f, 1.0f);
        Particles.spawn(location.getWorld(), "large_smoke", location, 20, 0.5, 0.2, 0.5, 0.1);
    }
}
