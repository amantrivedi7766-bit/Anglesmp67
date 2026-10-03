package com.angelsmp.angel;

import com.angelsmp.angel.ability.AbilityManager;
import com.angelsmp.angel.ability.PassiveManager;
import com.angelsmp.angel.command.SmpCommand;
import com.angelsmp.angel.command.SparkGuiCommand;
import com.angelsmp.angel.command.UpgradeCommand;
import com.angelsmp.angel.config.PluginConfig;
import com.angelsmp.angel.cooldown.CooldownTracker;
import com.angelsmp.angel.data.MySqlStorage;
import com.angelsmp.angel.data.ProfileManager;
import com.angelsmp.angel.data.SqliteStorage;
import com.angelsmp.angel.data.Storage;
import com.angelsmp.angel.item.RebirthShard;
import com.angelsmp.angel.listener.AbilityTriggerListener;
import com.angelsmp.angel.listener.BlockListener;
import com.angelsmp.angel.listener.ConnectionListener;
import com.angelsmp.angel.listener.DamageListener;
import com.angelsmp.angel.listener.DeathRespawnListener;
import com.angelsmp.angel.listener.GuiListener;
import com.angelsmp.angel.listener.MovementListener;
import com.angelsmp.angel.listener.RebirthShardListener;
import com.angelsmp.angel.progression.CombatProgression;
import com.angelsmp.angel.progression.LevelUpAnimation;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * AngelSMP - the Angel/Demon elemental SMP plugin.
 *
 * <p>Implements all seven modules: async data architecture, the dimensional
 * death loop &amp; banishment, the elemental ability matrix, timing/progression
 * frameworks, the interface menus, the keybind execution hooks and the
 * level-up animation engine.</p>
 */
public class AngelPlugin extends JavaPlugin {

    private PluginConfig pluginConfig;
    private ProfileManager profileManager;
    private AbilityManager abilityManager;
    private PassiveManager passiveManager;
    private CooldownTracker cooldownTracker;
    private CombatProgression combatProgression;
    private LevelUpAnimation levelUpAnimation;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.pluginConfig = new PluginConfig(this);

        // Module 1 - open the storage back-end.
        Storage storage = createStorage();
        try {
            storage.init();
        } catch (Exception ex) {
            getLogger().severe("Could not initialise the database: " + ex.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.profileManager = new ProfileManager(this, storage);
        this.abilityManager = new AbilityManager(this);
        this.passiveManager = new PassiveManager(this);
        this.cooldownTracker = new CooldownTracker(this);
        this.combatProgression = new CombatProgression(this);
        this.levelUpAnimation = new LevelUpAnimation(this);

        this.passiveManager.start();
        this.cooldownTracker.start();

        registerListeners();
        registerCommands();
        RebirthShard.registerRecipe(this);

        getLogger().info("AngelSMP enabled - Angel/Demon SMP, 6 element trees, keybinds, GUIs and animation engine online.");
    }

    @Override
    public void onDisable() {
        if (cooldownTracker != null) {
            cooldownTracker.stop();
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

    private Storage createStorage() {
        if ("mysql".equalsIgnoreCase(pluginConfig.getDatabaseType())) {
            String host = getConfig().getString("database.mysql.host", "127.0.0.1");
            int port = getConfig().getInt("database.mysql.port", 3306);
            String db = getConfig().getString("database.mysql.database", "angelsmp");
            String user = getConfig().getString("database.mysql.username", "root");
            String pass = getConfig().getString("database.mysql.password", "");
            getLogger().info("Using MySQL storage at " + host + ":" + port + "/" + db);
            return new MySqlStorage(host, port, db, user, pass);
        }
        getLogger().info("Using local SQLite storage.");
        return new SqliteStorage(new File(getDataFolder(), "angelsmp.db"));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathRespawnListener(this), this);
        getServer().getPluginManager().registerEvents(new RebirthShardListener(this), this);
        getServer().getPluginManager().registerEvents(new AbilityTriggerListener(this), this);
        getServer().getPluginManager().registerEvents(new DamageListener(this), this);
        getServer().getPluginManager().registerEvents(new MovementListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
    }

    private void registerCommands() {
        SparkGuiCommand sparkGui = new SparkGuiCommand(this);
        SmpCommand smp = new SmpCommand(this);
        UpgradeCommand upgrade = new UpgradeCommand(this);

        bind("sparkgui", sparkGui);
        bind("angel", sparkGui); // /angel power opens the same Player Selection Screen
        bind("smp", smp);
        bind("upgrade", upgrade);
    }

    private void bind(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
        }
    }

    // ---- Accessors -----------------------------------------------------

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    public PassiveManager getPassiveManager() {
        return passiveManager;
    }

    public CooldownTracker getCooldownTracker() {
        return cooldownTracker;
    }

    public CombatProgression getCombatProgression() {
        return combatProgression;
    }

    public LevelUpAnimation getLevelUpAnimation() {
        return levelUpAnimation;
    }
}
