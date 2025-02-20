package com.destroystokyo.paper.event.executor.asm;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import java.util.concurrent.ConcurrentMap;
import org.jetbrains.annotations.NotNull;

public class SafeClassDefiner implements ClassDefiner {
    /* default */ static final SafeClassDefiner INSTANCE = new SafeClassDefiner();

    private SafeClassDefiner() {
    }

    private final ConcurrentMap<ClassLoader, GeneratedClassLoader> loaders = new MapMaker().weakKeys().makeMap();

    @NotNull
    @Override
    public Class<?> defineClass(@NotNull ClassLoader parentLoader, @NotNull String name, @NotNull byte[] data) {
        GeneratedClassLoader loader = this.loaders.computeIfAbsent(parentLoader, GeneratedClassLoader::new);
        synchronized (loader.getClassLoadingLock(name)) {
            Preconditions.checkState(!loader.hasClass(name), "%s already defined", name);
            Class<?> c = loader.define(name, data);
            assert c.getName().equals(name);
            return c;
        }
    }

    private static class GeneratedClassLoader extends ClassLoader {
        static {
            ClassLoader.registerAsParallelCapable();
        }

        protected GeneratedClassLoader(@NotNull ClassLoader parent) {
            super(parent);
        }

        private Class<?> define(@NotNull String name, byte[] data) {
            synchronized (this.getClassLoadingLock(name)) {
                assert !this.hasClass(name);
                Class<?> c = this.defineClass(name, data, 0, data.length);
                this.resolveClass(c);
                return c;
            }
        }

        @Override
        @NotNull
        public Object getClassLoadingLock(@NotNull String name) {
            return super.getClassLoadingLock(name);
        }

        public boolean hasClass(@NotNull String name) {
            synchronized (this.getClassLoadingLock(name)) {
                try {
                    Class.forName(name);
                    return true;
                } catch (ClassNotFoundException e) {
                    return false;
                }
            }
        }
    }
}
