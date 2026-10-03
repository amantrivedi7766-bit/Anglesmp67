package com.angelsmp.angel.listener;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.Element;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

/**
 * Module 3 - Pyro Immunity (Fire passive), Aerodynamic Descent (Wind passive)
 * and the Blaze Fireball anti-grief explosion safeguard.
 */
public class DamageListener implements Listener {

    private final AngelPlugin plugin;

    public DamageListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        if (profile == null) {
            return;
        }

        // Passive - Pyro Immunity (Level 0): a Fire Angel takes no fire damage.
        if (profile.getElement() == Element.FIRE) {
            switch (event.getCause()) {
                case FIRE, FIRE_TICK, LAVA, HOT_FLOOR -> {
                    event.setDamage(0.0);
                    event.setCancelled(true);
                    return;
                }
                default -> {
                    // not a fire source
                }
            }
        }

        // Passive - Aerodynamic Descent (Level 2): cancel fall damage from a Wind Leap.
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL
                && plugin.getPassiveManager().consumeFallExempt(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!plugin.getAbilityManager().getProjectileTracker().isBlazeFireball(projectile.getUniqueId())) {
            return;
        }
        plugin.getAbilityManager().getProjectileTracker().forget(projectile.getUniqueId());

        Location impact;
        if (event.getHitEntity() != null) {
            impact = event.getHitEntity().getLocation();
        } else if (event.getHitBlock() != null) {
            impact = event.getHitBlock().getLocation().add(0.5, 0.5, 0.5);
        } else {
            impact = projectile.getLocation();
        }

        Entity shooter = projectile.getShooter() instanceof Entity entity ? entity : null;

        // Anti-grief: zero block-destruction radius, no ignition; only player damage.
        for (Entity nearby : impact.getWorld().getNearbyEntities(impact, 4.0, 4.0, 4.0)) {
            if (nearby instanceof Player victim && !nearby.equals(shooter)) {
                if (shooter instanceof Player caster) {
                    victim.damage(8.0, caster); // exactly 4 full hearts
                } else {
                    victim.damage(8.0);
                }
                victim.setFireTicks(100); // 5 seconds
            }
        }

        Particles.spawn(impact.getWorld(), "large_smoke", impact, 30, 0.6, 0.1, 0.6, 0.15);
        Particles.spawn(impact.getWorld(), "flame", impact, 25, 0.6, 0.1, 0.6, 0.12);
        Sounds.playAt(impact, "entity.generic.explode", 1.0f, 1.0f);
        Sounds.playAt(impact, "block.fire.ambient", 1.0f, 1.0f);

        projectile.remove();
    }
}
