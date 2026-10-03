package com.angelsmp.angel.util;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/** Vector raycast helpers used by the Ice and Lightning abilities. */
public final class Raycast {

    private Raycast() {
    }

    /** First living entity along the player's line of sight (excluding the caster). */
    public static LivingEntity targetEntity(Player player, double range, double raySize) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        RayTraceResult result = player.getWorld().rayTraceEntities(
                eye, direction, range, raySize, entity -> !entity.equals(player) && entity instanceof LivingEntity);
        if (result == null) {
            return null;
        }
        Entity hit = result.getHitEntity();
        return hit instanceof LivingEntity living ? living : null;
    }

    /** Point the player is looking at, preferring an entity hit, then a block, then max range. */
    public static Location targetPoint(Player player, double range) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        RayTraceResult entityResult = player.getWorld().rayTraceEntities(
                eye, direction, range, 0.5, entity -> !entity.equals(player) && entity instanceof LivingEntity);
        RayTraceResult blockResult = player.getWorld().rayTraceBlocks(eye, direction, range, FluidCollisionMode.NEVER, true);

        double entityDist = entityResult == null ? Double.MAX_VALUE
                : entityResult.getHitPosition().distance(eye.toVector());
        double blockDist = blockResult == null ? Double.MAX_VALUE
                : blockResult.getHitPosition().distance(eye.toVector());

        if (entityDist == Double.MAX_VALUE && blockDist == Double.MAX_VALUE) {
            return eye.clone().add(direction.clone().multiply(range));
        }
        if (entityDist <= blockDist) {
            return entityResult.getHitPosition().toLocation(player.getWorld());
        }
        return blockResult.getHitPosition().toLocation(player.getWorld());
    }
}
