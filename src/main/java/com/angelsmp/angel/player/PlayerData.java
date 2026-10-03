package com.angelsmp.angel.player;

import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.element.TierData;

import java.util.UUID;

/** In-memory profile for a single player: assigned element + progression tier. */
public class PlayerData {

    private final UUID uuid;
    private AngelElement element;
    private int tier;

    public PlayerData(UUID uuid, AngelElement element, int tier) {
        this.uuid = uuid;
        this.element = element;
        this.tier = Math.max(1, Math.min(AngelElement.MAX_TIER, tier));
    }

    public UUID getUuid() {
        return uuid;
    }

    public AngelElement getElement() {
        return element;
    }

    public void setElement(AngelElement element) {
        this.element = element;
    }

    public boolean hasElement() {
        return element != null;
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = Math.max(1, Math.min(AngelElement.MAX_TIER, tier));
    }

    public void upgrade() {
        setTier(this.tier + 1);
    }

    public boolean isMaxTier() {
        return tier >= AngelElement.MAX_TIER;
    }

    /** @return the metric block for the player's current element + tier, or null if no element. */
    public TierData getCurrentTierData() {
        if (element == null) {
            return null;
        }
        return element.getTier(tier);
    }
}
