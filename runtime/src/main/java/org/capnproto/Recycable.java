package org.capnproto;

/**
 * A recycable class is initialized with it's recycler and puts itself into this recycler when recycled.
 * The class is responsible to clean up its content. A recycable class needs a factory, if it should be generated on demand by a {@link Recycler}.
 *
 * @param <T> The type of this Recycable.
 */
public interface Recycable<T extends Recycable<T>> {

    /**
     * Inits this object with its Recycler. This happens automatically if the object is created by a {@link Recycler}.
     *
     * @param recycler
     */
    void init(Recycler<T> recycler);

    /**
     * Returns this object into the recycler, if it is set. otherwise nothing happens.
     * This method should clean up the internal data, to allow the gc() to catch up.
     */
    void recycle();
}
