package com.angelsmp.angel.listener;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.player.PlayerData;
import com.angelsmp.angel.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.ThreadLocalRandom;

/** Keeps player profiles loaded and the HUD tidy across sessions. */
public class ConnectionListener implements Listener {

    private final AngelPlugin plugin;

    public ConnectionListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerManager().get(player.getUniqueId());

        // Assign a random element the first time a player joins (configurable).
        if (!data.hasElement() && plugin.getConfig().getBoolean("player.assign-random-on-first-join", true)) {
            AngelElement[] elements = AngelElement.values();
            AngelElement assigned = elements[ThreadLocalRandom.current().nextInt(elements.length)];
            plugin.getPlayerManager().setElement(player.getUniqueId(), assigned);
            player.sendMessage(Text.color("&6&l✦ WELCOME, ANGEL ✦"));
            player.sendMessage(Text.color("&7Your element has been forged: " + assigned.getColoredName()
                    + "&7. Use &e/angel power&7 to unleash it and &e/angel menu&7 to view your profile."));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getHudManager().cleanup(player);
        plugin.getPlayerManager().save();
    }
}
