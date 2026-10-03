package com.angelsmp.angel.config;

import com.angelsmp.angel.AngelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.Map;

/** Typed access to config.yml values. */
public class PluginConfig {

    private final AngelPlugin plugin;
    private final Map<Integer, Cost> upgradeCosts = new LinkedHashMap<>();

    public PluginConfig(AngelPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        upgradeCosts.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("upgrade.costs");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    int level = Integer.parseInt(key);
                    String materialName = section.getString(key + ".material", "DIAMOND");
                    int amount = section.getInt(key + ".amount", 1);
                    Material material = Material.matchMaterial(materialName);
                    if (material != null) {
                        upgradeCosts.put(level, new Cost(material, amount));
                    }
                } catch (NumberFormatException ignored) {
                    // skip malformed key
                }
            }
        }
        if (upgradeCosts.isEmpty()) {
            upgradeCosts.put(1, new Cost(Material.DIAMOND, 64));
            upgradeCosts.put(2, new Cost(Material.NETHERITE_SCRAP, 32));
        }
    }

    /** @return the cost to reach the given level, or null if none configured. */
    public Cost costFor(int level) {
        return upgradeCosts.get(level);
    }

    public Map<Integer, Cost> getUpgradeCosts() {
        return upgradeCosts;
    }

    /** @return the configured custom spawn (defaults to the Nether). */
    public Location getSpawnLocation() {
        String worldName = plugin.getConfig().getString("spawn.world", "world_nether");
        double x = plugin.getConfig().getDouble("spawn.x", 0.0);
        double y = plugin.getConfig().getDouble("spawn.y", 80.0);
        double z = plugin.getConfig().getDouble("spawn.z", 0.0);
        float yaw = (float) plugin.getConfig().getDouble("spawn.yaw", 0.0);
        float pitch = (float) plugin.getConfig().getDouble("spawn.pitch", 0.0);
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
        }
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    /** @return the Overworld spawn used when a Demon is purified. */
    public Location getOverworldSpawn() {
        String worldName = plugin.getConfig().getString("purification.overworld", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
        }
        if (world == null) {
            return null;
        }
        return world.getSpawnLocation();
    }

    public String getDatabaseType() {
        return plugin.getConfig().getString("database.type", "sqlite");
    }

    public record Cost(Material material, int amount) {
    }
}
