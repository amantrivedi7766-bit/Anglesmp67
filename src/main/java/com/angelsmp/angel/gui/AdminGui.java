package com.angelsmp.angel.gui;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.ItemBuilder;
import com.angelsmp.angel.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Phase 5 - The Admin Management GUI ({@code /angel admin}).
 * A 54-slot operator-only screen with an online-player head grid.
 */
public class AdminGui implements InventoryHolder {

    private final AngelPlugin plugin;
    private final Player admin;
    private final Inventory inventory;
    private final Map<Integer, UUID> slotToPlayer = new HashMap<>();

    public AdminGui(AngelPlugin plugin, Player admin) {
        this.plugin = plugin;
        this.admin = admin;
        this.inventory = Bukkit.createInventory(this, 54,
                Text.color(plugin.getConfigManager().titleAdminMenu()));
        build();
    }

    private void build() {
        slotToPlayer.clear();
        List<Player> online = List.copyOf(Bukkit.getOnlinePlayers());
        int slot = 0;
        for (Player target : online) {
            if (slot >= 54) {
                break;
            }
            PlayerProfile profile = plugin.getProfileManager().get(target.getUniqueId());
            String alignment = profile == null ? "NONE" : profile.getAlignment().name();
            int tier = profile == null ? 1 : profile.getTier();
            int kills = profile == null ? 0 : profile.getKills();
            int deaths = profile == null ? 0 : profile.getDeaths();

            ItemStack head = ItemBuilder.skull(target)
                    .name("§e" + target.getName())
                    .lore(
                            "§7Target Name: §f" + target.getName(),
                            "§7Alignment: §f" + alignment,
                            "§7Current Level: §eTier " + tier + "/3",
                            "§7Kills: §f" + kills + " §8| §7Deaths: §f" + deaths,
                            "",
                            "§a▶ Click to open the control sub-menu.")
                    .build();
            inventory.setItem(slot, head);
            slotToPlayer.put(slot, target.getUniqueId());
            slot++;
        }
        if (slot == 0) {
            inventory.setItem(22, ItemBuilder.of(org.bukkit.Material.GRAY_DYE)
                    .name("§7No players online").build());
        }
    }

    public void refresh() {
        inventory.clear();
        build();
    }

    public UUID getPlayerAt(int slot) {
        return slotToPlayer.get(slot);
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
