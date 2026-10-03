package com.angelsmp.angel.hud;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.Alignment;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Compat;
import com.angelsmp.angel.util.Sounds;
import com.angelsmp.angel.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase 5 - The Player Screen Indicators.
 * The top-of-screen boss bar (drains 100% -> 0% with the remaining cooldown) and
 * the cooldown action bar ([■■■■■■□□□□] Cooldown: 4.5 seconds), plus the Phase 6
 * soul-lockout warning and the SOUL RESTORED notification.
 */
public class HudManager {

    private final AngelPlugin plugin;
    private final Map<UUID, BossBar> bossBars = new ConcurrentHashMap<>();
    private final Set<UUID> wasCooling = ConcurrentHashMap.newKeySet();
    private final Set<UUID> wasLocked = ConcurrentHashMap.newKeySet();
    private BukkitTask task;

    public HudManager(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task != null) {
            return;
        }
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 2L, 2L);
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
        wasCooling.clear();
        wasLocked.clear();
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
            if (profile == null || !profile.hasAlignment()) {
                hideBossBar(player);
                continue;
            }

            // Phase 6 - the lockout countdown and the SOUL RESTORED moment.
            if (profile.isSoulLocked()) {
                wasLocked.add(player.getUniqueId());
                hideBossBar(player);
                Compat.sendActionBar(player, lockoutText(profile));
                continue;
            }
            if (wasLocked.remove(player.getUniqueId())) {
                onSoulRestored(player);
            }

            double remaining = plugin.getAbilityManager().remainingCooldown(profile);
            double total = plugin.getAbilityManager().cooldownSeconds(profile.getAlignment(), profile.getTier());

            updateBossBar(player, profile, remaining, total);
            updateActionBar(player, remaining, total);
        }
    }

    // ---- Boss bar ------------------------------------------------------

    private void updateBossBar(Player player, PlayerProfile profile, double remaining, double total) {
        Alignment alignment = profile.getAlignment();
        BossBar bar = bossBars.computeIfAbsent(player.getUniqueId(), id -> {
            BossBar created = Bukkit.createBossBar("", colorFor(alignment), BarStyle.SOLID);
            created.addPlayer(player);
            return created;
        });

        String title = alignment.getColoredName() + " §8| §7Tier §e" + profile.getTier()
                + (remaining > 0 ? " §8| §c" + fmt(remaining) + "s" : " §8| §aREADY");
        bar.setTitle(Text.color(title));
        double progress = total <= 0 ? 1.0 : Math.max(0.0, Math.min(1.0, remaining / total));
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

    // ---- Action bar ----------------------------------------------------

    private void updateActionBar(Player player, double remaining, double total) {
        if (remaining > 0.0) {
            wasCooling.add(player.getUniqueId());
            Compat.sendActionBar(player, "§7[" + bar(remaining, total) + "§7] Cooldown: §c"
                    + fmt(remaining) + " seconds");
        } else {
            Compat.sendActionBar(player, "§a§l[■■■■■■■■■■] ABILITY READY");
            if (wasCooling.remove(player.getUniqueId())) {
                Sounds.playTo(player, "block.note_block.bell", 1.0f, 1.6f);
            }
        }
    }

    /** Builds ■■■■■■□□□□ (filled = remaining fraction). */
    private String bar(double remaining, double total) {
        int filled = total <= 0 ? 10 : (int) Math.ceil(Math.max(0.0, Math.min(1.0, remaining / total)) * 10);
        return "§a" + "■".repeat(filled) + "§7" + "□".repeat(10 - filled);
    }

    // ---- Lockout -------------------------------------------------------

    public void showLockoutWarning(Player player, PlayerProfile profile) {
        Compat.sendActionBar(player, lockoutText(profile));
    }

    private String lockoutText(PlayerProfile profile) {
        long ms = Math.max(0L, profile.getSoulLockedUntil() - System.currentTimeMillis());
        long minutes = ms / 60000L;
        long seconds = (ms % 60000L) / 1000L;
        return "§c❌ Your soul is fractured. Remaining Lockout: §e"
                + minutes + "m " + seconds + "s";
    }

    private void onSoulRestored(Player player) {
        Compat.sendTitle(player, "§6§lSOUL RESTORED", "§7Your powers have returned.", 10, 50, 10);
        Sounds.playTo(player, "block.amethyst_block.chime", 1.0f, 1.4f);
        Sounds.playTo(player, "entity.player.levelup", 1.0f, 1.2f);
    }

    private static BarColor colorFor(Alignment alignment) {
        return switch (alignment) {
            case FIRE -> BarColor.RED;
            case ICE -> BarColor.BLUE;
            case LIGHTNING -> BarColor.YELLOW;
            case WIND -> BarColor.WHITE;
            case EARTH -> BarColor.GREEN;
            case LIGHT -> BarColor.PINK;
            case DEVIL -> BarColor.PURPLE;
            default -> BarColor.WHITE;
        };
    }

    private static String fmt(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
