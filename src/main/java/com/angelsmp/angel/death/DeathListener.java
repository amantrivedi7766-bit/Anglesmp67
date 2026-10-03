package com.angelsmp.angel.death;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.item.SoulEssence;
import com.angelsmp.angel.util.Compat;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Phase 6 - The Death System.
 * Aura Shatter animation + broadcast + Soul Essence drop on death, then the
 * Fractured Soul state (Weakness I + Slowness I) on respawn and the 15-minute
 * hard lockout timer.
 */
public class DeathListener implements Listener {

    private final AngelPlugin plugin;

    public DeathListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        PlayerProfile profile = plugin.getProfileManager().get(victim.getUniqueId());
        if (profile == null || !profile.hasAlignment()) {
            return;
        }
        profile.incrementDeaths();

        Location location = victim.getLocation();

        // 1. Aura Shatter Animation.
        if (plugin.getConfigManager().shatterParticles()) {
            Particles.spawn(location.getWorld(), "end_rod", location.add(0, 1, 0), 40, 0.6, 0.8, 0.6, 0.1);
            Particles.spawn(location.getWorld(), "soul", location, 30, 0.6, 0.6, 0.6, 0.05);
        }
        Sounds.playAt(location, "block.glass.break", 1.0f, 0.7f);
        Sounds.playAt(location, "entity.wither.spawn", 1.0f, 0.5f);

        // 2. Server Broadcast Message.
        org.bukkit.Bukkit.broadcastMessage(plugin.getMessages().get("death.broadcast",
                "%player%", victim.getName()));

        // 3. Drop Custom Loot (Soul Essence).
        if (plugin.getConfigManager().dropSoulEssence()) {
            Material material = Material.matchMaterial(plugin.getConfigManager().soulEssenceItem());
            if (material == null) {
                material = Material.NETHER_STAR;
            }
            location.getWorld().dropItemNaturally(location, SoulEssence.create(plugin, material));
        }

        // The Hard Lockout Timer.
        long lockoutMillis = plugin.getConfigManager().lockoutMinutes() * 60000L;
        profile.setSoulLockedUntil(System.currentTimeMillis() + lockoutMillis);
        plugin.getProfileManager().saveAsync(profile);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        if (profile == null || !profile.isSoulLocked()) {
            return;
        }
        if (!plugin.getConfigManager().applyWeaknessOnRespawn()) {
            return;
        }
        int duration = plugin.getConfigManager().weaknessSeconds() * 20;
        PotionEffectType weakness = Compat.effect("WEAKNESS");
        PotionEffectType slowness = Compat.effect("SLOWNESS", "SLOW");
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (weakness != null) {
                player.addPotionEffect(new PotionEffect(weakness, duration, 0, false, true, true));
            }
            if (slowness != null) {
                player.addPotionEffect(new PotionEffect(slowness, duration, 0, false, true, true));
            }
            plugin.getHudManager().showLockoutWarning(player, profile);
        }, 1L);
    }
}
