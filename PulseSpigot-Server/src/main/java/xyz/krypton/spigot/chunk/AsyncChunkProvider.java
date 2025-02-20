package xyz.krypton.spigot.chunk;

import com.destroystokyo.paper.util.PriorityQueuedExecutor;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkCoordIntPair;
import net.minecraft.server.ChunkProviderServer;
import net.minecraft.server.IChunkLoader;
import net.minecraft.server.IChunkProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.World;
import net.minecraft.server.WorldServer;
import org.bukkit.craftbukkit.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.util.LongHash;
import xyz.krypton.spigot.async.AsyncPriority;
import xyz.krypton.spigot.config.PulseConfig;

public class AsyncChunkProvider extends ChunkProviderServer {

    private static final int GEN_THREAD_PRIORITY = Integer.getInteger("paper.genThreadPriority", 3);
    private static final int LOAD_THREAD_PRIORITY = Integer.getInteger("paper.loadThreadPriority", 4);
    private static final PriorityQueuedExecutor SHARED_LOAD_EXECUTOR;
    private static final PriorityQueuedExecutor SHARED_GEN_EXECUTOR;
    private static final ConcurrentLinkedDeque<Runnable> MAIN_THREAD_QUEUE = new ConcurrentLinkedDeque<>();

    private final boolean shouldGenSync;

