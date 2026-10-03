package com.angelsmp.angel.listener;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.Alignment;
import com.angelsmp.angel.data.PlayerProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

/**
 * Phase 2 &amp; 4 - stasis immunity and the element passives that are event-driven:
 * Lightning immunity, Wind Safe Fall and Earth Knockback Resistance.
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

        // Phase 2 - the player cannot take damage while in Stasis.
        if (plugin.getStasisManager().isInStasis(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        if (profile == null || !profile.hasAlignment()) {
            return;
        }

        // Lightning passive: immune to natural lightning and electric hazards.
        if (profile.getAlignment() == Alignment.LIGHTNING
                && event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
            event.setCancelled(true);
            return;
        }

        // Wind passive (Tier II): Safe Fall up to 30 blocks.
        if (profile.getAlignment() == Alignment.WIND && profile.getTier() >= 2
                && event.getCause() == EntityDamageEvent.DamageCause.FALL
                && player.getFallDistance() <= plugin.getConfigManager().windSafeFallBlocks()) {
            event.setCancelled(true);
            return;
        }

        // Earth passive (Tier II): Knockback Resistance - clamp the push next tick.
        if (profile.getAlignment() == Alignment.EARTH && profile.getTier() >= 2) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (player.isValid()) {
                    Vector velocity = player.getVelocity();
                    player.setVelocity(new Vector(velocity.getX() * 0.2, velocity.getY(), velocity.getZ() * 0.2));
                }
            });
        }
    }
}
