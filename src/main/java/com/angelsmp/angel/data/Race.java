package com.angelsmp.angel.data;

import java.util.Locale;

/** The player's race state: a holy Angel, or a banished Demon. */
public enum Race {

    ANGEL("Angel", "&a"),
    DEMON("Demon", "&c");

    private final String displayName;
    private final String color;

    Race(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public static Race fromString(String name) {
        if (name == null) {
            return ANGEL;
        }
        try {
            return valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return ANGEL;
        }
    }
}
