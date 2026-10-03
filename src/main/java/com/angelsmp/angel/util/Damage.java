package com.angelsmp.angel.util;

import org.bukkit.entity.LivingEntity;

/** Damage helpers, including armour-ignoring "true damage". */
public final class Damage {

    private Damage() {
    }

    /**
     * Deals true damage that ignores standard armour protection values,
     * by writing straight to the health pool.
     */
    public static void trueDamage(LivingEntity entity, double amount) {
        if (entity == null || entity.isDead()) {
            return;
        }
        double max = Compat.maxHealth(entity);
        double result = entity.getHealth() - amount;
        entity.setHealth(Math.max(0.0, Math.min(max, result)));
    }
}
