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

    public Cache<K, V> build() {
        CacheType type = context.getCacheType();
        return switch (type) {
            case RamLRU -> new RamLRUCache<>(context.getCacheContext());
            case FileLRU -> {
                try {
                    yield new FileSysLRUCache<>(context.getCacheContext());
                } catch (CacheException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public Cache<K, V> build(String key) {
        Cache<K, V> cache = build();
        if (key != null) {
            CachePool.put(key, cache);
        }
        return cache;
    }
}