    private final PriorityQueuedExecutor loadExecutor;
    private final PriorityQueuedExecutor generationExecutor;
    private final Long2ObjectMap<PendingChunk> pendingChunks = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());

    private final MinecraftServer server;

    public AsyncChunkProvider(WorldServer worldserver, IChunkLoader ichunkloader, IChunkProvider ichunkprovider) {
        super(worldserver, ichunkloader, ichunkprovider);

        this.server = worldserver.getMinecraftServer();
        String worldName = this.world.getWorld().getName();

        PulseConfig.Chunks.Async config = PulseConfig.get().chunks.async;
        // Loading
        PulseConfig.Chunks.Async.Loading loadingConfig = config.loading;
        this.loadExecutor = loadingConfig.executorPerWorld
                ? new PriorityQueuedExecutor("PulseSpigot - Chunk Loading - " + worldName, loadingConfig.threads, LOAD_THREAD_PRIORITY)
                : SHARED_LOAD_EXECUTOR;
        // Generation
        PulseConfig.Chunks.Async.Generation generationConfig = config.generation;
        boolean supportsAsyncChunkGen = !(ichunkprovider instanceof CustomChunkGenerator) || (((CustomChunkGenerator) ichunkprovider).asyncSupported)
                || generationConfig.forceAsync || generationConfig.forceAsyncWorlds.contains(worldName);
        this.shouldGenSync = !generationConfig.enabled || !supportsAsyncChunkGen;
        this.generationExecutor = generationConfig.executorPerWorld
                ? new PriorityQueuedExecutor("PulseSpigot - Chunk Generation - " + worldName, 1, GEN_THREAD_PRIORITY)
                : SHARED_GEN_EXECUTOR;
    }

    static {
         PulseConfig.Chunks.Async config = PulseConfig.get().chunks.async;
         SHARED_LOAD_EXECUTOR = !config.loading.executorPerWorld
                 ? new PriorityQueuedExecutor("PulseSpigot - Chunk Loading - Shared", config.loading.threads, LOAD_THREAD_PRIORITY)
                 : null;
         SHARED_GEN_EXECUTOR = !config.generation.executorPerWorld
                 ? new PriorityQueuedExecutor("PulseSpigot - Chunk Generation - Shared", 1, GEN_THREAD_PRIORITY)
                 : null;
    }

    private static PriorityQueuedExecutor.Priority calculatePriority(boolean isBlockingMain, AsyncPriority priority) {
        if (isBlockingMain) {
            return PriorityQueuedExecutor.Priority.URGENT;
        }

        switch (priority) {
            case URGENT:
                return PriorityQueuedExecutor.Priority.URGENT;
            case HIGH:
                return PriorityQueuedExecutor.Priority.HIGH;
            default:
                return PriorityQueuedExecutor.Priority.NORMAL;
        }
    }

    public static void stop(MinecraftServer server) {
        for (WorldServer world : server.worlds) {
            world.getPlayerChunkMap().shutdown();
        }
    }

    public static void processMainThreadQueue(MinecraftServer server) {
        for (WorldServer world : server.worlds) {
            processMainThreadQueue(world);
        }
    }

    public static void processMainThreadQueue(World world) {
        IChunkProvider chunkProvider = world.chunkProvider;
        if (chunkProvider instanceof AsyncChunkProvider) {
            ((AsyncChunkProvider) chunkProvider).processMainThreadQueue();
        }
    }

    private void processMainThreadQueue() {
        this.processMainThreadQueue((PendingChunk) null);
    }

    private boolean processMainThreadQueue(PendingChunk pending) {
        Runnable run;
        boolean hadLoad = false;
        while ((run = MAIN_THREAD_QUEUE.poll()) != null) {
            run.run();
            hadLoad = true;
            if (pending != null && pending.hasPosted) {
                break;
            }
        }
        return hadLoad;
    }

    @Override
    public void bumpPriority(ChunkCoordIntPair coords) {
        PendingChunk pending = this.pendingChunks.get(LongHash.toLong(coords.x, coords.z));
        if (pending != null) {
            pending.bumpPriority(PriorityQueuedExecutor.Priority.HIGH);
        }
    }

    @Override
    public @Nullable Chunk getChunkAt(int i, int j, Runnable runnable) {
        if (runnable != null) {
            return this.getChunkAt(i, j, true, true, chunk -> runnable.run());
        }
        synchronized (this) {
            return this.getChunkAt(i, j, true, true);
        }
    }

    @Override
    public @Nullable Chunk getChunkAt(int x, int z, boolean load, boolean gen) {
        return this.getChunkAt(x, z, load, gen, (Consumer<Chunk>) null);
    }

    @Override
    public @Nullable Chunk getChunkAt(int x, int z, boolean load, boolean gen, AsyncPriority priority, Consumer<Chunk> consumer) {
        long key = LongHash.toLong(x, z);
        Chunk chunk = this.chunks.get(key);
        this.unloadQueue.remove(key);
        if (chunk != null || !load) { // return null if we aren't loading
            if (consumer != null) {
                consumer.accept(chunk);
            }
            return chunk;
        }
        return this.loadOrGenerateChunk(x, z, gen, priority, consumer); // Async overrides this method
    }

    private Chunk loadOrGenerateChunk(int x, int z, boolean gen, AsyncPriority priority, Consumer<Chunk> consumer) {
        return this.requestChunk(x, z, gen, priority, consumer).getChunk();
    }

    @Override
    protected final PendingChunkRequest requestChunk(int x, int z, boolean gen, AsyncPriority priority, Consumer<Chunk> consumer) {
        long key = LongHash.toLong(x, z);
        boolean isChunkThread = this.isChunkThread();
        boolean isBlockingMain = consumer == null && this.server.isMainThread();
        boolean loadOnThisThread = isChunkThread || isBlockingMain;
        PriorityQueuedExecutor.Priority taskPriority = calculatePriority(isBlockingMain, priority);

        // Obtain a PendingChunk
        PendingChunk pending;
        synchronized (this.pendingChunks) {
            PendingChunk pendingChunk = this.pendingChunks.get(key);
            if (pendingChunk == null) {
                pending = new PendingChunk(x, z, key, gen, taskPriority);
                this.pendingChunks.put(key, pending);
            } else if (pendingChunk.hasFinished && gen && !pendingChunk.canGenerate && pendingChunk.chunk == null) {
                // need to overwrite the old
                pending = new PendingChunk(x, z, key, true, taskPriority);
                this.pendingChunks.put(key, pending);
            } else {
                pending = pendingChunk;
                if (pending.taskPriority != taskPriority) {
                    pending.bumpPriority(taskPriority);
                }
            }
        }

        // Listen for when result is ready
        final CompletableFuture<Chunk> future = new CompletableFuture<>();
        final PendingChunkRequest request = pending.addListener(future, gen, !loadOnThisThread);

        // Chunk Generation can trigger Chunk Loading, those loads may need to convert, and could be slow
        // Give an opportunity for urgent tasks to jump in at these times
        if (isChunkThread) {
            this.processUrgentTasks();
        }

        if (loadOnThisThread) {
            // do loads on main if blocking, or on current if we are a load/gen thread
            // gen threads do trigger chunk loads
            pending.loadTask.run();
        }

        if (isBlockingMain) {
            while (!future.isDone()) {
                // We aren't done, obtain lock on queue
                synchronized (MAIN_THREAD_QUEUE) {
                    // We may of received our request now, check it
                    if (this.processMainThreadQueue(pending)) {
                        // If we processed SOMETHING, don't wait
                        continue;
                    }
                    try {
                        // We got nothing from the queue, wait until something has been added
                        MAIN_THREAD_QUEUE.wait(1);
                    } catch (InterruptedException ignored) {
                    }
                }
                // Queue has been notified or timed out, process it
                this.processMainThreadQueue(pending);
            }
            // We should be done AND posted into chunk map now, return it
            request.initialReturnChunk = pending.postChunk();
        } else if (consumer == null) {
            // This is on another thread
            request.initialReturnChunk = future.join();
        } else {
            future.thenAccept((c) -> {
                synchronized (this) {
                    consumer.accept(c);
                }
            });
        }

        return request;
    }

    private void processUrgentTasks() {
        final PriorityQueuedExecutor executor = PriorityQueuedExecutor.getExecutor();
        if (executor != null) {
            executor.processUrgentTasks();
        }
    }

    boolean chunkGoingToExists(int x, int z) {
        synchronized (this.pendingChunks) {
            PendingChunk pendingChunk = this.pendingChunks.get(LongHash.toLong(x, z));
            return pendingChunk != null && pendingChunk.canGenerate;
        }
    }

    private enum PendingStatus {
        /**
         * Request has just started
         */
        STARTED,
        /**
         * Chunk is attempting to be loaded from disk
         */
        LOADING,
        /**
         * Chunk must generate on main and is pending main
         */
        GENERATION_PENDING,
        /**
         * Chunk is generating
         */
        GENERATING,
        /**
         * Chunk is ready and is pending post to main
         */
        PENDING_MAIN,
        /**
         * Could not load chunk, and did not need to generat
         */
        FAIL,
        /**
         * Fully done with this request (may or may not of loaded)
         */
        DONE,
        /**
         * Chunk load was cancelled (no longer needed)
         */
        CANCELLED
    }

    private enum ChunkSource {

        GENERATE,
        LOAD,
        UNKNOWN

    }

    public interface CancellableChunkRequest {

        void cancel();

        Chunk getChunk();

    }

    public static class PendingChunkRequest implements CancellableChunkRequest {

        private final PendingChunk pending;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private volatile boolean generating;
        private volatile Chunk initialReturnChunk;

        private PendingChunkRequest(PendingChunk pending) {
            this.pending = pending;
            this.cancelled.set(true);
        }

        private PendingChunkRequest(PendingChunk pending, boolean gen) {
            this.pending = pending;
            this.generating = gen;
        }

        @Override
        public void cancel() {
            this.pending.cancel(this);
        }

        /**
         * Will be null on asynchronous loads
         */
        @Override
        @Nullable
        public Chunk getChunk() {
            return this.initialReturnChunk;
        }

    }

    private boolean isLoadThread() {
        return this.loadExecutor.isCurrentThread();
    }

    private boolean isGenThread() {
        return this.generationExecutor.isCurrentThread();
    }

    private boolean isChunkThread() {
        return this.isLoadThread() || this.isGenThread();
    }

    private class PendingChunk implements Runnable {

        private final int x;
        private final int z;
        private final long key;
        private final long started = System.currentTimeMillis();
        private final CompletableFuture<Chunk> loadOnly = new CompletableFuture<>();
        private final CompletableFuture<Chunk> generate = new CompletableFuture<>();
        private final AtomicInteger requests = new AtomicInteger(0);

        private volatile PendingStatus status = PendingStatus.STARTED;
        private volatile PriorityQueuedExecutor.PendingTask<Void> loadTask;
        private volatile PriorityQueuedExecutor.PendingTask<Chunk> genTask;
        private volatile PriorityQueuedExecutor.Priority taskPriority;
        private volatile boolean generating;
        private volatile boolean canGenerate;
        private volatile boolean hasPosted;
        private volatile boolean hasFinished;
        private volatile Chunk chunk;
        private volatile ChunkSource source = ChunkSource.UNKNOWN;
        private volatile NBTTagCompound pendingLevel;

        PendingChunk(int x, int z, long key, boolean canGenerate, boolean priority) {
            this.x = x;
            this.z = z;
            this.key = key;
            this.canGenerate = canGenerate;
            this.taskPriority = priority
                    ? PriorityQueuedExecutor.Priority.HIGH
                    : PriorityQueuedExecutor.Priority.NORMAL;
        }

        PendingChunk(int x, int z, long key, boolean canGenerate, PriorityQueuedExecutor.Priority taskPriority) {
            this.x = x;
            this.z = z;
            this.key = key;
            this.canGenerate = canGenerate;
            this.taskPriority = taskPriority;
        }

        private synchronized void setStatus(PendingStatus status) {
            this.status = status;
        }

        private Chunk loadChunk(int x, int z) throws IOException {
            this.setStatus(PendingStatus.LOADING);
            Object[] data = AsyncChunkProvider.this.chunkLoader.loadChunk(AsyncChunkProvider.this.world, x, z);
            if (data != null) {
                // Level must be loaded on main
                this.pendingLevel = ((NBTTagCompound) data[1]).getCompound("Level");
                this.source = ChunkSource.LOAD;
                return (Chunk) data[0];
            } else {
                return null;
            }
        }

        private Chunk generateChunk() {
            synchronized (this) {
                if (this.requests.get() <= 0) {
                    return null;
                }
            }

            try {
                Chunk chunk = AsyncChunkProvider.this.chunkProvider.getOrCreateChunk(this.x, this.z);
                this.source = ChunkSource.GENERATE;
                this.generateFinished(chunk);
                return chunk;
            } catch (Throwable e) {
                MinecraftServer.LOGGER.error("Couldn't generate chunk (" + AsyncChunkProvider.this.world.getWorld().getName() + ":" + this.x + "," + this.z + ")", e);
                this.generateFinished(null);
                return null;
            }
        }

        boolean loadFinished(Chunk chunk) {
            if (chunk != null) {
                this.postChunkToMain(chunk);
                return false;
            }
            this.loadOnly.complete(null);

            synchronized (this) {
                boolean cancelled = this.requests.get() <= 0;
                if (!this.canGenerate || cancelled) {
                    if (!cancelled) {
                        this.setStatus(PendingStatus.FAIL);
                    }
                    this.chunk = null;
                    this.hasFinished = true;
                    AsyncChunkProvider.this.pendingChunks.remove(this.key);
                    return false;
                } else {
                    this.setStatus(PendingStatus.GENERATING);
                    this.generating = true;
                    return true;
                }
            }
        }

        void generateFinished(Chunk chunk) {
            synchronized (this) {
                this.chunk = chunk;
                this.hasFinished = true;
            }
            if (chunk != null) {
                this.postChunkToMain(chunk);
            } else {
                synchronized (this) {
                    AsyncChunkProvider.this.pendingChunks.remove(this.key);
                    this.completeFutures(null);
                }
            }
        }

        synchronized private void completeFutures(Chunk chunk) {
            this.loadOnly.complete(chunk);
            this.generate.complete(chunk);
        }

        private void postChunkToMain(Chunk chunk) {
            synchronized (this) {
                this.setStatus(PendingStatus.PENDING_MAIN);
                this.chunk = chunk;
                this.hasFinished = true;
            }

            if (AsyncChunkProvider.this.server.isMainThread()) {
                this.postChunk();
                return;
            }

            // Don't post here, even if on main, it must enter the queue so we can exit any open batch
            // schedulers, as post stage may trigger a new generation and cause errors
            synchronized (MAIN_THREAD_QUEUE) {
                if (this.taskPriority == PriorityQueuedExecutor.Priority.URGENT) {
                    MAIN_THREAD_QUEUE.addFirst(this::postChunk);
                } else {
                    MAIN_THREAD_QUEUE.addLast(this::postChunk);
                }
                MAIN_THREAD_QUEUE.notify();
            }
        }

        Chunk postChunk() {
            if (!AsyncChunkProvider.this.server.isMainThread()) {
                throw new IllegalStateException("Must post from main");
            }
            synchronized (this) {
                if (this.hasPosted || this.requests.get() <= 0) { // if pending is 0, all were cancelled
                    return this.chunk;
                }
                this.hasPosted = true;
            }
            try {
                if (this.chunk == null) {
                    this.chunk = AsyncChunkProvider.this.chunks.get(this.key);
                    this.completeFutures(this.chunk);
                    return this.chunk;
                }
                if (this.pendingLevel != null) {
                    AsyncChunkProvider.this.chunkLoader.loadEntities(this.chunk, this.pendingLevel, AsyncChunkProvider.this.world);
                    this.pendingLevel = null;
                }
                synchronized (AsyncChunkProvider.this.chunks) {
                    Chunk other = AsyncChunkProvider.this.chunks.get(this.key);
                    if (other != null) {
                        this.chunk = other;
                        AsyncChunkProvider.this.postChunk(this.chunk, false, false);
                        this.completeFutures(other);
                        return other;
                    }
                    if (this.chunk != null) {
                        AsyncChunkProvider.this.chunks.put(this.key, this.chunk);
                    }
                }

                AsyncChunkProvider.this.postChunk(this.chunk, this.source == ChunkSource.GENERATE, this.source == ChunkSource.LOAD);

                this.completeFutures(this.chunk);
                return this.chunk;
            } finally {
                AsyncChunkProvider.this.pendingChunks.remove(this.key);
                this.setStatus(PendingStatus.DONE);
            }
        }

        synchronized PendingChunkRequest addListener(CompletableFuture<Chunk> future, boolean gen, boolean autoSubmit) {
            this.requests.incrementAndGet();
            if (this.loadTask == null) {
                // Take care of a race condition in that a request could be cancelled after the synchronize
                // on pendingChunks, but before a listener is added, which would erase these pending tasks.
                this.genTask = AsyncChunkProvider.this.generationExecutor.createPendingTask(this::generateChunk, this.taskPriority);
                this.loadTask = AsyncChunkProvider.this.loadExecutor.createPendingTask(this, this.taskPriority);
                if (autoSubmit) {
                    // We will execute it outside of the synchronized context immediately after
                    this.loadTask.submit();
                }
            }

            if (this.hasFinished) {
                future.complete(this.chunk);
                return new PendingChunkRequest(this);
            } else if (gen) {
                this.canGenerate = true;
                this.generate.thenAccept(future::complete);
            } else {
                if (this.generating) {
                    future.complete(null);
                    return new PendingChunkRequest(this);
                } else {
                    this.loadOnly.thenAccept(future::complete);
                }
            }

            return new PendingChunkRequest(this, gen);
        }

        @Override
        public void run() {
            try {
                if (!this.loadFinished(this.loadChunk(this.x, this.z))) {
                    return;
                }
            } catch (Throwable ex) {
                MinecraftServer.LOGGER.error("Couldn't load chunk (" + AsyncChunkProvider.this.world.getWorld().getName() + ":" + this.x + "," + this.z + ")", ex);
                if (ex instanceof IOException) {
                    this.generateFinished(null);
                    return;
                }
            }

            if (AsyncChunkProvider.this.shouldGenSync) {
                synchronized (this) {
                    this.setStatus(PendingStatus.GENERATION_PENDING);
                    if (this.taskPriority == PriorityQueuedExecutor.Priority.URGENT) {
                        MAIN_THREAD_QUEUE.addFirst(() -> this.generateFinished(this.generateChunk()));
                    } else {
                        MAIN_THREAD_QUEUE.addLast(() -> this.generateFinished(this.generateChunk()));
                    }

                }
                synchronized (MAIN_THREAD_QUEUE) {
                    MAIN_THREAD_QUEUE.notify();
                }
            } else {
                if (AsyncChunkProvider.this.isGenThread()) {
                    // ideally we should never run into 1 chunk generating another chunk...
                    // but if we do, let's apply same solution
                    this.genTask.run();
                } else {
                    this.genTask.submit();
                }
            }
        }

        void bumpPriority(PriorityQueuedExecutor.Priority newPriority) {
            if (this.taskPriority.ordinal() >= newPriority.ordinal()) {
                return;
            }

            this.taskPriority = newPriority;
            PriorityQueuedExecutor.PendingTask<Void> loadTask = this.loadTask;
            PriorityQueuedExecutor.PendingTask<Chunk> genTask = this.genTask;
            if (loadTask != null) {
                loadTask.bumpPriority(newPriority);
            }
            if (genTask != null) {
                genTask.bumpPriority(newPriority);
            }
        }

        public synchronized boolean isCancelled() {
            return this.requests.get() <= 0;
        }

        public synchronized void cancel(PendingChunkRequest request) {
            synchronized (AsyncChunkProvider.this.pendingChunks) {
                if (!request.cancelled.compareAndSet(false, true)) {
                    return;
                }

                if (this.requests.decrementAndGet() > 0) {
                    return;
                }

                boolean c1 = this.genTask.cancel();
                boolean c2 = this.loadTask.cancel();
                this.loadTask = null;
                this.genTask = null;
                AsyncChunkProvider.this.pendingChunks.remove(this.key);
                this.setStatus(PendingStatus.CANCELLED);
            }
        }

    }

}
