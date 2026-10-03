package com.angelsmp.angel.util;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cross-version / cross-platform compatibility layer.
 *
 * <p>AngelSMP targets Minecraft <b>1.21 through the latest stable release</b> on
 * <b>Paper, Spigot and Purpur</b>. Over that range a handful of Bukkit symbols
 * have been renamed, moved or added, and a few conveniences exist on Paper only.
 * Referencing those symbols directly at compile time produces
 * {@code NoSuchFieldError} / {@code NoSuchMethodError} at runtime on the wrong
 * platform or version.</p>
 *
 * <p>This class resolves every such symbol <b>by name at runtime</b>, caching the
 * result, and degrades gracefully (returns {@code null} / a safe default) when a
 * symbol is genuinely unavailable rather than crashing the ability. No NMS and
 * no version-specific code is used anywhere.</p>
 */
public final class Compat {

    private Compat() {
    }

    private static final Map<String, PotionEffectType> EFFECT_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Particle> PARTICLE_CACHE = new ConcurrentHashMap<>();

    // ---- PotionEffectType ---------------------------------------------

    /**
     * Resolves a potion effect by any of its historical names, e.g.
     * {@code effect("RESISTANCE", "DAMAGE_RESISTANCE")}.
     *
     * @return the resolved type, or {@code null} if unavailable on this server.
     */
    public static PotionEffectType effect(String... candidates) {
        String cacheKey = String.join("|", candidates);
        PotionEffectType cached = EFFECT_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        PotionEffectType found = null;
        // 1) Static field lookup (fast, present on every supported version).
        for (String name : candidates) {
            try {
                Field field = PotionEffectType.class.getField(name);
                Object value = field.get(null);
                if (value instanceof PotionEffectType type) {
                    found = type;
                    break;
                }
            } catch (Throwable ignored) {
                // try next candidate
            }
        }
        // 2) Registry lookup by namespaced key (1.20.5+ data-driven registry).
        if (found == null) {
            for (String name : candidates) {
                Object value = registryLookup("EFFECT", name.toLowerCase(Locale.ROOT));
                if (value instanceof PotionEffectType type) {
                    found = type;
                    break;
                }
            }
        }
        // 3) Legacy PotionEffectType#getByName.
        if (found == null) {
            for (String name : candidates) {
                try {
                    Method method = PotionEffectType.class.getMethod("getByName", String.class);
                    Object value = method.invoke(null, name);
                    if (value instanceof PotionEffectType type) {
                        found = type;
                        break;
                    }
                } catch (Throwable ignored) {
                    // try next candidate
                }
            }
        }
        if (found != null) {
            EFFECT_CACHE.put(cacheKey, found);
        }
        return found;
    }

    // ---- Particle ------------------------------------------------------

    /**
     * Resolves a particle from its {@code minecraft:} key, falling back to the
     * data-driven registry so newly renamed particles keep working.
     */
    public static Particle particle(String key) {
        if (key == null) {
            return null;
        }
        String clean = key.toLowerCase(Locale.ROOT);
        if (clean.startsWith("minecraft:")) {
            clean = clean.substring("minecraft:".length());
        }
        Particle cached = PARTICLE_CACHE.get(clean);
        if (cached != null) {
            return cached;
        }
        Particle found = null;
        try {
            found = Particle.valueOf(clean.toUpperCase(Locale.ROOT));
        } catch (Throwable ignored) {
            // fall through to registry lookup
        }
        if (found == null) {
            Object value = registryLookup("PARTICLE", clean);
            if (value instanceof Particle particle) {
                found = particle;
            }
        }
        if (found != null) {
            PARTICLE_CACHE.put(clean, found);
        }
        return found;
    }

    // ---- Max health ----------------------------------------------------

    /**
     * Reads an entity's maximum health without binding to a specific
     * {@code Attribute} constant (which was renamed in newer versions).
     */
    public static double maxHealth(LivingEntity entity) {
        // 1) Legacy LivingEntity#getMaxHealth (present on all supported versions).
        try {
            Method method = LivingEntity.class.getMethod("getMaxHealth");
            Object value = method.invoke(entity);
            if (value instanceof Number number) {
                return number.doubleValue();
            }
        } catch (Throwable ignored) {
            // fall through
        }
        // 2) Attribute lookup, trying both the new and legacy constant names.
        try {
            Class<?> attributeClass = Class.forName("org.bukkit.attribute.Attribute");
            Method getAttribute = LivingEntity.class.getMethod("getAttribute", attributeClass);
            for (String name : new String[]{"MAX_HEALTH", "GENERIC_MAX_HEALTH"}) {
                try {
                    Object attribute = attributeClass.getField(name).get(null);
                    Object instance = getAttribute.invoke(entity, attribute);
                    if (instance != null) {
                        Object value = instance.getClass().getMethod("getValue").invoke(instance);
                        if (value instanceof Number number) {
                            return number.doubleValue();
                        }
                    }
                } catch (Throwable ignored) {
                    // try next constant name
                }
            }
        } catch (Throwable ignored) {
            // fall through
        }
        return 20.0;
    }

    // ---- Action bar ----------------------------------------------------

    /**
     * Sends an action-bar message using whichever API the running server
     * exposes (Paper's {@code sendActionBar}, Spigot's Bungee
     * {@code ChatMessageType.ACTION_BAR}, or a chat fallback).
     */
    public static void sendActionBar(Player player, String legacyText) {
        // 1) Paper / modern: Player#sendActionBar(String).
        try {
            Method method = player.getClass().getMethod("sendActionBar", String.class);
            method.invoke(player, legacyText);
            return;
        } catch (Throwable ignored) {
            // fall through
        }
        // 2) Spigot: player.spigot().sendMessage(ChatMessageType.ACTION_BAR, BaseComponent...).
        try {
            Object spigot = player.getClass().getMethod("spigot").invoke(player);
            Class<?> messageType = Class.forName("net.md_5.bungee.api.ChatMessageType");
            Object actionBar = messageType.getField("ACTION_BAR").get(null);
            Class<?> textComponent = Class.forName("net.md_5.bungee.api.chat.TextComponent");
            Object components = textComponent.getMethod("fromLegacyText", String.class)
                    .invoke(null, legacyText);
            Method send = spigot.getClass().getMethod("sendMessage", messageType, components.getClass());
            send.invoke(spigot, actionBar, components);
            return;
        } catch (Throwable ignored) {
            // fall through
        }
        // 3) Last resort: plain chat message.
        player.sendMessage(legacyText);
    }

    // ---- Registry helper ----------------------------------------------

    private static Object registryLookup(String registryField, String key) {
        try {
            Class<?> registryClass = Class.forName("org.bukkit.Registry");
            Object registry = registryClass.getField(registryField).get(null);
            Class<?> namespacedKey = Class.forName("org.bukkit.NamespacedKey");
            Object namespaced = namespacedKey.getMethod("minecraft", String.class).invoke(null, key);
            Method get = registryClass.getMethod("get", namespacedKey);
            return get.invoke(registry, namespaced);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
