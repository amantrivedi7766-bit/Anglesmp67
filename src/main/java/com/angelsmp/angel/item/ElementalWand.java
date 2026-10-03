package com.angelsmp.angel.item;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Module 6, Keybind Method 3 - The Interaction Wand.
 *
 * <p>A Blaze Rod tagged so right-click (or Shift + right-click) fires the
 * player's Level 1 / Level 2 ability.</p>
 */
public final class ElementalWand {

    public static final String KEY_NAME = "elemental_wand";

    private ElementalWand() {
    }

    public static NamespacedKey key(AngelPlugin plugin) {
        return new NamespacedKey(plugin, KEY_NAME);
    }

    public static ItemStack create(AngelPlugin plugin) {
        ItemStack item = ItemBuilder.of(Material.BLAZE_ROD)
                .name("&eElemental Wand")
                .lore(
                        "&7Right-click &fto cast your &aLevel 1 &fability.",
                        "&7Shift + Right-click &fto cast your &cUltimate&f.",
                        "",
                        "&8Angel SMP ritual focus")
                .glow()
                .build();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(key(plugin), PersistentDataType.STRING, "true");
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isWand(AngelPlugin plugin, ItemStack item) {
        if (item == null || item.getType() != Material.BLAZE_ROD) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(key(plugin), PersistentDataType.STRING);
    }
}
