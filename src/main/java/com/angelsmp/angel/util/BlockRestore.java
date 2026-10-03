package com.angelsmp.angel.util;

import com.angelsmp.angel.AngelPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Temporarily swaps a set of blocks and restores their original types later. */
public final class BlockRestore {

    private BlockRestore() {
    }

    /**
     * Caches the original material of every block, sets the replacement, then
     * restores the originals after {@code restoreTicks}.
     */
    public static void setTemporary(AngelPlugin plugin, List<Block> blocks, Material replacement, long restoreTicks) {
        Map<Location, Material> originals = new LinkedHashMap<>();
        for (Block block : blocks) {
            originals.put(block.getLocation(), block.getType());
            block.setType(replacement, false);
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Map.Entry<Location, Material> entry : originals.entrySet()) {
                entry.getKey().getBlock().setType(entry.getValue(), false);
            }
        }, restoreTicks);
    }
}
