package com.destroystokyo.paper.event.server;

import com.destroystokyo.paper.network.StatusClient;
import com.destroystokyo.paper.profile.PlayerProfile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import static java.util.Objects.requireNonNull;

/**
 * Extended version of {@link ServerListPingEvent} that allows full control
 * of the response sent to the client.
 */
public class PaperServerListPingEvent extends ServerListPingEvent implements Cancellable {

    @NotNull private final StatusClient client;

    private int numPlayers;
    private boolean hidePlayers;
    @NotNull private List<PlayerProfile> playerSample = new ArrayList<>();

    @NotNull private String version;
    private int protocolVersion;

    @Nullable private CachedServerIcon favicon;

    private boolean cancelled;

    private boolean originalPlayerCount = true;
    private Object[] players;

    public PaperServerListPingEvent(@NotNull StatusClient client, @NotNull String motd, int numPlayers, int maxPlayers,
                                    @NotNull String version, int protocolVersion, @Nullable CachedServerIcon favicon) {
        super(client.getAddress().getAddress(), motd, numPlayers, maxPlayers);
        this.client = client;
        this.numPlayers = numPlayers;
        this.version = version;
        this.protocolVersion = protocolVersion;
        setServerIcon(favicon);
    }

    /**
     * Returns the {@link StatusClient} pinging the server.
     *
     * @return The client
     */
    @NotNull
    public StatusClient getClient() {
        return this.client;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code -1} if players are hidden using
     * {@link #shouldHidePlayers()}.</p>
     */
    @Override
    public int getNumPlayers() {
        if (this.hidePlayers) {
            return -1;
        }

        return this.numPlayers;
    }

    /**
     * Sets the number of players displayed in the server list.
     *
     * <p>Note that this won't have any effect if {@link #shouldHidePlayers()}
     * is enabled.</p>
     *
     * @param numPlayers The number of online players
     */
    public void setNumPlayers(int numPlayers) {
        if (this.numPlayers != numPlayers) {
            this.numPlayers = numPlayers;
            this.originalPlayerCount = false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code -1} if players are hidden using
     * {@link #shouldHidePlayers()}.</p>
     */
    @Override
    public int getMaxPlayers() {
        if (this.hidePlayers) {
            return -1;
        }

        return super.getMaxPlayers();
    }

    /**
     * Returns whether all player related information is hidden in the server
     * list. This will cause {@link #getNumPlayers()}, {@link #getMaxPlayers()}
     * and {@link #getPlayerSample()} to be skipped in the response.
     *
     * <p>The Vanilla Minecraft client will display the player count as {@code ???}
     * when this option is enabled.</p>
     *
     * @return {@code true} if the player count is hidden
     */
    public boolean shouldHidePlayers() {
        return hidePlayers;
    }

    /**
     * Sets whether all player related information is hidden in the server
     * list. This will cause {@link #getNumPlayers()}, {@link #getMaxPlayers()}
     * and {@link #getPlayerSample()} to be skipped in the response.
     *
     * <p>The Vanilla Minecraft client will display the player count as {@code ???}
     * when this option is enabled.</p>
     *
     * @param hidePlayers {@code true} if the player count should be hidden
     */
    public void setHidePlayers(boolean hidePlayers) {
        this.hidePlayers = hidePlayers;
    }

    /**
     * Returns a mutable list of {@link PlayerProfile} that will be displayed
     * as online players on the client.
     *
     * <p>The Vanilla Minecraft client will display them when hovering the
     * player count with the mouse.</p>
     *
     * @return The mutable player sample list
     */
    @NotNull
    public List<PlayerProfile> getPlayerSample() {
        return this.playerSample;
    }

    // PulseSpigot start
    /**
     * Sets a mutable list of {@link PlayerProfile} that will be displayed
     * as online players on the client.
     *
     * <p>The Vanilla Minecraft client will display them when hovering the
     * player count with the mouse.</p>
     *
     * @param playerSample The mutable player sample list
     */
    public void setPlayerSample(List<PlayerProfile> playerSample) {
        if (playerSample == null) {
            return;
        }
        this.playerSample = playerSample;
    }
    // PulseSpigot end

    /**
     * Returns the version that will be sent as server version on the client.
     *
     * @return The server version
     */
    @NotNull
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version that will be sent as server version to the client.
     *
     * @param version The server version
     */
    public void setVersion(@NotNull String version) {
        this.version = requireNonNull(version, "version");
    }

    /**
     * Returns the protocol version that will be sent as the protocol version
     * of the server to the client.
     *
     * @return The protocol version of the server, or {@code -1} if the server
     * has not finished initialization yet
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Sets the protocol version that will be sent as the protocol version
     * of the server to the client.
     *
     * @param protocolVersion The protocol version of the server
     */
    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    /**
     * Gets the server icon sent to the client.
     *
     * @return The icon to send to the client, or {@code null} for none
     */
    @Nullable
    public CachedServerIcon getServerIcon() {
        return this.favicon;
    }

    /**
     * Sets the server icon sent to the client.
     *
     * @param icon The icon to send to the client, or {@code null} for none
     */
    @Override
    public void setServerIcon(@Nullable CachedServerIcon icon) {
        if (icon != null && icon.isEmpty()) {
            // Represent empty icons as null
            icon = null;
        }

        this.favicon = icon;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Cancelling this event will cause the connection to be closed immediately,
     * without sending a response to the client.</p>
     */
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Cancelling this event will cause the connection to be closed immediately,
     * without sending a response to the client.</p>
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Note:</b> For compatibility reasons, this method will return all
     * online players, not just the ones referenced in {@link #getPlayerSample()}.
     * Removing a player will:</p>
     *
     * <ul>
     *     <li>Decrement the online player count (if and only if) the player
     *     count wasn't changed by another plugin before.</li>
     *     <li>Remove all entries from {@link #getPlayerSample()} that refer to
     *     the removed player (based on their {@link UUID}).</li>
     * </ul>
     */
    @NotNull
    @Override
    public Iterator<Player> iterator() {
        if (this.players == null) {
            this.players = getOnlinePlayers();
        }

        return new PlayerIterator();
    }

    @NotNull
    protected Object[] getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().toArray();
    }

    @NotNull
    protected Player getBukkitPlayer(@NotNull Object player) {
        return (Player) player;
    }

    private final class PlayerIterator implements Iterator<Player> {

        private int next;
        private int current;
        @Nullable private Player player;

        @Override
        public boolean hasNext() {
            for (; this.next < players.length; this.next++) {
                if (players[this.next] != null) {
                    return true;
                }
            }

            return false;
        }

        @NotNull
        @Override
        public Player next() {
            if (!hasNext()) {
                this.player = null;
                throw new NoSuchElementException();
            }

            this.current = this.next++;
            return this.player = getBukkitPlayer(players[this.current]);
        }

        @Override
        public void remove() {
            if (this.player == null) {
                throw new IllegalStateException();
            }

            UUID uniqueId = this.player.getUniqueId();
            this.player = null;

            // Remove player from iterator
            players[this.current] = null;

            // Remove player from sample
            getPlayerSample().removeIf(p -> uniqueId.equals(p.getId()));

            // Decrement player count
            if (originalPlayerCount) {
                numPlayers--;
            }
        }
    }

}
