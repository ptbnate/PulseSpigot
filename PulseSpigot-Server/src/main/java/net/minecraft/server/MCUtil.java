package net.minecraft.server;

import java.util.Queue;
import java.util.function.Supplier;
import xyz.krypton.spigot.util.async.AsyncUtil;
import org.bukkit.Location;
import org.spigotmc.AsyncCatcher;

public final class MCUtil {

    private MCUtil() {
    }

    public static boolean isMainThread() {
        return MinecraftServer.getServer().isMainThread();
    }

    public static Queue<Runnable> getProcessQueue() {
        return MinecraftServer.getServer().processQueue;
    }

    public static void ensureMain(Runnable run) {
        ensureMain(null, run);
    }

    /**
     * Ensures the target code is running on the main thread
     *
     * @param reason
     * @param run
     * @return
     */
    public static void ensureMain(String reason, Runnable run) {
        if (AsyncCatcher.enabled && Thread.currentThread() != MinecraftServer.getServer().primaryThread) {
            if (reason != null) {
                new IllegalStateException("Asynchronous " + reason + "!").printStackTrace();
            }
            AsyncUtil.runNextTick(run);
            return;
        }
        run.run();
    }

    public static <T> T ensureMain(Supplier<T> run) {
        return ensureMain(null, run);
    }

    /**
     * Ensures the target code is running on the main thread
     *
     * @param reason
     * @param run
     * @param <T>
     * @return
     */
    public static <T> T ensureMain(String reason, Supplier<T> run) {
        if (AsyncCatcher.enabled && Thread.currentThread() != MinecraftServer.getServer().primaryThread) {
            if (reason != null) {
                new IllegalStateException("Asynchronous " + reason + "! Blocking thread until it returns ").printStackTrace();
            }
            return AsyncUtil.supplyNextTick(run);
        }
        return run.get();
    }

    /**
     * Converts a NMS World/BlockPosition to Bukkit Location
     *
     * @param world
     * @param pos
     * @return
     */
    public static Location toLocation(World world, BlockPosition pos) {
        // PulseSpigot start
        if (pos == null) {
            return null;
        }
        // PulseSpigot end
        return new Location(world.getWorld(), pos.getX(), pos.getY(), pos.getZ());
    }

    public static org.bukkit.block.Block toBukkitBlock(World world, BlockPosition pos) {
        if (pos == null) {
            return null;
        }
        return world.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
    }

}