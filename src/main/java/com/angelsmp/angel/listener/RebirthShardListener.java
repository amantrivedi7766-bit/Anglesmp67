package com.angelsmp.angel.listener;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.data.Race;
import com.angelsmp.angel.item.RebirthShard;
import com.angelsmp.angel.util.Compat;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Sounds;
import com.angelsmp.angel.util.Text;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

/**
 * Module 2C - The Rebirth Shard &amp; Purification Ritual interaction logic.
 */
public class RebirthShardListener implements Listener {

    private final AngelPlugin plugin;

    public RebirthShardListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        // Verify the hidden metadata tag; if missing, cancel further execution.
        if (!RebirthShard.isShard(plugin, item)) {
            return;
        }
        event.setCancelled(true);

        Player player = event.getPlayer();
        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        if (profile == null) {
            return;
        }

        // 1) Angel Rejection.
        if (profile.isAngel()) {
            Compat.sendActionBar(player, Text.color("§cYou are already an Angel!"));
            return;
        }

        // 2) Demon Consumption: subtract exactly 1 unit from the active hand.
        item.setAmount(item.getAmount() - 1);

        // 3) Profile Restoration: race -> ANGEL, teleport to the Overworld spawn.
        profile.setRace(Race.ANGEL);
        plugin.getProfileManager().saveAsync(profile);

        Location overworld = plugin.getPluginConfig().getOverworldSpawn();
        if (overworld != null) {
            player.teleport(overworld);
        }

        // 4) The Purification Visual Loop.
        purification(player);
    }

    private void purification(Player player) {
        Sounds.playTo(player, "ui.toast.challenge_complete", 1.0f, 1.2f);
        Sounds.playTo(player, "item.totem.use", 1.0f, 1.0f);

        final int[] ticks = {0};
        final BukkitTask[] holder = new BukkitTask[1];
        holder[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || ticks[0]++ >= 60) { // 3 seconds
                holder[0].cancel();
                return;
            }
            Location base = player.getLocation();
            double t = (ticks[0] % 60) / 60.0;
            for (int i = 0; i < 6; i++) {
                double height = (i / 6.0) * 3.0;
                double angle = t * Math.PI * 4 + height * 2.0;
                Location point = base.clone().add(Math.cos(angle) * 0.8, height, Math.sin(angle) * 0.8);
                Particles.spawn(point.getWorld(), "totem_of_undying", point, 1, 0.0, 0.0, 0.0, 0.0);
                Particles.spawn(point.getWorld(), "glow", point, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }, 0L, 1L);
    }
}
