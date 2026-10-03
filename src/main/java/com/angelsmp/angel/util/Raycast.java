package com.angelsmp.angel.util;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/** Vector raycast helpers shared by the Ice and Lightning archetypes. */
public final class Raycast {

    private Raycast() {
    }

    /**
     * Casts a ray from the player's eyes along their facing vector and returns
     * the first living entity it intersects (excluding the caster).
     */
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

    /** Casts a ray from the player's eyes and returns the first block hit location. */
    public static Location targetBlock(Player player, double range) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        World world = player.getWorld();
        RayTraceResult blockResult = world.rayTraceBlocks(eye, direction, range, FluidCollisionMode.NEVER, true);
        if (blockResult != null && blockResult.getHitBlock() != null) {
            return blockResult.getHitBlock().getLocation();
        }
        return null;
    }

    /**
     * Returns the point the player is looking at, preferring an entity hit and
     * falling back to the block hit or the maximum range point.
     */
    public static Location targetPoint(Player player, double range) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        World world = player.getWorld();

        RayTraceResult entityResult = world.rayTraceEntities(
                eye, direction, range, 0.5, entity -> !entity.equals(player) && entity instanceof LivingEntity);
        RayTraceResult blockResult = world.rayTraceBlocks(eye, direction, range, FluidCollisionMode.NEVER, true);

        double entityDist = entityResult == null ? Double.MAX_VALUE : entityResult.getHitPosition()
                .distance(eye.toVector());
        double blockDist = blockResult == null ? Double.MAX_VALUE : blockResult.getHitPosition()
                .distance(eye.toVector());

        if (entityDist == Double.MAX_VALUE && blockDist == Double.MAX_VALUE) {
            return eye.clone().add(direction.clone().multiply(range));
        }
        if (entityDist <= blockDist) {
            return entityResult.getHitPosition().toLocation(world);
        }
        return blockResult.getHitPosition().toLocation(world);
    }
}
