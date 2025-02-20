package net.minecraft.server;

import java.io.IOException;

public interface Packet<T extends PacketListener> {

    void a(PacketDataSerializer packetdataserializer) throws IOException;

    void b(PacketDataSerializer packetdataserializer) throws IOException;

    void a(T t0);

    // Paper start
    /**
     * @param player Null if not at PLAY stage yet
     */
    default void onPacketDispatch(@org.jetbrains.annotations.Nullable EntityPlayer player) {}

    /**
     * @param player Null if not at PLAY stage yet
     * @param future Can be null if packet was cancelled
     */
    default void onPacketDispatchFinish(@org.jetbrains.annotations.Nullable EntityPlayer player, @org.jetbrains.annotations.Nullable io.netty.channel.ChannelFuture future) {}
    default boolean hasFinishListener() { return false; }
    default boolean isReady() { return true; }
    default java.util.List<Packet> getExtraPackets() { return null; }
    // Paper end
}
