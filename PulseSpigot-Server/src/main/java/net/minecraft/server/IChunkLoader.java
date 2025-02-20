package net.minecraft.server;

import java.io.IOException;

public interface IChunkLoader {

    // PulseSpigot start
    default void loadEntities(Chunk chunk, NBTTagCompound nbttagcompound, World world) {
    }

    default Object[] loadChunk(World world, int x, int z) throws IOException {
        return null;
    }

    default void saveChunk(World world, Chunk chunk) throws IOException, ExceptionWorldConflict  {
    }
    // PulseSpigot end

    Chunk a(World world, int i, int j) throws IOException;

    void a(World world, Chunk chunk) throws IOException, ExceptionWorldConflict;

    void b(World world, Chunk chunk) throws IOException;

    void a();

    void b();
}
