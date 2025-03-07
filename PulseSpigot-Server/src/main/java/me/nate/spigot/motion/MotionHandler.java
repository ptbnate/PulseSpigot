package me.nate.spigot.motion;

import java.util.HashMap;
import java.util.Map;

/*
 *
 *  * Copyright (c) 2024 Krypton Development Services. All rights reserved.
 *  * Author: Nate
 *  * This code is proprietary and not to be used or shared without permission.
 *  * Unauthorized use may result in appropriate actions being taken.
 *
 *  Note: Never use float getters since they are always null thanks to java, if you're interested in finding why they are null then go ahead and goodluck!
 *
 */

public class MotionHandler {

    private float potionOffset;
    private double potionSpeed;
    private float potionFallSpeed;
    private boolean reducedSplash;
    private boolean smallPearlHitbox;
    private int potionTime;
    private boolean smoothPots;

    private float pearlGravity;
    private float pearlSpeed;
    private float pearlOffset;

    private final Map<String, Object> properties = new HashMap<>();

    public MotionHandler() {
        reloadProperties();
    }

    public void reloadProperties() {

        this.potionOffset = (float) MotionConfig.getMotionModifier("potion-offset", -20.0F);
        this.potionTime = MotionConfig.getIntMotionModifier("potion-time", 5);
        this.potionSpeed = MotionConfig.getMotionModifier("potion-speed", 1.0);
        this.reducedSplash = MotionConfig.getBooleanMotionModifier("potion-reduced-splash", false);
        this.potionFallSpeed = (float) MotionConfig.getMotionModifier("potion-fall-speed", 0.05F);
        this.smoothPots = MotionConfig.getBooleanMotionModifier("potion-smooth", true);

        this.pearlGravity = (float) MotionConfig.getMotionModifier("pearl-gravity", 0.03F);
        this.smallPearlHitbox = MotionConfig.getBooleanMotionModifier("pearl-small-hitbox", true);
        this.pearlSpeed = (float) MotionConfig.getMotionModifier("pearl-speed", 1.5F);
        this.pearlOffset = (float) MotionConfig.getMotionModifier("pearl-offset", 0.0F);

        properties.clear();
        properties.put("potion-time", potionTime);
        properties.put("potion-offset", potionOffset);
        properties.put("potion-fall-speed", potionFallSpeed);
        properties.put("potion-speed", potionSpeed);
        properties.put("potion-reduced-splash", reducedSplash);
        properties.put("potion-smooth", smoothPots);

        properties.put("pearl-gravity", pearlGravity);
        properties.put("pearl-small-hitbox", smallPearlHitbox);
        properties.put("pearl-speed", pearlSpeed);
        properties.put("pearl-offset", pearlOffset);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public boolean isValidProperty(String key) {
        return properties.containsKey(key);
    }

    public boolean setProperty(String key, String value) {
        if (!isValidProperty(key)) return false;
        try {
            Object currentValue = properties.get(key);
            Object newValue;

            if (currentValue instanceof Float) {
                newValue = Float.parseFloat(value);
            } else if (currentValue instanceof Double) {
                newValue = Double.parseDouble(value);
            } else if (currentValue instanceof Boolean) {
                newValue = Boolean.parseBoolean(value);
            } else if (currentValue instanceof Integer) {
                newValue = Integer.parseInt(value);
            } else {
                return false;
            }

            properties.put(key, newValue);
            MotionConfig.setMotionModifier(key, newValue);
            reloadProperties();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getPotionTime() {
        return potionTime;
    }

    public float getPotionOffset() {
        return potionOffset;
    }

    public float getPotionFallSpeed() {
        return potionFallSpeed;
    }

    public float getPearlSpeed() {
        return pearlSpeed;
    }

    public float getPearlOffset() {
        return pearlOffset;
    }

    public float getPearlGravity() {
        return pearlGravity;
    }

    public double getPotionSpeed() {
        return potionSpeed;
    }

    public boolean isSmoothPots() {
        return smoothPots;
    }

    public boolean isReducedSplash() {
        return reducedSplash;
    }

    public boolean isSmallPearlHitbox() {
        return smallPearlHitbox;
    }
}
