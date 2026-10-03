package com.angelsmp.angel.ability;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.impl.EarthAbility;
import com.angelsmp.angel.ability.impl.FireAbility;
import com.angelsmp.angel.ability.impl.IceAbility;
import com.angelsmp.angel.ability.impl.LightAbility;
import com.angelsmp.angel.ability.impl.LightningAbility;
import com.angelsmp.angel.ability.impl.WindAbility;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.element.TierData;
import com.angelsmp.angel.player.PlayerData;
import com.angelsmp.angel.util.Text;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.Map;

/** Registry of the six archetype abilities plus cooldown gating. */
public class AbilityManager {

    private final AngelPlugin plugin;
    private final Map<AngelElement, Ability> abilities = new EnumMap<>(AngelElement.class);

    public AbilityManager(AngelPlugin plugin) {
        this.plugin = plugin;
        register(new FireAbility());
        register(new IceAbility());
        register(new LightningAbility());
        register(new WindAbility());
        register(new EarthAbility());
        register(new LightAbility());
    }

    private void register(Ability ability) {
        abilities.put(ability.element(), ability);
    }

    public Ability get(AngelElement element) {
        return abilities.get(element);
    }

    /**
     * Attempts to activate the player's current element ability.
     *
     * @return true if the ability fired, false if it was on cooldown / unassigned.
     */
    public boolean activate(Player player, PlayerData data) {
        if (!data.hasElement()) {
            player.sendMessage(Text.color("&c&l✦ You have no Angel element assigned. Use &e/angel menu&c to view your profile."));
            return false;
        }
        AngelElement element = data.getElement();
        Ability ability = abilities.get(element);
        if (ability == null) {
            return false;
        }
        if (!plugin.getCooldownManager().isReady(player.getUniqueId())) {
            long remaining = plugin.getCooldownManager().remainingSeconds(player.getUniqueId());
            player.sendMessage(Text.color("&c&l⏳ Ability on cooldown: &e" + remaining + "s"));
            return false;
        }
        TierData tier = data.getCurrentTierData();
        if (tier == null) {
            return false;
        }
        // Start the cooldown before casting so rapid re-triggers cannot slip through.
        plugin.getCooldownManager().startSeconds(player.getUniqueId(), tier.getCooldownSeconds(),
                (long) Math.ceil(tier.getCooldownSeconds()));
        ability.cast(plugin, player, data, tier);
        return true;
    }

    public Map<AngelElement, Ability> getAbilities() {
        return abilities;
    }
}
