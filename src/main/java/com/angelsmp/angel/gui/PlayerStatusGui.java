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
 * Phase 5 - The Player Status GUI ({@code /angel power}).
 * 27 slots: the player's head at slot 13 and a barrier power-toggle at slot 22.
 */
public class PlayerStatusGui implements InventoryHolder {

    public static final int SLOT_HEAD = 13;
    public static final int SLOT_TOGGLE = 22;

    private final AngelPlugin plugin;
    private final Player player;
    private final Inventory inventory;

    public PlayerStatusGui(AngelPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 27,
                Text.color(plugin.getConfigManager().titleMainMenu()));
        build();
    }

    private void build() {
        if (plugin.getConfigManager().fillEmptySlots()) {
            ItemStack pane = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
            for (int i = 0; i < 27; i++) {
                inventory.setItem(i, pane);
            }
        }

        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        String alignment = profile == null || !profile.hasAlignment()
                ? "&8None" : profile.getAlignment().getColoredName();
        int kills = profile == null ? 0 : profile.getKills();
        int deaths = profile == null ? 0 : profile.getDeaths();
        int tier = profile == null ? 1 : profile.getTier();
        boolean disabled = profile != null && profile.isPowersDisabled();

        inventory.setItem(SLOT_HEAD, ItemBuilder.skull(player)
                .name("&e&lYOUR PROFILE")
                .lore(
                        "&7Alignment Rank: " + alignment,
                        "&7Total Kills: &f" + kills,
                        "&7Total Deaths: &f" + deaths,
                        "&7Element Level: &eTier " + tier + "/3",
                        "",
                        disabled ? "&cPowers are currently DISABLED." : "&aPowers are ACTIVE.")
                .glow()
                .build());

        inventory.setItem(SLOT_TOGGLE, ItemBuilder.of(Material.BARRIER)
                .name(disabled ? "&a&lENABLE POWERS" : "&c&lDEACTIVATE POWERS")
                .lore(
                        "&7Toggle your elemental powers off to",
                        "&7play purely vanilla Minecraft.",
                        "",
                        disabled ? "&a▶ Click to enable." : "&c▶ Click to deactivate.")
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
