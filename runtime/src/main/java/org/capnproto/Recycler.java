package org.capnproto;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;

/**
 * The Recycler is used to create Classes of a defined type if the internal cache is empty, otherwise provides the oldest internal recycled object.
 *
 * @param <T> The type of the recycled objects.
 */
public class Recycler<T extends Recycable<T>> {

    private final Queue<T> recycler = new ArrayDeque<>();

    private final Supplier<T> factory;

    /**
     * Constructs a Recycler with an object factory.
     *
     * @param factory the factory to create new objects.
     */
    public Recycler(Supplier<T> factory) {
        this.factory = factory;
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
        final T recycable = factory.get();
        recycable.init(this);
        return recycable;
    }

    /**
     * Puts an object back into the recycler.
     *
     * @param recycable The recycable object.
     */
    public void recycle(T recycable) {
        recycler.offer(recycable);
    }

}
