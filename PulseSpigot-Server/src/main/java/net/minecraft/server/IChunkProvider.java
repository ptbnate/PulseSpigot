package net.minecraft.server;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IChunkProvider {

    boolean isChunkLoaded(int i, int j);

    Chunk getOrCreateChunk(int i, int j);

    Chunk getChunkAt(BlockPosition blockposition);

    // PulseSpigot start
    default @Nullable Chunk getChunkIfLoaded(int i, int j) {
        if (!this.isChunkLoaded(i, j)) {
            return null;
        }
        return this.getOrCreateChunk(i, j);
    }
    // PulseSpigot end

    void getChunkAt(IChunkProvider ichunkprovider, int i, int j);

    boolean a(IChunkProvider ichunkprovider, Chunk chunk, int i, int j);

    boolean saveChunks(boolean flag, IProgressUpdate iprogressupdate);

    boolean unloadChunks();

    boolean canSave();

    String getName();

    List<BiomeBase.BiomeMeta> getMobsFor(EnumCreatureType enumcreaturetype, BlockPosition blockposition);

    BlockPosition findNearestMapFeature(World world, String s, BlockPosition blockposition);

    int getLoadedChunks();

    void recreateStructures(Chunk chunk, int i, int j);

    void c();
}
