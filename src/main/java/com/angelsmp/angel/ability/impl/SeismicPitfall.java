package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.ElementalAbility;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.BlockRestore;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Raycast;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * ⛰️ Earth - Ultimate Level 2: Seismic Pitfall.
 *
 * <p>Targets an enemy within 10 blocks, drops them into a 3-block-deep pit by
 * clearing the 2x2 floor under their feet, blocks them from building out, then
 * restores the floor after 4 seconds.</p>
 */
public class SeismicPitfall implements ElementalAbility {

    private static final double RANGE = 10.0;
    private static final int TRAP_TICKS = 80; // 4 seconds

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerProfile profile) {
        LivingEntity target = Raycast.targetEntity(player, RANGE, 0.6);
        if (target == null) {
            player.sendMessage(com.angelsmp.angel.util.Text.color("&c&l✦ No target within 10 blocks."));
            return;
        }

        Location feet = target.getLocation();
        int baseX = feet.getBlockX();
        int baseY = feet.getBlockY() - 1;
        int baseZ = feet.getBlockZ();

        List<Block> floor = new ArrayList<>();
        for (int x = 0; x <= 1; x++) {
            for (int z = 0; z <= 1; z++) {
                for (int depth = 0; depth < 3; depth++) {
                    Block block = feet.getWorld().getBlockAt(baseX + x, baseY - depth, baseZ + z);
                    if (block.getType().isSolid()) {
                        floor.add(block);
                    }
                }
            }
        }
        if (!floor.isEmpty()) {
            BlockRestore.setTemporary(plugin, floor, Material.AIR, TRAP_TICKS);
        }

        // The Escape Block Blocker: stop the trapped player placing blocks.
        plugin.getPassiveManager().trapInPit(target.getUniqueId());

        // Once the 4 seconds are up, teleport them safely back to the surface.
        final Location safeSpot = feet.clone().add(0, 1, 0);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getPassiveManager().releaseFromPit(target.getUniqueId());
            if (target.isValid()) {
                target.teleport(safeSpot);
            }
        }, TRAP_TICKS);

        Sounds.playAt(feet, "block.stone.break", 1.0f, 0.6f);
        Sounds.playAt(feet, "entity.iron_golem.attack", 1.0f, 0.8f);
        Particles.spawn(feet.getWorld(), "block_crumble", feet, 40, 1.5, 0.5, 1.5, 0.1);
    }
}
