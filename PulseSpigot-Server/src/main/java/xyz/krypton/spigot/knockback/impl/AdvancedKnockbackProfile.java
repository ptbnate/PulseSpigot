package xyz.krypton.spigot.knockback.impl;

import net.minecraft.server.Entity;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MathHelper;
import net.minecraft.server.PacketPlayOutEntityVelocity;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;
import xyz.krypton.spigot.knockback.KnockbackModifier;
import xyz.krypton.spigot.knockback.KnockbackProfile;

import java.util.Arrays;
import java.util.List;

/*
 *
 *  * Copyright (c) 2024 Krypton Development Services. All rights reserved.
 *  * Author: Nate
 *  * This code is proprietary and not to be used or shared without permission.
 *  * Unauthorized use may result in appropriate actions being taken.
 *
 */

public class AdvancedKnockbackProfile extends KnockbackProfile {

    public AdvancedKnockbackProfile(String name) {
        super(name);
    }

    @Override
    public List<KnockbackModifier<?>> getDefaultModifiers() {
        return Arrays.asList(
                new KnockbackModifier<>(double.class, "horizontal", 0.9055),
                new KnockbackModifier<>(double.class, "vertical", 0.8835),
                new KnockbackModifier<>(boolean.class,"combo-mode", false),
                new KnockbackModifier<>(double.class,"combo-max-height", 3.0),
                new KnockbackModifier<>(double.class,"combo-velocity", -0.05),
                new KnockbackModifier<>(double.class, "arrow-horizontal", 0.4),
                new KnockbackModifier<>(double.class, "arrow-vertical", 0.4),
                new KnockbackModifier<>(double.class, "egg-horizontal", 0.4),
                new KnockbackModifier<>(double.class, "egg-vertical", 0.4),
                new KnockbackModifier<>(double.class, "pearl-horizontal", 0.4),
                new KnockbackModifier<>(double.class, "pearl-vertical", 0.4),
                new KnockbackModifier<>(double.class, "snowball-horizontal", 0.4),
                new KnockbackModifier<>(double.class, "snowball-vertical", 0.4),
                new KnockbackModifier<>(double.class, "rod-horizontal", 0.4),
                new KnockbackModifier<>(double.class, "rod-speed", 1.5),
                new KnockbackModifier<>(double.class, "rod-vertical", 0.4),
                new KnockbackModifier<>(double.class, "start-range", 3.0),
                new KnockbackModifier<>(double.class, "max-range", 3.0),
                new KnockbackModifier<>(Integer.class, "no-damage-ticks", 20),
                new KnockbackModifier<>(double.class, "range-factor", 3.0),
                new KnockbackModifier<>(boolean.class, "one-point-seven", false),
                new KnockbackModifier<>(double.class, "friction", 2.0),
                new KnockbackModifier<>(boolean.class, "w-tap", false),
                new KnockbackModifier<>(double.class, "extra-horizontal", 0.2),
                new KnockbackModifier<>(double.class, "extra-vertical", 0.1),
                new KnockbackModifier<>(double.class, "vertical-limit", 0.3534)
        );
    }

    @Override
    public String getImplementationName() {
        return "advanced";
    }

    public double getDistanceBetweenEntities(Entity entity1, Entity entity2) {
        if (!entity1.getBukkitEntity().getWorld().equals(entity2.getBukkitEntity().getWorld())) {
            return -1.0;
        }
        return entity1.getBukkitEntity().getLocation().distance(entity2.getBukkitEntity().getLocation());
    }

    private double rangeReduction(double distance) {
        double startRangeReduction = (double) getKnockbackModifier("start-range", false).getValue();
        double rangeFactor = (double) getKnockbackModifier("range-factor", false).getValue();
        double maxRangeReduction = (double) getKnockbackModifier("max-range", false).getValue();

        if (distance >= startRangeReduction) {
            return Math.min(((distance) - startRangeReduction) * rangeFactor, maxRangeReduction);
        }
        return 0.0;
    }

