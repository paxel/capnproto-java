package org.capnproto;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StructReaderCache<T extends StructReader> {

    private final Queue<T> recycler = new ArrayDeque<>();

    private final Supplier<T> factory;
    private final Consumer<StructReader> fn;

    public StructReaderCache(Supplier<T> factory) {
        this.factory = factory;
        this.fn = this::recycle;
    }

    /**
     * Provides a new StructReader if the Queue is empty, or the oldest Reader
     * from the recycler queue.
     *
     * @return a new or a recycled T.
     */
    public T getOrCreate() {
        T recycled = recycler.poll();
        if (recycled != null) {
            return recycled;
        }
        final T newReader = factory.get();
        newReader.onRecycle(this.fn);
        return newReader;
    }

    private <U extends StructReader> void recycle(U reader) {
        reader.deinit();
        recycler.offer((T) reader);
    }
}
