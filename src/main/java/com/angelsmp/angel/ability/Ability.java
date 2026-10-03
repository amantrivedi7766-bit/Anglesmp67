package com.angelsmp.angel.ability;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.PlayerProfile;
import org.bukkit.entity.Player;

/** Phase 4 - a single castable elemental ability. */
public interface Ability {

    void cast(AngelPlugin plugin, Player player, PlayerProfile profile);
}
