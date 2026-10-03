package com.angelsmp.angel.data;

import org.bukkit.Material;

import java.util.Locale;

/**
 * Phase 2 - the player's Alignment / Guardian Spirit.
 * Six Angel elements plus the Devil (Darkness) alignment.
 */
public enum Alignment {

    FIRE("Fire Angel", "&c", Material.BLAZE_POWDER, false),
    ICE("Ice Angel", "&b", Material.PACKED_ICE, false),
    LIGHTNING("Lightning Angel", "&e", Material.LIGHTNING_ROD, false),
    WIND("Wind Angel", "&f", Material.FEATHER, false),
    EARTH("Earth Angel", "&2", Material.ROOTED_DIRT, false),
    LIGHT("Light Angel", "&d", Material.GLOWSTONE_DUST, false),
    DEVIL("Devil", "&5", Material.MAGMA_CREAM, true),
    NONE("None", "&8", Material.BARRIER, false);

    private final String displayName;
    private final String color;
    private final Material icon;
    private final boolean devil;

    Alignment(String displayName, String color, Material icon, boolean devil) {
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
        this.devil = devil;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public String getColoredName() {
        return color + displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public boolean isDevil() {
        return devil;
    }

    public boolean isAngel() {
        return !devil && this != NONE;
    }

    public boolean isNone() {
        return this == NONE;
    }

    public static Alignment fromString(String name) {
        if (name == null) {
            return NONE;
        }
        try {
            return valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return NONE;
        }
    }
}
