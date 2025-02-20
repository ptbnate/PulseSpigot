package org.spigotmc;

import net.minecraft.server.Entity;
import net.minecraft.server.EntityExperienceOrb;
import net.minecraft.server.EntityGhast;
import net.minecraft.server.EntityItem;
import net.minecraft.server.EntityItemFrame;
import net.minecraft.server.EntityPainting;
import net.minecraft.server.EntityPlayer;
import xyz.krypton.spigot.config.WorldConfig;

public class TrackingRange
{

    /**
     * Gets the range an entity should be 'tracked' by players and visible in
     * the client.
     *
     * @param entity
     * @param defaultRange Default range defined by Mojang
     * @return
     */
    public static int getEntityTrackingRange(Entity entity, int defaultRange)
    {
        WorldConfig config = entity.world.spigotConfigPulseSpigot;
        WorldConfig.EntityTrackingRange trackingConfig = config.entityTrackingRange;
        if ( entity instanceof EntityPlayer )
        {
            return trackingConfig.players;
        }  else if ( entity.activationType == 1 )
        {
            return trackingConfig.monsters;
        } else if ( entity instanceof EntityGhast )
        {
            if ( trackingConfig.monsters > config.entityActivationRange.monsters )
            {
                return trackingConfig.monsters;
            } else
            {
                return config.entityActivationRange.monsters;
            }
        } else if ( entity.activationType == 2 )
        {
            return trackingConfig.animals;
        } else if ( entity instanceof EntityItemFrame || entity instanceof EntityPainting || entity instanceof EntityItem || entity instanceof EntityExperienceOrb )
        {
            return trackingConfig.misc;
        } else 
        {
            return trackingConfig.other;
        }
    }
}
