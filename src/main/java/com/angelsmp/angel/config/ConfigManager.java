package com.angelsmp.angel.config;

import com.angelsmp.angel.AngelPlugin;

/** Phase 7 - typed access to the structured config.yml blueprint. */
public class ConfigManager {

    private final AngelPlugin plugin;

    public ConfigManager(AngelPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
    }

    // ---- server-settings ----------------------------------------------
    public boolean checkUpdates() {
        return plugin.getConfig().getBoolean("server-settings.check-updates", true);
    }

    public int saveIntervalMinutes() {
        return plugin.getConfig().getInt("server-settings.save-interval-minutes", 5);
    }

    // ---- alignment-settings -------------------------------------------
    public int haloParticleDensity() {
        return plugin.getConfig().getInt("alignment-settings.halo-particle-density", 15);
    }

    public double haloSpinSpeed() {
        return plugin.getConfig().getDouble("alignment-settings.halo-spin-speed", 1.2);
    }

    public boolean enableDevilHorns() {
        return plugin.getConfig().getBoolean("alignment-settings.enable-devil-horns", true);
    }

    // ---- ability-settings ---------------------------------------------
    public double fireDamage() {
        return plugin.getConfig().getDouble("ability-settings.fire-angel.base-damage", 8.0);
    }

    public int fireCooldown() {
        return plugin.getConfig().getInt("ability-settings.fire-angel.cooldown-seconds", 10);
    }

    public int fireBurnSeconds() {
        return plugin.getConfig().getInt("ability-settings.fire-angel.burn-duration-seconds", 6);
    }

    public int iceFreezeSeconds() {
        return plugin.getConfig().getInt("ability-settings.ice-angel.freeze-duration-seconds", 3);
    }

    public int iceCooldown() {
        return plugin.getConfig().getInt("ability-settings.ice-angel.cooldown-seconds", 15);
    }

    public int iceRange() {
        return plugin.getConfig().getInt("ability-settings.ice-angel.range-blocks", 12);
    }

    public double lightningDamage() {
        return plugin.getConfig().getDouble("ability-settings.lightning-angel.base-damage", 6.0);
    }

    public int lightningStunSeconds() {
        return plugin.getConfig().getInt("ability-settings.lightning-angel.stun-duration-seconds", 2);
    }

    public int lightningCooldown() {
        return plugin.getConfig().getInt("ability-settings.lightning-angel.cooldown-seconds", 12);
    }

    public int windCooldown() {
        return plugin.getConfig().getInt("ability-settings.wind-angel.cooldown-seconds", 8);
    }

    public double windDashBlocks() {
        return plugin.getConfig().getDouble("ability-settings.wind-angel.dash-blocks", 15.0);
    }

    public int windSafeFallBlocks() {
        return plugin.getConfig().getInt("ability-settings.wind-angel.safe-fall-blocks", 30);
    }

    public int earthCooldown() {
        return plugin.getConfig().getInt("ability-settings.earth-angel.cooldown-seconds", 20);
    }

    public int earthShieldSeconds() {
        return plugin.getConfig().getInt("ability-settings.earth-angel.shield-duration-seconds", 6);
    }

    public int lightCooldown() {
        return plugin.getConfig().getInt("ability-settings.light-angel.cooldown-seconds", 18);
    }

    public double lightHeal() {
        return plugin.getConfig().getDouble("ability-settings.light-angel.heal-amount", 8.0);
    }

    // ---- death-system --------------------------------------------------
    public boolean shatterParticles() {
        return plugin.getConfig().getBoolean("death-system.enable-shatter-particles", true);
    }

    public boolean dropSoulEssence() {
        return plugin.getConfig().getBoolean("death-system.drop-soul-essence", true);
    }

    public String soulEssenceItem() {
        return plugin.getConfig().getString("death-system.soul-essence-item", "NETHER_STAR");
    }

    public int lockoutMinutes() {
        return plugin.getConfig().getInt("death-system.lockout-duration-minutes", 15);
    }

    public boolean applyWeaknessOnRespawn() {
        return plugin.getConfig().getBoolean("death-system.apply-weakness-on-respawn", true);
    }

    public int weaknessSeconds() {
        return plugin.getConfig().getInt("death-system.weakness-duration-seconds", 30);
    }

    // ---- altar recovery (Phase 6B/6C) ---------------------------------
    public int altarItemAmount() {
        return plugin.getConfig().getInt("altar-recovery.item-amount", 3);
    }

    public String altarItemMaterial() {
        return plugin.getConfig().getString("altar-recovery.item-material", "DIAMOND_BLOCK");
    }

    // ---- upgrade (Phase 5) --------------------------------------------
    public String upgradeCurrency() {
        return plugin.getConfig().getString("upgrade.currency", "XP_LEVELS");
    }

    public int upgradeXpLevels() {
        return plugin.getConfig().getInt("upgrade.xp-levels-per-tier", 50);
    }

    public int upgradeTokens() {
        return plugin.getConfig().getInt("upgrade.tokens-per-tier", 10);
    }

    // ---- gui-settings --------------------------------------------------
    public String titleMainMenu() {
        return plugin.getConfig().getString("gui-settings.title-main-menu", "&8[&bAngel SMP&8] &7Menu");
    }

    public String titleAdminMenu() {
        return plugin.getConfig().getString("gui-settings.title-admin-menu", "&4&lAdmin Power Matrix");
    }

    public boolean fillEmptySlots() {
        return plugin.getConfig().getBoolean("gui-settings.fill-empty-slots-with-panes", true);
    }
}
