package com.angelsmp.angel.config;

import com.angelsmp.angel.AngelPlugin;
import com.angelsmp.angel.util.Text;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Phase 1 - the messages.yml chat-layout store. */
public class Messages {

    private final AngelPlugin plugin;
    private final File file;
    private FileConfiguration config;

    public Messages(AngelPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "messages.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        // Fall back to the bundled defaults for any missing key.
        try (InputStream in = plugin.getResource("messages.yml")) {
            if (in != null) {
                config.setDefaults(YamlConfiguration.loadConfiguration(
                        new InputStreamReader(in, StandardCharsets.UTF_8)));
            }
        } catch (Exception ignored) {
            // defaults optional
        }
    }

    public void reload() {
        load();
    }

    /** @return the coloured message for the key (never null). */
    public String get(String key) {
        String raw = config.getString(key);
        if (raw == null) {
            return "";
        }
        return Text.color(raw);
    }

    public String get(String key, String... replacements) {
        String raw = config.getString(key, "");
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            raw = raw.replace(replacements[i], replacements[i + 1]);
        }
        return Text.color(raw);
    }
}
