package com.angelsmp.angel.listener;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.gui.AlignmentSelectionGui;
import com.angelsmp.angel.item.ElementalWand;
import com.angelsmp.angel.util.Compat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Phase 1 &amp; 2 - the connection lifecycle and the first-join Welcome / Stasis
 * state that forces a new player to choose their destiny.
 */
public class ConnectionListener implements Listener {

    private final AngelPlugin plugin;

    public ConnectionListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        plugin.getProfileManager().preLoad(event.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = plugin.getProfileManager().join(player.getUniqueId());

        if (!profile.hasAlignment()) {
            // Phase 2 - Welcome State: freeze the player and force the selection.
            plugin.getStasisManager().begin(player.getUniqueId());
            Compat.sendTitle(player, "&6&lSELECT YOUR DESTINY", "&7Choose your Guardian Spirit", 10, 60, 10);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && plugin.getStasisManager().isInStasis(player.getUniqueId())) {
                    new AlignmentSelectionGui(plugin, player).open();
                }
            }, 10L);
        } else {
            // Returning player: make sure they hold their bound focus item.
            ensureWand(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getStasisManager().end(player.getUniqueId());
        plugin.getProfileManager().quit(player.getUniqueId());
    }

    private void ensureWand(Player player) {
        for (org.bukkit.inventory.ItemStack stack : player.getInventory().getContents()) {
            if (ElementalWand.isWand(plugin, stack)) {
                return;
            }
        }
        player.getInventory().addItem(ElementalWand.create(plugin));
    }
}
