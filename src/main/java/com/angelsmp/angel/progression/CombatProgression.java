package com.angelsmp.angel.progression;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.PlayerProfile;
import org.bukkit.entity.Player;

/**
 * Module 4B, Path A - The Combat Progression Loop (Auto-Upgrades).
 *
 * <p>Hard kill thresholds:
 * <ul>
 *     <li>Level 0 &rarr; Level 1 : 5 kills</li>
 *     <li>Level 1 &rarr; Level 2 : 15 kills</li>
 *     <li>Level 2 &rarr; Level 3 : 30 kills</li>
 * </ul>
 */
public class CombatProgression {

    private final AngelPlugin plugin;

    public CombatProgression(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    /** @return the level a kill total qualifies for (combat path tops out at 3). */
    public static int levelForKills(int kills) {
        if (kills >= 30) {
            return 3;
        }
        if (kills >= 15) {
            return 2;
        }
        if (kills >= 5) {
            return 1;
        }
        return 0;
    }

    /**
     * Registers a kill for the killer and auto-upgrades their level if a
     * threshold was crossed.
     *
     * @return true if a level-up occurred (the caller may then animate).
     */
    public boolean registerKill(Player killer, PlayerProfile killerProfile) {
        int kills = killerProfile.incrementKills();
        int qualified = levelForKills(kills);
        if (qualified > killerProfile.getLevel()) {
            killerProfile.setLevel(qualified);
            plugin.getProfileManager().saveAsync(killerProfile);
            return true;
        }
        return false;
    }
}
