package com.angelsmp.angel.gui;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.Element;
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
 * Module 5A - The Player Selection Screen ({@code /sparkgui} or {@code /angel power}).
 * 3 rows (27 slots) with a gray-pane frame and six element icons.
 */
public class ElementSelectionGui implements InventoryHolder {

    public static final String TITLE = "§6§lElement Selection";

    public static final int SLOT_FIRE = 11;
    public static final int SLOT_ICE = 12;
    public static final int SLOT_LIGHTNING = 13;
    public static final int SLOT_WIND = 14;
    public static final int SLOT_EARTH = 15;
    public static final int SLOT_LIGHT = 16;

    private final AngelPlugin plugin;
    private final Player player;
    private final Inventory inventory;

    public ElementSelectionGui(AngelPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 27, Text.color(TITLE));
        build();
    }

    private void build() {
        ItemStack pane = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        // Decorative border: slots 0-10, 17-18, 25-26.
        for (int slot : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 17, 18, 25, 26}) {
            inventory.setItem(slot, pane);
        }

        inventory.setItem(SLOT_FIRE, icon(Element.FIRE, "&7Level 1: &fBlaze Fireball", "&7Level 2: &fHellfire Dome"));
        inventory.setItem(SLOT_ICE, icon(Element.ICE, "&7Level 1: &fFrost Nova", "&7Level 2: &fGlacial Path"));
        inventory.setItem(SLOT_LIGHTNING, icon(Element.LIGHTNING, "&7Level 1: &fStorm Caller", "&7Level 2: &fAngelic Fury"));
        inventory.setItem(SLOT_WIND, icon(Element.WIND, "&7Level 1: &fWind Leap", "&7Level 2: &fAerodynamic Descent"));
        inventory.setItem(SLOT_EARTH, icon(Element.EARTH, "&7Level 1: &fEarthen Fortify", "&7Level 2: &fSeismic Pitfall"));
        inventory.setItem(SLOT_LIGHT, icon(Element.LIGHT, "&7Level 1: &fDivine Intervention", "&7Level 2: &fPurifying Beacon"));
    }

    private ItemStack icon(Element element, String... abilityLines) {
        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        boolean locked = profile != null && profile.hasElement();
        return ItemBuilder.of(element.getIcon())
                .name(element.getColor() + "§l" + element.getDisplayName().toUpperCase())
                .lore(
                        "&7Element: " + element.getColoredName(),
                        "",
                        abilityLines[0],
                        abilityLines[1],
                        "",
                        locked ? "&cYour element is already locked." : "&a▶ Click to choose this element.")
                .build();
    }

    /** @return the element mapped to the given raw slot, or null. */
    public static Element elementForSlot(int slot) {
        return switch (slot) {
            case SLOT_FIRE -> Element.FIRE;
            case SLOT_ICE -> Element.ICE;
            case SLOT_LIGHTNING -> Element.LIGHTNING;
            case SLOT_WIND -> Element.WIND;
            case SLOT_EARTH -> Element.EARTH;
            case SLOT_LIGHT -> Element.LIGHT;
            default -> null;
        };
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
