package net.minecraft.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// CraftBukkit start

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.craftbukkit.chunkio.ChunkIOExecutor;
import org.bukkit.craftbukkit.util.LongHash;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.github.paperspigot.event.ServerExceptionEvent;
import org.github.paperspigot.exception.ServerInternalException;
import xyz.krypton.spigot.async.AsyncPriority;
import xyz.krypton.spigot.chunk.AsyncChunkProvider;
import xyz.krypton.spigot.chunk.ChunkMap;
// CraftBukkit end

public class ChunkProviderServer implements IChunkProvider {

    private static final Logger b = LogManager.getLogger();
    public it.unimi.dsi.fastutil.longs.LongSet unloadQueue = new it.unimi.dsi.fastutil.longs.LongOpenHashSet(20); // IonSpigot - use fastutil set
    public Chunk emptyChunk;
    public IChunkProvider chunkProvider;
    public IChunkLoader chunkLoader; // FlamePaper - Make chunkloader public
    public boolean forceChunkLoad = false; // CraftBukkit - true -> false
    public it.unimi.dsi.fastutil.longs.Long2ObjectMap<Chunk> chunks = new ChunkMap(8192, 0.5f); // PulseSpigot - Long2ObjectOpenHashMap -> ChunkMap
    public WorldServer world;

    public ChunkProviderServer(WorldServer worldserver, IChunkLoader ichunkloader, IChunkProvider ichunkprovider) {
        this.emptyChunk = new EmptyChunk(worldserver, Integer.MIN_VALUE, Integer.MIN_VALUE); // Migot
        this.world = worldserver;
        this.chunkLoader = ichunkloader;
        this.chunkProvider = ichunkprovider;
    }

    public boolean isChunkLoaded(int i, int j) {
        return this.getChunkIfLoaded(i, j) != null; // PulseSpigot
    }

    // CraftBukkit start - Change return type to Collection and return the values of our chunk map
    public java.util.Collection a() {
        // return this.chunkList;
        return this.chunks.values();
        // CraftBukkit end
    }

    public void queueUnload(int i, int j) {
        long key = LongHash.toLong(i, j); // IonSpigot - Only create key once
        // PaperSpigot start - Asynchronous lighting updates
        Chunk chunk = chunks.get(key); // IonSpigot
        if (chunk != null && chunk.world.paperConfigPulseSpigot.useAsyncLightning && (chunk.pendingLightUpdates.get() > 0 || chunk.world.getTime() - chunk.lightUpdateTime < 20)) {
            return;
        }
        // PaperSpigot end
        // PaperSpigot start - Don't unload chunk if it contains an entity that loads chunks
        if (chunk != null) {
            for (List<Entity> entities : chunk.entitySlices) {
                for (Entity entity : entities) {
                    if (entity.loadChunks) {
                        return;
                    }
                }
            }
        }
        // PaperSpigot end
        if (this.world.worldProvider.e()) {
            if (!this.world.c(i, j)) {
                // CraftBukkit start
                this.unloadQueue.add(key); // IonSpigot

                Chunk c = chunks.get(key); // IonSpigot
                if (c != null) {
                    c.mustSave = true;
                }
                // CraftBukkit end
            }
        } else {
            // CraftBukkit start
            this.unloadQueue.add(key); // IonSpigot

            Chunk c = chunks.get(key); // IonSpigot
            if (c != null) {
                c.mustSave = true;
            }
            // CraftBukkit end
        }

    }

    public void b() {
        Iterator iterator = this.chunks.values().iterator();

        while (iterator.hasNext()) {
            Chunk chunk = (Chunk) iterator.next();

            this.queueUnload(chunk.locX, chunk.locZ);
        }

    }

     // PulseSpigot start - Backport Paper 1.13.2 chunk system
    boolean chunkGoingToExists(int x, int z) {
        return ChunkIOExecutor.isQueuedToLoad(this.world, x, z);
    }

    public void bumpPriority(ChunkCoordIntPair coords) {
        // do nothing, override in async
    }

