package net.minecraft.server;

import org.jetbrains.annotations.Nullable;

public interface IBlockAccess {

    // PulseSpigot start
    default TileEntity getTileEntity(BlockPosition blockposition) {
        return this.getTileEntity(blockposition.getX(), blockposition.getY(), blockposition.getZ());
    }

    TileEntity getTileEntity(int x, int y, int z);

    default IBlockData getType(BlockPosition blockposition) {
        return this.getType(blockposition.getX(), blockposition.getY(), blockposition.getZ());
    }

    IBlockData getType(int x, int y, int z);
    // PulseSpigot end

    // PulseSpigot start
    default @Nullable IBlockData getTypeIfLoaded(BlockPosition blockposition) {
        return this.getType(blockposition);
    }
    // PulseSpigot end

    boolean isEmpty(BlockPosition blockposition);

    int getBlockPower(BlockPosition blockposition, EnumDirection enumdirection);
}
