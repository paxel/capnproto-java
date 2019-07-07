package org.capnproto;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Supplier;

public class StructReaderCache<T extends StructReader> {

    private final Queue<T> cache = new ArrayBlockingQueue<>(CACHE_LIMIT);
    private static final int CACHE_LIMIT = 500;

    private final Supplier<T> factory;

    public StructReaderCache(Supplier<T> factory) {
        this.factory = factory;
    }

    public T getOrCreate() {
        T reuse = cache.poll();
        if (reuse != null) {
            return reuse;
        }
        final T newReader = factory.get();
        newReader.onReuse(f -> {
            f.deinit();
            cache.add((T) f);
        });
        return newReader;
    }
}