    public List<ChunkCoordIntPair> getSpiralOutChunks(BlockPosition blockposition, int radius) {
        List<ChunkCoordIntPair> list = new ArrayList<>();

        list.add(new ChunkCoordIntPair(blockposition.getX() >> 4, blockposition.getZ() >> 4));
        for (int r = 1; r <= radius; r++) {
            int x = -r;
            int z = r;

            // Iterates the edge of half of the box; then negates for other half.
            while (x <= r && z > -r) {
                list.add(new ChunkCoordIntPair((blockposition.getX() + (x << 4)) >> 4, (blockposition.getZ() + (z << 4)) >> 4));
                list.add(new ChunkCoordIntPair((blockposition.getX() - (x << 4)) >> 4, (blockposition.getZ() - (z << 4)) >> 4));

                if (x < r) {
                    x++;
                } else {
                    z--;
                }
            }
        }
        return list;
    }

    public Chunk getChunkAt(int x, int z, boolean load, boolean gen, Consumer<Chunk> consumer) {
        return this.getChunkAt(x, z, load, gen, AsyncPriority.NORMAL, consumer);
    }

    public Chunk getChunkAt(int x, int z, boolean load, boolean gen, AsyncPriority priority, Consumer<Chunk> consumer) {
        return this.getChunkAt(x, z, consumer != null
                ? () -> consumer.accept(this.chunks.get(LongHash.toLong(x, z)))
                : null);
    }

    public Chunk getChunkAt(int x, int z, boolean load, boolean gen) {
        return this.getChunkAt(x, z, () -> {});
    }

    protected AsyncChunkProvider.CancellableChunkRequest requestChunk(int x, int z, boolean gen, AsyncPriority priority, Consumer<Chunk> consumer) {
        Chunk chunk = this.getChunkAt(x, z, true, gen, consumer);
        return new AsyncChunkProvider.CancellableChunkRequest() {
            @Override
            public void cancel() {

            }

            @Override
            public Chunk getChunk() {
                return chunk;
            }
        };
    }
    // PulseSpigot end

    // CraftBukkit start - Add async variant, provide compatibility
    public Chunk getChunkIfLoaded(int x, int z) {
        return chunks.get(LongHash.toLong(x, z));
    }

    public Chunk getChunkAt(int i, int j) {
        return getChunkAt(i, j, null);
    }

    // PulseSpigot start - Backport Paper 1.13.2 chunk system
    public Chunk getChunkAt(int i, int j, Runnable runnable) {
        return this.getChunkAt(i, j, true, true, runnable);
    }

    public Chunk getChunkAt(int i, int j, boolean load, boolean gen, Runnable runnable) {
    // PulseSpigot end
        // IonSpigot start - Only create key once
        long key = LongHash.toLong(i, j);
        Chunk chunk = chunks.get(key);
        // IonSpigot end
        ChunkRegionLoader loader = null;

        if (this.chunkLoader instanceof ChunkRegionLoader) {
            loader = (ChunkRegionLoader) this.chunkLoader;

        }
        // We can only use the queue for already generated chunks
        if (load && chunk == null && loader != null && loader.chunkExists(world, i, j)) { // PulseSpigot
            if (runnable != null) {
                ChunkIOExecutor.queueChunkLoad(world, loader, this, i, j, runnable);
                return null;
            } else {
                chunk = ChunkIOExecutor.syncChunkLoad(world, loader, this, i, j);
            }
        } else if (gen && chunk == null) { // PulseSpigot
            chunk = originalGetChunkAt(i, j);
        // PulseSpigot start
        } else if (chunk == null) {
            chunk = this.emptyChunk;
        // PulseSpigot end
        }

        unloadQueue.remove(key); // SportPaper
        // If we didn't load the chunk async and have a callback run it now
        if (runnable != null) {
            runnable.run();
        }

        return chunk;
    }
    public Chunk originalGetChunkAt(int i, int j) {
        // IonSpigot start - Only create key once
        long key = LongHash.toLong(i, j);
        Chunk chunk = this.chunks.get(key);
        // IonSpigot end
        boolean newChunk = false;
        // CraftBukkit end

        if (chunk == null) {
            chunk = this.loadChunk(i, j);
            if (chunk == null) {
                if (this.chunkProvider == null) {
                    chunk = this.emptyChunk;
                } else {
                    try {
                        chunk = this.chunkProvider.getOrCreateChunk(i, j);
                    } catch (Throwable throwable) {
                        throw this.generateChunkError(i, j, throwable); // PulseSpigot - moved to method
                    }
                }
                newChunk = true; // CraftBukkit
            }

            this.chunks.put(key, chunk); // IonSpigot
            this.postChunk(chunk, newChunk, false); // PulseSpigot - moved to method
        }

        unloadQueue.remove(key); // SportPaper
        return chunk;
    }

