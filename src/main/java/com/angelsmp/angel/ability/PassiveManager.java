package com.angelsmp.angel.ability;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.Alignment;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Compat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

/**
 * Phase 4 - the permanent Passive Buffs (unlocked at Tier II).
 * Fire Resistance, Water Breathing, Night Vision are applied here; Lightning
 * immunity, Safe Fall and Knockback Resistance are handled in the listeners.
 */
public class PassiveManager {

    private final AngelPlugin plugin;
    private BukkitTask task;

    public PassiveManager(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task != null) {
            return;
        }
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 40L, 40L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        PotionEffectType fireResistance = Compat.effect("FIRE_RESISTANCE");
        PotionEffectType waterBreathing = Compat.effect("WATER_BREATHING");
        PotionEffectType nightVision = Compat.effect("NIGHT_VISION");

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
            if (profile == null || !profile.hasAlignment() || profile.getTier() < 2) {
                continue; // passives unlock at Tier II
            }
            Alignment alignment = profile.getAlignment();
            switch (alignment) {
                case FIRE -> apply(player, fireResistance, 0);
                case ICE -> apply(player, waterBreathing, 0);
                case LIGHT -> apply(player, nightVision, 0);
                default -> {
                    // Wind / Earth / Lightning passives are event-driven.
                }
            }
        }
    }

    private void apply(Player player, PotionEffectType type, int amplifier) {
        if (type == null) {
            return;
        }
        if (!player.hasPotionEffect(type)) {
            // 100 ticks, refreshed every 40 ticks, ambient + no icon.
            player.addPotionEffect(new PotionEffect(type, 100, amplifier, true, false, false));
        }
    }
}
