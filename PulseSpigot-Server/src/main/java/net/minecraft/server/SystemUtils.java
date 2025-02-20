package net.minecraft.server;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.apache.logging.log4j.Logger;

public class SystemUtils {

    public static <V> V a(FutureTask<V> futuretask, Logger logger) {
        try {
            futuretask.run();
            return futuretask.get();
        } catch (ExecutionException executionexception) {
            logger.fatal("Error executing task", executionexception);
        } catch (InterruptedException interruptedexception) {
            logger.fatal("Error executing task", interruptedexception);
        }

        return null;
    }

    // PandaSpigot start - Modern tick Loop
    public static long getMonotonicMillis() {
        return getMonotonicNanos() / 1_000_000L;
    }

    public static long getMonotonicNanos() {
        return System.nanoTime();
    }
// PandaSpigot end

}
