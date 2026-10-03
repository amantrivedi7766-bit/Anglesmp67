package com.angelsmp.angel.listener;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.AbilityProjectile;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

/**
 * Handles projectile impacts (the fireball explosion sphere + DoT) and the
 * defensive milestones (fall immunity, knockback immunity).
 */
public class CombatListener implements Listener {

    private final AngelPlugin plugin;

    public CombatListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        AbilityProjectile data = plugin.getProjectileManager().get(projectile);
        if (data == null) {
            return;
        }
        Location impact;
        Entity hitEntity = event.getHitEntity();
        if (hitEntity != null) {
            impact = hitEntity.getLocation();
        } else if (event.getHitBlock() != null) {
            impact = event.getHitBlock().getLocation().add(0.5, 0.5, 0.5);
        } else {
            impact = projectile.getLocation();
        }
        plugin.getProjectileManager().handleImpact(data, impact);
        projectile.remove();
    }

    /**
     * Prevents the raw fireball from dealing its own hit damage: the ability
     * applies the exact specification damage (base impact + DoT) itself.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFireballDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Fireball fireball
                && plugin.getProjectileManager().get(fireball) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        // Sonic Boom: nullify personal fall damage.
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL
                && plugin.getStatusManager().isFallImmune(living)) {
            event.setCancelled(true);
            return;
        }
        // Unmovable Titan: cancel any incoming knockback velocity next tick.
        if (plugin.getStatusManager().isKnockbackImmune(living)) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (living.isValid()) {
                    living.setVelocity(new Vector(0, living.getVelocity().getY(), 0));
                }
            });
        }
        if (living instanceof Player player && plugin.getStatusManager().isStunned(player)) {
            // Frozen targets cannot be pushed around while stunned.
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (player.isValid()) {
                    player.setVelocity(new Vector(0, Math.min(player.getVelocity().getY(), 0), 0));
                }
            });
        }
    }
}
