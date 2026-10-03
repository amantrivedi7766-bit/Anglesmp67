package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.ElementalAbility;
import com.angelsmp.angel.data.PlayerProfile;
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
 * 🔥 Fire - Ultimate Level 2: Hellfire Dome.
 *
 * <p>Runs every tick for 5 seconds (100 ticks). Draws a 4-block flame ring and
 * pushes any enemy inside the bubble out while applying Wither.</p>
 */
public class HellfireDome implements ElementalAbility {

    private static final double RADIUS = 4.0;
    private static final int DURATION_TICKS = 100;

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerProfile profile) {
        Sounds.playAt(player.getLocation(), "entity.blaze.shoot", 1.0f, 1.0f);
        Sounds.playAt(player.getLocation(), "entity.ghast.shoot", 1.0f, 0.8f);
        PotionEffectType wither = Compat.effect("WITHER");

        final int[] ticks = {0};
        final BukkitTask[] holder = new BukkitTask[1];
        holder[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || ticks[0]++ >= DURATION_TICKS) {
                holder[0].cancel();
                return;
            }
            Location center = player.getLocation();

            // The dome geometry: sine/cosine ring, theta 0 -> 2pi in steps of 0.2.
            for (double theta = 0; theta < Math.PI * 2; theta += 0.2) {
                double dx = RADIUS * Math.cos(theta);
                double dz = RADIUS * Math.sin(theta);
                Location point = center.clone().add(dx, 0.2, dz);
                Particles.spawn(point.getWorld(), "flame", point, 2, 0.0, 0.0, 0.0, 0.01);
            }

            // Burn / push mechanics.
            for (Entity entity : center.getWorld().getNearbyEntities(center, RADIUS, RADIUS, RADIUS)) {
                if (!(entity instanceof LivingEntity living) || entity.equals(player)) {
                    continue;
                }
                Vector away = living.getLocation().toVector().subtract(center.toVector());
                if (away.lengthSquared() < 0.01) {
                    away = new Vector(0, 0, 1);
                }
                Vector push = away.normalize().multiply(0.8);
                push.setY(0.35);
                living.setVelocity(push);
                if (wither != null) {
                    living.addPotionEffect(new PotionEffect(wither, 40, 1, false, true, true));
                }
            }
        }, 0L, 1L);
    }
}
