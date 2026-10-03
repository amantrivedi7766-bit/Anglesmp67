package com.angelsmp.angel.ability;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.impl.GigaShield;
import com.angelsmp.angel.ability.impl.GlacialFreeze;
import com.angelsmp.angel.ability.impl.HolyRestoration;
import com.angelsmp.angel.ability.impl.InfernoBlast;
import com.angelsmp.angel.ability.impl.ThunderBolt;
import com.angelsmp.angel.ability.impl.ZephyrLeap;
import com.angelsmp.angel.data.Alignment;
import com.angelsmp.angel.data.PlayerProfile;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.Map;

/**
 * Phase 4 - the elemental ability registry and cast gating.
 * Every cast verifies: an assigned alignment, an unlocked soul, enabled powers,
 * the server-wide combat toggle and an expired cooldown.
 */
public class AbilityManager {

    private final AngelPlugin plugin;
    private final Map<Alignment, Ability> actives = new EnumMap<>(Alignment.class);

    public AbilityManager(AngelPlugin plugin) {
        this.plugin = plugin;
        actives.put(Alignment.FIRE, new InfernoBlast());
        actives.put(Alignment.ICE, new GlacialFreeze());
        actives.put(Alignment.LIGHTNING, new ThunderBolt());
        actives.put(Alignment.WIND, new ZephyrLeap());
        actives.put(Alignment.EARTH, new GigaShield());
        actives.put(Alignment.LIGHT, new HolyRestoration());
    }

    /** Phase 5 - Tier II: damage output +1 heart (2.0 HP). */
    public double tierDamageBonus(int tier) {
        return tier >= 2 ? 2.0 : 0.0;
    }

    /** Phase 5 - Tier II: cooldown reduced by 10%. */
    public double cooldownSeconds(Alignment alignment, int tier) {
        double base = switch (alignment) {
            case FIRE -> plugin.getConfigManager().fireCooldown();
            case ICE -> plugin.getConfigManager().iceCooldown();
            case LIGHTNING -> plugin.getConfigManager().lightningCooldown();
            case WIND -> plugin.getConfigManager().windCooldown();
            case EARTH -> plugin.getConfigManager().earthCooldown();
            case LIGHT -> plugin.getConfigManager().lightCooldown();
            default -> 0;
        };
        return tier >= 2 ? base * 0.9 : base;
    }

    public double remainingCooldown(PlayerProfile profile) {
        double total = cooldownSeconds(profile.getAlignment(), profile.getTier());
        long elapsed = System.currentTimeMillis() - profile.getLastAbilityTimestamp();
        return Math.max(0.0, total - elapsed / 1000.0);
    }

    public boolean isReady(PlayerProfile profile) {
        return remainingCooldown(profile) <= 0.0;
    }

    /** @return true if the active ability fired. */
    public boolean useActive(Player player, PlayerProfile profile) {
        if (profile == null || !profile.hasAlignment()) {
            return false;
        }
        if (profile.isPowersDisabled()) {
            com.angelsmp.angel.util.Compat.sendActionBar(player, plugin.getMessages().get("ability.powers-disabled"));
            return false;
        }
        if (profile.isSoulLocked()) {
            plugin.getHudManager().showLockoutWarning(player, profile);
            return false;
        }
        if (!plugin.isCombatEnabled()) {
            player.sendMessage(plugin.getMessages().get("ability.combat-disabled"));
            return false;
        }
        Ability ability = actives.get(profile.getAlignment());
        if (ability == null) {
            return false;
        }
        double remaining = remainingCooldown(profile);
        if (remaining > 0.0) {
            return false; // the action-bar tracker shows the live countdown
        }
        ability.cast(plugin, player, profile);
        profile.setLastAbilityTimestamp(System.currentTimeMillis());
        plugin.getProfileManager().saveAsync(profile);
        return true;
    }
}
