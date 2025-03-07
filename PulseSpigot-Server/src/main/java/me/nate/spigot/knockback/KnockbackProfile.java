package me.nate.spigot.knockback;

import net.minecraft.server.Entity;
import net.minecraft.server.EntityPlayer;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/*
 *
 *  * Copyright (c) 2024 Krypton Development Services. All rights reserved.
 *  * Author: Nate
 *  * This code is proprietary and not to be used or shared without permission.
 *  * Unauthorized use may result in appropriate actions being taken.
 *
 */

public abstract class KnockbackProfile {

    private final String name;

    public KnockbackProfile(String name) {
        this.name = name;
    }

    /**
     * We sort this list alphabetical so that tab completion for commands won't look autistic,
     * we convert this {@link List} to a {@link CopyOnWriteArrayList} to ensure
     * the {@link KnockbackProfile#getKnockbackModifier(String, boolean)} is thread safe.
     */
    private final List<KnockbackModifier<?>> modifiers = getDefaultModifiers()
            .stream()
            .sorted((modifierOne, modifierTwo) ->
                    String.CASE_INSENSITIVE_ORDER.compare(modifierOne.getLabel(), modifierTwo.getLabel()))
            .collect(Collectors.toCollection(CopyOnWriteArrayList::new));

    public abstract List<KnockbackModifier<?>> getDefaultModifiers();

    public abstract String getImplementationName();

    public abstract void handleEntityLiving(EntityPlayer victim, Entity source, float f, double d0, double d1);

    public abstract void handleEntityHuman(EntityPlayer victim, Entity source, int i, Vector vector);

    public int getNoDamageTicks() {
        return 20;
    }

    public double getRodHorizontal() {
        return 0.4;
    }

    public double getRodSpeed() {
        return 1.5;
    }

    public double getRodVertical() {
        return 0.4;
    }

    public double getSnowballHorizontal() {
        return 0.4;
    }

    public double getSnowballVertical() {
        return 0.4;
    }

    public double getEggHorizontal() {
        return 0.4;
    }

    public double getEggVertical() {
        return 0.4;
    }

    public double getPearlHorizontal() {
        return 0.4;
    }

    public double getPearlVertical() {
        return 0.4;
    }

    public double getArrowHorizontal() {
        return 0.4;
    }

    public double getArrowVertical() {
        return 0.4;
    }

    public List<KnockbackModifier<?>> getModifiers() {
        return modifiers;
    }

    @SuppressWarnings("unchecked")
    public <T> KnockbackModifier<T> getKnockbackModifier(String label, boolean ignoreCase) {
        synchronized (modifiers) {
            return (KnockbackModifier<T>) modifiers.stream()
                    .filter(modifier -> (ignoreCase ?
                            modifier.getLabel().equalsIgnoreCase(label) : modifier.getLabel().equals(label)))
                    .findFirst()
                    .orElse(null);
        }
    }

    public String getName() {
        return name;
    }

    public KnockbackProfile modify(String label, Object newValue) {
        KnockbackModifier<?> modifier = getKnockbackModifier(label, true);

        if (modifier == null)
            throw new IllegalArgumentException("KnockbackModifier with label " + label + " does not exist!");

        modifier.setValueUnsafe(newValue);
        return this;
    }

    public KnockbackProfile modify(KnockbackModifier<?> modifier, Object newValue) {
        modifier.setValueUnsafe(newValue);
        return this;
    }
}

