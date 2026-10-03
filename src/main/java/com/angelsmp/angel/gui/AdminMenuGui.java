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
 * Module 5B - The Master Admin Overlord Menu ({@code /smp menu}).
 * 6 rows (54 slots) populated dynamically with online-player heads.
 */
public class AdminMenuGui implements InventoryHolder {

    public static final String TITLE = "§4§lSMP Admin Overlord";

    private final AngelPlugin plugin;
    private final Player admin;
    private final Inventory inventory;
    private final Map<Integer, UUID> slotToPlayer = new HashMap<>();

    public AdminMenuGui(AngelPlugin plugin, Player admin) {
        this.plugin = plugin;
        this.admin = admin;
        this.inventory = Bukkit.createInventory(this, 54, Text.color(TITLE));
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
            String race = profile == null ? "ANGEL" : profile.getRace().name();
            String element = profile == null ? "NONE" : profile.getElement().name();
            int level = profile == null ? 0 : profile.getLevel();

            inventory.setItem(slot, ItemBuilder.skull(target)
                    .name("§e" + target.getName())
                    .lore(
                            "§7Target Name: §f" + target.getName(),
                            "§7Race Type: §f[" + race + "]",
                            "§7Current Element: §f" + element,
                            "§7Current Level: §e" + level,
                            "",
                            "§8Shift-Click: toggle Angel/Demon",
                            "§8Right-Click: clear cooldowns",
                            "§8Left-Click: open Level Adjuster")
                    .build());
            slotToPlayer.put(slot, target.getUniqueId());
            slot++;
        }
    }

    /** Rebuilds the head population (used after a state change). */
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
