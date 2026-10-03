package com.angelsmp.angel.ability;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.PlayerProfile;
import org.bukkit.entity.Player;

/** A single castable elemental ability (Level 1 active or Level 2 ultimate). */
public interface ElementalAbility {

    void cast(AngelPlugin plugin, Player player, PlayerProfile profile);
}
