package com.angelsmp.angel.hud;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.element.TierData;
import com.angelsmp.angel.player.PlayerData;
import com.angelsmp.angel.util.Compat;
import com.angelsmp.angel.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drives both real-time cooldown interface vectors:
 * the action-bar live counter and the floating boss-bar overlay.
 *
 * <p>The action bar is dispatched through {@link Compat#sendActionBar} so it
 * works on Paper, Spigot and Purpur alike without binding to an Adventure or
 * Paper-only method.</p>
 */
public class HudManager {

    private final AngelPlugin plugin;
    private final Map<UUID, BossBar> bossBars = new ConcurrentHashMap<>();
    private BukkitTask task;

    private final boolean actionBarEnabled;
    private final boolean bossBarEnabled;
    private final BarStyle barStyle;

    public HudManager(AngelPlugin plugin) {
        this.plugin = plugin;
        this.actionBarEnabled = plugin.getConfig().getBoolean("hud.action-bar", true);
        this.bossBarEnabled = plugin.getConfig().getBoolean("hud.boss-bar.enabled", true);
        BarStyle style;
        try {
            style = BarStyle.valueOf(plugin.getConfig().getString("hud.boss-bar.style", "SEGMENTED_10"));
        } catch (IllegalArgumentException ex) {
            style = BarStyle.SEGMENTED_10;
        }
        this.barStyle = style;
    }

    public void start() {
        if (task != null) {
            return;
        }
        long interval = Math.max(1L, plugin.getConfig().getLong("hud.refresh-ticks", 5L));
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, interval, interval);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (BossBar bar : bossBars.values()) {
            bar.removeAll();
        }
        bossBars.clear();
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getPlayerManager().get(player.getUniqueId());
            if (!data.hasElement()) {
                hideBossBar(player);
                continue;
            }
            AngelElement element = data.getElement();
            boolean cooling = plugin.getCooldownManager().isCoolingDown(player.getUniqueId());
            long remaining = plugin.getCooldownManager().remainingSeconds(player.getUniqueId());
            long total = plugin.getCooldownManager().totalSeconds(player.getUniqueId());

            if (actionBarEnabled) {
                updateActionBar(player, cooling, remaining, total);
            }
            if (bossBarEnabled) {
                updateBossBar(player, element, data, cooling, remaining, total);
            }
        }
    }

    private void updateActionBar(Player player, boolean cooling, long remaining, long total) {
        String template;
        if (!cooling) {
            template = plugin.getConfig().getString("messages.action-bar.ready",
                    "&a&l⚡ ANGEL POWER READY &7[Type /angel power]");
        } else {
            template = plugin.getConfig().getString("messages.action-bar.cooldown",
                    "&c&l⏳ ANGEL POWER COOLDOWN: &e%time%s &7[&f%bars%&7]");
            template = template.replace("%time%", String.valueOf(remaining))
                    .replace("%bars%", Text.progressBar(remaining, Math.max(1, total), 12));
        }
        Compat.sendActionBar(player, Text.color(template));
    }

    private void updateBossBar(Player player, AngelElement element, PlayerData data,
                               boolean cooling, long remaining, long total) {
        BossBar bar = bossBars.computeIfAbsent(player.getUniqueId(), id -> {
            BossBar created = Bukkit.createBossBar("", element.getBarColor(), barStyle);
            created.addPlayer(player);
            return created;
        });

        String title = element.getColoredName() + " §8| §7Tier §e" + data.getTier() + "§7/3";
        if (cooling) {
            title += " §8| §c⏳ " + remaining + "s";
        } else {
            title += " §8| §a✔ READY";
        }
        bar.setTitle(Text.color(title));

        double progress = cooling ? Math.max(0.0, Math.min(1.0, (double) remaining / Math.max(1, total))) : 1.0;
        bar.setProgress(progress);

        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }
    }

    private void hideBossBar(Player player) {
        BossBar bar = bossBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }

    /** Removes all HUD state for a player (used on quit). */
    public void cleanup(Player player) {
        hideBossBar(player);
    }
}
