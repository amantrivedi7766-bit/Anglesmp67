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
 * Module 6 - Ability Use &amp; Custom Keybind Execution.
 * Vanilla trigger hooks: F, Shift+F and the Elemental Wand.
 */
public class AbilityTriggerListener implements Listener {

    private final AngelPlugin plugin;

    public AbilityTriggerListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    /** Keybind Methods 1 &amp; 2: the Offhand Swap Hook (F / Shift+F). */
    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        if (profile == null || !profile.hasElement()) {
            return;
        }
        // Intercept so their weapon does not swap slots.
        event.setCancelled(true);

        if (player.isSneaking()) {
            plugin.getAbilityManager().useUltimate(player, profile);
        } else {
            plugin.getAbilityManager().useActive(player, profile);
        }
    }

    /** Keybind Method 3: The Interaction Wand (right-click / Shift + right-click). */
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
        if (profile == null || !profile.hasElement()) {
            return;
        }
        if (player.isSneaking()) {
            plugin.getAbilityManager().useUltimate(player, profile);
        } else {
            plugin.getAbilityManager().useActive(player, profile);
        }
    }
}
