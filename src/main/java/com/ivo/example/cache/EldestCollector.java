package com.ivo.example.cache;

public interface EldestCollector<K, V> {
    void collect(Cache sender, K key, V value);
}
