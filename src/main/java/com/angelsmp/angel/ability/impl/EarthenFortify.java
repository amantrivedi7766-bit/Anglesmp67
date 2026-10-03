package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.ElementalAbility;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Compat;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * ⛰️ Earth - Active Level 1: Earthen Fortify.
 *
 * <p>Grants Resistance III and Absorption II for 5 seconds, shown by a
 * horizontal shockwave ring of dirt particles at the hip line.</p>
 */
public class EarthenFortify implements ElementalAbility {

    private static final int DURATION_TICKS = 100; // 5 seconds

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerProfile profile) {
        PotionEffectType resistance = Compat.effect("RESISTANCE", "DAMAGE_RESISTANCE");
        PotionEffectType absorption = Compat.effect("ABSORPTION");
        if (resistance != null) {
            player.addPotionEffect(new PotionEffect(resistance, DURATION_TICKS, 2, false, true, true)); // III
        }
        if (absorption != null) {
            player.addPotionEffect(new PotionEffect(absorption, DURATION_TICKS, 1, false, true, true)); // II
        }

        Sounds.playAt(player.getLocation(), "block.stone.break", 1.0f, 1.0f);
        Sounds.playAt(player.getLocation(), "entity.iron_golem.attack", 1.0f, 1.0f);

        // Horizontal shockwave ring of dirt block particles from the hip line.
        Location hip = player.getLocation().add(0, 0.9, 0);
        for (double radius = 0.5; radius <= 3.0; radius += 0.5) {
            for (double theta = 0; theta < Math.PI * 2; theta += 0.35) {
                Location point = hip.clone().add(radius * Math.cos(theta), 0.0, radius * Math.sin(theta));
                Particles.spawnBlock(point.getWorld(), "block_marker", point, 1, 0.0, 0.0, 0.0,
                        Material.DIRT.createBlockData());
            }
        }
    }
}
