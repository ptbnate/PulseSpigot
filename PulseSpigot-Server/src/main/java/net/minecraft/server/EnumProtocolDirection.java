package net.minecraft.server;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.function.Supplier;

public enum EnumProtocolDirection {

    SERVERBOUND, CLIENTBOUND;

    // Universe start
    public static final EnumProtocolDirection[] VALUES = values();
    private final Int2ObjectOpenHashMap<Supplier<? extends Packet<?>>> handshake = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<Supplier<? extends Packet<?>>> play = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<Supplier<? extends Packet<?>>> status = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<Supplier<? extends Packet<?>>> login = new Int2ObjectOpenHashMap<>();

    public void add(EnumProtocol enumProtocol, int id, Supplier<? extends Packet<?>> supplier) {
        this.map(enumProtocol.a()).put(id, supplier);
    }
    public Supplier<? extends Packet<?>> find(EnumProtocol enumProtocol, int id) {
        return this.map(enumProtocol.a()).get(id);
    }
    private Int2ObjectOpenHashMap<Supplier<? extends Packet<?>>> map(int id) {
        switch (id) {
            case -1:
                return this.handshake;
            case 0:
                return this.play;
            case 1:
                return this.status;
            case 2:
                return this.login;
        }
        throw new UnsupportedOperationException();
    }
    // Universe end

    private EnumProtocolDirection() {}
}
