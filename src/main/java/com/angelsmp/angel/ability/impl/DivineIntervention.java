package com.angelsmp.angel.ability.impl;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.ability.ElementalAbility;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Compat;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Raycast;
import com.angelsmp.angel.util.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * ☀️ Light - Active Level 1: Divine Intervention.
 *
 * <p>Projects an 8-block targeting line. A friendly player in the line is healed;
 * otherwise the caster heals themselves. Grants a flat 4 hearts of true healing.</p>
 */
public class DivineIntervention implements ElementalAbility {

    private static final double RANGE = 8.0;
    private static final double HEAL = 8.0; // 4 hearts

    @Override
    public void cast(AngelPlugin plugin, Player player, PlayerProfile profile) {
        Player recipient = player;

        // If a friendly player is in the line, target them; otherwise default to the caster.
        LivingEntity hit = Raycast.targetEntity(player, RANGE, 0.6);
        if (hit instanceof Player other && !other.equals(player)) {
            recipient = other;
        }

        double max = Compat.maxHealth(recipient);
        recipient.setHealth(Math.min(max, recipient.getHealth() + HEAL));

        Location center = recipient.getLocation().add(0, 1, 0);
        Particles.spawn(recipient.getWorld(), "heart", center, 25, 0.6, 0.6, 0.6, 0.1);
        Particles.spawn(recipient.getWorld(), "totem_of_undying", center, 20, 0.5, 0.7, 0.5, 0.1);
        Sounds.playAt(recipient.getLocation(), "block.note_block.chime", 1.0f, 1.2f);
        Sounds.playAt(recipient.getLocation(), "entity.player.levelup", 1.0f, 1.4f);

        if (!recipient.equals(player)) {
            recipient.sendMessage(com.angelsmp.angel.util.Text.color("&d&l✦ A Divine Intervention heals you!"));
        }
    }
}
