package com.angelsmp.angel.gui;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.element.TierData;
import com.angelsmp.angel.player.PlayerData;
import com.angelsmp.angel.util.ItemBuilder;
import com.angelsmp.angel.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 📊 Stats &amp; Codex Ledger GUI - reached from the Player Menu.
 * Displays detailed stat breakdowns for every unlocked tier of the
 * player's current element.
 */
public class CodexGui implements InventoryHolder {

    public static final String TITLE = "§6§lStats & Codex Ledger";

    public static final int SLOT_BACK = 49;

    private final AngelPlugin plugin;
    private final Player player;
    private final Inventory inventory;

    public CodexGui(AngelPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, Text.color(TITLE));
        build();
    }

    private void build() {
        ItemStack pane = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, pane);
            inventory.setItem(45 + i, pane);
        }
        inventory.setItem(9, pane);
        inventory.setItem(17, pane);
        inventory.setItem(36, pane);
        inventory.setItem(44, pane);

        PlayerData data = plugin.getPlayerManager().get(player.getUniqueId());
        AngelElement element = data.getElement();

        inventory.setItem(4, ItemBuilder.of(Material.BOOK)
                .name("§6§lANGEL CODEX")
                .lore(
                        "§7Current Angel: " + (element == null ? "§8None" : element.getColoredName()),
                        "§7Progression: §eTier " + data.getTier() + "/3",
                        " ",
                        "§8Stat breakdown for all tiers below.")
                .build());

        int[] slots = {19, 21, 23};
        for (int i = 0; i < 3; i++) {
            int tier = i + 1;
            if (element == null) {
                inventory.setItem(slots[i], ItemBuilder.of(Material.GRAY_DYE).name("§7No element assigned").build());
                continue;
            }
            TierData metrics = element.getTier(tier);
            boolean unlocked = data.getTier() >= tier;
            List<String> lore = new ArrayList<>();
            lore.add("§7Cooldown: §e" + metrics.getCooldownSeconds() + "s");
            lore.add("§7Metric: §e" + metrics.getDamageLabel());
            lore.add("§7Duration: §e" + metrics.getDurationSeconds() + "s");
            lore.add(" ");
            lore.add("§7Milestone:");
            lore.add("§f" + metrics.getMilestone());
            lore.add(" ");
            lore.add(unlocked ? "§a✔ UNLOCKED" : "§c✘ LOCKED");

            ItemBuilder builder = ItemBuilder.of(unlocked ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK)
                    .name(element.getColorCode() + "§lTIER " + tier)
                    .lore(lore);
            if (unlocked) {
                builder.glow();
            }
            inventory.setItem(slots[i], builder.build());
        }

        inventory.setItem(SLOT_BACK, ItemBuilder.of(Material.BARRIER)
                .name("§c◀ Return to Main Interface Screen")
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
