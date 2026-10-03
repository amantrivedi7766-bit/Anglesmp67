package com.angelsmp.angel.listener;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.item.ElementalWand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

/**
 * Phase 4 - ability triggers: the F offhand-swap keybind and the custom bound item.
 */
public class AbilityTriggerListener implements Listener {

    private final AngelPlugin plugin;

    public AbilityTriggerListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    /** Pressing F (offhand swap) fires the Active Ability. */
    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        if (profile == null || !profile.hasAlignment()) {
            return;
        }
        event.setCancelled(true);
        plugin.getAbilityManager().useActive(player, profile);
    }

    /** Right-clicking the bound focus item fires the Active Ability. */
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!ElementalWand.isWand(plugin, event.getItem())) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        if (profile == null || !profile.hasAlignment()) {
            return;
        }
        plugin.getAbilityManager().useActive(player, profile);
    }
}
