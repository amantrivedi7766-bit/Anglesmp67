package com.angelsmp.angel.gui;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.ItemBuilder;
import com.angelsmp.angel.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Module 5B - The Level Adjuster Sub-GUI.
 * A 9-slot row opened by left-clicking an admin skull icon.
 */
public class LevelAdjusterGui implements InventoryHolder {

    public static final String TITLE = "§8§lLevel Adjuster";

    public static final int SLOT_DECREASE = 2;
    public static final int SLOT_INFO = 4;
    public static final int SLOT_INCREASE = 6;

    private final AngelPlugin plugin;
    private final Player admin;
    private final UUID targetId;
    private final Inventory inventory;

    public LevelAdjusterGui(AngelPlugin plugin, Player admin, UUID targetId) {
        this.plugin = plugin;
        this.admin = admin;
        this.targetId = targetId;
        this.inventory = Bukkit.createInventory(this, 9, Text.color(TITLE));
        build();
    }

    private void build() {
        inventory.setItem(SLOT_DECREASE, ItemBuilder.of(Material.RED_WOOL)
                .name("§c§lDecrease Level (-1)")
                .build());
        inventory.setItem(SLOT_INCREASE, ItemBuilder.of(Material.LIME_WOOL)
                .name("§a§lIncrease Level (+1)")
                .build());
        refreshInfo();
    }

    /** Updates the central barrier block text. */
    public void refreshInfo() {
        PlayerProfile profile = plugin.getProfileManager().get(targetId);
        int level = profile == null ? 0 : profile.getLevel();
        inventory.setItem(SLOT_INFO, ItemBuilder.of(Material.BARRIER)
                .name("§e§lCurrent Level: " + level)
                .lore("§7Target: §f" + name())
                .build());
    }

    private String name() {
        Player target = Bukkit.getPlayer(targetId);
        return target == null ? targetId.toString() : target.getName();
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
