package com.ivo.example.cache;

import java.util.Iterator;
import java.util.Map;

public interface Cache<K, V> {

  Cache<K, V> put(K key, V value);

  Cache<K, V> putAll(Map<K, ? extends V> map);

  V get(K key);

  V remove(K key);

  Iterator<K> keysIt();

  Map<K, ? super V> toMap(Map<K, ? super V> map);

  Cache<K, V> clear();

  int size();

  void setListener(CacheListener<K, V> listener);
}
