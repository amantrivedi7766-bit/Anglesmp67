package com.angelsmp.angel.ability;

import com.angelsmp.angel.element.AngelElement;
import org.bukkit.Location;
import org.bukkit.entity.Projectile;

/** Runtime metadata attached to a tracked ability projectile (e.g. a fireball). */
public class AbilityProjectile {

    private final Projectile projectile;
    private final AngelElement element;
    private final int tier;
    private final java.util.UUID ownerId;

    private final double baseDamage;
    private final double dotDamage;
    private final long dotDurationTicks;
    private final double splashRadius;
    private final boolean explosive;

    private Location lastLocation;
    private long spawnTick;

    public AbilityProjectile(Projectile projectile, AngelElement element, int tier, java.util.UUID ownerId,
                             double baseDamage, double dotDamage, long dotDurationTicks,
                             double splashRadius, boolean explosive) {
        this.projectile = projectile;
        this.element = element;
        this.tier = tier;
        this.ownerId = ownerId;
        this.baseDamage = baseDamage;
        this.dotDamage = dotDamage;
        this.dotDurationTicks = dotDurationTicks;
        this.splashRadius = splashRadius;
        this.explosive = explosive;
        this.lastLocation = projectile.getLocation().clone();
    }

    public Projectile getProjectile() {
        return projectile;
    }

    public AngelElement getElement() {
        return element;
    }

    public int getTier() {
        return tier;
    }

    public java.util.UUID getOwnerId() {
        return ownerId;
    }

    public double getBaseDamage() {
        return baseDamage;
    }

    public double getDotDamage() {
        return dotDamage;
    }

    public long getDotDurationTicks() {
        return dotDurationTicks;
    }

    public double getSplashRadius() {
        return splashRadius;
    }

    public boolean isExplosive() {
        return explosive;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location location) {
        this.lastLocation = location;
    }

    public long getSpawnTick() {
        return spawnTick;
    }

    public void setSpawnTick(long spawnTick) {
        this.spawnTick = spawnTick;
    }
}
