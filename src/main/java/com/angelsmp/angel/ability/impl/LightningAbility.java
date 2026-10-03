package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.Ability;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.element.TierData;
import com.angelsmp.angel.player.PlayerData;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Raycast;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * ⚡ Lightning Angel (The Stormbringer).
 *
 * <p>Fires a vector raycast up to a max threshold, identifies the target
 * coordinate and forces a live LightningBolt strike down upon that exact spot.</p>
 */
public class LightningAbility implements Ability {

    private static final double MAX_RANGE_T1 = 10.0;
    private static final double MAX_RANGE_T2 = 18.0;

    @Override
    public AngelElement element() {
        return AngelElement.LIGHTNING;
    }

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerData data, TierData tier) {
        int level = tier.getTier();
        double range = level >= 2 ? MAX_RANGE_T2 : MAX_RANGE_T1;

        Location target = Raycast.targetPoint(player, range);
        LivingEntity direct = Raycast.targetEntity(player, range, 0.7);

        if (level >= 3) {
            // Storm Call: strikes the target 3 times consecutively over 1.5s.
            for (int i = 0; i < 3; i++) {
                final Location strike = target.clone();
                plugin.getServer().getScheduler().runTaskLater(plugin,
                        () -> performStrike(plugin, player, strike, tier), i * 15L);
            }
        } else {
            performStrike(plugin, player, target, tier);
        }

        if (direct != null) {
            plugin.getStatusManager().stun(direct, tier.getDurationTicks());
        }
    }

    private void performStrike(AngelPlugin plugin, Player caster, Location strike, TierData tier) {
        // Live LightningBolt strike (visual) at the exact coordinate.
        strike.getWorld().strikeLightningEffect(strike);

        // Exact audio triggers at maximum volume.
        Sounds.playAt(strike, "entity.lightning_bolt.thunder", 1.0f, 1.0f);
        Sounds.playAt(strike, "entity.lightning_bolt.impact", 1.0f, 1.0f);

        // Particle footprint: energetic ions along a flat horizontal plane (Y +0.1).
        Location plane = strike.clone().add(0, 0.1, 0);
        Particles.spawn(strike.getWorld(), "electric_spark", plane, 50, 0.8, 0.0, 0.8, 0.35);
        Particles.spawn(strike.getWorld(), "glow", plane, 15, 0.6, 0.0, 0.6, 0.05);

        // Vertical, blinding white stroke from sky limit to the floor.
        renderLightningColumn(strike);

        // Screen flash for any player looking directly at the target.
        renderFlash(plugin, strike);

        // Environmental damage + hard stun window.
        for (Entity entity : strike.getWorld().getNearbyEntities(strike, 3.0, 3.0, 3.0)) {
            if (entity instanceof LivingEntity living && !entity.equals(caster)) {
                living.damage(tier.getDamage(), caster);
                plugin.getStatusManager().stun(living, tier.getDurationTicks());
            }
        }
    }

    private void renderLightningColumn(Location strike) {
        int top = strike.getWorld().getMaxHeight();
        for (int y = strike.getBlockY(); y <= top; y += 2) {
            Location point = new Location(strike.getWorld(), strike.getX(), y, strike.getZ());
            Particles.spawn(strike.getWorld(), "end_rod", point, 2, 0.05, 0.05, 0.05, 0.0);
        }
    }

    private void renderFlash(AngelPlugin plugin, Location strike) {
        Vector toStrike = strike.toVector();
        for (Player viewer : plugin.getServer().getOnlinePlayers()) {
            if (!viewer.getWorld().equals(strike.getWorld())) {
                continue;
            }
            if (viewer.getLocation().distanceSquared(strike) > 40 * 40) {
                continue;
            }
            Vector look = viewer.getEyeLocation().getDirection().normalize();
            Vector dir = toStrike.clone().subtract(viewer.getEyeLocation().toVector()).normalize();
            if (look.dot(dir) > 0.7) {
                // Flash particle creates a full-screen white overlay for that viewer.
                Particles.spawn(viewer.getWorld(), "flash", viewer.getEyeLocation(), 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }
}
