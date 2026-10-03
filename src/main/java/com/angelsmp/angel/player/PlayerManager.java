package com.angelsmp.angel.player;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.element.AngelElement;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Loads, caches and persists {@link PlayerData} profiles. */
public class PlayerManager {

    private final AngelPlugin plugin;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private final File dataFile;
    private FileConfiguration dataConfig;

    public PlayerManager(AngelPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("data-file", "players.yml"));
        load();
    }

    public void load() {
        if (!dataFile.getParentFile().exists() && !dataFile.getParentFile().mkdirs()) {
            plugin.getLogger().warning("Could not create data folder for players.yml");
        }
        if (!dataFile.exists()) {
            try {
                if (dataFile.createNewFile()) {
                    plugin.getLogger().info("Created new players.yml data file.");
                }
            } catch (IOException ex) {
                plugin.getLogger().severe("Could not create players.yml: " + ex.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        cache.clear();
        for (String key : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String elementName = dataConfig.getString(key + ".element", null);
                int tier = dataConfig.getInt(key + ".tier", 1);
                cache.put(uuid, new PlayerData(uuid, AngelElement.fromString(elementName), tier));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Skipping invalid player entry in players.yml: " + key);
            }
        }
    }

    public void save() {
        if (dataConfig == null) {
            return;
        }
        for (Map.Entry<UUID, PlayerData> entry : cache.entrySet()) {
            String base = entry.getKey().toString();
            PlayerData data = entry.getValue();
            dataConfig.set(base + ".element", data.getElement() == null ? null : data.getElement().name());
            dataConfig.set(base + ".tier", data.getTier());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("Could not save players.yml: " + ex.getMessage());
        }
    }

    /** Returns the profile for the uuid, creating a fresh one if absent. */
    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, id -> new PlayerData(id, null, 1));
    }

    public boolean has(UUID uuid) {
        return cache.containsKey(uuid);
    }

    public void setElement(UUID uuid, AngelElement element) {
        PlayerData data = get(uuid);
        data.setElement(element);
        save();
    }

    public void reset(UUID uuid) {
        cache.remove(uuid);
        if (dataConfig != null) {
            dataConfig.set(uuid.toString(), null);
            save();
        }
    }
}
