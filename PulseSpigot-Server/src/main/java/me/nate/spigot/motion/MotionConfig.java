package me.nate.spigot.motion;

import me.nate.spigot.util.yaml.YamlConfig;

import java.util.HashMap;
import java.util.Map;

/*
 *
 *  * Copyright (c) 2024 Krypton Development Services. All rights reserved.
 *  * Author: Nate
 *  * This code is proprietary and not to be used or shared without permission.
 *  * Unauthorized use may result in appropriate actions being taken.
 *
 */

public class MotionConfig {

    private static YamlConfig motionConfig;
    private static final Map<String, Double> defaultMotionModifiers = new HashMap<>();
    private static final Map<String, Integer> defaultIntMotionModifiers = new HashMap<>();
    private static final Map<String, Boolean> defaultBooleanModifiers = new HashMap<>();

    static {
        try {
            motionConfig = new YamlConfig("motion.yml");
            motionConfig.reload();
            initializeDefaults();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initializeDefaults() {
        defaultMotionModifiers.put("potion-offset", -20.0);
        defaultMotionModifiers.put("potion-fall-speed", 0.05);
        defaultIntMotionModifiers.put("potion-time", 5);
        defaultBooleanModifiers.put("potion-reduced-splash", false);
        defaultMotionModifiers.put("potion-speed", 1.0);
        defaultBooleanModifiers.put("potion-smooth", true);

        defaultMotionModifiers.put("pearl-gravity", 0.03);
        defaultBooleanModifiers.put("pearl-small-hitbox", true);
        defaultMotionModifiers.put("pearl-speed", 1.5);
        defaultMotionModifiers.put("pearl-offset", 0.0);

        setDefaults(defaultMotionModifiers);
        setDefaults(defaultIntMotionModifiers);
        setDefaults(defaultBooleanModifiers);

        motionConfig.save();
    }

    private static <T> void setDefaults(Map<String, T> defaultModifiers) {
        for (Map.Entry<String, T> entry : defaultModifiers.entrySet()) {
            if (!motionConfig.getConfig().contains(entry.getKey())) {
                motionConfig.getConfig().set(entry.getKey(), entry.getValue());
            }
        }
    }

    public static double getMotionModifier(String key, double defaultValue) {
        return motionConfig.getConfig().getDouble(key, defaultValue);
    }

    public static int getIntMotionModifier(String key, int defaultValue) {
        return motionConfig.getConfig().getInt(key, defaultValue);
    }

    public static boolean getBooleanMotionModifier(String key, boolean defaultValue) {
        return motionConfig.getConfig().getBoolean(key, defaultValue);
    }

    public static void setMotionModifier(String key, Object value) {
        motionConfig.getConfig().set(key, value);
        motionConfig.save();
    }
}
