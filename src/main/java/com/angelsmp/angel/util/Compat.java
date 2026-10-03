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
 * Cross-version / cross-platform compatibility layer (Paper, Spigot, Purpur,
 * Minecraft 1.21+). Every symbol that has been renamed or moved across versions
 * is resolved <b>by name at runtime</b> and degrades gracefully instead of
 * throwing {@code NoSuchFieldError} / {@code NoSuchMethodError}. No NMS.
 */
public final class Compat {

    private Compat() {
    }

    private static final Map<String, PotionEffectType> EFFECT_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Particle> PARTICLE_CACHE = new ConcurrentHashMap<>();

    // ---- PotionEffectType ---------------------------------------------

    public static PotionEffectType effect(String... candidates) {
        String cacheKey = String.join("|", candidates);
        PotionEffectType cached = EFFECT_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        PotionEffectType found = null;
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
        if (found == null) {
            for (String name : candidates) {
                Object value = registryLookup("EFFECT", name.toLowerCase(Locale.ROOT));
                if (value instanceof PotionEffectType type) {
                    found = type;
                    break;
                }
            }
        }
        if (found != null) {
            EFFECT_CACHE.put(cacheKey, found);
        }
        return found;
    }

    // ---- Particle ------------------------------------------------------

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

    public static double maxHealth(LivingEntity entity) {
        try {
            Method method = LivingEntity.class.getMethod("getMaxHealth");
            Object value = method.invoke(entity);
            if (value instanceof Number number) {
                return number.doubleValue();
            }
        } catch (Throwable ignored) {
            // fall through
        }
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

    public static void sendActionBar(Player player, String legacyText) {
        try {
            Method method = player.getClass().getMethod("sendActionBar", String.class);
            method.invoke(player, legacyText);
            return;
        } catch (Throwable ignored) {
            // fall through
        }
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
        player.sendMessage(legacyText);
    }

    // ---- Title ---------------------------------------------------------

    public static void sendTitle(Player player, String title, String subtitle,
                                 int fadeIn, int stay, int fadeOut) {
        try {
            Method method = player.getClass().getMethod("sendTitle", String.class, String.class,
                    int.class, int.class, int.class);
            method.invoke(player, title, subtitle, fadeIn, stay, fadeOut);
            return;
        } catch (Throwable ignored) {
            // fall through
        }
        try {
            Method method = player.getClass().getMethod("sendTitle", String.class, String.class);
            method.invoke(player, title, subtitle);
            return;
        } catch (Throwable ignored) {
            // fall through
        }
        player.sendMessage(title + " " + subtitle);
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
