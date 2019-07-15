package org.capnproto;

import java.util.HashMap;
import java.util.Map;

/**
 * A ThreadLocal Map of StructReaderCaches. This is a possible mem leak, as
 * there is no way to clean up this cache.
 */
public class ThreadLocalStructReaderCache {

    private static final ThreadLocal<Map<Class, Object>> FACTORY = new ThreadLocal<Map<Class, Object>>() {
        @Override
        protected Map<Class, Object> initialValue() {
            return new HashMap<>();
        }
    };

    /**
     * Provides a thread local cache for the given class.
     *
     * @param <T> The type of the StructReader
     * @param key The actual class of the Reader
     * @return the thread local Cache.
     */
    public <T extends StructReader> StructReaderCache<T> getCache(Class<T> key) {
        final Object result = FACTORY.get().get(key);
        if (result != null) {
            return (StructReaderCache<T>) result;
        }
        StructReaderCache<T> newCache = new StructReaderCache<>(() -> {
            try {
                return key.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new RuntimeException("Reader" + key + " needs public default constructor.");
            }
        });
        FACTORY.get().put(key, newCache);
        return newCache;
    }
}
