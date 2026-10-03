package com.angelsmp.angel.listener;

import com.angelsmp.angel.AngelPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/** Phase 2 - a player in Stasis cannot break or place blocks. */
public class BlockListener implements Listener {

    private final AngelPlugin plugin;

    public BlockListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (plugin.getStasisManager().isInStasis(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (plugin.getStasisManager().isInStasis(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
