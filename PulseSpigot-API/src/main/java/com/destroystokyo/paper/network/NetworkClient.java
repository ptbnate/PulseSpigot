package com.destroystokyo.paper.network;

import java.net.InetSocketAddress;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a client connected to the server.
 */
public interface NetworkClient {

    /**
     * Returns the socket address of the client.
     *
     * @return The client's socket address
     */
    @NotNull
    InetSocketAddress getAddress();

    /**
     * Returns the protocol version of the client.
     *
     * @return The client's protocol version, or {@code -1} if unknown
     * @see <a href="http://wiki.vg/Protocol_version_numbers">List of protocol
     *     version numbers</a>
     */
    int getProtocolVersion();

    /**
     * Returns the virtual host the client is connected to.
     *
     * <p>The virtual host refers to the hostname/port the client used to
     * connect to the server.</p>
     *
     * @return The client's virtual host, or {@code null} if unknown
     */
    @Nullable
    InetSocketAddress getVirtualHost();

}
