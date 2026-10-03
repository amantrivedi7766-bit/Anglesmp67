package com.angelsmp.angel.death;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.data.Alignment;
import com.angelsmp.angel.data.PlayerProfile;
import com.angelsmp.angel.util.Compat;
import com.angelsmp.angel.util.Particles;
import com.angelsmp.angel.util.Sounds;
import com.angelsmp.angel.util.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Phase 6 - the recovery rituals.
 * Pathway B: the Sacred Altar (Crying Obsidian + Gold Blocks + Quartz) wipes the
 * lockout instantly. Pathway C: the Nether Altar turns the fractured Angel into
 * a Tier 1 Devil.
 */
public class AltarListener implements Listener {

    private final AngelPlugin plugin;

    public AltarListener(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }
        Player player = event.getPlayer();
        PlayerProfile profile = plugin.getProfileManager().get(player.getUniqueId());
        if (profile == null || !profile.isSoulLocked()) {
            return; // only a fractured soul can use an altar
        }

        Block center = event.getClickedBlock();
        if (center.getType() != Material.CRYING_OBSIDIAN || !isAltarStructure(center)) {
            return;
        }
        event.setCancelled(true);

        boolean nether = center.getWorld().getEnvironment() == World.Environment.NETHER;
        if (nether) {
            performDarkTurn(player, profile, center.getLocation());
        } else {
            performPurification(player, profile, center.getLocation());
        }
    }

    // ---- Pathway B: Sacred Altar purification --------------------------

    private void performPurification(Player player, PlayerProfile profile, Location altar) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!isOffering(hand)) {
            Compat.sendActionBar(player, Text.color("&cYou must offer "
                    + plugin.getConfigManager().altarItemAmount() + "x "
                    + pretty(plugin.getConfigManager().altarItemMaterial()) + " or 1 Nether Star."));
            return;
        }
        consumeOffering(player, hand);

        altar.getWorld().strikeLightning(altar); // an actual environmental lightning bolt
        profile.setSoulLockedUntil(0L);
        plugin.getProfileManager().saveAsync(profile);

        Compat.sendTitle(player, "&6&lSOUL RESTORED", "&7Your powers return at full strength.", 10, 50, 10);
        Sounds.playAt(player.getLocation(), "block.amethyst_block.chime", 1.0f, 1.4f);
        Particles.spawn(player.getWorld(), "totem_of_undying", player.getLocation().add(0, 1, 0), 40, 0.6, 0.8, 0.6, 0.1);
        player.sendMessage(Text.color("&a&l✦ The Sacred Altar has purified your fractured soul!"));
    }

    // ---- Pathway C: Nether Altar dark turn -----------------------------

    private void performDarkTurn(Player player, PlayerProfile profile, Location altar) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!isOffering(hand)) {
            Compat.sendActionBar(player, Text.color("&5Offer a relic to the Nether Altar to become a Devil."));
            return;
        }
        consumeOffering(player, hand);

        // Sacrifice the fractured light essence: instant Tier 1 Devil, tiers stripped.
        profile.setAlignment(Alignment.DEVIL);
        profile.setTier(1);
        profile.setSoulLockedUntil(0L);
        plugin.getProfileManager().saveAsync(profile);

        altar.getWorld().strikeLightning(altar);
        Compat.sendTitle(player, "&5&lTHE DARK TURN", "&7You are reborn as a Tier 1 Devil.", 10, 50, 10);
        Sounds.playAt(player.getLocation(), "entity.wither.spawn", 1.0f, 0.6f);
        Particles.spawn(player.getWorld(), "portal", player.getLocation().add(0, 1, 0), 60, 0.6, 0.8, 0.6, 0.2);
        player.sendMessage(Text.color("&5&l✦ You have embraced the dark. All Angel tiers are lost."));
    }

    // ---- Helpers -------------------------------------------------------

    private boolean isOffering(ItemStack hand) {
        if (hand == null || hand.getType() == Material.AIR) {
            return false;
        }
        if (hand.getType() == Material.NETHER_STAR) {
            return true;
        }
        Material required = Material.matchMaterial(plugin.getConfigManager().altarItemMaterial());
        return required != null
                && hand.getType() == required
                && hand.getAmount() >= plugin.getConfigManager().altarItemAmount();
    }

    private void consumeOffering(Player player, ItemStack hand) {
        if (hand.getType() == Material.NETHER_STAR) {
            hand.setAmount(hand.getAmount() - 1);
        } else {
            hand.setAmount(hand.getAmount() - plugin.getConfigManager().altarItemAmount());
        }
    }

    /** Center must be Crying Obsidian with Gold Blocks and Quartz around it. */
    private boolean isAltarStructure(Block center) {
        int gold = 0;
        int quartz = 0;
        for (int x = -3; x <= 3; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -3; z <= 3; z++) {
                    Material type = center.getRelative(x, y, z).getType();
                    if (type == Material.GOLD_BLOCK) {
                        gold++;
                    } else if (type == Material.QUARTZ_BLOCK) {
                        quartz++;
                    }
                }
            }
        }
        return gold >= 4 && quartz >= 4;
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
