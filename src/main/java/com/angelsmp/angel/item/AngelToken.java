package com.angelsmp.angel.item;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/** Phase 5 - the custom "Angel Token" upgrade currency. */
public final class AngelToken {

    public static final String KEY_NAME = "angel_token";

    private AngelToken() {
    }

    public static NamespacedKey key(AngelPlugin plugin) {
        return new NamespacedKey(plugin, KEY_NAME);
    }

    public static ItemStack create(AngelPlugin plugin) {
        ItemStack item = ItemBuilder.of(Material.GOLD_NUGGET)
                .name("&6&lAngel Token")
                .lore(
                        "&7A token of favour, earned by",
                        "&7completing server challenges.",
                        "&7Spend it to evolve your element.")
                .glow()
                .build();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(key(plugin), PersistentDataType.STRING, "true");
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isToken(AngelPlugin plugin, ItemStack item) {
        if (item == null || item.getType() != Material.GOLD_NUGGET) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(key(plugin), PersistentDataType.STRING);
    }
}
