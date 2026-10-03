package com.angelsmp.angel.cooldown;

import com.angelsmp.angel.data.Element;

/** Module 3 - ability cooldown durations (seconds), per element. */
public final class Cooldowns {

    private Cooldowns() {
    }

    /** Level 1 (Active) cooldown in seconds. */
    public static int active(Element element) {
        if (element == null) {
            return 0;
        }
        return switch (element) {
            case FIRE -> 10;
            case ICE -> 15;
            case LIGHTNING -> 12;
            case WIND -> 8;
            case EARTH -> 20;
            case LIGHT -> 18;
            default -> 0;
        };
    }

    /** Level 2 (Ultimate) cooldown in seconds. */
    public static int ultimate(Element element) {
        if (element == null) {
            return 0;
        }
        return switch (element) {
            case FIRE -> 30;
            case EARTH -> 25;
            default -> 0;
        };
    }
}
