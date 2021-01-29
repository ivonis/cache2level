package com.ivo.example.cache.impl;

import com.ivo.example.cache.Cache;
import com.ivo.example.cache.EldestCollector;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class RamCache<K, V> implements Cache<K, V> {

    static final int DEFAULT_CAPACITY = 16;

    private final int capacity;
    private LinkedHashMap<K, V> storage;
    EldestCollector<K, V> collector = null;

    public RamCache(int capacity) {
        this.capacity = capacity;
        storage = new LinkedHashMap<>(capacity, 0.75f, true){
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                if (capacity < storage.size()) {
                    if (collector != null) {
                        collector.collect(RamCache.this, eldest.getKey(), eldest.getValue());
                    }
                    return true;
                }
                return false;
            }
        };
    }

    public RamCache() {
        this(DEFAULT_CAPACITY);
    }

    @Override
    public void put(K key, V value) {
        storage.put(key, value);
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
    public Iterator<K> keysIt() {
        return storage.keySet().iterator();
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public void setEldestCollector(EldestCollector<K, V> collector) {
        this.collector = collector;
    }

}
