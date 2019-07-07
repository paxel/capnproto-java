package org.capnproto;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * This is a cache for one type of StructReader. It is only filled with
 * StructReader on which recycle() is called.
 *
 * @param <T> The type of the StructReader.
 */
public class StructReaderCache<T extends StructReader> {

    private final Queue<T> recycler = new ConcurrentLinkedQueue<>();

    private final Supplier<T> factory;

    public StructReaderCache(Supplier<T> factory) {
        this.factory = factory;
    }

    /**
     * Provides a new StructReader if the Queue is empty, or the oldes Reader
     * from the recycler queue.
     *
     * @return a new or a recycled T.
     */
    public T getOrCreate() {
        T recycled = recycler.poll();
        if (recycled != null) {
            // We are the only one that removed that from the queue, so no racing condition here
            activateRecycler(recycled);
            return recycled;
        }
        final T newReader = factory.get();
        activateRecycler(newReader);
        return newReader;
    }

    private void activateRecycler(final T reader) {
        reader.onRecycle(f -> {
            // first deinit, then put to queue, to avoid racing conditions
            f.deinit();
            recycler.add((T) f);
        });
    }
}
