package org.capnproto;

import java.util.HashMap;
import java.util.Map;

/**
 * This cache factory is given to the root {@link StructReader} and from there a
 * new instance is given via {@link #getOrCreateFactory(java.lang.Class) into every sub {@link StructReader}.
 * Each {@link StructReader} uses {@link #getOrCreateCache(java.lang.Class) } to create instances of sub {@link StructReader}.
 * Every {@link StructReader} can use the {@link StructReader#recycle() } method, to be reused by it's {@link StructReaderCache}.
 */
public class StructReaderCacheFactory {

    public static final StructReaderCacheFactory DEFAULT = new StructReaderCacheFactory();

    private final Map<Class, StructReaderCache> readerCaches = new HashMap<>();
    private final Map<Class, StructReaderCacheFactory> subFactories = new HashMap<>();

    /**
     *
     * @param <T>
     * @param t
     * @return
     */
    public <T extends StructReader> StructReaderCache<T> getOrCreateCache(Class<T> t) {
        return readerCaches.computeIfAbsent(t, clazz -> new StructReaderCache<>(() -> {
            try {
                return (T) clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new IllegalStateException("Can't instantiate " + t + ". A public default constructor is required");
            }
        }));
    }

    /**
     * 
     * @param <R>
     * @param t
     * @return 
     */
    public <R> StructReaderCacheFactory getOrCreateFactory(Class<R> t) {
        return subFactories.computeIfAbsent(t, clazz -> {
            return new StructReaderCacheFactory();
        });
    }
}
