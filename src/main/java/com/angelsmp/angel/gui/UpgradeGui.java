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

/**
 * Phase 5 - The Upgrade GUI (a progression matrix).
 * Tiers I / II / III, paid with Angel Tokens or raw XP levels.
 */
public class UpgradeGui implements InventoryHolder {

    public static final String TITLE = "§d§lEvolve Path";
    public static final int SLOT_TIER1 = 11;
    public static final int SLOT_TIER2 = 13;
    public static final int SLOT_TIER3 = 15;

    private final AngelPlugin plugin;
    private final Player player;
    private final Inventory inventory;

    public UpgradeGui(AngelPlugin plugin, Player player) {
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

        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        int tier = profile == null ? 1 : profile.getTier();
        String currency = currencyText();

        inventory.setItem(SLOT_TIER1, card(Material.EMERALD_BLOCK, "§a§lTIER I - BASE", tier >= 1,
                "&7Standard damage, standard cooldown."));
        inventory.setItem(SLOT_TIER2, card(Material.DIAMOND_BLOCK, "§b§lTIER II - ADVANCED", tier >= 2,
                "&7+1 heart damage, -10% cooldown,",
                "&7and the permanent passive buff."));
        inventory.setItem(SLOT_TIER3, card(Material.NETHERITE_BLOCK, "§5§lTIER III - MASTERY", tier >= 3,
                "&7Unlocks the secondary ultimate passive",
                "&7(e.g. Fire melts Ice freeze fields)."));

        // Cost reminder on the next upgrade.
        int next = Math.min(3, tier + 1);
        if (tier < 3) {
            inventory.setItem(22, ItemBuilder.of(Material.EXPERIENCE_BOTTLE)
                    .name("§e§lUPGRADE COST")
                    .lore("§7Next: §fTier " + next, "§7Price: §e" + currency,
                            "", "§a▶ Click the next tier block to evolve.")
                    .build());
        }
    }

    private ItemStack card(Material material, String name, boolean unlocked, String... lines) {
        java.util.List<String> lore = new java.util.ArrayList<>();
        java.util.Collections.addAll(lore, lines);
        lore.add("");
        lore.add(unlocked ? "&a✔ UNLOCKED" : "&c✘ LOCKED - click to unlock");
        ItemBuilder builder = ItemBuilder.of(material).name(name).lore(lore);
        if (unlocked) {
            builder.glow();
        }
        return builder.build();
    }

    private String currencyText() {
        if ("TOKENS".equalsIgnoreCase(plugin.getConfigManager().upgradeCurrency())) {
            return plugin.getConfigManager().upgradeTokens() + "x Angel Tokens";
        }
        return plugin.getConfigManager().upgradeXpLevels() + " XP levels";
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
