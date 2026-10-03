package com.angelsmp.angel.util;

import org.bukkit.ChatColor;

/** Small helpers for colour-code translation and text formatting. */
public final class Text {

    private Text() {
    }

    /** Translates both legacy '&' codes and passes through '§' codes. */
    public static String color(String input) {
        if (input == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static String strip(String input) {
        return ChatColor.stripColor(color(input));
    }

    /**
     * Builds the draining progress bar used by the action-bar HUD.
     * The filled sub-blocks (█) drop off one by one as time runs out.
     *
     * @param remainingSeconds seconds left
     * @param totalSeconds     total cooldown length
     * @param length           number of sub-blocks
     */
    public static String progressBar(long remainingSeconds, long totalSeconds, int length) {
        if (totalSeconds <= 0) {
            totalSeconds = 1;
        }
        double ratio = Math.max(0.0, Math.min(1.0, (double) remainingSeconds / (double) totalSeconds));
        int filled = (int) Math.round(ratio * length);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(i < filled ? '█' : '▒');
        }
        return builder.toString();
    }
}
