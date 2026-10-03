package com.angelsmp.angel;

import com.angelsmp.angel.ability.AbilityManager;
import com.angelsmp.angel.ability.ProjectileManager;
import com.angelsmp.angel.command.AngelCommand;
import com.angelsmp.angel.cooldown.CooldownManager;
import com.angelsmp.angel.element.AngelElement;
import com.angelsmp.angel.hud.HudManager;
import com.angelsmp.angel.listener.CombatListener;
import com.angelsmp.angel.listener.ConnectionListener;
import com.angelsmp.angel.listener.GuiListener;
import com.angelsmp.angel.player.PlayerManager;
import com.angelsmp.angel.status.StatusManager;
import com.angelsmp.angel.tier.UpgradeService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛠️ AngelSMP - Core Engine.
 *
 * <p>Hooks into the Paper/Spigot API event listeners to track player instances,
 * assign unique elemental profiles, manage cooldown frames, render persistent
 * custom heads inside interactive GUIs and fire real-time server-to-client
 * particle and sound animations.</p>
 */
public class AngelPlugin extends JavaPlugin {

    private PlayerManager playerManager;
    private CooldownManager cooldownManager;
    private StatusManager statusManager;
    private AbilityManager abilityManager;
    private ProjectileManager projectileManager;
    private UpgradeService upgradeService;
    private HudManager hudManager;

    /** Active cursor brush cache per operator, used by the Admin Dashboard. */
    private final Map<UUID, AngelElement> adminBrush = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.playerManager = new PlayerManager(this);
        this.cooldownManager = new CooldownManager();
        this.statusManager = new StatusManager(this);
        this.abilityManager = new AbilityManager(this);
        this.projectileManager = new ProjectileManager(this);
        this.upgradeService = new UpgradeService(this);
        this.hudManager = new HudManager(this);

        this.statusManager.start();
        this.projectileManager.start();
        this.hudManager.start();

        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);

        PluginCommand command = getCommand("angel");
        if (command != null) {
            AngelCommand executor = new AngelCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        getLogger().info("AngelSMP enabled - 6 elements, 3 tiers each, GUIs, HUD, particles and sounds online.");
    }

    @Override
    public void onDisable() {
        if (statusManager != null) {
            statusManager.stop();
        }
        if (projectileManager != null) {
            projectileManager.stop();
        }
        if (hudManager != null) {
            hudManager.stop();
        }
        if (playerManager != null) {
            playerManager.save();
        }
        getLogger().info("AngelSMP disabled.");
    }

    /** Silent on-the-fly refresh used by the Admin Dashboard's reload trigger. */
    public void reloadAll() {
        reloadConfig();
        if (playerManager != null) {
            playerManager.load();
        }
        if (hudManager != null) {
            hudManager.stop();
            this.hudManager = new HudManager(this);
            this.hudManager.start();
        }
    }

    // ---- Accessors -----------------------------------------------------

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public StatusManager getStatusManager() {
        return statusManager;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    public ProjectileManager getProjectileManager() {
        return projectileManager;
    }

    public UpgradeService getUpgradeService() {
        return upgradeService;
    }

    public HudManager getHudManager() {
        return hudManager;
    }

    public AngelElement getAdminBrush(UUID uuid) {
        return adminBrush.get(uuid);
    }

    public void setAdminBrush(UUID uuid, AngelElement element) {
        if (element == null) {
            adminBrush.remove(uuid);
        } else {
            adminBrush.put(uuid, element);
        }
    }
}
