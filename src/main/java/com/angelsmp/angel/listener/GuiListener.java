package com.angelsmp.angel.listener;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.Alignment;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.gui.AdminElementGui;
import com.angelsmp.angel.gui.AdminGui;
import com.angelsmp.angel.gui.AdminSubMenuGui;
import com.angelsmp.angel.gui.AlignmentSelectionGui;
import com.angelsmp.angel.gui.PlayerStatusGui;
import com.angelsmp.angel.gui.UpgradeGui;
import com.angelsmp.angel.item.AngelToken;
import com.angelsmp.angel.item.ElementalWand;
import com.angelsmp.angel.util.Compat;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Sounds;
import com.angelsmp.angel.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/** Phase 2, 5 - every GUI click (all clicks cancelled to prevent stealing). */
public class GuiListener implements Listener {

    private final AngelPlugin plugin;

    public GuiListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof AlignmentSelectionGui gui) {
            event.setCancelled(true);
            handleSelection(event, gui);
        } else if (holder instanceof PlayerStatusGui gui) {
            event.setCancelled(true);
            handleStatus(event, gui);
        } else if (holder instanceof UpgradeGui gui) {
            event.setCancelled(true);
            handleUpgrade(event, gui);
        } else if (holder instanceof AdminGui gui) {
            event.setCancelled(true);
            handleAdmin(event, gui);
        } else if (holder instanceof AdminSubMenuGui gui) {
            event.setCancelled(true);
            handleSubMenu(event, gui);
        } else if (holder instanceof AdminElementGui gui) {
            event.setCancelled(true);
            handleElementPicker(event, gui);
        }
    }

    // ---- Phase 2: Alignment Selection ----------------------------------

    private void handleSelection(InventoryClickEvent event, AlignmentSelectionGui gui) {
        Alignment chosen = AlignmentSelectionGui.alignmentForSlot(event.getRawSlot());
        if (chosen == null) {
            return;
        }
        Player player = gui.getPlayer();
        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        if (profile == null || profile.hasAlignment()) {
            player.closeInventory();
            return;
        }
        profile.setAlignment(chosen);
        plugin.getProfileManager().saveAsync(profile);
        plugin.getStasisManager().end(player.getUniqueId());
        player.closeInventory();

        Sounds.playTo(player, "entity.generic.explode", 1.0f, 1.2f);
        Sounds.playTo(player, "block.amethyst_block.chime", 1.0f, 1.2f);
        Particles.spawn(player.getWorld(), "totem_of_undying", player.getLocation().add(0, 1, 0), 50, 0.6, 0.8, 0.6, 0.1);
        Compat.sendTitle(player, "&6&lALIGNMENT CHOSEN", chosen.getColoredName() + " &7- welcome, Guardian.", 10, 50, 10);
        player.getInventory().addItem(ElementalWand.create(plugin));
        player.sendMessage(Text.color("&a&l✦ You are now " + chosen.getColoredName() + "&a!"));
    }

    // ---- Phase 5: Player Status GUI ------------------------------------

    private void handleStatus(InventoryClickEvent event, PlayerStatusGui gui) {
        if (event.getRawSlot() != PlayerStatusGui.SLOT_TOGGLE) {
            return;
        }
        Player player = gui.getPlayer();
        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        if (profile == null) {
            return;
        }
        profile.setPowersDisabled(!profile.isPowersDisabled());
        plugin.getProfileManager().saveAsync(profile);
        Sounds.playTo(player, "ui.button.click", 1.0f, profile.isPowersDisabled() ? 0.7f : 1.3f);
        player.closeInventory();
        player.sendMessage(Text.color(profile.isPowersDisabled()
                ? "&c&l✦ Powers DEACTIVATED. You are vanilla now."
                : "&a&l✦ Powers REACTIVATED."));
    }

    // ---- Phase 5: Upgrade GUI ------------------------------------------

    private void handleUpgrade(InventoryClickEvent event, UpgradeGui gui) {
        int slot = event.getRawSlot();
        int target;
        if (slot == UpgradeGui.SLOT_TIER2) {
            target = 2;
        } else if (slot == UpgradeGui.SLOT_TIER3) {
            target = 3;
        } else {
            return;
        }
        Player player = gui.getPlayer();
        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        if (profile == null) {
            return;
        }
        if (profile.getTier() >= target) {
            Sounds.playTo(player, "entity.villager.no", 1.0f, 0.8f);
            return;
        }
        if (profile.getTier() != target - 1) {
            Sounds.playTo(player, "entity.villager.no", 1.0f, 0.6f);
            player.sendMessage(Text.color("&c&l✦ Unlock Tier " + (target - 1) + " first."));
            return;
        }
        if (!pay(player)) {
            Sounds.playTo(player, "entity.villager.no", 1.0f, 0.5f);
            player.closeInventory();
            player.sendMessage(Text.color("&c&l✦ Insufficient funds to evolve."));
            return;
        }
        profile.setTier(target);
        plugin.getProfileManager().saveAsync(profile);
        player.closeInventory();
        Sounds.playTo(player, "entity.player.levelup", 1.0f, 1.2f);
        Compat.sendTitle(player, "&6&l★ TIER " + target + " ★", "&7Your elemental strength has expanded!", 10, 40, 10);
    }

    /** Pays the configured upgrade currency (XP levels or Angel Tokens). */
    private boolean pay(Player player) {
        if ("TOKENS".equalsIgnoreCase(plugin.getConfigManager().upgradeCurrency())) {
            int needed = plugin.getConfigManager().upgradeTokens();
            if (countTokens(player) < needed) {
                return false;
            }
            removeTokens(player, needed);
            return true;
        }
        int needed = plugin.getConfigManager().upgradeXpLevels();
        if (player.getLevel() < needed) {
            return false;
        }
        player.setLevel(player.getLevel() - needed);
        return true;
    }

    private int countTokens(Player player) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (AngelToken.isToken(plugin, stack)) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    private void removeTokens(Player player, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack stack = contents[i];
            if (AngelToken.isToken(plugin, stack)) {
                int take = Math.min(stack.getAmount(), remaining);
                stack.setAmount(stack.getAmount() - take);
                remaining -= take;
                if (stack.getAmount() <= 0) {
                    player.getInventory().setItem(i, null);
                }
            }
        }
    }

    // ---- Phase 5: Admin Management GUI ---------------------------------

    private void handleAdmin(InventoryClickEvent event, AdminGui gui) {
        UUID targetId = gui.getPlayerAt(event.getRawSlot());
        if (targetId == null) {
            return;
        }
        gui.getAdmin().closeInventory();
        new AdminSubMenuGui(plugin, gui.getAdmin(), targetId).open();
    }

    private void handleSubMenu(InventoryClickEvent event, AdminSubMenuGui gui) {
        Player admin = gui.getAdmin();
        UUID targetId = gui.getTargetId();
        PlayerProfile profile = plugin.getProfileManager().get(targetId);
        if (profile == null) {
            admin.closeInventory();
            return;
        }
        switch (event.getRawSlot()) {
            case AdminSubMenuGui.SLOT_ELEMENT -> new AdminElementGui(plugin, admin, targetId).open();
            case AdminSubMenuGui.SLOT_RESET_COOLDOWN -> {
                profile.setLastAbilityTimestamp(0L);
                profile.setSoulLockedUntil(0L);
                plugin.getProfileManager().saveAsync(profile);
                Sounds.playTo(admin, "ui.button.click", 1.0f, 1.4f);
                admin.sendMessage(Text.color("&a&l✦ Cooldowns reset for that target."));
            }
            case AdminSubMenuGui.SLOT_WIPE_STATS -> {
                profile.wipeStats();
                plugin.getProfileManager().saveAsync(profile);
                Sounds.playTo(admin, "entity.generic.explode", 1.0f, 0.7f);
                admin.sendMessage(Text.color("&c&l✦ Stats wiped for that target."));
            }
            case AdminSubMenuGui.SLOT_LEVEL3 -> {
                profile.setTier(3);
                plugin.getProfileManager().saveAsync(profile);
                Sounds.playTo(admin, "entity.player.levelup", 1.0f, 1.2f);
                admin.sendMessage(Text.color("&5&l✦ Target levelled up to Tier 3."));
            }
            case AdminSubMenuGui.SLOT_COMBAT_TOGGLE -> {
                plugin.setCombatEnabled(!plugin.isCombatEnabled());
                Sounds.playTo(admin, "block.beacon.power_select", 1.0f, 1.2f);
                admin.sendMessage(Text.color("&e&l✦ Combat mechanics now "
                        + (plugin.isCombatEnabled() ? "&aON" : "&cOFF") + "&e server-wide."));
            }
            case AdminSubMenuGui.SLOT_BACK -> new AdminGui(plugin, admin).open();
            default -> {
                // decorative
            }
        }
        if (event.getRawSlot() != AdminSubMenuGui.SLOT_BACK
                && event.getRawSlot() != AdminSubMenuGui.SLOT_ELEMENT) {
            gui.open(); // refresh to reflect the new state
        }
    }

    private void handleElementPicker(InventoryClickEvent event, AdminElementGui gui) {
        Alignment chosen = AdminElementGui.alignmentForSlot(event.getRawSlot());
        if (chosen == null) {
            return;
        }
        Player admin = gui.getAdmin();
        PlayerProfile profile = plugin.getProfileManager().get(gui.getTargetId());
        if (profile == null) {
            admin.closeInventory();
            return;
        }
        profile.setAlignment(chosen);
        plugin.getProfileManager().saveAsync(profile);
        Sounds.playTo(admin, "ui.button.click", 1.0f, 1.2f);
        admin.sendMessage(Text.color("&a&l✦ Target's element forced to " + chosen.getColoredName() + "&a."));
        new AdminSubMenuGui(plugin, admin, gui.getTargetId()).open();
    }
}
