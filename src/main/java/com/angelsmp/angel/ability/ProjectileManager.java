package com.angelsmp.angel.ability;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks ability projectiles (fireballs) frame-by-frame: it renders the flight
 * particle footprint, the spinning centre animation and, on impact, the
 * expansion sphere, splash damage and the burn damage-over-time.
 */
public class ProjectileManager {

    private final AngelPlugin plugin;
    private final Map<UUID, AbilityProjectile> tracked = new ConcurrentHashMap<>();
    private BukkitTask task;

    public ProjectileManager(AngelPlugin plugin) {
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
        tracked.clear();
    }

    public void track(AbilityProjectile projectile) {
        projectile.setSpawnTick(plugin.getServer().getCurrentTick());
        tracked.put(projectile.getProjectile().getUniqueId(), projectile);
    }

    public AbilityProjectile get(Projectile projectile) {
        return tracked.get(projectile.getUniqueId());
    }

    public void untrack(Projectile projectile) {
        tracked.remove(projectile.getUniqueId());
    }

    // ---- Flight animation ---------------------------------------------

    private void tick() {
        for (AbilityProjectile data : tracked.values()) {
            Projectile projectile = data.getProjectile();
            if (projectile == null || projectile.isDead() || !projectile.isValid()) {
                tracked.remove(data.getProjectile().getUniqueId());
                continue;
            }
            Location current = projectile.getLocation();
            Location last = data.getLastLocation();
            double travelled = last == null ? 0.0 : current.distance(last);

            if (data.getElement() == AngelElement.FIRE) {
                renderFireFlight(current, travelled);
            }
            data.setLastLocation(current.clone());

            // Hard timeout so a stray projectile cannot linger forever.
            if (plugin.getServer().getCurrentTick() - data.getSpawnTick() > 20L * 20L) {
                projectile.remove();
                tracked.remove(projectile.getUniqueId());
            }
        }
    }

    private void renderFireFlight(Location loc, double travelled) {
        int flameCount = Math.max(5, (int) Math.round(travelled * 5));
        int lavaCount = Math.max(1, (int) Math.round(travelled));
        Particles.spawn(loc.getWorld(), "flame", loc, flameCount, 0.05, 0.05, 0.05, 0.01);
        Particles.spawn(loc.getWorld(), "lava", loc, lavaCount, 0.05, 0.05, 0.05, 0.0);

        // Spinning centre-axis animation: a small rotating ring of flame.
        double angle = (plugin.getServer().getCurrentTick() * 0.5) % (Math.PI * 2);
        for (int i = 0; i < 4; i++) {
            double a = angle + (i * Math.PI / 2.0);
            Location ring = loc.clone().add(Math.cos(a) * 0.3, Math.sin(a) * 0.3, Math.sin(a) * 0.3);
            Particles.spawn(loc.getWorld(), "flame", ring, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    // ---- Impact resolution --------------------------------------------

    public void handleImpact(AbilityProjectile data, Location impact) {
        Projectile projectile = data.getProjectile();
        tracked.remove(projectile.getUniqueId());

        Player owner = plugin.getServer().getPlayer(data.getOwnerId());

        // Non-destructive explosion block event.
        if (data.isExplosive()) {
            impact.getWorld().createExplosion(impact, 1.5f, false, false, projectile);
        }

        // Explosion particle footprint: horizontal burst of smoke + small flame.
        Particles.spawn(impact.getWorld(), "large_smoke", impact, 30, 0.6, 0.1, 0.6, 0.15);
        Particles.spawn(impact.getWorld(), "small_flame", impact, 20, 0.6, 0.1, 0.6, 0.12);
        // Flash-expansion sphere stretching over the splash radius.
        for (int i = 0; i < 24; i++) {
            double theta = (i / 24.0) * Math.PI * 2;
            Location sphere = impact.clone().add(
                    Math.cos(theta) * data.getSplashRadius(),
                    Math.sin(theta) * data.getSplashRadius() * 0.5,
                    Math.sin(theta) * data.getSplashRadius());
            Particles.spawn(impact.getWorld(), "flame", sphere, 1, 0.0, 0.0, 0.0, 0.0);
        }

        // Audio: explosion + underlying sizzle.
        Sounds.playAt(impact, "entity.generic.explode", 1.0f, 1.0f);
        Sounds.playAt(impact, "block.fire.ambient", 1.0f, 1.0f);

        // Splash damage + burn DoT.
        for (Entity entity : impact.getWorld().getNearbyEntities(impact, data.getSplashRadius(),
                data.getSplashRadius(), data.getSplashRadius())) {
            if (!(entity instanceof LivingEntity target) || entity.equals(owner)) {
                continue;
            }
            if (owner != null) {
                target.damage(data.getBaseDamage(), owner);
            } else {
                target.damage(data.getBaseDamage());
            }
            applyDot(target, data.getDotDamage(), data.getDotDurationTicks(), owner);
        }
    }

    /** Applies a burn damage-over-time in evenly spaced ticks. */
    public void applyDot(LivingEntity target, double totalDamage, long durationTicks, Player source) {
        if (totalDamage <= 0.0 || durationTicks <= 0L) {
            return;
        }
        int steps = Math.max(1, (int) (durationTicks / 10L));
        double perStep = totalDamage / steps;
        final int[] remaining = {steps};
        final BukkitTask[] holder = new BukkitTask[1];
        holder[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (target.isDead() || !target.isValid()) {
                holder[0].cancel();
                return;
            }
            if (source != null && source.isOnline()) {
                target.damage(perStep, source);
            } else {
                target.damage(perStep);
            }
            Particles.spawn(target.getWorld(), "flame", target.getLocation().add(0, 1, 0), 3, 0.2, 0.3, 0.2, 0.01);
            if (--remaining[0] <= 0) {
                holder[0].cancel();
            }
        }, 10L, 10L);
    }

    /** Utility for abilities that want to push a velocity without adding gravity drift. */
    public static void applyKnockback(Entity entity, Vector direction, double force) {
        Vector push = direction.clone().normalize().multiply(force);
        push.setY(Math.max(push.getY(), 0.35));
        entity.setVelocity(push);
    }
}
