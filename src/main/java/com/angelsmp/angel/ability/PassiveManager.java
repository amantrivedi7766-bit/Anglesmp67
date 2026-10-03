package com.angelsmp.angel.ability;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.Element;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Compat;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Module 3 - the element passives that are event- or loop-driven:
 * Glacial Path, Aerodynamic Descent, Angelic Fury and Purifying Beacon.
 */
public class PassiveManager {

    private final AngelPlugin plugin;

    /** Module 3 (Wind) - the Aerodynamic Descent exempt list. */
    private final Set<UUID> fallExempt = ConcurrentHashMap.newKeySet();
    /** Module 3 (Earth) - players trapped by a Seismic Pitfall (cannot build out). */
    private final Set<UUID> pitTrapped = ConcurrentHashMap.newKeySet();

    private BukkitTask beaconTask;

    public PassiveManager(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    // ---- Loop task: Purifying Beacon + Glacial Path scanning -----------

    public void start() {
        if (beaconTask != null) {
            return;
        }
        // Purifying Beacon runs every 3 seconds (60 ticks).
        beaconTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::purifyingBeaconTick, 60L, 60L);
    }

    public void stop() {
        if (beaconTask != null) {
            beaconTask.cancel();
            beaconTask = null;
        }
        fallExempt.clear();
        pitTrapped.clear();
    }

    // ---- Aerodynamic Descent -------------------------------------------

    public void addFallExempt(UUID uuid) {
        fallExempt.add(uuid);
    }

    /** @return true if the player was on the list (and is now removed). */
    public boolean consumeFallExempt(UUID uuid) {
        return fallExempt.remove(uuid);
    }

    // ---- Seismic Pitfall escape blocker --------------------------------

    public void trapInPit(UUID uuid) {
        pitTrapped.add(uuid);
    }

    public void releaseFromPit(UUID uuid) {
        pitTrapped.remove(uuid);
    }

    public boolean isPitTrapped(UUID uuid) {
        return pitTrapped.contains(uuid);
    }

    // ---- Angelic Fury --------------------------------------------------

    /** Lightning passive: the caster gains Speed II + Strength I for 6s. */
    public void triggerAngelicFury(Player caster) {
        PotionEffectType speed = Compat.effect("SPEED");
        PotionEffectType strength = Compat.effect("STRENGTH", "INCREASE_DAMAGE");
        if (speed != null) {
            caster.addPotionEffect(new PotionEffect(speed, 120, 1, false, true, true));
        }
        if (strength != null) {
            caster.addPotionEffect(new PotionEffect(strength, 120, 0, false, true, true));
        }
        Sounds.playTo(caster, "entity.lightning_bolt.impact", 1.0f, 1.4f);

        // Electric spark particles orbiting the player model for the buff window.
        final int[] ticks = {0};
        final BukkitTask[] holder = new BukkitTask[1];
        holder[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!caster.isOnline() || ticks[0]++ > 120) {
                holder[0].cancel();
                return;
            }
            Location base = caster.getLocation().add(0, 1, 0);
            double angle = ticks[0] * 0.4;
            for (int i = 0; i < 3; i++) {
                double a = angle + (i * Math.PI * 2 / 3.0);
                Location point = base.clone().add(Math.cos(a) * 0.8, 0.2 * Math.sin(angle), Math.sin(a) * 0.8);
                Particles.spawn(point.getWorld(), "electric_spark", point, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }, 0L, 1L);
    }

    // ---- Glacial Path --------------------------------------------------

    /** Called when a player crosses a block coordinate. */
    public void onBlockChange(Player player, PlayerProfile profile) {
        if (profile == null || profile.getElement() != Element.ICE || profile.getLevel() < 2) {
            return;
        }
        Block standing = player.getLocation().subtract(0, 1, 0).getBlock();
        if (standing.getType() != Material.WATER) {
            return;
        }
        int baseX = standing.getX();
        int baseY = standing.getY();
        int baseZ = standing.getZ();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block block = standing.getWorld().getBlockAt(baseX + x, baseY, baseZ + z);
                if (block.getType() == Material.WATER) {
                    // Frosted Ice naturally cracks and melts away over time.
                    block.setType(Material.FROSTED_ICE, false);
                }
            }
        }
    }

    // ---- Purifying Beacon ----------------------------------------------

    private void purifyingBeaconTick() {
        PotionEffectType regeneration = Compat.effect("REGENERATION");
        PotionEffectType[] negatives = new PotionEffectType[]{
                Compat.effect("POISON"),
                Compat.effect("WITHER"),
                Compat.effect("BLINDNESS"),
                Compat.effect("SLOWNESS", "SLOW"),
                Compat.effect("WEAKNESS"),
                Compat.effect("HUNGER"),
                Compat.effect("NAUSEA", "CONFUSION"),
                Compat.effect("DARKNESS"),
                Compat.effect("UNLUCK"),
                Compat.effect("BAD_OMEN"),
                Compat.effect("LEVITATION")
        };

        for (Player beacon : Bukkit.getOnlinePlayers()) {
            PlayerProfile profile = plugin.getProfileManager().get(beacon.getUniqueId());
            if (profile == null || profile.getElement() != Element.LIGHT || profile.getLevel() < 2) {
                continue;
            }
            Location center = beacon.getLocation();
            Particles.spawn(beacon.getWorld(), "happy_villager", center.clone().add(0, 1, 0), 4, 3.0, 0.5, 3.0, 0.05);
            for (Player ally : beacon.getWorld().getPlayers()) {
                if (ally.getLocation().distanceSquared(center) > 36.0) { // 6-block circle
                    continue;
                }
                for (PotionEffectType negative : negatives) {
                    if (negative != null) {
                        ally.removePotionEffect(negative);
                    }
                }
                if (regeneration != null) {
                    ally.addPotionEffect(new PotionEffect(regeneration, 80, 0, false, true, true));
                }
            }
        }
    }
}
