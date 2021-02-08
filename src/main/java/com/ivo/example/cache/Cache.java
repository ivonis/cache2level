package com.ivo.example.cache;

import com.ivo.example.cache.exception.CacheException;

import java.util.Iterator;
import java.util.Map;

public interface Cache<K, V> {
    Cache<K, V> put(K key, V value) throws CacheException;

    Cache<K, V> putAll(Map<K, ? extends V> map) throws CacheException;

    V get(K key);

    V remove(K key);

    Iterator<K> keysIt();

    Map<K, ? super V> toMap(Map<K, ? super V> map);

    Cache<K, V> clear();

    int size();

    void setListener(CacheListener<K, V> listener);
}
