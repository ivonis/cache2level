package com.ivo.example.cache;

import java.util.Iterator;
import java.util.Map;

public interface Cache<K, V> {
    void put(K key, V value);

    V get(K key);

    V remove(K key);

    Iterator<K> keysIt();

    void clear();

    void setEldestCollector(EldestCollector<K, V> collector);

}
