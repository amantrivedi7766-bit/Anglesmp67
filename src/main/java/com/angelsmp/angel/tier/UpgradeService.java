package com.angelsmp.angel.tier;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.player.PlayerData;
import com.angelsmp.angel.util.Sounds;
import com.angelsmp.angel.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/** Handles the token economy for evolving an element to its next tier. */
public class UpgradeService {

    private final AngelPlugin plugin;

    public UpgradeService(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    /** @return the required materials for the given target tier, or an empty map if none. */
    public Map<Material, Integer> costFor(int targetTier) {
        Map<Material, Integer> cost = new LinkedHashMap<>();
        int diamondBlocks = plugin.getConfig().getInt("upgrade.tier-" + targetTier + ".diamond-blocks", 0);
        int netherite = plugin.getConfig().getInt("upgrade.tier-" + targetTier + ".netherite-ingots", 0);
        int netherStars = plugin.getConfig().getInt("upgrade.tier-" + targetTier + ".nether-stars", 0);
        if (diamondBlocks > 0) {
            cost.put(Material.DIAMOND_BLOCK, diamondBlocks);
        }
        if (netherite > 0) {
            cost.put(Material.NETHERITE_INGOT, netherite);
        }
        if (netherStars > 0) {
            cost.put(Material.NETHER_STAR, netherStars);
        }
        return cost;
    }

    public boolean canAfford(Player player, int targetTier) {
        for (Map.Entry<Material, Integer> entry : costFor(targetTier).entrySet()) {
            if (count(player, entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Attempts to evolve the player's element to the given target tier.
     *
     * @return true if the evolution succeeded.
     */
    public boolean tryUpgrade(Player player, int targetTier) {
        PlayerData data = plugin.getPlayerManager().get(player.getUniqueId());
        if (!data.hasElement()) {
            player.sendMessage(Text.color("&c&l✦ You have no Angel element assigned yet."));
            return false;
        }
        if (data.getTier() >= targetTier) {
            player.sendMessage(Text.color("&c&l✦ You have already unlocked Tier " + targetTier + "."));
            return false;
        }
        if (data.getTier() != targetTier - 1) {
            player.sendMessage(Text.color("&c&l✦ You must unlock Tier " + (targetTier - 1) + " first."));
            return false;
        }
        Map<Material, Integer> cost = costFor(targetTier);
        if (!canAfford(player, targetTier)) {
            player.sendMessage(Text.color("&c&l✦ Insufficient tokens to evolve to Tier " + targetTier + "."));
            player.sendMessage(Text.color("&7Required: &e" + describe(cost)));
            return false;
        }
        // Consume the tokens.
        for (Map.Entry<Material, Integer> entry : cost.entrySet()) {
            remove(player, entry.getKey(), entry.getValue());
        }
        data.upgrade();
        plugin.getPlayerManager().save();

        AngelElement element = data.getElement();
        var metrics = element.getTier(targetTier);
        player.sendMessage(Text.color("&a&l✦ EVOLVED! &7Your &f" + element.getDisplayName()
                + "&7 Angel ascended to &eTier " + targetTier + "&7."));
        player.sendMessage(Text.color("&7New cooldown: &e" + metrics.getCooldownSeconds()
                + "s &8| &7Milestone: &f" + metrics.getMilestone()));
        Sounds.playAt(player.getLocation(), "entity.player.levelup", 1.0f, 1.2f);
        Sounds.playAt(player.getLocation(), "block.beacon.activate", 1.0f, 1.0f);
        return true;
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

    private String describe(Map<Material, Integer> cost) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Material, Integer> entry : cost.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&7, ");
            }
            builder.append(entry.getValue()).append("x ")
                    .append(entry.getKey().name().replace('_', ' '));
        }
        return builder.toString();
    }
}
