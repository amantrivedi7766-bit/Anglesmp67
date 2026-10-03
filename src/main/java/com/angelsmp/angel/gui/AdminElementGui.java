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

import java.util.UUID;

/** Phase 5 - the admin element picker (force-change a target's element). */
public class AdminElementGui implements InventoryHolder {

    public static final int SLOT_FIRE = 10;
    public static final int SLOT_ICE = 11;
    public static final int SLOT_LIGHTNING = 12;
    public static final int SLOT_WIND = 13;
    public static final int SLOT_EARTH = 14;
    public static final int SLOT_LIGHT = 15;
    public static final int SLOT_DEVIL = 16;
    public static final int SLOT_NONE = 22;

    private final AngelPlugin plugin;
    private final Player admin;
    private final UUID targetId;
    private final Inventory inventory;

    public AdminElementGui(AngelPlugin plugin, Player admin, UUID targetId) {
        this.plugin = plugin;
        this.admin = admin;
        this.targetId = targetId;
        this.inventory = Bukkit.createInventory(this, 27, Text.color("§4§lForce Element"));
        build();
    }

    private void build() {
        ItemStack pane = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, pane);
        }
        inventory.setItem(SLOT_FIRE, icon(Alignment.FIRE));
        inventory.setItem(SLOT_ICE, icon(Alignment.ICE));
        inventory.setItem(SLOT_LIGHTNING, icon(Alignment.LIGHTNING));
        inventory.setItem(SLOT_WIND, icon(Alignment.WIND));
        inventory.setItem(SLOT_EARTH, icon(Alignment.EARTH));
        inventory.setItem(SLOT_LIGHT, icon(Alignment.LIGHT));
        inventory.setItem(SLOT_DEVIL, icon(Alignment.DEVIL));
        inventory.setItem(SLOT_NONE, icon(Alignment.NONE));
    }

    private ItemStack icon(Alignment alignment) {
        return ItemBuilder.of(alignment.getIcon())
                .name(alignment.getColor() + "§l" + alignment.getDisplayName().toUpperCase())
                .lore("§a▶ Click to assign.")
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
            case SLOT_NONE -> Alignment.NONE;
            default -> null;
        };
    }

    public UUID getTargetId() {
        return targetId;
    }

    public Player getAdmin() {
        return admin;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open() {
        admin.openInventory(inventory);
    }
}
