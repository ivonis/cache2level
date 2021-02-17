package com.ivo.example.cache;

public interface CacheListener<K, V> {

  void onEvicted(Object owner, K key, V value);
}