    public Chunk getOrCreateChunk(int i, int j) {
        // CraftBukkit start
        Chunk chunk = (Chunk) this.chunks.get(LongHash.toLong(i, j));

        chunk = chunk == null ? (!this.world.ad() && !this.forceChunkLoad ? this.emptyChunk : this.getChunkAt(i, j)) : chunk;

        if (chunk == emptyChunk) return chunk;
        if (i != chunk.locX || j != chunk.locZ) {
            // Paper start
            String msg = "Chunk (" + chunk.locX + ", " + chunk.locZ + ") stored at  (" + i + ", " + j + ") in world '" + world.getWorld().getName() + "'";
            b.error(msg);
            b.error(chunk.getClass().getName());
            ServerInternalException ex = new ServerInternalException(msg);
            ex.printStackTrace();
            Bukkit.getPluginManager().callEvent(new ServerExceptionEvent(ex));
            // Paper end
        }

        return chunk;
        // CraftBukkit end
    }

    public Chunk loadChunk(int i, int j) {
        if (this.chunkLoader == null) {
            return null;
        } else {
            try {
                Chunk chunk = this.chunkLoader.a(this.world, i, j);

                if (chunk != null) {
                    chunk.setLastSaved(this.world.getTime());
                    if (this.chunkProvider != null) {
                        this.chunkProvider.recreateStructures(chunk, i, j);
                    }
                }

                return chunk;
            } catch (Exception exception) {
                // Paper start
                String msg = "Couldn\'t load chunk";
                ChunkProviderServer.b.error(msg, exception);
                ServerInternalException.reportInternalException(exception);
                // Paper end
                return null;
            }
        }
    }

     // PulseSpigot start
    protected ReportedException generateChunkError(int x, int z, Throwable throwable) { return a(x, z, throwable); } // Paper - OBFHELPER
    private ReportedException a(int i, int j, Throwable throwable) {
        CrashReport crashreport = CrashReport.a(throwable, "Exception generating new chunk");
        CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Chunk to be generated");

        crashreportsystemdetails.a("Location", String.format("%d,%d", i, j));
        crashreportsystemdetails.a("Position hash", LongHash.toLong(i, j));
        crashreportsystemdetails.a("Generator", this.chunkProvider.getName());
        return new ReportedException(crashreport);
    }

    public void postChunk(Chunk chunk, boolean isNew, boolean recreateStructures) {
        if (chunk == null || chunk == emptyChunk) {
            return;
        }

        chunk.addEntities();

        if (recreateStructures && this.chunkProvider != null) {
            this.chunkProvider.recreateStructures(chunk, chunk.locX, chunk.locZ);
        }

        Server server = world.getServer();
        if (server != null) {
            /*
             * If it's a new world, the first few chunks are generated inside
             * the World constructor. We can't reliably alter that, so we have
             * no way of creating a CraftWorld/CraftServer at that point.
             */
            server.getPluginManager().callEvent(new org.bukkit.event.world.ChunkLoadEvent(chunk.bukkitChunk, isNew));
        }

        chunk.loadNearby(this, this.chunkProvider);
    }
    // PulseSpigot end

    public void saveChunkNOP(Chunk chunk) {
        if (canSave() && this.chunkLoader != null) { // FlamePaper
            try {
                this.chunkLoader.b(this.world, chunk);
            } catch (Exception exception) {
                ChunkProviderServer.b.error("Couldn\'t save entities", exception);
            }

        }
    }

    public void saveChunk(Chunk chunk) {
        if (canSave() && this.chunkLoader != null) { // FlamePaper
            try {
                chunk.setLastSaved(this.world.getTime());
                this.chunkLoader.a(this.world, chunk);
            } catch (IOException ioexception) {
                ChunkProviderServer.b.error("Couldn\'t save chunk", ioexception);
            } catch (ExceptionWorldConflict exceptionworldconflict) {
                ChunkProviderServer.b.error("Couldn\'t save chunk; already in use by another instance of Minecraft?", exceptionworldconflict);
            }

        }
    }

