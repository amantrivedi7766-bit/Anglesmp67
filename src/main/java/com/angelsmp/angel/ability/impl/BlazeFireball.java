package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.ElementalAbility;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * 🔥 Fire - Active Level 1: Blaze Fireball.
 *
 * <p>Fires a Ghast fireball from just in front of the face at double speed.
 * The anti-grief explosion safeguard is applied in the impact listener.</p>
 */
public class BlazeFireball implements ElementalAbility {

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerProfile profile) {
        Vector direction = player.getEyeLocation().getDirection().normalize();
        Location spawn = player.getEyeLocation().add(direction.clone().multiply(1.0)); // slightly in front of face

        LargeFireball fireball = player.getWorld().spawn(spawn, LargeFireball.class);
        fireball.setShooter(player);
        fireball.setDirection(direction);
        fireball.setYield(0f);            // anti-grief: zero block-destruction radius
        fireball.setIsIncendiary(false);  // anti-grief: no block ignition
        fireball.setVelocity(direction.clone().multiply(2.0)); // 2x a standard Ghast fireball

        plugin.getAbilityManager().getProjectileTracker().trackBlazeFireball(fireball.getUniqueId());

        Sounds.playAt(player.getLocation(), "item.firecharge.use", 1.0f, 1.0f);
        Sounds.playAt(player.getLocation(), "entity.ghast.shoot", 1.0f, 1.2f);
    }
}
