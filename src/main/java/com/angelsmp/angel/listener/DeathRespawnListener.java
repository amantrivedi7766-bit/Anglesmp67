package com.angelsmp.angel.listener;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.Element;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.data.Race;
import com.angelsmp.angel.util.Compat;
import com.angelsmp.angel.util.Coords;
import com.angelsmp.angel.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Module 2 - The Core Dimensional Death Loop &amp; Banishment.
 */
public class DeathRespawnListener implements Listener {

    private final AngelPlugin plugin;

    public DeathRespawnListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    // ---- Module 2A: Step-by-step execution on death --------------------

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        PlayerProfile profile = plugin.getProfileManager().get(victim.getUniqueId());
        if (profile == null) {
            return;
        }
        profile.incrementDeaths();

        // 2) Verify Active Race: only Angels can be demoted.
        boolean demoted = false;
        if (profile.getRace() == Race.ANGEL) {
            profile.setRace(Race.DEMON);   // Set race = DEMON.
            profile.setElement(Element.NONE); // Element Stripper: instantly locks abilities.

            // 3) The Level Penalty Math: max(0, level - 1).
            int newLevel = Math.max(0, profile.getLevel() - 1);
            profile.setLevel(newLevel);
            demoted = true;
        }

        // 4) The Killer Audit.
        Player killer = victim.getKiller();
        if (killer != null && !killer.equals(victim)) {
            PlayerProfile killerProfile = plugin.getProfileManager().get(killer.getUniqueId());
            if (killerProfile != null) {
                boolean leveled = plugin.getCombatProgression().registerKill(killer, killerProfile);
                if (leveled) {
                    plugin.getLevelUpAnimation().play(killer);
                }
            }
        }

        // 5) Dimension Sound Packet: ENTITY_WITHER_SPAWN at pitch 0.4f.
        for (Player online : victim.getWorld().getPlayers()) {
            online.playSound(online.getLocation(), "entity.wither.spawn", 1.0f, 0.4f);
        }

        // 6) The Global Announcement.
        if (demoted) {
            Bukkit.broadcastMessage(Text.color("§c§l☠ " + victim.getName()
                    + " §7has fallen from grace and been transformed into a §c§lDemon§7! §8(Level -1)"));
        } else {
            Bukkit.broadcastMessage(Text.color("§c§l☠ " + victim.getName() + " §7was slain."));
        }

        plugin.getProfileManager().saveAsync(profile);
    }

    // ---- Module 2B: Overriding Dimensional Coordinates -----------------

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        if (profile == null || !profile.isDemon()) {
            return;
        }

        // 2) Bypass Checkpoints: clear vanilla bed spawns / respawn anchors.
        try {
            player.setBedSpawnLocation(null, true);
        } catch (Throwable ignored) {
            // older API - ignore
        }

        // 3) Dimensional Redirection: custom spawn (defaults to the Nether).
        Location target = plugin.getPluginConfig().getSpawnLocation();
        if (target == null) {
            return;
        }

        // 4) The Anti-Suffocation Block Search.
        Location safe = Coords.findSafeSpot(target);
        event.setRespawnLocation(safe);

        // 5) Post-Teleport Visual Effects: Blindness 4s + Darkness 6s, 1 tick later.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            PotionEffectType blindness = Compat.effect("BLINDNESS");
            PotionEffectType darkness = Compat.effect("DARKNESS");
            if (blindness != null) {
                player.addPotionEffect(new PotionEffect(blindness, 80, 0, false, false, false));
            }
            if (darkness != null) {
                player.addPotionEffect(new PotionEffect(darkness, 120, 0, false, false, false));
            }
        }, 1L);
    }
}