    @Override
    public void handleEntityLiving(EntityPlayer victim, Entity source, float f, double d0, double d1) {
        victim.ai = true;

        double horizontal = (double) getKnockbackModifier("horizontal", false).getValue();
        double vertical = (double) getKnockbackModifier("vertical", false).getValue();
        double verticalLimit = (double) getKnockbackModifier("vertical-limit", false).getValue();
        double friction = (double) getKnockbackModifier("friction", false).getValue();

        double entityDistance = getDistanceBetweenEntities(source, victim);

        victim.motX /= friction;
        victim.motY /= friction;
        victim.motZ /= friction;

        double reduction = rangeReduction(entityDistance);

        horizontal *= (1.0 - reduction);

        double magnitude = Math.sqrt(d0 * d0 + d1 * d1);

        victim.motX -= (d0 / magnitude) * horizontal;
        victim.motZ -= (d1 / magnitude) * horizontal;

        if ((boolean) getKnockbackModifier("combo-mode", false).getValue()) {
            double deltaY = source.locY - victim.locY;

            if (deltaY < (double) getKnockbackModifier("combo-max-height", false).getValue()) {
                victim.motY += vertical;
            } else {
                victim.motY += (double) getKnockbackModifier("combo-velocity", false).getValue();
            }
        } else {
            victim.motY += vertical;
        }

        if (victim.motY > verticalLimit)
            victim.motY = verticalLimit;

    }

    @Override
    public void handleEntityHuman(EntityPlayer victim, Entity source, int i, Vector vector) {
        boolean onePointSeven = (boolean) getKnockbackModifier("one-point-seven", false).getValue();
        boolean sameDirection = source instanceof EntityPlayer
                && (((EntityPlayer) source).positiveXMovement == victim.positiveXMovement)
                && (((EntityPlayer) source).positiveZMovement == victim.positiveZMovement);
        if ((onePointSeven && !sameDirection) || i > 0) {
            double extraHorizontal = (double) getKnockbackModifier("extra-horizontal", false).getValue();
            boolean wTap = (boolean) getKnockbackModifier("w-tap", false).getValue();
            source.g(
                    (-MathHelper.sin(victim.yaw * 3.1415927F / 180.0F) *
                            (float) (i == 0 ? 1 : i)
                            * extraHorizontal),
                    (double) getKnockbackModifier("extra-vertical", false).getValue(),
                    (MathHelper.cos(victim.yaw * 3.1415927F / 180.0F) *
                            (float) (i == 0 ? 1 : i) * extraHorizontal)
            );

            if (wTap) victim.setSprinting(false);
        }

        if (source instanceof EntityPlayer && source.velocityChanged) {
            EntityPlayer attackedPlayer = (EntityPlayer) source;
            PlayerVelocityEvent event = new PlayerVelocityEvent(attackedPlayer.getBukkitEntity(), attackedPlayer.getBukkitEntity().getVelocity());
            victim.world.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                attackedPlayer.getBukkitEntity().setVelocityDirect(event.getVelocity());
                attackedPlayer.playerConnection.sendPacket(new PacketPlayOutEntityVelocity(attackedPlayer));
            }

            attackedPlayer.velocityChanged = false;
            attackedPlayer.motX = vector.getX();
            attackedPlayer.motY = vector.getY();
            attackedPlayer.motZ = vector.getZ();
        }
    }

    @Override
    public int getNoDamageTicks() {
        return (int) getKnockbackModifier("no-damage-ticks", false).getValue();
    }

    @Override
    public double getArrowHorizontal() {
        return (double) getKnockbackModifier("arrow-horizontal", false).getValue();
    }

    @Override
    public double getArrowVertical() {
        return (double) getKnockbackModifier("arrow-vertical", false).getValue();
    }

    @Override
    public double getEggHorizontal() {
        return (double) getKnockbackModifier("egg-horizontal", false).getValue();
    }

    @Override
    public double getEggVertical() {
        return (double) getKnockbackModifier("egg-vertical", false).getValue();
    }

    @Override
    public double getPearlHorizontal() {
        return (double) getKnockbackModifier("pearl-horizontal", false).getValue();
    }

    @Override
    public double getPearlVertical() {
        return (double) getKnockbackModifier("pearl-vertical", false).getValue();
    }

    @Override
    public double getRodHorizontal() {
        return (double) getKnockbackModifier("rod-horizontal", false).getValue();
    }

    @Override
    public double getRodSpeed() {
        return (double) getKnockbackModifier("rod-speed", false).getValue();
    }

    @Override
    public double getRodVertical() {
        return (double) getKnockbackModifier("rod-vertical", false).getValue();
    }

    @Override
    public double getSnowballHorizontal() {
        return (double) getKnockbackModifier("snowball-horizontal", false).getValue();
    }

    @Override
    public double getSnowballVertical() {
        return (double) getKnockbackModifier("snowball-vertical", false).getValue();
    }
}