package com.angelsmp.angel;

import com.angelsmp.angel.ability.AbilityManager;
import com.angelsmp.angel.ability.ControlManager;
import com.angelsmp.angel.ability.PassiveManager;
import com.angelsmp.angel.alignment.HaloTask;
import com.angelsmp.angel.alignment.StasisManager;
import com.angelsmp.angel.command.AngelCommand;
import com.angelsmp.angel.config.ConfigManager;
import com.angelsmp.angel.config.Messages;
import com.angelsmp.angel.data.ProfileManager;
import com.angelsmp.angel.data.SqliteStorage;
import com.angelsmp.angel.data.Storage;
import com.angelsmp.angel.death.AltarListener;
import com.angelsmp.angel.death.DeathListener;
import com.angelsmp.angel.hud.HudManager;
import com.angelsmp.angel.listener.AbilityTriggerListener;
import com.angelsmp.angel.listener.BlockListener;
import com.angelsmp.angel.listener.ConnectionListener;
import com.angelsmp.angel.listener.DamageListener;
import com.angelsmp.angel.listener.GuiListener;
import com.angelsmp.angel.listener.MovementListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * AngelSMP - the Angel / Devil elemental SMP plugin.
 *
 * <p>Implements all seven phases: installation &amp; requirements, first-time
 * alignment selection, the floating head-sign system, the full elemental ability
 * breakdown, the interface system, the death &amp; lockout system and the
 * configuration architecture.</p>
 */
public class AngelPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private Messages messages;
    private ProfileManager profileManager;
    private StasisManager stasisManager;
    private AbilityManager abilityManager;
    private PassiveManager passiveManager;
    private ControlManager controlManager;
    private HudManager hudManager;
    private HaloTask haloTask;

    private boolean combatEnabled = true;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);
        this.configManager = new ConfigManager(this);
        this.messages = new Messages(this);

        Storage storage = new SqliteStorage(new File(getDataFolder(), "database.db"));
        try {
            storage.init();
        } catch (Exception ex) {
            getLogger().severe("Could not initialise database.db: " + ex.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.profileManager = new ProfileManager(this, storage);
        this.stasisManager = new StasisManager();
        this.controlManager = new ControlManager();
        this.abilityManager = new AbilityManager(this);
        this.passiveManager = new PassiveManager(this);
        this.hudManager = new HudManager(this);
        this.haloTask = new HaloTask(this);

        this.passiveManager.start();
        this.hudManager.start();
        this.haloTask.start();

        registerListeners();
        registerCommands();
        startAutoSave();

        getLogger().info("AngelSMP enabled - Phases 1-7 online (alignments, head-signs, abilities, HUD, death lockout, altars, admin matrix).");
    }

    @Override
    public void onDisable() {
        if (haloTask != null) {
            haloTask.stop();
        }
        if (hudManager != null) {
            hudManager.stop();
        }
        if (passiveManager != null) {
            passiveManager.stop();
        }
        if (profileManager != null) {
            profileManager.saveAll();
            profileManager.shutdown();
        }
        getLogger().info("AngelSMP disabled.");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new AbilityTriggerListener(this), this);
        getServer().getPluginManager().registerEvents(new DamageListener(this), this);
        getServer().getPluginManager().registerEvents(new MovementListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new AltarListener(this), this);
    }

    private void registerCommands() {
        AngelCommand executor = new AngelCommand(this);
        bind("angel", executor);
        bind("upgrade", executor);
    }

    private void bind(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
            if (executor instanceof org.bukkit.command.TabCompleter completer) {
                command.setTabCompleter(completer);
            }
        }
    }

    private void startAutoSave() {
        long minutes = Math.max(1, configManager.saveIntervalMinutes());
        long ticks = minutes * 60L * 20L;
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (profileManager != null) {
                profileManager.saveAll();
            }
        }, ticks, ticks);
    }

    /** Phase 7 - on-the-fly reload. */
    public void reloadAll() {
        configManager.reload();
        messages.reload();
        if (profileManager != null) {
            profileManager.saveAll();
        }
    }

    // ---- Accessors -----------------------------------------------------

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Messages getMessages() {
        return messages;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public StasisManager getStasisManager() {
        return stasisManager;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    public PassiveManager getPassiveManager() {
        return passiveManager;
    }

    public ControlManager getControlManager() {
        return controlManager;
    }

    public HudManager getHudManager() {
        return hudManager;
    }

    public boolean isCombatEnabled() {
        return combatEnabled;
    }

    public void setCombatEnabled(boolean combatEnabled) {
        this.combatEnabled = combatEnabled;
    }
}
