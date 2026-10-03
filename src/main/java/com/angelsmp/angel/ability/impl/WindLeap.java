package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.ElementalAbility;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * 💨 Wind - Active Level 1: Wind Leap.
 *
 * <p>Multiplies the horizontal look vector by 2.2 and forces Y to a fixed 1.1,
 * launching the player high into the air.</p>
 */
public class WindLeap implements ElementalAbility {

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerProfile profile) {
        Vector direction = player.getLocation().getDirection();
        Vector velocity = new Vector(direction.getX() * 2.2, 1.1, direction.getZ() * 2.2);
        player.setVelocity(velocity);

        // A blast of 50 cloud particles at their feet + a loud rocket launch sound.
        Location feet = player.getLocation();
        Particles.spawn(feet.getWorld(), "cloud", feet, 50, 0.6, 0.1, 0.6, 0.05);
        Sounds.playAt(feet, "item.trident.riptide", 1.0f, 1.0f);
        Sounds.playAt(feet, "entity.phantom.flap", 1.0f, 1.2f);

        // Passive - Aerodynamic Descent (Level 2): exempt this leap from fall damage.
        if (profile.getLevel() >= 2) {
            plugin.getPassiveManager().addFallExempt(player.getUniqueId());
        }
    }
}
