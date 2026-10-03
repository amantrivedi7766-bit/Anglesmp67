package com.angelsmp.angel.gui;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.config.PluginConfig;
import com.angelsmp.angel.data.PlayerProfile;
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
 * Module 4B, Path B - The Sacrifice Upgrade GUI ({@code /upgrade}).
 * A 9-slot row whose Evolve Path button lists the dynamic material price.
 */
public class UpgradeGui implements InventoryHolder {

    public static final String TITLE = "§d§lEvolve Path";

    public static final int SLOT_EVOLVE = 4;

    private final AngelPlugin plugin;
    private final Player player;
    private final Inventory inventory;

    public UpgradeGui(AngelPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 9, Text.color(TITLE));
        build();
    }

    private void build() {
        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        int level = profile == null ? 0 : profile.getLevel();
        int next = Math.min(PlayerProfile.MAX_LEVEL, level + 1);
        PluginConfig.Cost cost = plugin.getPluginConfig().costFor(next);

        List<String> lore = new ArrayList<>();
        lore.add("§7Current Level: §e" + level);
        lore.add("§7Next Level: §e" + next);
        lore.add("");
        if (level >= PlayerProfile.MAX_LEVEL) {
            lore.add("§aYou have reached the maximum level.");
        } else if (cost != null) {
            lore.add("§7Cost Required:");
            lore.add("§c- " + cost.amount() + "x " + pretty(cost.material().name()));
            lore.add("");
            lore.add("§d▶ Click to sacrifice and evolve.");
        } else {
            lore.add("§cNo cost configured for this level.");
        }

        inventory.setItem(SLOT_EVOLVE, ItemBuilder.of(Material.NETHERITE_BLOCK)
                .name("§d§lEvolve Path")
                .lore(lore)
                .build());
    }

    private static String pretty(String materialName) {
        String lower = materialName.replace('_', ' ').toLowerCase(java.util.Locale.ROOT);
        StringBuilder builder = new StringBuilder();
        for (String word : lower.split(" ")) {
            if (!word.isEmpty()) {
                builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
            }
        }
        return builder.toString().trim();
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
