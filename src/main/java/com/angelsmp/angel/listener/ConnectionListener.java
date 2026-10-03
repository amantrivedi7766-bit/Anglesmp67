package com.angelsmp.angel.listener;

import com.angelsmp.angel.AngelPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Module 1B - The Handshake &amp; Connection Safety Loop.
 * Pre-login async fetch, join map injection, safe exit async save.
 */
public class ConnectionListener implements Listener {

    private final AngelPlugin plugin;

    public ConnectionListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    /** The Login Pre-Check Phase (fires on an async thread while loading). */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        plugin.getProfileManager().preLoad(event.getUniqueId());
    }

    /** The Join Map Injection Phase. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        plugin.getProfileManager().join(event.getPlayer().getUniqueId());
    }

    /** The Safe Exit Phase. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getProfileManager().quit(event.getPlayer().getUniqueId());
    }
}
