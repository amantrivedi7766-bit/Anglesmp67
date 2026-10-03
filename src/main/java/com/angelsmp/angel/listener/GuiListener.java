package com.angelsmp.angel.listener;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.config.PluginConfig;
import com.angelsmp.angel.data.Element;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.data.Race;
import com.angelsmp.angel.gui.AdminMenuGui;
import com.angelsmp.angel.gui.ElementSelectionGui;
import com.angelsmp.angel.gui.LevelAdjusterGui;
import com.angelsmp.angel.gui.UpgradeGui;
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

/** Module 5 - all GUI click logic (every click is cancelled to prevent stealing). */
public class GuiListener implements Listener {

    private final AngelPlugin plugin;

    public GuiListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof ElementSelectionGui gui) {
            event.setCancelled(true);
            handleElementSelection(event, gui);
        } else if (holder instanceof AdminMenuGui gui) {
            event.setCancelled(true);
            handleAdminMenu(event, gui);
        } else if (holder instanceof LevelAdjusterGui gui) {
            event.setCancelled(true);
            handleLevelAdjuster(event, gui);
        } else if (holder instanceof UpgradeGui gui) {
            event.setCancelled(true);
            handleUpgrade(event, gui);
        }
    }

    // ---- Module 5A: Element Selection ----------------------------------

    private void handleElementSelection(InventoryClickEvent event, ElementSelectionGui gui) {
        Element chosen = ElementSelectionGui.elementForSlot(event.getRawSlot());
        if (chosen == null) {
            return;
        }
        Player player = gui.getPlayer();
        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        if (profile == null) {
            return;
        }
        // If element != NONE: play a bass sound, close the menu, and show an error.
        if (profile.hasElement()) {
            Sounds.playTo(player, "entity.villager.no", 1.0f, 0.5f);
            player.closeInventory();
            player.sendMessage(Text.color("&c&l✦ You already have an element assigned!"));
            return;
        }
        // If element == NONE: assign, close, explosion sound, Level-Up Engine.
        profile.setElement(chosen);
        plugin.getProfileManager().saveAsync(profile);
        player.closeInventory();
        Sounds.playTo(player, "entity.generic.explode", 1.0f, 1.2f);
        player.sendMessage(Text.color("&a&l✦ Your element is now " + chosen.getColoredName() + "&a!"));
        // Module 6 Method 3: hand the player their Elemental Wand marker.
        player.getInventory().addItem(com.angelsmp.angel.item.ElementalWand.create(plugin));
        plugin.getLevelUpAnimation().play(player);
    }

    // ---- Module 5B: Admin Overlord Menu --------------------------------

    private void handleAdminMenu(InventoryClickEvent event, AdminMenuGui gui) {
        UUID targetId = gui.getPlayerAt(event.getRawSlot());
        if (targetId == null) {
            return;
        }
        PlayerProfile profile = plugin.getProfileManager().get(targetId);
        if (profile == null) {
            return;
        }
        Player admin = gui.getAdmin();

        // Shift + Click: toggle race.
        if (event.isShiftClick()) {
            if (profile.getRace() == Race.ANGEL) {
                profile.setRace(Race.DEMON);
                profile.setElement(Element.NONE);
            } else {
                profile.setRace(Race.ANGEL);
            }
            plugin.getProfileManager().saveAsync(profile);
            Sounds.playTo(admin, "ui.button.click", 1.0f, 1.0f);
            gui.refresh();
            return;
        }

        // Right-Click: overwrite the cooldown timestamps to 0.
        if (event.isRightClick()) {
            profile.setActiveAbilityTimestamp(0L);
            profile.setUltimateAbilityTimestamp(0L);
            plugin.getProfileManager().saveAsync(profile);
            Sounds.playTo(admin, "ui.button.click", 1.0f, 1.4f);
            admin.sendMessage(Text.color("&a&l✦ Cooldowns cleared for that target."));
            return;
        }

        // Left-Click: close and open the Level Adjuster Sub-GUI.
        if (event.isLeftClick()) {
            admin.closeInventory();
            new LevelAdjusterGui(plugin, admin, targetId).open();
        }
    }

    // ---- Module 5B: Level Adjuster Sub-GUI -----------------------------

    private void handleLevelAdjuster(InventoryClickEvent event, LevelAdjusterGui gui) {
        PlayerProfile profile = plugin.getProfileManager().get(gui.getTargetId());
        if (profile == null) {
            return;
        }
        int slot = event.getRawSlot();
        if (slot == LevelAdjusterGui.SLOT_DECREASE) {
            profile.setLevel(Math.max(PlayerProfile.MIN_LEVEL, profile.getLevel() - 1));
        } else if (slot == LevelAdjusterGui.SLOT_INCREASE) {
            profile.setLevel(Math.min(PlayerProfile.MAX_LEVEL, profile.getLevel() + 1));
        } else {
            return;
        }
        plugin.getProfileManager().saveAsync(profile);
        Sounds.playTo(gui.getAdmin(), "block.note_block.bell", 1.0f, 1.5f);
        gui.refreshInfo();
    }

    // ---- Module 4B Path B: Sacrifice Upgrade ---------------------------

    private void handleUpgrade(InventoryClickEvent event, UpgradeGui gui) {
        if (event.getRawSlot() != UpgradeGui.SLOT_EVOLVE) {
            return;
        }
        Player player = gui.getPlayer();
        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        if (profile == null) {
            return;
        }
        int level = profile.getLevel();
        if (level >= PlayerProfile.MAX_LEVEL) {
            player.closeInventory();
            Sounds.playTo(player, "entity.villager.no", 1.0f, 0.8f);
            return;
        }
        int next = level + 1;
        PluginConfig.Cost cost = plugin.getPluginConfig().costFor(next);
        if (cost == null) {
            player.closeInventory();
            return;
        }

        // 1) Count the exact number of required items across the inventory.
        int have = count(player, cost.material());
        if (have < cost.amount()) {
            // 2) Insufficient: close and play a low-pitch failure sound.
            player.closeInventory();
            Sounds.playTo(player, "entity.villager.no", 1.0f, 0.6f);
            player.sendMessage(Text.color("&c&l✦ You need " + cost.amount() + "x "
                    + pretty(cost.material().name()) + " to evolve."));
            return;
        }

        // 3) Sufficient: deduct, level +1, close, boot the animation engine.
        remove(player, cost.material(), cost.amount());
        profile.setLevel(next);
        plugin.getProfileManager().saveAsync(profile);
        player.closeInventory();
        plugin.getLevelUpAnimation().play(player);
    }

    private int count(Player player, Material material) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == material) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    private void remove(Player player, Material material, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack stack = contents[i];
            if (stack != null && stack.getType() == material) {
                int take = Math.min(stack.getAmount(), remaining);
                stack.setAmount(stack.getAmount() - take);
                remaining -= take;
                if (stack.getAmount() <= 0) {
                    player.getInventory().setItem(i, null);
                }
            }
        }
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
}
