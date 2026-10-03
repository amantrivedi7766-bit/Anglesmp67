package com.angelsmp.angel.ability;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.impl.BlazeFireball;
import com.angelsmp.angel.ability.impl.DivineIntervention;
import com.angelsmp.angel.ability.impl.EarthenFortify;
import com.angelsmp.angel.ability.impl.FrostNova;
import com.angelsmp.angel.ability.impl.HellfireDome;
import com.angelsmp.angel.ability.impl.SeismicPitfall;
import com.angelsmp.angel.ability.impl.StormCaller;
import com.angelsmp.angel.ability.impl.WindLeap;
import com.angelsmp.angel.cooldown.Cooldowns;
import com.angelsmp.angel.data.Element;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Text;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.Map;

/**
 * Module 3 - Absolute Elemental Ability Matrix.
 *
 * <p>Gates every cast behind the strict verification steps: an assigned element,
 * the required active level, and an expired cooldown.</p>
 */
public class AbilityManager {

    private final AngelPlugin plugin;
    private final ProjectileTracker projectileTracker = new ProjectileTracker();
    private final Map<Element, ElementalAbility> actives = new EnumMap<>(Element.class);
    private final Map<Element, ElementalAbility> ultimates = new EnumMap<>(Element.class);

    public AbilityManager(AngelPlugin plugin) {
        this.plugin = plugin;
        actives.put(Element.FIRE, new BlazeFireball());
        actives.put(Element.ICE, new FrostNova());
        actives.put(Element.LIGHTNING, new StormCaller());
        actives.put(Element.WIND, new WindLeap());
        actives.put(Element.EARTH, new EarthenFortify());
        actives.put(Element.LIGHT, new DivineIntervention());

        ultimates.put(Element.FIRE, new HellfireDome());
        ultimates.put(Element.EARTH, new SeismicPitfall());
    }

    public ProjectileTracker getProjectileTracker() {
        return projectileTracker;
    }

    // ---- Level 1 (Active) ----------------------------------------------

    public boolean useActive(Player player, PlayerProfile profile) {
        if (!canUse(player, profile, 1)) {
            return false;
        }
        ElementalAbility ability = actives.get(profile.getElement());
        if (ability == null) {
            player.sendMessage(Text.color("&c&l✦ Your element has no Level 1 ability."));
            return false;
        }
        long now = System.currentTimeMillis();
        if (now < profile.getActiveAbilityTimestamp() + Cooldowns.active(profile.getElement()) * 1000L) {
            return false; // still cooling down (the action-bar tracker shows the timer)
        }
        ability.cast(plugin, player, profile);
        profile.setActiveAbilityTimestamp(now);
        plugin.getProfileManager().saveAsync(profile);
        return true;
    }

    // ---- Level 2 (Ultimate) --------------------------------------------

    public boolean useUltimate(Player player, PlayerProfile profile) {
        if (!canUse(player, profile, 2)) {
            return false;
        }
        ElementalAbility ability = ultimates.get(profile.getElement());
        if (ability == null) {
            player.sendMessage(Text.color("&c&l✦ Your element has no Ultimate ability."));
            return false;
        }
        long now = System.currentTimeMillis();
        if (now < profile.getUltimateAbilityTimestamp() + Cooldowns.ultimate(profile.getElement()) * 1000L) {
            return false;
        }
        ability.cast(plugin, player, profile);
        profile.setUltimateAbilityTimestamp(now);
        plugin.getProfileManager().saveAsync(profile);
        return true;
    }

    /** Shared verification: assigned element + required level. */
    private boolean canUse(Player player, PlayerProfile profile, int requiredLevel) {
        if (profile == null || !profile.hasElement()) {
            return false;
        }
        if (profile.getLevel() < requiredLevel) {
            player.sendMessage(Text.color("&c&l✦ You need Level " + requiredLevel + " to use this ability."));
            return false;
        }
        return true;
    }
}
