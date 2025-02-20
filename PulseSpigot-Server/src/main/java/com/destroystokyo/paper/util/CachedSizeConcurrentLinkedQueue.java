package com.destroystokyo.paper.util;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.LongAdder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CachedSizeConcurrentLinkedQueue<E> extends ConcurrentLinkedQueue<E> {
    private final LongAdder cachedSize = new LongAdder();

    @Override
    public boolean add(@NotNull E e) {
        boolean result = super.add(e);
        if (result) {
            this.cachedSize.increment();
        }
        return result;
    }

    @Nullable
    @Override
    public E poll() {
        E result = super.poll();
        if (result != null) {
            this.cachedSize.decrement();
        }
        return result;
    }

    @Override
    public int size() {
        return this.cachedSize.intValue();
    }
}
