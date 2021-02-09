package com.ivo.example.cache.impl;


import com.ivo.example.cache.Cache;
import com.ivo.example.cache.CacheContext;
import com.ivo.example.cache.CacheListener;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class RamLRUCache<K, V> extends AbstractCache<K, V> {
    private final LinkedHashMap<K, V> storage;

    public RamLRUCache(CacheContext<K, V> context) {
        super(context);
        storage = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                if (capacity < storage.size()) {
                    CacheListener<K, V> listener = RamLRUCache.this.listener;
                    if (listener != null) {
                        listener.removeEldest(RamLRUCache.this, eldest.getKey(), eldest.getValue());
                    }
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public Cache<K, V> put(K key, V value) {
        storage.put(key, value);
        return this;
    }

    @Override
    public Cache<K, V> putAll(Map<K, ? extends V> map) {
        if (map != null) {
            storage.putAll(map);
        }
        return this;
    }

    @Override
    public V get(K key) {
        return storage.get(key);
    }

    @Override
    public V remove(K key) {
        return storage.remove(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<K> keysIt() {
        K[] a = (K[]) storage.keySet().toArray();
        return Stream.of(a).iterator();
    }

    @Override
    public Map<K, ? super V> toMap(Map<K, ? super V> map) {
        if (map != null) {
            map.putAll(storage);
        }
        return map;
    }

    @Override
    public Cache<K, V> clear() {
        storage.clear();
        return this;
    }

    @Override
    public int size() {
        return storage.size();
    }
}
