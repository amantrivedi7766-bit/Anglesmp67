package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.Ability;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.BlockRestore;
import com.angelsmp.angel.util.Compat;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Raycast;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * ❄️ Ice Angel - Active: Glacial Freeze.
 * Scans a 12-block forward line; a caught enemy gets an ice block pattern at
 * their feet and Slowness 255, frozen in place for 3 seconds. Cooldown 15s.
 */
public class GlacialFreeze implements Ability {

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerProfile profile) {
        Sounds.playAt(player.getLocation(), "block.glass.break", 1.0f, 1.5f);

        int range = plugin.getConfigManager().iceRange();
        int freezeTicks = plugin.getConfigManager().iceFreezeSeconds() * 20;

        LivingEntity target = Raycast.targetEntity(player, range, 0.6);
        Location landing = Raycast.targetPoint(player, range);
        if (target != null) {
            landing = target.getLocation();

            // Hardcoded Slowness Level 255 + a movement/jump lock.
            PotionEffectType slowness = Compat.effect("SLOWNESS", "SLOW");
            if (slowness != null) {
                target.addPotionEffect(new PotionEffect(slowness, freezeTicks, 254, false, true, true));
            }
            plugin.getControlManager().freeze(target.getUniqueId(), freezeTicks);
            Sounds.playAt(target.getLocation(), "entity.player.hurt_freeze", 1.0f, 1.0f);

            // Localized ice block pattern surrounding their feet.
            List<Block> ice = new ArrayList<>();
            Location feet = target.getLocation();
            int bx = feet.getBlockX();
            int by = feet.getBlockY();
            int bz = feet.getBlockZ();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) {
                        continue;
                    }
                    Block block = feet.getWorld().getBlockAt(bx + x, by, bz + z);
                    if (!block.getType().isSolid()) {
                        ice.add(block);
                    }
                }
            }
            if (!ice.isEmpty()) {
                BlockRestore.setTemporary(plugin, ice, Material.PACKED_ICE, freezeTicks);
            }
        }

        for (int i = 0; i < 30; i++) {
            Location point = player.getLocation().add(
                    (Math.random() - 0.5) * 1.5, Math.random() * 2.0, (Math.random() - 0.5) * 1.5);
            Particles.spawn(point.getWorld(), "snowflake", point, 1, 0, 0, 0, 0);
        }
        Particles.spawn(landing.getWorld(), "item_snowball", landing, 25, 0.8, 0.2, 0.8, 0.1);
        Particles.spawn(landing.getWorld(), "ambient_entity_effect", landing, 20, 0.8, 0.5, 0.8, 0.02);
    }
}
