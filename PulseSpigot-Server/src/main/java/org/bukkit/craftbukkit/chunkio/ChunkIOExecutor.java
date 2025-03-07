package org.bukkit.craftbukkit.chunkio;

import me.nate.spigot.config.PulseConfig;
import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkProviderServer;
import net.minecraft.server.ChunkRegionLoader;
import net.minecraft.server.World;
import org.bukkit.craftbukkit.util.AsynchronousExecutor;

@Deprecated // PulseSpigot
public class ChunkIOExecutor {
    static final int BASE_THREADS = PulseConfig.get().chunks.baseThreads; // PaperSpigot - Bumped value
    static final int PLAYERS_PER_THREAD = PulseConfig.get().chunks.playersPerThread;

    private static final AsynchronousExecutor<QueuedChunk, Chunk, Runnable, RuntimeException> instance = new AsynchronousExecutor<QueuedChunk, Chunk, Runnable, RuntimeException>(new ChunkIOProvider(), BASE_THREADS);

    public static Chunk syncChunkLoad(World world, ChunkRegionLoader loader, ChunkProviderServer provider, int x, int z) {
        return instance.getSkipQueue(new QueuedChunk(x, z, loader, world, provider));
    }

    public static void queueChunkLoad(World world, ChunkRegionLoader loader, ChunkProviderServer provider, int x, int z, Runnable runnable) {
        instance.add(new QueuedChunk(x, z, loader, world, provider), runnable);
    }

    // PulseSpigot start
    public static boolean isQueuedToLoad(World world, int x, int z) {
        try {
            return instance.get(new QueuedChunk(x, z, null, world, null)) != null;
        } catch (IllegalStateException ex) {
            return false;
        }
    }
    // PulseSpigot end

    // Abuses the fact that hashCode and equals for QueuedChunk only use world and coords
    public static void dropQueuedChunkLoad(World world, int x, int z, Runnable runnable) {
        instance.drop(new QueuedChunk(x, z, null, world, null), runnable);
    }

    public static void adjustPoolSize(int players) {
        // PulseSpigot start
        /*int size = Math.max(BASE_THREADS, (int) Math.ceil(players / PLAYERS_PER_THREAD));
        instance.setActiveThreads(size);*/
        // PulseSpigot end
    }

    public static void tick() {
        instance.finishActive();
    }
}
