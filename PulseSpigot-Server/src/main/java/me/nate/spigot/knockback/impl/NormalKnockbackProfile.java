package me.nate.spigot.knockback.impl;

import me.nate.spigot.knockback.KnockbackModifier;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MathHelper;
import net.minecraft.server.PacketPlayOutEntityVelocity;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;
import me.nate.spigot.knockback.KnockbackProfile;
import me.nate.spigot.util.MathUtil;

import java.util.Arrays;
import java.util.List;

/*
 *
 *  * Copyright (c) 2024 Krypton Development Services. All rights reserved.
 *  * Author: Nate
 *  * victim code is proprietary and not to be used or shared without permission.
 *  * Unauthorized use may result in appropriate actions being taken.
 *
 */

public class NormalKnockbackProfile extends KnockbackProfile {

    public NormalKnockbackProfile(String name) {
        super(name);
    }

    @Override
    public List<KnockbackModifier<?>> getDefaultModifiers() {
        return Arrays.asList(
                new KnockbackModifier<>(double.class, "horizontal", 0.9055),
                new KnockbackModifier<>(double.class, "vertical", 0.8835),
                new KnockbackModifier<>(double.class, "friction", 2.0),
                new KnockbackModifier<>(double.class, "slowdown", 0.6),
                new KnockbackModifier<>(double.class, "arrow-horizontal", 0.4),
                new KnockbackModifier<>(double.class, "rod-speed", 1.5),
                new KnockbackModifier<>(double.class, "arrow-vertical", 0.4),
                new KnockbackModifier<>(double.class, "egg-horizontal", 0.4),
                new KnockbackModifier<>(double.class, "egg-vertical", 0.4),
                new KnockbackModifier<>(double.class, "pearl-horizontal", 0.4),
                new KnockbackModifier<>(double.class, "pearl-vertical", 0.4),
                new KnockbackModifier<>(double.class, "snowball-horizontal", 0.4),
                new KnockbackModifier<>(double.class, "snowball-vertical", 0.4),
                new KnockbackModifier<>(double.class, "rod-horizontal", 0.4),
                new KnockbackModifier<>(double.class, "rod-vertical", 0.4),
                new KnockbackModifier<>(boolean.class, "one-point-seven", false),
                new KnockbackModifier<>(double.class, "extra-horizontal", 0.4),
                new KnockbackModifier<>(Integer.class, "no-damage-ticks", 20),
                new KnockbackModifier<>(double.class, "extra-vertical", 0.4),
                new KnockbackModifier<>(double.class, "vertical-max", 0.3534),
                new KnockbackModifier<>(double.class, "vertical-min", -1.0)
        );
    }

    @Override
    public String getImplementationName() {
        return "normal";
    }

    @Override
    public void handleEntityLiving(EntityPlayer victim, Entity source, float f, double d0, double d1) {
        victim.ai = true;

        double magnitude = Math.sqrt(MathUtil.pow2(d0) + MathUtil.pow2(d1));
        double horizontal = (double) getKnockbackModifier("horizontal", false).getValue();
        double vertical = (double) getKnockbackModifier("vertical", false).getValue();
        double friction = (double) getKnockbackModifier("friction", false).getValue();
        double verticalMax = (double) getKnockbackModifier("vertical-max", false).getValue();
        double verticalMin = (double) getKnockbackModifier("vertical-min", false).getValue();

        victim.motX /= friction;
        victim.motY /= friction;
        victim.motZ /= friction;

        victim.motX -= d0 / magnitude * horizontal;
        victim.motY += vertical;
        victim.motZ -= d1 / magnitude * horizontal;

        if (victim.motY > verticalMax) victim.motY = verticalMax;
        if (victim.motY < verticalMin) victim.motY = verticalMin;
    }

    @Override
    public void handleEntityHuman(EntityPlayer victim, Entity source, int i, Vector vector) {
        boolean onePointSeven = (boolean) getKnockbackModifier("one-point-seven", false).getValue();
        double extraHorizontal = (double) getKnockbackModifier("extra-horizontal", false).getValue();
        double slowdown = (double) getKnockbackModifier("slowdown", false).getValue();
        boolean sameDirection = source instanceof EntityPlayer
                && (((EntityPlayer) source).positiveXMovement == victim.positiveXMovement)
                && (((EntityPlayer) source).positiveZMovement == victim.positiveZMovement);

        if (source instanceof EntityPlayer && source.velocityChanged) {
            if ((onePointSeven && !sameDirection) || i > 0) {
                source.g(
                        (-MathHelper.sin(victim.yaw * 3.1415927F / 180.0F) *
                                (float) (i == 0 ? 1 : i)
                                * extraHorizontal),
                        (double) getKnockbackModifier("extra-vertical", false).getValue(),
                        (MathHelper.cos(victim.yaw * 3.1415927F / 180.0F) *
                                (float) (i == 0 ? 1 : i) * extraHorizontal)
                );

                victim.motX *= slowdown;
                victim.motZ *= slowdown;
                victim.setSprinting(false);
            }

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
    public double getRodSpeed() {
        return (double) getKnockbackModifier("rod-speed", false).getValue();
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
