package me.nate.spigot.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkCoordIntPair;
import net.minecraft.server.MCUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.util.LongHash;

public class ChunkMap extends Long2ObjectOpenHashMap<Chunk> {

    private static final Logger LOGGER = LogManager.getLogger();

    private Chunk lastChunkByPos = null;

    public ChunkMap(int expected, float f) {
        super(expected, f);
    }

    @Override
    public Chunk put(long key, Chunk chunk) {
        org.spigotmc.AsyncCatcher.catchOp("Async Chunk put");

        this.lastChunkByPos = chunk;
        Chunk oldChunk;
        synchronized (this) {
            // synchronize so any async gets are safe
            oldChunk = super.put(key, chunk);
        }

        if (oldChunk == null) { // Paper - we should never be overwriting chunks
            // Update neighbor counts
            for (int x = -2; x < 3; x++) {
                for (int z = -2; z < 3; z++) {
                    if (x == 0 && z == 0) {
                        continue;
                    }

                    Chunk neighbor = super.get(LongHash.toLong(chunk.locX + x, chunk.locZ + z));
                    if (neighbor != null) {
                        neighbor.setNeighborLoaded(-x, -z);
                        chunk.setNeighborLoaded(x, z);
                    }
                }
            }
        } else {
            LOGGER.error("Overwrote existing chunk! (" + chunk.world.getWorld().getName() + ":" + chunk.locX+"," + chunk.locZ + ")", new IllegalStateException());
        }

        return oldChunk;
    }

    @Override
    public Chunk put(Long key, Chunk chunk) {
        return this.put(key.longValue(), chunk);
    }

    @Override
    public Chunk remove(long key) {
        org.spigotmc.AsyncCatcher.catchOp("Async Chunk remove");

        Chunk oldChunk;
        synchronized (this) {
            // synchronize so any async gets are safe
            oldChunk = super.remove(key);
        }

        if (oldChunk != null) { // Paper - don't decrement if we didn't remove anything
            // Update neighbor counts
            for (int x = -2; x < 3; x++) {
                for (int z = -2; z < 3; z++) {
                    if (x == 0 && z == 0) {
                        continue;
                    }

                    Chunk neighbor = super.get(LongHash.toLong(oldChunk.locX + x, oldChunk.locZ + z));
                    if (neighbor != null) {
                        neighbor.setNeighborUnloaded(-x, -z);
                        oldChunk.setNeighborUnloaded(x, z);
                    }
                }
            }
        }

        if (this.lastChunkByPos != null && key == this.lastChunkByPos.chunkKey) {
            this.lastChunkByPos = null;
        }

        return oldChunk;
    }

    @Override
    public Chunk get(long key) {
        if (MCUtil.isMainThread()) {
            if (this.lastChunkByPos != null && key == this.lastChunkByPos.chunkKey) {
                return this.lastChunkByPos;
            }
            Chunk chunk = super.get(key);
            return chunk != null ? (this.lastChunkByPos = chunk) : null;
        } else {
            synchronized (this) {
                return super.get(key);
            }
        }
    }

    @Override
    public Chunk remove(Object key) {
        return MCUtil.ensureMain("Chunk Remove", () -> this.remove(((Long) key).longValue()));
    }

    @Override
    public void putAll(Map<? extends Long, ? extends Chunk> map) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public boolean remove(Object object, Object object1) {
        throw new RuntimeException("Not yet implemented");
    }

}
