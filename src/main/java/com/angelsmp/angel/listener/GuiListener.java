package com.angelsmp.angel.listener;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.gui.AdminGui;
import com.angelsmp.angel.gui.CodexGui;
import com.angelsmp.angel.gui.MenuGui;
import com.angelsmp.angel.gui.UpgradeGui;
import com.angelsmp.angel.util.Sounds;
import com.angelsmp.angel.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/** Routes clicks inside the three Angel GUIs (plus the codex ledger). */
public class GuiListener implements Listener {

    private final AngelPlugin plugin;

    public GuiListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof MenuGui menu) {
            event.setCancelled(true);
            handleMenu(event, menu);
        } else if (holder instanceof UpgradeGui upgrade) {
            event.setCancelled(true);
            handleUpgrade(event, upgrade);
        } else if (holder instanceof CodexGui codex) {
            event.setCancelled(true);
            handleCodex(event, codex);
        } else if (holder instanceof AdminGui admin) {
            event.setCancelled(true);
            handleAdmin(event, admin);
        }
    }

    private void handleMenu(InventoryClickEvent event, MenuGui menu) {
        Player player = menu.getPlayer();
        switch (event.getRawSlot()) {
            case 13 -> {
                Sounds.playTo(player, "ui.button.click", 1.0f, 1.0f);
                new UpgradeGui(plugin, player).open();
            }
            case 16 -> {
                Sounds.playTo(player, "item.book.page_turn", 1.0f, 1.0f);
                new CodexGui(plugin, player).open();
            }
            default -> {
                // Profile status / spacers: no action.
            }
        }
    }

    private void handleUpgrade(InventoryClickEvent event, UpgradeGui upgrade) {
        Player player = upgrade.getPlayer();
        switch (event.getRawSlot()) {
            case UpgradeGui.SLOT_RETURN -> {
                upgrade.stopAnimation();
                new MenuGui(plugin, player).open();
            }
            case UpgradeGui.SLOT_TIER2 -> {
                if (plugin.getUpgradeService().tryUpgrade(player, 2)) {
                    upgrade.refreshDynamic();
                }
            }
            case UpgradeGui.SLOT_TIER3 -> {
                if (plugin.getUpgradeService().tryUpgrade(player, 3)) {
                    upgrade.refreshDynamic();
                }
            }
            default -> {
                // Non-interactive decorative slots.
            }
        }
    }

    private void handleCodex(InventoryClickEvent event, CodexGui codex) {
        if (event.getRawSlot() == CodexGui.SLOT_BACK) {
            new MenuGui(plugin, codex.getPlayer()).open();
        }
    }

    private void handleAdmin(InventoryClickEvent event, AdminGui admin) {
        Player operator = admin.getAdmin();
        int slot = event.getRawSlot();

        // [E1..E6] Instant Target Shifting Buttons.
        AngelElement brush = brushForSlot(slot);
        if (brush != null) {
            plugin.setAdminBrush(operator.getUniqueId(), brush);
            operator.sendMessage(Text.color("&a&l✦ Cursor brush locked: " + brush.getColoredName() + "&a."));
            Sounds.playTo(operator, "block.beacon.power_select", 1.0f, 1.4f);
            new AdminGui(plugin, operator).open();
            return;
        }

        // [R] Cold Reboot Engine.
        if (slot == AdminGui.SLOT_RELOAD) {
            plugin.reloadAll();
            operator.sendMessage(Text.color("&a&l✦ Master cache reloaded. &7config.yml refreshed without dropping connections."));
            Sounds.playTo(operator, "block.beacon.deactivate", 1.0f, 1.0f);
            new AdminGui(plugin, operator).open();
            return;
        }

        // [PL] Dynamic connected players feed.
        UUID targetId = admin.getPlayerAt(slot);
        if (targetId != null) {
            Player target = Bukkit.getPlayer(targetId);
            if (target == null) {
                operator.sendMessage(Text.color("&c&l✦ That player is no longer online."));
                return;
            }
            AngelElement active = plugin.getAdminBrush(operator.getUniqueId());
            if (active == null) {
                operator.sendMessage(Text.color("&c&l✦ Select an element injector (E1-E6) first."));
                return;
            }
            plugin.getPlayerManager().setElement(targetId, active);
            // Executes /angel set <player> <element> directly from the console layer backend.
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "angel set " + target.getName() + " " + active.name());
            operator.sendMessage(Text.color("&a&l✦ " + target.getName() + " &7element overridden to " + active.getColoredName() + "&7."));
            target.sendMessage(Text.color("&e&l✦ Your Angel element is now " + active.getColoredName() + "&e."));
            Sounds.playTo(operator, "entity.experience_orb.pickup", 1.0f, 1.4f);
            new AdminGui(plugin, operator).open();
        }
    }

    private AngelElement brushForSlot(int slot) {
        return switch (slot) {
            case AdminGui.SLOT_FIRE -> AngelElement.FIRE;
            case AdminGui.SLOT_ICE -> AngelElement.ICE;
            case AdminGui.SLOT_LIGHTNING -> AngelElement.LIGHTNING;
            case AdminGui.SLOT_WIND -> AngelElement.WIND;
            case AdminGui.SLOT_EARTH -> AngelElement.EARTH;
            case AdminGui.SLOT_LIGHT -> AngelElement.LIGHT;
            default -> null;
        };
    }
}
