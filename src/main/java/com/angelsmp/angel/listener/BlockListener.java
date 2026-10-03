package com.angelsmp.angel.listener;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.util.Text;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Module 3 (Earth) - The Seismic Pitfall Escape Block Blocker.
 * Prevents a trapped player from placing blocks beneath themselves.
 */
public class BlockListener implements Listener {

    private final AngelPlugin plugin;

    public BlockListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (plugin.getPassiveManager().isPitTrapped(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Text.color("&cYou cannot build your way out of the pit!"));
        }
    }
}
