package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.Ability;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Compat;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

/**
 * ⛰️ Earth Angel - Active: Giga Shield.
 * Encases the caster in a dome of green/brown block-crack particles and grants
 * Resistance II plus 4 golden Absorption hearts for 6 seconds. Cooldown 20s.
 */
public class GigaShield implements Ability {

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerProfile profile) {
        int duration = plugin.getConfigManager().earthShieldSeconds() * 20;

        PotionEffectType resistance = Compat.effect("RESISTANCE", "DAMAGE_RESISTANCE");
        PotionEffectType absorption = Compat.effect("ABSORPTION");
        if (resistance != null) {
            player.addPotionEffect(new PotionEffect(resistance, duration, 1, false, true, true)); // Resistance II
        }
        if (absorption != null) {
            // Absorption II = 8 HP = 4 golden hearts.
            player.addPotionEffect(new PotionEffect(absorption, duration, 1, false, true, true));
        }

        Sounds.playAt(player.getLocation(), "entity.iron_golem.damage", 1.0f, 1.0f);

        // Visible dome of green/brown block-crack particles for the shield window.
        final int[] ticks = {0};
        final BukkitTask[] holder = new BukkitTask[1];
        holder[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || ticks[0]++ > duration) {
                holder[0].cancel();
                return;
            }
            Location center = player.getLocation();
            for (int i = 0; i < 20; i++) {
                double theta = Math.random() * Math.PI * 2;
                double phi = Math.random() * Math.PI;
                double radius = 1.4;
                Location point = center.clone().add(
                        radius * Math.sin(phi) * Math.cos(theta),
                        radius * Math.cos(phi) + 1.0,
                        radius * Math.sin(phi) * Math.sin(theta));
                Material material = Math.random() < 0.5 ? Material.GRASS_BLOCK : Material.DIRT;
                Particles.spawnBlock(point.getWorld(), "block_crumble", point, 1, 0.0, 0.0, 0.0,
                        material.createBlockData());
            }
        }, 0L, 2L);
    }
}
