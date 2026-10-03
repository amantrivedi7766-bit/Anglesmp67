package com.angelsmp.angel.listener;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.PlayerProfile;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Module 7 (movement freeze) and Module 3 (Ice Glacial Path block-change scan).
 */
public class MovementListener implements Listener {

    private final AngelPlugin plugin;

    public MovementListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Module 7 - Input Vector Freeze: cancel horizontal movement for 20 ticks.
        if (plugin.getLevelUpAnimation().isFrozen(player.getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
                Location frozen = from.clone();
                frozen.setYaw(to.getYaw());
                frozen.setPitch(to.getPitch());
                event.setTo(frozen);
            }
            return;
        }

        // Module 3 - Ice passive: Glacial Path, only on a block-coordinate change.
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
            if (profile != null) {
                plugin.getPassiveManager().onBlockChange(player, profile);
            }
        }
    }
}
