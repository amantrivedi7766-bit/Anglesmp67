package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.Ability;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.element.TierData;
import com.angelsmp.angel.player.PlayerData;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * ☀️ Light Angel (The Seraph).
 *
 * <p>Modifies the player's health attribute, forces an instant health
 * restoration and tags the entity with the glowing outline state.</p>
 */
public class LightAbility implements Ability {

    private static final double HOLY_AURA_RADIUS = 6.0;

    @Override
    public AngelElement element() {
        return AngelElement.LIGHT;
    }

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerData data, TierData tier) {
        int level = tier.getTier();
        int duration = (int) tier.getDurationTicks();
        double heal = tier.getDamage();

        // Instant health injection + lingering effects.
        heal(player, heal);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, level - 1, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration, 0, false, true, true));

        if (level >= 2) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 0, false, true, true));
        }

        if (level >= 3) {
            // Holy Aura: heals all allied players within a 6-block radius.
            for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(),
                    HOLY_AURA_RADIUS, HOLY_AURA_RADIUS, HOLY_AURA_RADIUS)) {
                if (entity instanceof Player ally && !entity.equals(player)) {
                    heal(ally, heal / 2.0);
                    ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 0, false, true, true));
                    Particles.spawn(ally.getWorld(), "totem_of_undying", ally.getLocation().add(0, 1, 0),
                            20, 0.4, 0.6, 0.4, 0.1);
                }
            }
        }

        // Exact audio triggers.
        Sounds.playAt(player.getLocation(), "entity.player.levelup", 1.0f, 1.0f);
        Sounds.playAt(player.getLocation(), "entity.beacon.activate", 1.0f, 1.0f);

        // Healing fountain particle footprint.
        renderHealingFountain(player);
    }

    private void heal(Player player, double amount) {
        var attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double max = attribute == null ? 20.0 : attribute.getValue();
        player.setHealth(Math.min(max, player.getHealth() + amount));
    }

    private void renderHealingFountain(Player player) {
        Location top = player.getLocation().add(0, 2, 0);
        // Waterfall of 60 sparkling golden stars cascading downward.
        for (int i = 0; i < 60; i++) {
            double offsetX = (Math.random() - 0.5) * 1.2;
            double offsetZ = (Math.random() - 0.5) * 1.2;
            double y = -Math.random() * 1.5;
            Location point = top.clone().add(offsetX, y, offsetZ);
            Particles.spawn(player.getWorld(), "totem_of_undying", point, 1, 0.0, 0.0, 0.0, 0.1);
        }
        // Emerald-green happy villager crosses.
        Particles.spawn(player.getWorld(), "happy_villager", top, 20, 0.6, 0.5, 0.6, 0.1);
    }
}
