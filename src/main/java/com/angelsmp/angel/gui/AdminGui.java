package com.angelsmp.angel.gui;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.util.ItemBuilder;
import com.angelsmp.angel.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 🛡️ The Operator Admin Dashboard GUI ({@code /angel admin}).
 *
 * <p>A 9×6 master matrix (54 slots) accessible only to {@code angel.admin}.</p>
 */
public class AdminGui implements InventoryHolder {

    public static final String TITLE = "§4§lOperator Admin Dashboard";

    public static final int SLOT_FIRE = 10;
    public static final int SLOT_ICE = 11;
    public static final int SLOT_LIGHTNING = 12;
    public static final int SLOT_WIND = 13;
    public static final int SLOT_EARTH = 14;
    public static final int SLOT_LIGHT = 15;
    public static final int SLOT_RELOAD = 48;

    public static final int[] PLAYER_SLOTS = {
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final AngelPlugin plugin;
    private final Player admin;
    private final Inventory inventory;
    private final Map<Integer, UUID> slotToPlayer = new HashMap<>();

    public AdminGui(AngelPlugin plugin, Player admin) {
        this.plugin = plugin;
        this.admin = admin;
        this.inventory = Bukkit.createInventory(this, 54, Text.color(TITLE));
        build();
    }

    private void build() {
        ItemStack divider = ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();

        // Row 1 (0-8): black glass trim.
        for (int i = 0; i <= 8; i++) {
            inventory.setItem(i, divider);
        }
        // Row 6 (45-53): black glass trim, except the reload trigger.
        for (int i = 45; i <= 53; i++) {
            inventory.setItem(i, divider);
        }
        // Column borders (rows 2-5).
        inventory.setItem(9, divider);
        inventory.setItem(17, divider);
        inventory.setItem(18, divider);
        inventory.setItem(26, divider);
        inventory.setItem(27, divider);
        inventory.setItem(35, divider);
        inventory.setItem(36, divider);
        inventory.setItem(44, divider);

        // [E1..E6] Instant Target Shifting Buttons.
        inventory.setItem(SLOT_FIRE, elementButton(AngelElement.FIRE, Material.MAGMA_BLOCK));
        inventory.setItem(SLOT_ICE, elementButton(AngelElement.ICE, Material.PACKED_ICE));
        inventory.setItem(SLOT_LIGHTNING, elementButton(AngelElement.LIGHTNING, Material.LIGHTNING_ROD));
        inventory.setItem(SLOT_WIND, elementButton(AngelElement.WIND, Material.FEATHER));
        inventory.setItem(SLOT_EARTH, elementButton(AngelElement.EARTH, Material.OBSIDIAN));
        inventory.setItem(SLOT_LIGHT, elementButton(AngelElement.LIGHT, Material.GLOWSTONE));

        // [R] Cold Reboot Engine.
        inventory.setItem(SLOT_RELOAD, ItemBuilder.of(Material.TNT)
                .name("§4§lRELOAD MASTER CACHE")
                .lore(
                        "§7Executes a silent server thread refresh,",
                        "§7reloading config.yml on the fly without",
                        "§7breaking ongoing connections.",
                        " ",
                        "§c▶ Click to reload.")
                .build());

        refreshPlayers();
    }

    private ItemStack elementButton(AngelElement element, Material material) {
        AngelElement brush = plugin.getAdminBrush(admin.getUniqueId());
        boolean active = brush == element;
        ItemBuilder builder = ItemBuilder.of(material)
                .name(element.getColorCode() + "§l" + element.getDisplayName().toUpperCase() + " INJECTOR")
                .lore(
                        "§7Locks the §f" + element.getDisplayName() + "§7 element into",
                        "§7your active cursor brush cache.",
                        " ",
                        active ? "§a✔ ACTIVE BRUSH" : "§8Click to select.");
        if (active) {
            builder.glow();
        }
        return builder.build();
    }

    /** Loops through currently online players and injects their skull profiles. */
    public void refreshPlayers() {
        // Clear previous player feed slots.
        slotToPlayer.clear();
        for (int slot : PLAYER_SLOTS) {
            inventory.setItem(slot, null);
        }
        List<Player> online = List.copyOf(Bukkit.getOnlinePlayers());
        int index = 0;
        for (Player target : online) {
            if (index >= PLAYER_SLOTS.length) {
                break;
            }
            slotToPlayer.put(PLAYER_SLOTS[index], target.getUniqueId());
            var data = plugin.getPlayerManager().get(target.getUniqueId());
            String elementName = data.hasElement() ? data.getElement().getColoredName() : "§8None";
            inventory.setItem(PLAYER_SLOTS[index], ItemBuilder.skull(target)
                    .name("§e" + target.getName())
                    .lore(
                            "§7Current Angel: " + elementName,
                            "§7Progression: §eTier " + data.getTier() + "/3",
                            " ",
                            "§a▶ Click to override this target's element")
                    .build());
            index++;
        }
        if (index == 0) {
            inventory.setItem(PLAYER_SLOTS[0], ItemBuilder.of(Material.GRAY_DYE)
                    .name("§7No players online")
                    .build());
        }
    }

    public Player getAdmin() {
        return admin;
    }

    /** @return the uuid of the player shown in the given slot, or null. */
    public UUID getPlayerAt(int slot) {
        return slotToPlayer.get(slot);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open() {
        admin.openInventory(inventory);
    }
}
