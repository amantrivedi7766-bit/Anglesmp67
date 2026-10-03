package com.angelsmp.angel.util;

import org.bukkit.ChatColor;

/** Colour-code translation helpers. */
public final class Text {

    private Text() {
    }

    /** Translates legacy '&' codes and passes through '§' codes. */
    public static String color(String input) {
        if (input == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static String strip(String input) {
        return ChatColor.stripColor(color(input));
    }
}
