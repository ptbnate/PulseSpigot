package org.bukkit.event.server;

import org.bukkit.Server;
import org.bukkit.event.HandlerList;

public class ServerShutdownEvent extends ServerEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Server server;

    public ServerShutdownEvent(final Server server) {
        this.server = server;
    }

    /**
     * Gets the server that is shutting down.
     *
     * @return Server that is shutting down
     */
    public Server getServer() {
        return this.server;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}