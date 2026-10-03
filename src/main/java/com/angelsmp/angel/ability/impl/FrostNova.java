package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.ElementalAbility;
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
 * ❄️ Ice - Active Level 1: Frost Nova.
 *
 * <p>Projects a 12-block line from the eyes. A caught enemy is frozen for 3
 * seconds (Slowness 10 + Jump Boost 200) and the 3x3 floor beneath them turns to
 * Packed Ice, restoring afterwards.</p>
 */
public class FrostNova implements ElementalAbility {

    private static final double RANGE = 12.0;
    private static final int FREEZE_TICKS = 60; // 3 seconds

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerProfile profile) {
        Sounds.playAt(player.getLocation(), "block.glass.break", 1.0f, 1.5f);

        LivingEntity target = Raycast.targetEntity(player, RANGE, 0.6);
        Location landing = Raycast.targetPoint(player, RANGE);
        if (target != null) {
            landing = target.getLocation();

            // The Deep Freeze mechanic.
            PotionEffectType slowness = Compat.effect("SLOWNESS", "SLOW");
            PotionEffectType jump = Compat.effect("JUMP_BOOST", "JUMP");
            if (slowness != null) {
                target.addPotionEffect(new PotionEffect(slowness, FREEZE_TICKS, 9, false, true, true));
            }
            if (jump != null) {
                target.addPotionEffect(new PotionEffect(jump, FREEZE_TICKS, 199, false, true, true));
            }

            // The Ice Trap visual: 3x3 floor -> Packed Ice, restored after 3s.
            freezeFloor(plugin, target.getLocation());
        }

        // Activation particle footprint.
        for (int i = 0; i < 30; i++) {
            Location point = player.getLocation().add(
                    (Math.random() - 0.5) * 1.5, Math.random() * 2.0, (Math.random() - 0.5) * 1.5);
            Particles.spawn(point.getWorld(), "snowflake", point, 1, 0.0, 0.0, 0.0, 0.0);
        }
        Particles.spawn(landing.getWorld(), "item_snowball", landing, 25, 0.8, 0.2, 0.8, 0.1);
        Particles.spawn(landing.getWorld(), "ambient_entity_effect", landing, 20, 0.8, 0.5, 0.8, 0.02);
        Sounds.playAt(landing, "block.powder_snow.break", 1.0f, 1.0f);
    }

    private void freezeFloor(AngelPlugin plugin, Location feet) {
        List<Block> blocks = new ArrayList<>();
        int baseX = feet.getBlockX();
        int baseY = feet.getBlockY() - 1;
        int baseZ = feet.getBlockZ();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block block = feet.getWorld().getBlockAt(baseX + x, baseY, baseZ + z);
                if (block.getType().isSolid()) {
                    blocks.add(block);
                }
            }
        }
        if (!blocks.isEmpty()) {
            BlockRestore.setTemporary(plugin, blocks, Material.PACKED_ICE, FREEZE_TICKS);
        }
    }
}
