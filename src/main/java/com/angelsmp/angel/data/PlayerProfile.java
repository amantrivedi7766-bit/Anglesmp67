package com.angelsmp.angel.data;

import java.util.UUID;

/**
 * Module 1 - The Live RAM Structure.
 *
 * <p>One profile per Minecraft UUID (never by name). Tracks exactly the seven
 * primitive fields required by the specification, in live memory.</p>
 */
public class PlayerProfile {

    public static final int MIN_LEVEL = 0;
    public static final int MAX_LEVEL = 5;

    private final UUID uuid;

    private Race race = Race.ANGEL;
    private Element element = Element.NONE;
    private int level = 0;
    private int kills = 0;
    private int deaths = 0;

    /** Epoch millis of the last basic (Level 1) ability execution. */
    private long activeAbilityTimestamp = 0L;
    /** Epoch millis of the last ultimate (Level 2) ability execution. */
    private long ultimateAbilityTimestamp = 0L;

    public PlayerProfile(UUID uuid) {
        this.uuid = uuid;
    }

    public PlayerProfile(UUID uuid, Race race, Element element, int level,
                         int kills, int deaths, long activeAbilityTimestamp, long ultimateAbilityTimestamp) {
        this.uuid = uuid;
        this.race = race;
        this.element = element;
        this.level = clampLevel(level);
        this.kills = Math.max(0, kills);
        this.deaths = Math.max(0, deaths);
        this.activeAbilityTimestamp = activeAbilityTimestamp;
        this.ultimateAbilityTimestamp = ultimateAbilityTimestamp;
    }

    public static int clampLevel(int value) {
        return Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, value));
    }

    public UUID getUuid() {
        return uuid;
    }

    public Race getRace() {
        return race;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public boolean isAngel() {
        return race == Race.ANGEL;
    }

    public boolean isDemon() {
        return race == Race.DEMON;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = clampLevel(level);
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

    public long getActiveAbilityTimestamp() {
        return activeAbilityTimestamp;
    }

    public void setActiveAbilityTimestamp(long value) {
        this.activeAbilityTimestamp = value;
    }

    public long getUltimateAbilityTimestamp() {
        return ultimateAbilityTimestamp;
    }

    public void setUltimateAbilityTimestamp(long value) {
        this.ultimateAbilityTimestamp = value;
    }

    /** Module 3 - abilities require an assigned element to fire. */
    public boolean hasElement() {
        return element != null && element != Element.NONE;
    }
}
