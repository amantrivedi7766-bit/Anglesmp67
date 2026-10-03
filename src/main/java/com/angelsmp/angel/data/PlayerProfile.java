package com.angelsmp.angel.data;

import java.util.UUID;

/**
 * Phase 1 - the live profile for a single player (keyed by UUID).
 * Stores alignment, tier, kills, deaths, the soul-lockout timestamp and the
 * power toggle state.
 */
public class PlayerProfile {

    public static final int MIN_TIER = 1;
    public static final int MAX_TIER = 3;

    private final UUID uuid;
    private Alignment alignment = Alignment.NONE;
    private int tier = 1;
    private int kills = 0;
    private int deaths = 0;
    /** Epoch millis until which the soul is locked (Phase 6). 0 = not locked. */
    private long soulLockedUntil = 0L;
    /** Phase 5 - the /angel power barrier toggle. */
    private boolean powersDisabled = false;
    /** Epoch millis of the last ability execution (cooldown state). */
    private long lastAbilityTimestamp = 0L;

    public PlayerProfile(UUID uuid) {
        this.uuid = uuid;
    }

    public PlayerProfile(UUID uuid, Alignment alignment, int tier, int kills, int deaths,
                         long soulLockedUntil, boolean powersDisabled, long lastAbilityTimestamp) {
        this.uuid = uuid;
        this.alignment = alignment;
        this.tier = clampTier(tier);
        this.kills = Math.max(0, kills);
        this.deaths = Math.max(0, deaths);
        this.soulLockedUntil = soulLockedUntil;
        this.powersDisabled = powersDisabled;
        this.lastAbilityTimestamp = lastAbilityTimestamp;
    }

    public static int clampTier(int value) {
        return Math.max(MIN_TIER, Math.min(MAX_TIER, value));
    }

    public UUID getUuid() {
        return uuid;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    public boolean hasAlignment() {
        return alignment != null && alignment != Alignment.NONE;
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = clampTier(tier);
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = Math.max(0, kills);
    }

    public int incrementKills() {
        return ++this.kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = Math.max(0, deaths);
    }

    public int incrementDeaths() {
        return ++this.deaths;
    }

    public long getSoulLockedUntil() {
        return soulLockedUntil;
    }

    public void setSoulLockedUntil(long value) {
        this.soulLockedUntil = value;
    }

    public boolean isSoulLocked() {
        return soulLockedUntil > System.currentTimeMillis();
    }

    public boolean isPowersDisabled() {
        return powersDisabled;
    }

    public void setPowersDisabled(boolean powersDisabled) {
        this.powersDisabled = powersDisabled;
    }

    public long getLastAbilityTimestamp() {
        return lastAbilityTimestamp;
    }

    public void setLastAbilityTimestamp(long value) {
        this.lastAbilityTimestamp = value;
    }

    /** Wipes combat stats (admin sub-menu action). */
    public void wipeStats() {
        this.kills = 0;
        this.deaths = 0;
        this.tier = MIN_TIER;
    }
}
