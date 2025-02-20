package com.destroystokyo.paper.network;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import javax.annotation.Nullable;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import org.bukkit.entity.Player;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.NotNull;

class PaperServerListPingEventImpl extends PaperServerListPingEvent {

    private final MinecraftServer server;

    PaperServerListPingEventImpl(MinecraftServer server, StatusClient client, int protocolVersion, @Nullable CachedServerIcon icon) {
        super(client, server.getMotd(), server.getPlayerList().getPlayerCount(), server.getPlayerList().getMaxPlayers(),
                server.getServerModName() + ' ' + "1.8.8", protocolVersion, icon);
        this.server = server;
    }

    @Override
    protected final Object[] getOnlinePlayers() {
        return this.server.getPlayerList().players.toArray();
    }

    @Override
    protected final Player getBukkitPlayer(Object player) {
        return ((EntityPlayer) player).getBukkitEntity();
    }

}