    public void getChunkAt(IChunkProvider ichunkprovider, int i, int j) {
        Chunk chunk = this.getOrCreateChunk(i, j);
        // PulseSpigot start
        if (!chunk.isDone()) {
            return;
        }
        chunk.populate(this, this.chunkProvider);
        // PulseSpigot end
    }

    public boolean a(IChunkProvider ichunkprovider, Chunk chunk, int i, int j) {
        return chunk.decorateStructures(this, this.chunkProvider); // PulseSpigot
    }

    public boolean saveChunks(boolean flag, IProgressUpdate iprogressupdate) {
        int i = 0;

        // CraftBukkit start
        Iterator iterator = this.chunks.values().iterator();
        while (iterator.hasNext()) {
            Chunk chunk = (Chunk) iterator.next();
            // CraftBukkit end

            if (flag) {
                this.saveChunkNOP(chunk);
            }

            if (chunk.a(flag)) {
                this.saveChunk(chunk);
                chunk.f(false);
                ++i;
                if (i == 24 && !flag && false) { // Spigot
                    return false;
                }
            }
        }

        return true;
    }

    public void c() {
        if (this.chunkLoader != null) {
            this.chunkLoader.b();
        }

    }

    // FlamePaper start
    public boolean unloadChunks(boolean force) {
        if (!this.world.savingDisabled && (canSave() || force)) {
    // FlamePaper end
            // CraftBukkit start
            Server server = this.world.getServer();
            // SportPaper start
            it.unimi.dsi.fastutil.longs.LongIterator iterator = unloadQueue.iterator();
            for (int i = 0; i < 100 && iterator.hasNext(); ++i) {
                long chunkcoordinates = iterator.nextLong();
                iterator.remove();
                // SportPaper end
                Chunk chunk = this.chunks.get(chunkcoordinates);
                if (chunk == null) continue;

                ChunkUnloadEvent event = new ChunkUnloadEvent(chunk.bukkitChunk);
                server.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {

                    //if (chunk != null) { // PulseSpigot - remove unnecessary null check
                        chunk.removeEntities();
                        this.saveChunk(chunk);
                        this.saveChunkNOP(chunk);
                        this.chunks.remove(chunkcoordinates); // CraftBukkit
                    //}

                    // this.unloadQueue.remove(olong);

                    // PulseSpigot start - Improve chunk system (Moved to ChunkMap)
                    /*// Update neighbor counts
                    for (int x = -2; x < 3; x++) {
                        for (int z = -2; z < 3; z++) {
                            if (x == 0 && z == 0) {
                                continue;
                            }

                            Chunk neighbor = this.getChunkIfLoaded(chunk.locX + x, chunk.locZ + z);
                            if (neighbor != null) {
                                neighbor.setNeighborUnloaded(-x, -z);
                                chunk.setNeighborUnloaded(x, z);
                            }
                        }
                    }*/
                    // PulseSpigot end
                }
            }
            // CraftBukkit end

            if (this.chunkLoader != null) {
                this.chunkLoader.a();
            }
        }

        return this.chunkProvider.unloadChunks();
    }

    // FlamePaper start
    public boolean unloadChunks() {
        return this.unloadChunks(false);
    }
    // FlamePaper end

    public boolean canSave() {
        return !this.world.savingDisabled;
    }

    public String getName() {
        // CraftBukkit - this.chunks.count() -> .size()
        return "ServerChunkCache: " + this.chunks.size() + " Drop: " + this.unloadQueue.size();
    }

    public List<BiomeBase.BiomeMeta> getMobsFor(EnumCreatureType enumcreaturetype, BlockPosition blockposition) {
        return this.chunkProvider.getMobsFor(enumcreaturetype, blockposition);
    }

    public BlockPosition findNearestMapFeature(World world, String s, BlockPosition blockposition) {
        return this.chunkProvider.findNearestMapFeature(world, s, blockposition);
    }

    public int getLoadedChunks() {
        // CraftBukkit - this.chunks.count() -> this.chunks.size()
        return this.chunks.size();
    }

    public void recreateStructures(Chunk chunk, int i, int j) {}

    public Chunk getChunkAt(BlockPosition blockposition) {
        return this.getOrCreateChunk(blockposition.getX() >> 4, blockposition.getZ() >> 4);
    }
}
