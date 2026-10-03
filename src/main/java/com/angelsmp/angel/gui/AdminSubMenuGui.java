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
 * Phase 5 - The Admin Sub-Menu Controls.
 * Opened by clicking a head in the Admin Management GUI.
 */
public class AdminSubMenuGui implements InventoryHolder {

    public static final int SLOT_ELEMENT = 11;
    public static final int SLOT_RESET_COOLDOWN = 12;
    public static final int SLOT_WIPE_STATS = 13;
    public static final int SLOT_LEVEL3 = 14;
    public static final int SLOT_COMBAT_TOGGLE = 15;
    public static final int SLOT_BACK = 22;

    private final AngelPlugin plugin;
    private final Player admin;
    private final UUID targetId;
    private final Inventory inventory;

    public AdminSubMenuGui(AngelPlugin plugin, Player admin, UUID targetId) {
        this.plugin = plugin;
        this.admin = admin;
        this.targetId = targetId;
        this.inventory = Bukkit.createInventory(this, 27, Text.color("§4§lControl Panel"));
        build();
    }

    private void build() {
        ItemStack pane = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, pane);
        }
        PlayerProfile profile = plugin.getProfileManager().get(targetId);
        String name = targetName();

        inventory.setItem(SLOT_ELEMENT, ItemBuilder.of(Material.NETHER_STAR)
                .name("§b§lFORCE-CHANGE ELEMENT")
                .lore("§7Instantly set the target's element.",
                        "§7Current: §f" + (profile == null ? "NONE" : profile.getAlignment().name()),
                        "", "§a▶ Click to pick.")
                .build());

        inventory.setItem(SLOT_RESET_COOLDOWN, ItemBuilder.of(Material.CLOCK)
                .name("§e§lRESET COOLDOWNS")
                .lore("§7Instantly clear the target's ability",
                        "§7cooldowns for testing.", "", "§a▶ Click to reset.")
                .build());

        inventory.setItem(SLOT_WIPE_STATS, ItemBuilder.of(Material.LAVA_BUCKET)
                .name("§c§lWIPE STATS")
                .lore("§7Reset kills, deaths and tier to base.", "", "§a▶ Click to wipe.")
                .build());

        inventory.setItem(SLOT_LEVEL3, ItemBuilder.of(Material.NETHERITE_INGOT)
                .name("§5§lLEVEL UP TO TIER 3")
                .lore("§7Set the target's level straight to Tier 3.", "", "§a▶ Click to grant.")
                .build());

        boolean combat = plugin.isCombatEnabled();
        inventory.setItem(SLOT_COMBAT_TOGGLE, ItemBuilder.of(
                        combat ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE)
                .name(combat ? "§a§lCOMBAT: ON" : "§c§lCOMBAT: OFF")
                .lore("§7Toggles the entire plugin's combat",
                        "§7mechanics server-wide.",
                        "", combat ? "§c▶ Click to turn OFF." : "§a▶ Click to turn ON.")
                .build());

        inventory.setItem(SLOT_BACK, ItemBuilder.of(Material.BARRIER)
                .name("§c◀ Back to Player Grid")
                .lore("§7Target: §f" + name)
                .build());
    }

    private String targetName() {
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
