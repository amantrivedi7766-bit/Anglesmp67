package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.ElementalAbility;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Compat;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Raycast;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * ⚡ Lightning - Active Level 1: Storm Caller.
 *
 * <p>Targets a block up to 16 blocks along the line of sight, delivers a purely
 * visual lightning strike (no terrain damage or fire), deals 6 hearts of true
 * damage within 3.5 blocks and applies Blindness for 2 seconds.</p>
 */
public class StormCaller implements ElementalAbility {

    private static final double RANGE = 16.0;
    private static final double RADIUS = 3.5;
    private static final double DAMAGE = 12.0; // 6 hearts

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerProfile profile) {
        Location strike = Raycast.targetPoint(player, RANGE);

        // Purely visual lightning strike (prevents terrain damage and fire).
        strike.getWorld().strikeLightningEffect(strike);

        Sounds.playAt(strike, "entity.lightning_bolt.thunder", 1.0f, 1.0f);
        Sounds.playAt(strike, "entity.lightning_bolt.impact", 1.0f, 1.0f);

        Location plane = strike.clone().add(0, 0.1, 0);
        Particles.spawn(strike.getWorld(), "electric_spark", plane, 50, 0.8, 0.0, 0.8, 0.35);
        Particles.spawn(strike.getWorld(), "glow", plane, 15, 0.6, 0.0, 0.6, 0.05);

        PotionEffectType blindness = Compat.effect("BLINDNESS");
        boolean hitEnemyPlayer = false;

        for (Entity entity : strike.getWorld().getNearbyEntities(strike, RADIUS, RADIUS, RADIUS)) {
            if (!(entity instanceof LivingEntity living) || entity.equals(player)) {
                continue;
            }
            living.damage(DAMAGE, player);
            if (blindness != null) {
                living.addPotionEffect(new PotionEffect(blindness, 40, 0, false, true, true));
            }
            if (living instanceof Player) {
                hitEnemyPlayer = true;
            }
        }

        // Passive - Angelic Fury (Level 2): reward the caster for a landed strike.
        if (hitEnemyPlayer && profile.getLevel() >= 2) {
            plugin.getPassiveManager().triggerAngelicFury(player);
        }
    }
}
