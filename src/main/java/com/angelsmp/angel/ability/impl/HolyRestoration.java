package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.Ability;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Compat;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * ☀️ Light Angel - Active: Holy Restoration.
 * Instantly heals the caster 4 full hearts and emits a 10-block anti-stealth
 * aura that forces the Glowing effect onto hidden / crouched / invisible
 * enemies. Cooldown 18s.
 */
public class HolyRestoration implements Ability {

    private static final double AURA_RADIUS = 10.0;

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerProfile profile) {
        double heal = plugin.getConfigManager().lightHeal();
        double max = Compat.maxHealth(player);
        player.setHealth(Math.min(max, player.getHealth() + heal));

        Sounds.playAt(player.getLocation(), "block.amethyst_block.chime", 1.0f, 1.2f);
        Particles.spawn(player.getWorld(), "heart", player.getLocation().add(0, 1, 0), 20, 0.6, 0.6, 0.6, 0.1);
        Particles.spawn(player.getWorld(), "end_rod", player.getLocation().add(0, 1, 0), 25, 0.5, 0.8, 0.5, 0.05);

        // Anti-Stealth Aura: reveal hidden / crouched / invisible enemies.
        PotionEffectType glowing = Compat.effect("GLOWING");
        PotionEffectType invisibility = Compat.effect("INVISIBILITY");
        if (glowing == null) {
            return;
        }
        for (Player enemy : player.getWorld().getPlayers()) {
            if (enemy.equals(player) || enemy.getLocation().distanceSquared(player.getLocation()) > AURA_RADIUS * AURA_RADIUS) {
                continue;
            }
            boolean hidden = enemy.isSneaking()
                    || (invisibility != null && enemy.hasPotionEffect(invisibility));
            if (hidden) {
                enemy.addPotionEffect(new PotionEffect(glowing, 120, 0, false, true, true));
            }
        }
    }
}
