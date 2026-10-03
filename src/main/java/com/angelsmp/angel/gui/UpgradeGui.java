package com.angelsmp.angel.gui;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.player.PlayerData;
import com.angelsmp.angel.util.ItemBuilder;
import com.angelsmp.angel.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 👑 The Upgrade Window GUI ({@code /angel menu} → upgrade).
 *
 * <p>A 9×5 grid (45 slots) formatted with golden panels and a diagonal
 * tier-progression path: Tier&nbsp;1 → » → Tier&nbsp;2 → » → Tier&nbsp;3.</p>
 */
public class UpgradeGui implements InventoryHolder {

    public static final String TITLE = "§6§lUpgrade Pipeline";

    public static final int SLOT_TIER1 = 10;
    public static final int SLOT_ARROW1 = 11;
    public static final int SLOT_TIER2 = 21;
    public static final int SLOT_ARROW2 = 22;
    public static final int SLOT_TIER3 = 32;
    public static final int SLOT_RETURN = 39;

    private final AngelPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private boolean flash;
    private BukkitTask animation;

    public UpgradeGui(AngelPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 45, Text.color(TITLE));
        build();
        startAnimation();
    }

    private void build() {
        ItemStack pane = ItemBuilder.of(Material.YELLOW_STAINED_GLASS_PANE).name("§eUpgrade Pipeline").build();

        // Row 1 (0-8) and Row 5 (36-44): outer border trim.
        for (int i = 0; i <= 8; i++) {
            inventory.setItem(i, pane);
        }
        for (int i = 36; i <= 44; i++) {
            inventory.setItem(i, pane);
        }
        // Column borders on rows 2-4.
        inventory.setItem(9, pane);
        inventory.setItem(17, pane);
        inventory.setItem(18, pane);
        inventory.setItem(26, pane);
        inventory.setItem(27, pane);
        inventory.setItem(35, pane);

        // [»] Progress arrows.
        ItemStack arrow = ItemBuilder.of(Material.ARROW).name("§7Next Progression Level").build();
        inventory.setItem(SLOT_ARROW1, arrow);
        inventory.setItem(SLOT_ARROW2, arrow);

        // [B] Navigation safeguard (return to main menu).
        inventory.setItem(SLOT_RETURN, ItemBuilder.of(Material.BARRIER)
                .name("§c◀ Return to Main Interface Screen")
                .build());

        refreshDynamic();
    }

    /** Rebuilds the tier tracker items; called on open and on each flash frame. */
    public void refreshDynamic() {
        PlayerData data = plugin.getPlayerManager().get(player.getUniqueId());
        int tier = data.getTier();
        AngelElement element = data.getElement();

        inventory.setItem(SLOT_TIER1, tierItem(1, tier, element));
        inventory.setItem(SLOT_TIER2, tierItem(2, tier, element));
        inventory.setItem(SLOT_TIER3, tierItem(3, tier, element));
    }

    private ItemStack tierItem(int target, int current, AngelElement element) {
        String elementName = element == null ? "Unassigned" : element.getDisplayName();

        if (current >= target) {
            // Unlocked: emerald block with the active enchantment glow wrapper.
            List<String> lore = new ArrayList<>();
            lore.add("§a§lTier " + target + " Unlocked §7(Active Features Running).");
            if (element != null) {
                var metrics = element.getTier(target);
                lore.add(" ");
                lore.add("§7Cooldown: §e" + trim(metrics.getCooldownSeconds()) + "s");
                lore.add("§7Metric: §e" + metrics.getDamageLabel());
                lore.add("§8" + metrics.getMilestone());
            }
            return ItemBuilder.of(Material.EMERALD_BLOCK)
                    .name("§a§lTIER " + target + " §7(" + elementName + ")")
                    .lore(lore)
                    .glow()
                    .build();
        }

        if (current == target - 1) {
            // Unlockable: a flashing gold ingot with the EVOLVE cost matrix.
            List<String> lore = evolveLore(target);
            ItemBuilder builder = ItemBuilder.of(Material.GOLD_INGOT)
                    .name("§e§lEVOLVE TO TIER " + target)
                    .lore(lore);
            if (flash) {
                builder.glow();
            }
            return builder.build();
        }

        // Locked: redstone block.
        return ItemBuilder.of(Material.REDSTONE_BLOCK)
                .name("§c§lTIER " + target + " LOCKED")
                .lore(
                        "§7Requires Tier " + (target - 1) + " to be unlocked first.",
                        "§8Progress through the pipeline to evolve.")
                .build();
    }

    private List<String> evolveLore(int target) {
        List<String> lore = new ArrayList<>();
        lore.add("§7Cost Required:");
        int diamondBlocks = plugin.getConfig().getInt("upgrade.tier-" + target + ".diamond-blocks", 0);
        int netherite = plugin.getConfig().getInt("upgrade.tier-" + target + ".netherite-ingots", 0);
        int netherStars = plugin.getConfig().getInt("upgrade.tier-" + target + ".nether-stars", 0);
        if (diamondBlocks > 0) {
            lore.add("§c- " + diamondBlocks + "x Diamond Blocks");
        }
        if (netherite > 0) {
            lore.add("§c- " + netherite + "x Netherite Ingots");
        }
        if (netherStars > 0) {
            lore.add("§c- " + netherStars + "x Nether Stars");
        }
        lore.add("§7-----------------");
        lore.add("§a▶ Click here to pay tokens and evolve.");
        return lore;
    }

    private static String trim(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private void startAnimation() {
        animation = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof UpgradeGui open) || open != this) {
                stopAnimation();
                return;
            }
            flash = !flash;
            refreshDynamic();
        }, 10L, 10L);
    }

    public void stopAnimation() {
        if (animation != null) {
            animation.cancel();
            animation = null;
        }
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
