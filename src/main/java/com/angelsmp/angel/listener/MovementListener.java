package com.angelsmp.angel.listener;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.Alignment;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Compat;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Phase 2 (Stasis), Phase 4 (Ice freeze + camera lock, Ice fast-on-ice passive).
 */
public class MovementListener implements Listener {

    private final AngelPlugin plugin;

    public MovementListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        // Phase 2 - Stasis: cannot walk.
        if (plugin.getStasisManager().isInStasis(player.getUniqueId())) {
            Location frozen = from.clone();
            frozen.setYaw(to.getYaw());
            frozen.setPitch(to.getPitch());
            event.setTo(frozen);
            return;
        }

        // Phase 4 - Ice freeze: lock horizontal movement and jumping.
        if (plugin.getControlManager().isFrozen(player.getUniqueId())) {
            if (from.getX() != to.getX() || from.getZ() != to.getZ() || from.getY() != to.getY()) {
                Location locked = from.clone();
                locked.setYaw(to.getYaw());
                locked.setPitch(to.getPitch());
                event.setTo(locked);
            }
            return;
        }

        // Phase 4 - Lightning camera/mouse lock.
        if (plugin.getControlManager().isCameraLocked(player.getUniqueId())) {
            float[] angles = plugin.getControlManager().getLockedAngles(player.getUniqueId());
            if (angles != null) {
                Location locked = to.clone();
                locked.setYaw(angles[0]);
                locked.setPitch(angles[1]);
                event.setTo(locked);
            }
            return;
        }

        // Phase 4 - Ice passive: fast movement while walking on ice blocks.
        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        if (profile != null && profile.getAlignment() == Alignment.ICE && profile.getTier() >= 2) {
            Material below = to.clone().subtract(0, 1, 0).getBlock().getType();
            if (isIce(below)) {
                PotionEffectType speed = Compat.effect("SPEED");
                if (speed != null) {
                    player.addPotionEffect(new PotionEffect(speed, 40, 1, true, false, false));
                }
            }
        }
    }

    private static boolean isIce(Material material) {
        return material == Material.ICE
                || material == Material.PACKED_ICE
                || material == Material.BLUE_ICE
                || material == Material.FROSTED_ICE;
    }
}
