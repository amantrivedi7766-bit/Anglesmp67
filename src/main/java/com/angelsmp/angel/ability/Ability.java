package com.angelsmp.angel.ability;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.element.TierData;
import com.angelsmp.angel.player.PlayerData;
import org.bukkit.entity.Player;

/** A single elemental active ability. */
public interface Ability {

    /** The archetype this ability belongs to. */
    AngelElement element();

    /**
     * Executes the ability.
     *
     * @param plugin the owning plugin (scheduler + managers)
     * @param player the casting player
     * @param data   the caster's profile (element + tier)
     * @param tier   the metric block for the caster's current tier
     */
    void cast(AngelPlugin plugin, Player player, PlayerData data, TierData tier);
}
