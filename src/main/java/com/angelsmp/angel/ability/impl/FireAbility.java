package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.Ability;
import com.angelsmp.angel.ability.AbilityProjectile;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.element.TierData;
import com.angelsmp.angel.player.PlayerData;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.util.Vector;

/**
 * 🔥 Fire Angel (The Pyromancer).
 *
 * <p>Spawns a localised fireball tied to the player's directional vector.
 * On impact it triggers a non-destructive explosion block event, a flash
 * expansion sphere over a 1.5-block splash radius and a burn DoT.</p>
 */
public class FireAbility implements Ability {

    private static final double BASE_IMPACT_DAMAGE = 4.0; // 2 hearts
    private static final double SPLASH_RADIUS = 1.5;

    @Override
    public AngelElement element() {
        return AngelElement.FIRE;
    }

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerData data, TierData tier) {
        // Exact audio triggers: cast.
        Sounds.playAt(player.getLocation(), "item.firecharge.use", 1.0f, 1.0f);
        Sounds.playAt(player.getLocation(), "entity.ghast.shoot", 1.0f, 1.2f);

        Vector direction = player.getEyeLocation().getDirection().normalize();
        int level = tier.getTier();

        switch (level) {
            case 1 -> launch(plugin, player, direction, SmallFireball.class, 1.4, false, tier);
            case 2 -> launch(plugin, player, direction, LargeFireball.class, 1.1, true, tier);
            default -> {
                // Triple-Threat: three fireballs in a spread cone.
                launch(plugin, player, rotateY(direction, -12), SmallFireball.class, 1.4, false, tier);
                launch(plugin, player, direction, SmallFireball.class, 1.4, false, tier);
                launch(plugin, player, rotateY(direction, 12), SmallFireball.class, 1.4, false, tier);
            }
        }
    }

    private void launch(AngelPlugin plugin, Player player, Vector direction,
                        Class<? extends Fireball> type, double speed, boolean explosive, TierData tier) {
        Vector velocity = direction.clone().multiply(speed);
        Fireball fireball = player.launchProjectile(type, velocity);
        fireball.setIsIncendiary(false);
        fireball.setYield(0f); // block damage handled manually (non-destructive)

        AbilityProjectile tracked = new AbilityProjectile(
                fireball,
                AngelElement.FIRE,
                tier.getTier(),
                player.getUniqueId(),
                BASE_IMPACT_DAMAGE,
                tier.getDamage(),          // DoT: 8 / 12 / 16 HP
                tier.getDurationTicks(),   // 5.0 / 6.5 / 8.0 s
                SPLASH_RADIUS,
                explosive);
        plugin.getProjectileManager().track(tracked);
    }

    private static Vector rotateY(Vector vector, double degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double x = vector.getX() * cos - vector.getZ() * sin;
        double z = vector.getX() * sin + vector.getZ() * cos;
        return new Vector(x, vector.getY(), z).normalize();
    }
}
