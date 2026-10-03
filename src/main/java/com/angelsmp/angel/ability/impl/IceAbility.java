package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.Ability;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.element.TierData;
import com.angelsmp.angel.player.PlayerData;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Raycast;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * ❄️ Ice Angel (The Cryomancer).
 *
 * <p>Raycasts from the caster's eyes. On an entity hit it stuns the victim and
 * locks their translation; on a water hit it temporarily converts
 * {@code minecraft:water} into {@code minecraft:frosted_ice}.</p>
 */
public class IceAbility implements Ability {

    private static final double RAY_RANGE = 20.0;
    private static final double WATER_FREEZE_RADIUS = 3.0;

    @Override
    public AngelElement element() {
        return AngelElement.ICE;
    }

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerData data, TierData tier) {
        // Activation animation: spinning double-helix cylinder of snow.
        renderDoubleHelix(player);

        // Exact audio trigger: cast.
        Sounds.playAt(player.getLocation(), "block.glass.break", 1.0f, 1.5f);

        int level = tier.getTier();
        long stunTicks = tier.getDurationTicks();
        double damage = tier.getDamage();
        double radius = level >= 3 ? 5.0 : WATER_FREEZE_RADIUS;

        LivingEntity target = Raycast.targetEntity(player, RAY_RANGE, 0.6);
        Location impact;

        if (target != null) {
            impact = target.getLocation();
            if (level >= 3) {
                // Glacial Tomb: freeze every entity within a 5-block radius circle.
                for (Entity entity : target.getWorld().getNearbyEntities(impact, radius, radius, radius)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        freeze(plugin, living, stunTicks, damage);
                    }
                }
            } else {
                freeze(plugin, target, stunTicks, damage);
            }
            if (level >= 2) {
                // Frosty trail: leave Slowness I on nearby enemies.
                for (Entity entity : impact.getWorld().getNearbyEntities(impact, 3.0, 3.0, 3.0)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                                (int) stunTicks + 40, 0, false, true, true));
                    }
                }
            }
        } else {
            impact = Raycast.targetBlock(player, RAY_RANGE);
            if (impact == null) {
                impact = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(RAY_RANGE));
            }
        }

        // Freeze any water around the impact point.
        freezeWater(plugin, impact, (int) radius);

        // Impact freeze footprint.
        renderImpactFreeze(impact);
        Sounds.playAt(impact, "block.powder_snow.break", 1.0f, 1.0f);
        Sounds.playAt(impact, "block.snow.break", 1.0f, 1.0f);
    }

    private void freeze(AngelPlugin plugin, LivingEntity target, long stunTicks, double damage) {
        plugin.getStatusManager().stun(target, stunTicks);
        target.setFreezeTicks(Math.max(target.getFreezeTicks(), (int) stunTicks + 40));
        if (damage > 0.0) {
            target.damage(damage);
        }
        // Ring of frosting blocks binding the victim's feet.
        Location feet = target.getLocation();
        for (int i = 0; i < 16; i++) {
            double theta = (i / 16.0) * Math.PI * 2;
            Location ring = feet.clone().add(Math.cos(theta) * 0.9, 0.05, Math.sin(theta) * 0.9);
            Particles.spawnBlock(target.getWorld(), "block_marker", ring, 1, 0.0, 0.0, 0.0,
                    Material.SNOW_BLOCK.createBlockData());
        }
    }

    private void renderDoubleHelix(Player player) {
        Location base = player.getLocation();
        for (int i = 0; i < 40; i++) {
            double t = i / 40.0;
            double y = t * 2.0;
            double angle = t * Math.PI * 2 * 3;
            double x = Math.cos(angle) * 0.5;
            double z = Math.sin(angle) * 0.5;
            Location point = base.clone().add(x, y, z);
            Particles.spawn(base.getWorld(), "snowball", point, 1, 0.0, 0.0, 0.0, 0.0);
            Particles.spawn(base.getWorld(), "snowflake", point, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private void renderImpactFreeze(Location impact) {
        // 25 breaking snowball chunks at the base + lingering mist.
        for (int i = 0; i < 25; i++) {
            double ox = (Math.random() - 0.5) * 1.4;
            double oz = (Math.random() - 0.5) * 1.4;
            Particles.spawn(impact.getWorld(), "item_snowball", impact.clone().add(ox, 0.1, oz),
                    1, 0.0, 0.0, 0.0, 0.1);
        }
        Particles.spawn(impact.getWorld(), "ambient_entity_effect", impact, 30, 1.0, 0.5, 1.0, 0.02);
    }

    private void freezeWater(AngelPlugin plugin, Location center, int radius) {
        List<Location> frozen = new ArrayList<>();
        int r = Math.max(1, radius);
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    Block block = center.getWorld().getBlockAt(
                            center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z);
                    if (block.getType() == Material.WATER) {
                        block.setType(Material.FROSTED_ICE, false);
                        frozen.add(block.getLocation());
                    }
                }
            }
        }
        if (!frozen.isEmpty()) {
            // Temporarily: restore the water after 5 seconds.
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                for (Location loc : frozen) {
                    Block block = loc.getBlock();
                    if (block.getType() == Material.FROSTED_ICE) {
                        block.setType(Material.WATER, false);
                    }
                }
            }, 100L);
        }
    }
}
