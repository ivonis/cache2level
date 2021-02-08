package com.ivo.example.cache;

import com.ivo.example.cache.exception.CacheException;
import com.ivo.example.cache.impl.Cache2l;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachePool {
    private static final Map<String, Cache<?, ?>> cacheMap = new ConcurrentHashMap<>();

    private CachePool() {

    }

    public static <K, V> Cache<K, V> get(String key) throws ClassCastException {
        Cache<?, ?> cache =  cacheMap.get(key);
        if (cache == null) {
            return null;
        }
        return (Cache<K, V>)cache;
    }

    public static Cache<?, ?> put(String key, Cache<?, ?> cache) {
        return cacheMap.put(key, cache);
    }

    public static <K, V> CacheBuilder<K, V> cacheBuilder() {
        return new CacheBuilder<>();
    }

    public static <K, V> CacheBuilder<K, V> cacheBuilder(CacheType type) {
        return new CacheBuilder<K, V>().begin(type);
    }

    public static <K, V> Cache<K, V> buildRamCache(String key, int capacity) throws CacheException {
        return new CacheBuilder<K, V>()
                .begin(CacheType.RamLRU)
                .capacity(capacity)
                .build(key);
    }

    public static <K, V> Cache<K, V> buildRamCache(String key, Algorithm algorithm, int capacity) throws CacheException {
        return new CacheBuilder<K, V>()
                .begin(CacheType.typeFor(false, algorithm))
                .capacity(capacity)
                .build(key);
    }

    public static <K, V> Cache<K, V> buildFileCache(String key, int capacity, String path) throws CacheException {
        return new CacheBuilder<K, V>()
                .begin(CacheType.FileLRU)
                .capacity(capacity)
                .path(path)
                .build(key);
    }

    public static <K, V> Cache<K, V> buildFileCache(String key, Algorithm algorithm, int capacity, String path) throws CacheException {
        return new CacheBuilder<K, V>()
                .begin(CacheType.typeFor(true, algorithm))
                .capacity(capacity)
                .path(path)
                .build(key);
    }

    public static <K, V> Cache2lBuilder<K, V> cache2lBuilder() {
        return new Cache2lBuilder<>();
    }

    public static <K, V> Cache2lBuilder<K, V> cache2lBuilder(CacheType primaryType, CacheType slaveType) {
        return new Cache2lBuilder<K, V>().begin(primaryType, slaveType);
    }

    public static <K, V> Cache2l<K, V> build2LevelCache(String key,
                                                        CacheType primaryType, CacheType slaveType,
                                                        int primaryCapacity, int slaveCapacity,
                                                        String primaryPath, String slavePath) throws CacheException {
        return new Cache2lBuilder<K, V>().begin(primaryType, slaveType)
                .capacity(primaryCapacity, slaveCapacity)
                .primaryPath(primaryPath).slavePath(slavePath).build(key);
    }

    public static <K, V> Cache2l<K, V> build2LRamFileCache(String key,
                                                           Algorithm primaryAlg, Algorithm slaveAlg,
                                                        int primaryCapacity, int slaveCapacity,
                                                        String slavePath) throws CacheException {
        return new Cache2lBuilder<K, V>()
                .begin(CacheType.typeFor(false, primaryAlg), CacheType.typeFor(true, slaveAlg))
                .capacity(primaryCapacity, slaveCapacity)
                .slavePath(slavePath).build(key);
    }

    public static <K, V> Cache2l<K, V> build2LRamFileCache(String key, Algorithm primaryAlg,
                                                           int primaryCapacity, int slaveCapacity,
                                                           String slavePath) throws CacheException {
        return new Cache2lBuilder<K, V>()
                .begin(CacheType.typeFor(false, primaryAlg), CacheType.FileLRU)
                .capacity(primaryCapacity, slaveCapacity)
                .slavePath(slavePath).build(key);
    }

    public static <K, V> Cache2l<K, V> build2LRamFileCache(String key,
                                                           int primaryCapacity, int slaveCapacity,
                                                           String slavePath) throws CacheException {
        return new Cache2lBuilder<K, V>()
                .begin(CacheType.RamLRU, CacheType.FileLRU)
                .capacity(primaryCapacity, slaveCapacity)
                .slavePath(slavePath).build(key);
    }
}
