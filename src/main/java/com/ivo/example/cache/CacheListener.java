package com.ivo.example.cache;

public interface CacheListener<K, V> {
    void removeEldest(Object owner, K key, V value);
}
