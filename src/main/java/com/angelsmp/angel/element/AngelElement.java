package com.angelsmp.angel.element;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;

import java.util.Locale;

/**
 * The six elemental archetypes of the Angel SMP.
 * Each archetype carries a colour code, a GUI icon, a boss-bar colour and
 * the three-tier progression table from the technical specification.
 */
public enum AngelElement {

    FIRE("Fire", "&c", Material.MAGMA_BLOCK, BarColor.RED,
            new TierData[]{
                    new TierData(1, 10.0, 8.0, 5.0, "8.0 HP DoT", "Shoots a basic single small fireball entity."),
                    new TierData(2, 8.0, 12.0, 6.5, "12.0 HP DoT", "Fireball changes into a full-sized Explosive Ghast Fireball."),
                    new TierData(3, 6.0, 16.0, 8.0, "16.0 HP DoT", "Triple-Threat: Fires 3 Fireballs simultaneously in a spread cone.")
            }),

    ICE("Ice", "&b", Material.PACKED_ICE, BarColor.BLUE,
            new TierData[]{
                    new TierData(1, 15.0, 0.0, 3.0, "0.0 HP / 3.0s Stun", "Freezes a single target entity; 3-block ice radius."),
                    new TierData(2, 13.0, 4.0, 4.5, "4.0 HP / 4.5s Stun", "Leaves a persistent frosty trail under feet (Slowness I to enemies)."),
                    new TierData(3, 10.0, 8.0, 6.0, "8.0 HP / 6.0s Stun", "Glacial Tomb: Freezes all entities within a 5-block radius circle.")
            }),

    LIGHTNING("Lightning", "&e", Material.LIGHTNING_ROD, BarColor.YELLOW,
            new TierData[]{
                    new TierData(1, 12.0, 5.0, 2.0, "Standard Environmental", "Targets a single entity within 10 blocks of sight."),
                    new TierData(2, 10.0, 9.0, 3.0, "+4.0 HP Extra", "Target range expands to 18 blocks out."),
                    new TierData(3, 8.0, 13.0, 4.0, "+8.0 HP Extra", "Storm Call: Strikes the target 3 times consecutively over 1.5s.")
            }),

    WIND("Wind", "&f", Material.FEATHER, BarColor.WHITE,
            new TierData[]{
                    new TierData(1, 8.0, 0.0, 4.0, "0.0 HP Base / 4.0b", "Launches forward; basic 1.2 knockback push."),
                    new TierData(2, 7.0, 3.0, 6.0, "3.0 HP Impact / 6.0b", "Grants Speed II for 3 seconds immediately upon landing."),
                    new TierData(3, 5.0, 6.0, 8.0, "6.0 HP Impact / 8.0b", "Sonic Boom: Completely nullifies all personal fall damage.")
            }),

    EARTH("Earth", "&2", Material.OBSIDIAN, BarColor.GREEN,
            new TierData[]{
                    new TierData(1, 20.0, 4.0, 5.0, "+4.0 HP Shield", "Grants Resistance II and Absorption I profiles."),
                    new TierData(2, 17.0, 8.0, 7.0, "+8.0 HP Shield", "Upgrades stats to Resistance III and Absorption II."),
                    new TierData(3, 14.0, 12.0, 10.0, "+12.0 HP Shield", "Unmovable Titan: Immune to all incoming knockback forces.")
            }),

    LIGHT("Light", "&d", Material.GLOWSTONE, BarColor.PINK,
            new TierData[]{
                    new TierData(1, 18.0, 8.0, 5.0, "Instantly Heals 8.0 HP", "Heals self; grants Regeneration I and Glowing status."),
                    new TierData(2, 15.0, 12.0, 7.0, "Instantly Heals 12.0 HP", "Upgrades stats to Regeneration II and adds Strength I."),
                    new TierData(3, 12.0, 16.0, 9.0, "Instantly Heals 16.0 HP", "Holy Aura: Heals all allied faction/clan members within a 6b radius.")
            });

    private final String displayName;
    private final String colorCode;
    private final Material icon;
    private final BarColor barColor;
    private final TierData[] tiers;

    AngelElement(String displayName, String colorCode, Material icon, BarColor barColor, TierData[] tiers) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.icon = icon;
        this.barColor = barColor;
        this.tiers = tiers;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getColoredName() {
        return colorCode + displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public BarColor getBarColor() {
        return barColor;
    }

    /** @return the metrics for the given 1-based tier (clamped to 1..3). */
    public TierData getTier(int tier) {
        int index = Math.max(1, Math.min(3, tier)) - 1;
        return tiers[index];
    }

    public TierData[] getTiers() {
        return tiers.clone();
    }

    public static final int MAX_TIER = 3;

    public static AngelElement fromString(String name) {
        if (name == null) {
            return null;
        }
        String key = name.trim().toUpperCase(Locale.ROOT);
        for (AngelElement element : values()) {
            if (element.name().equals(key) || element.displayName.toUpperCase(Locale.ROOT).equals(key)) {
                return element;
            }
        }
        return null;
    }
}
