package com.angelsmp.angel.item;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Phase 6 - the Soul Essence drop (a tagged Nether Star released at the death
 * coordinates when a player's aura shatters).
 */
public final class SoulEssence {

    public static final String KEY_NAME = "soul_essence";

    private SoulEssence() {
    }

    public static NamespacedKey key(AngelPlugin plugin) {
        return new NamespacedKey(plugin, KEY_NAME);
    }

    public static ItemStack create(AngelPlugin plugin, Material material) {
        ItemStack item = ItemBuilder.of(material)
                .name("&d&lSoul Essence")
                .lore(
                        "&7A shard of a shattered aura.",
                        "&7Offered at an altar to reclaim",
                        "&7what was lost.")
                .glow()
                .build();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(key(plugin), PersistentDataType.STRING, "true");
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isSoulEssence(AngelPlugin plugin, ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(key(plugin), PersistentDataType.STRING);
    }
}
