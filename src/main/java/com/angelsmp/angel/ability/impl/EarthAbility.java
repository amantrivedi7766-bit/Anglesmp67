package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.Ability;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.element.TierData;
import com.angelsmp.angel.player.PlayerData;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * ⛰️ Earth Angel (The Titan).
 *
 * <p>Injects defence multipliers into the player's live status effect
 * dictionary: Resistance (incoming-damage reduction) plus Absorption
 * (a golden-heart shield).</p>
 */
public class EarthAbility implements Ability {

    @Override
    public AngelElement element() {
        return AngelElement.EARTH;
    }

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerData data, TierData tier) {
        int level = tier.getTier();
        int duration = (int) tier.getDurationTicks();

        // Resistance II (T1) -> Resistance III (T2/T3); Absorption scaled to the shield HP.
        int resistanceAmp = level == 1 ? 1 : 2;
        int absorptionAmp = level - 1; // I -> 4 HP, II -> 8 HP, III -> 12 HP

        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, resistanceAmp, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, duration, absorptionAmp, false, true, true));

        if (level >= 3) {
            // Unmovable Titan: immune to all incoming knockback forces.
            plugin.getStatusManager().setKnockbackImmune(player, duration);
        }

        // Exact audio triggers.
        Sounds.playAt(player.getLocation(), "block.stone.break", 1.0f, 1.0f);
        Sounds.playAt(player.getLocation(), "entity.iron_golem.attack", 1.0f, 1.0f);

        // Buff shell: dense storm of rising dirt/stone fragments.
        renderBuffShell(plugin, player, duration);
    }

    private void renderBuffShell(AngelPlugin plugin, Player player, int duration) {
        final int[] ticks = {0};
        final int total = Math.max(20, duration);
        final org.bukkit.scheduler.BukkitTask[] holder = new org.bukkit.scheduler.BukkitTask[1];
        holder[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (ticks[0]++ > total || !player.isOnline()) {
                holder[0].cancel();
                return;
            }
            Location base = player.getLocation();
            for (int i = 0; i < 45; i++) {
                double theta = Math.random() * Math.PI * 2;
                double r = 0.6;
                double height = (ticks[0] % 40) / 40.0 * 2.0;
                Location point = base.clone().add(Math.cos(theta) * r, height, Math.sin(theta) * r);
                Material material = Math.random() < 0.5 ? Material.DIRT : Material.STONE;
                Particles.spawnBlock(base.getWorld(), "block_marker", point, 1, 0.0, 0.05, 0.0,
                        material.createBlockData());
            }
        }, 0L, 2L);
    }
}
