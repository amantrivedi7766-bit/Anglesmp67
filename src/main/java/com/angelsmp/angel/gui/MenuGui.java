package com.angelsmp.angel.gui;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.player.PlayerData;
import com.angelsmp.angel.util.ItemBuilder;
import com.angelsmp.angel.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * 👥 The Player Menu GUI ({@code /angel menu}).
 *
 * <p>A single 9×3 grid (27 slots) wrapped in an iron/gray-pane frame.</p>
 */
public class MenuGui implements InventoryHolder {

    public static final String TITLE = "§6§lAngel Interface";

    private final AngelPlugin plugin;
    private final Player player;
    private final Inventory inventory;

    public MenuGui(AngelPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 27, Text.color(TITLE));
        build();
    }

    private void build() {
        ItemStack spacer = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();

        // Row 1 (0-8): border window panes.
        for (int i = 0; i <= 8; i++) {
            inventory.setItem(i, spacer);
        }
        // Row 3 (18-26): border window panes.
        for (int i = 18; i <= 26; i++) {
            inventory.setItem(i, spacer);
        }
        // Row 2 side panes: slots 9 and 17.
        inventory.setItem(9, spacer);
        inventory.setItem(17, spacer);

        PlayerData data = plugin.getPlayerManager().get(player.getUniqueId());

        // [P] Slot 10 - Profile Status: the player's own skull.
        inventory.setItem(10, ItemBuilder.skull(player)
                .name("§e§lYOUR PROFILE")
                .lore(
                        "§7Current Angel: §b" + (data.hasElement() ? data.getElement().getColoredName() : "§8None"),
                        "§7Current Progression: §eTier " + data.getTier() + "/3",
                        " ",
                        "§8Your elemental identity on this server.")
                .glow()
                .build());

        // [U] Slot 13 - Evolutionary Upgrade Trigger.
        inventory.setItem(13, ItemBuilder.of(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                .name("§e§lEVOLUTIONARY UPGRADE")
                .lore(
                        "§7Ascend your Angel to the next tier.",
                        "§7Unlock new milestones and power.",
                        " ",
                        "§a▶ Click to open the Upgrade Pipeline.")
                .build());

        // [S] Slot 16 - Stats & Codex Ledger.
        inventory.setItem(16, ItemBuilder.of(Material.BOOK)
                .name("§e§lSTATS & CODEX LEDGER")
                .lore(
                        "§7Detailed stat breakdowns for",
                        "§7every unlocked tier.",
                        " ",
                        "§a▶ Click to open the ledger.")
                .build());
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
