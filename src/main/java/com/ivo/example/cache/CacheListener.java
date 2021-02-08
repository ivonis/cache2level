package com.ivo.example.cache;

public interface CacheListener<K, V> {
    void removeEldest(Cache<K, V> owner, K key, V value);
}
