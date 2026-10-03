package com.angelsmp.angel.element;

/**
 * Immutable metric block for a single element at a single tier.
 * Values are taken directly from the "Tier Progression Metrics Table"
 * of the technical specification.
 */
public final class TierData {

    private final int tier;
    private final double cooldownSeconds;
    private final double damage;          // HP dealt (or healed for Light)
    private final double durationSeconds; // buff / stun / DoT duration
    private final String damageLabel;     // human readable metric for the GUI ledger
    private final String milestone;       // special tier milestone unlock text

    public TierData(int tier, double cooldownSeconds, double damage, double durationSeconds,
                    String damageLabel, String milestone) {
        this.tier = tier;
        this.cooldownSeconds = cooldownSeconds;
        this.damage = damage;
        this.durationSeconds = durationSeconds;
        this.damageLabel = damageLabel;
        this.milestone = milestone;
    }

    public int getTier() {
        return tier;
    }

    public double getCooldownSeconds() {
        return cooldownSeconds;
    }

    public long getCooldownTicks() {
        return (long) Math.ceil(cooldownSeconds * 20.0);
    }

    public double getDamage() {
        return damage;
    }

    public double getDurationSeconds() {
        return durationSeconds;
    }

    public long getDurationTicks() {
        return (long) Math.ceil(durationSeconds * 20.0);
    }

    public String getDamageLabel() {
        return damageLabel;
    }

    public String getMilestone() {
        return milestone;
    }
}
