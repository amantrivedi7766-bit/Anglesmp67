package com.angelsmp.angel.item;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Module 2C - The Rebirth Shard.
 *
 * <p>A Nether Star carrying a hidden, permanent namespaced metadata tag so
 * ordinary vanilla Nether Stars cannot be used to purify a Demon.</p>
 */
public final class RebirthShard {

    public static final String KEY_NAME = "rebirth_shard";

    private RebirthShard() {
    }

    public static NamespacedKey key(AngelPlugin plugin) {
        return new NamespacedKey(plugin, KEY_NAME);
    }

    /** Builds the tagged Rebirth Shard item. */
    public static ItemStack create(AngelPlugin plugin) {
        ItemStack item = ItemBuilder.of(Material.NETHER_STAR)
                .name("&d&lRebirth Shard")
                .lore(
                        "&7A crystallised fragment of divine light.",
                        "&7Right-click while you are a &cDemon&7 to",
                        "&7be purified and returned to the Overworld.",
                        "",
                        "&8Soulbound ritual relic")
                .glow()
                .build();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // The anti-cheat identity marker: hidden namespaced key-value tag.
            meta.getPersistentDataContainer().set(key(plugin), PersistentDataType.STRING, "true");
            item.setItemMeta(meta);
        }
        return item;
    }

    /** @return true if the item carries the hidden Rebirth Shard tag. */
    public static boolean isShard(AngelPlugin plugin, ItemStack item) {
        if (item == null || item.getType() != Material.NETHER_STAR) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(key(plugin), PersistentDataType.STRING);
    }

    /**
     * Registers the shaped crafting grid:
     * <pre>
     *   D N D
     *   N S N
     *   D N D
     * </pre>
     * (4 Diamond Blocks in the corners, 4 Netherite Ingots on the sides,
     * a single Nether Star in the centre.)
     */
    public static void registerRecipe(AngelPlugin plugin) {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "rebirth_shard_recipe");
        try {
            plugin.getServer().removeRecipe(recipeKey);
        } catch (Throwable ignored) {
            // recipe did not exist yet
        }
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, create(plugin));
        recipe.shape("DND", "NSN", "DND");
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('S', Material.NETHER_STAR);
        plugin.getServer().addRecipe(recipe);
    }
}
