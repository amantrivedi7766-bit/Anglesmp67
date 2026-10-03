package com.angelsmp.angel.util;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Fluent builder for GUI icons, including the enchantment-glow wrapper and player skulls. */
public final class ItemBuilder {

    private final ItemStack item;

    private ItemBuilder(ItemStack item) {
        this.item = item;
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(new ItemStack(material));
    }

    public static ItemBuilder of(Material material, int amount) {
        return new ItemBuilder(new ItemStack(material, Math.max(1, Math.min(64, amount))));
    }

    public static ItemBuilder skull(OfflinePlayer owner) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(owner);
        }
        stack.setItemMeta(meta);
        return new ItemBuilder(stack);
    }

    public ItemBuilder name(String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(name));
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder lore(String... lines) {
        return lore(Arrays.asList(lines));
    }

    public ItemBuilder lore(List<String> lines) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> colored = new ArrayList<>();
            for (String line : lines) {
                colored.add(Text.color(line));
            }
            meta.setLore(colored);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(Math.max(1, Math.min(64, amount)));
        return this;
    }

    /**
     * Applies the active enchantment glow wrapper via the modern glint
     * override plus {@link ItemFlag#HIDE_ENCHANTS}. If the running server
     * does not support the glint override, the item is left un-glinted
     * rather than risking an unsafe enchantment reference.
     */
    public ItemBuilder glow() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return this;
        }
        try {
            meta.setEnchantmentGlintOverride(Boolean.TRUE);
        } catch (Throwable ignored) {
            // Glint override unavailable on this API level - skip the glow.
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(flags);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemStack build() {
        return item;
    }
}
