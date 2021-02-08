package com.ivo.example.cache;

import com.ivo.example.cache.exception.CacheException;
import com.ivo.example.cache.impl.CacheBuilderContextImpl;
import com.ivo.example.cache.impl.FileSysLRUCache;
import com.ivo.example.cache.impl.RamLRUCache;

public class CacheBuilder<K, V> {
    private final CacheBuilderContext<K, V> context;

    public CacheBuilder() {
        context = new CacheBuilderContextImpl<>();
    }

    public CacheBuilder(CacheBuilderContext<K, V> context) {
        this.context = context;
    }

    public CacheBuilder<K, V> begin(CacheType type) {
        context.setCacheType(type);
        return this;
    }

    public CacheBuilder<K, V> capacity(int capacity) {
        context.setCapacity(capacity);
        return this;
    }


    public CacheBuilder<K, V> path(String path) {
        context.setPath(path);
        return this;
    }

    public Cache<K, V> build() throws CacheException {
        CacheType type = context.getCacheType();
        return switch (type) {
            case RamLRU -> new RamLRUCache<>(context.getCacheContext());
            case FileLRU -> new FileSysLRUCache<>(context.getCacheContext());
        };
    }

    public Cache<K, V> build(String key) throws CacheException {
        Cache<K, V> cache = build();
        if (key != null) {
            CachePool.put(key, cache);
        }
        return cache;
    }
}
