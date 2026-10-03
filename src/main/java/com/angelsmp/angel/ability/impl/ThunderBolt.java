package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.Ability;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Damage;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Raycast;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * ⚡ Lightning Angel - Active: Thunder Bolt.
 * A packet lightning strike on the crosshair target up to 15 blocks away:
 * 3 hearts of damage and a 2-second camera/mouse lock. Cooldown 12s.
 */
public class ThunderBolt implements Ability {

    private static final double RANGE = 15.0;

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerProfile profile) {
        Location strike = Raycast.targetPoint(player, RANGE);

        // Packet-based (purely visual) lightning strike - no terrain damage/fire.
        strike.getWorld().strikeLightningEffect(strike);

        // Thunder heard by everyone within 50 blocks.
        Sounds.playNearby(strike, 50.0, "entity.lightning_bolt.thunder", 1.0f, 1.0f);

        Location plane = strike.clone().add(0, 0.1, 0);
        Particles.spawn(strike.getWorld(), "electric_spark", plane, 50, 0.8, 0.0, 0.8, 0.35);
        Particles.spawn(strike.getWorld(), "glow", plane, 15, 0.6, 0.0, 0.6, 0.05);

        double damage = plugin.getConfigManager().lightningDamage()
                + plugin.getAbilityManager().tierDamageBonus(profile.getTier());
        int stunTicks = plugin.getConfigManager().lightningStunSeconds() * 20;

        for (Entity entity : strike.getWorld().getNearbyEntities(strike, 3.5, 3.5, 3.5)) {
            if (!(entity instanceof LivingEntity living) || entity.equals(player)) {
                continue;
            }
            Damage.trueDamage(living, damage);
            if (living instanceof Player victim) {
                // Lock their camera and mouse for the stun window.
                plugin.getControlManager().lockCamera(victim.getUniqueId(),
                        victim.getLocation().getYaw(), victim.getLocation().getPitch(), stunTicks);
                plugin.getControlManager().freeze(victim.getUniqueId(), stunTicks);
            }
        }
    }
}
