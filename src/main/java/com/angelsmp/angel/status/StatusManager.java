package com.angelsmp.angel.status;

import com.angelsmp.angel.AngelPlugin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks timed combat states that are not covered by vanilla potion effects:
 * <ul>
 *     <li><b>Stun / Freeze</b> &mdash; horizontal velocity is zeroed every tick.</li>
 *     <li><b>Knockback immunity</b> &mdash; the Unmovable Titan milestone.</li>
 *     <li><b>Fall immunity</b> &mdash; the Sonic Boom milestone.</li>
 * </ul>
 */
public class StatusManager {

    private final AngelPlugin plugin;
    private final Map<UUID, Long> stunnedUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Long> knockbackImmuneUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Long> fallImmuneUntil = new ConcurrentHashMap<>();
    private BukkitTask task;

    public StatusManager(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task != null) {
            return;
        }
        // Runs every tick so a stun really pins the target in place.
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        stunnedUntil.clear();
        knockbackImmuneUntil.clear();
        fallImmuneUntil.clear();
    }

    private void tick() {
        long now = System.currentTimeMillis();
        stunnedUntil.entrySet().removeIf(entry -> {
            UUID id = entry.getKey();
            if (entry.getValue() <= now) {
                return true;
            }
            LivingEntity entity = resolve(id);
            if (entity == null || entity.isDead()) {
                return true;
            }
            // Zero out the horizontal translation vector and refresh the visual freeze.
            Vector velocity = entity.getVelocity();
            entity.setVelocity(new Vector(0.0, Math.min(velocity.getY(), 0.0), 0.0));
            entity.setFallDistance(0f);
            if (!entity.hasPotionEffect(PotionEffectType.SLOWNESS)) {
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 10, 250, false, false, false));
            }
            return false;
        });
        knockbackImmuneUntil.entrySet().removeIf(entry -> entry.getValue() <= now);
        fallImmuneUntil.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    private LivingEntity resolve(UUID id) {
        org.bukkit.entity.Entity entity = plugin.getServer().getEntity(id);
        return entity instanceof LivingEntity living ? living : null;
    }

    // ---- Stun / freeze -------------------------------------------------

    public void stun(LivingEntity entity, long ticks) {
        stunnedUntil.put(entity.getUniqueId(), System.currentTimeMillis() + (ticks * 50L));
    }

    public boolean isStunned(LivingEntity entity) {
        Long until = stunnedUntil.get(entity.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    // ---- Knockback immunity (Unmovable Titan) --------------------------

    public void setKnockbackImmune(LivingEntity entity, long ticks) {
        knockbackImmuneUntil.put(entity.getUniqueId(), System.currentTimeMillis() + (ticks * 50L));
    }

    public boolean isKnockbackImmune(LivingEntity entity) {
        Long until = knockbackImmuneUntil.get(entity.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    // ---- Fall immunity (Sonic Boom) ------------------------------------

    public void setFallImmune(LivingEntity entity, long ticks) {
        fallImmuneUntil.put(entity.getUniqueId(), System.currentTimeMillis() + (ticks * 50L));
    }

    public boolean isFallImmune(LivingEntity entity) {
        Long until = fallImmuneUntil.get(entity.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }
}
