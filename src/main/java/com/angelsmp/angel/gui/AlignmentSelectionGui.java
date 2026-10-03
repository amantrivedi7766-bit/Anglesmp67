package com.angelsmp.angel.gui;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.Alignment;
import com.angelsmp.angel.util.ItemBuilder;
import com.angelsmp.angel.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * Phase 2 - The Selection GUI (9x3).
 * The player clicks an item to choose their element / alignment.
 */
public class AlignmentSelectionGui implements InventoryHolder {

    public static final String TITLE = "§6§lSELECT YOUR DESTINY";

    public static final int SLOT_FIRE = 10;
    public static final int SLOT_ICE = 11;
    public static final int SLOT_LIGHTNING = 12;
    public static final int SLOT_WIND = 13;
    public static final int SLOT_EARTH = 14;
    public static final int SLOT_LIGHT = 15;
    public static final int SLOT_DEVIL = 16;

    private final AngelPlugin plugin;
    private final Player player;
    private final Inventory inventory;

    public AlignmentSelectionGui(AngelPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 27, Text.color(TITLE));
        build();
    }

    private void build() {
        ItemStack pane = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, pane);
        }
        inventory.setItem(SLOT_FIRE, icon(Alignment.FIRE, "&7Inferno Blast - a burning projectile stream."));
        inventory.setItem(SLOT_ICE, icon(Alignment.ICE, "&7Glacial Freeze - lock enemies in solid ice."));
        inventory.setItem(SLOT_LIGHTNING, icon(Alignment.LIGHTNING, "&7Thunder Bolt - call a blinding strike."));
        inventory.setItem(SLOT_WIND, icon(Alignment.WIND, "&7Zephyr Leap - a 15-block aerial dash."));
        inventory.setItem(SLOT_EARTH, icon(Alignment.EARTH, "&7Giga Shield - an unbreakable defence."));
        inventory.setItem(SLOT_LIGHT, icon(Alignment.LIGHT, "&7Holy Restoration - heal and reveal."));
        inventory.setItem(SLOT_DEVIL, icon(Alignment.DEVIL, "&7The Darkness - a Devil Alignment."));
    }

    private ItemStack icon(Alignment alignment, String description) {
        return ItemBuilder.of(alignment.getIcon())
                .name(alignment.getColor() + "§l" + alignment.getDisplayName().toUpperCase())
                .lore(
                        "&7Guardian Spirit: " + alignment.getColoredName(),
                        "",
                        description,
                        "",
                        "&a▶ Click to choose your destiny.")
                .build();
    }

    public static Alignment alignmentForSlot(int slot) {
        return switch (slot) {
            case SLOT_FIRE -> Alignment.FIRE;
            case SLOT_ICE -> Alignment.ICE;
            case SLOT_LIGHTNING -> Alignment.LIGHTNING;
            case SLOT_WIND -> Alignment.WIND;
            case SLOT_EARTH -> Alignment.EARTH;
            case SLOT_LIGHT -> Alignment.LIGHT;
            case SLOT_DEVIL -> Alignment.DEVIL;
            default -> null;
        };
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open() {
        player.openInventory(inventory);
    }
}
