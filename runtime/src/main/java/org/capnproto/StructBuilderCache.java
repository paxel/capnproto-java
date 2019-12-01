package org.capnproto;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StructBuilderCache<T extends StructBuilder> {

    private final Queue<T> recycler = new ArrayDeque<>();

    private final Supplier<T> factory;
    private final Consumer<StructBuilder> fn;

    public StructBuilderCache(Supplier<T> factory) {
        this.factory = factory;
        this.fn = this::recycle;
    }

    /**
     * Provides a new StructBuilder if the Queue is empty, or the oldest Builder
     * from the recycler queue.
     *
     * @return a new or a recycled T.
     */
    public T getOrCreate() {
        T recycled = recycler.poll();
        if (recycled != null) {
            return recycled;
        }
        final T newBuilder = factory.get();
        newBuilder.onRecycle(this.fn);
        return newBuilder;
    }

    private <U extends StructBuilder> void recycle(U builder) {
        builder.deinit();
        recycler.offer((T) builder);
    }
}
