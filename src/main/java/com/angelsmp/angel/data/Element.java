package com.angelsmp.angel.data;

import org.bukkit.Material;

import java.util.Locale;

/** The seven elemental states tracked for every profile. */
public enum Element {

    FIRE("Fire", "&c", Material.FIRE_CHARGE),
    ICE("Ice", "&b", Material.PACKED_ICE),
    LIGHTNING("Lightning", "&e", Material.LIGHTNING_ROD),
    WIND("Wind", "&f", Material.FEATHER),
    EARTH("Earth", "&2", Material.ROOTED_DIRT),
    LIGHT("Light", "&d", Material.GLOWSTONE),
    NONE("None", "&8", Material.BARRIER);

    private final String displayName;
    private final String color;
    private final Material icon;

    Element(String displayName, String color, Material icon) {
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
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

    public boolean isNone() {
        return this == NONE;
    }

    public static Element fromString(String name) {
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
