package com.ivo.example.cache.impl;

import com.ivo.example.cache.Algorithm;
import com.ivo.example.cache.Cache;
import com.ivo.example.cache.CacheContext;
import com.ivo.example.cache.CacheImpl;
import com.ivo.example.util.ConcurrentHashQueue;
import com.ivo.example.util.ConcurrentLRUHashQueue;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@CacheImpl(alg = Algorithm.TwoQ, async = true)
public class ConcurrentRam2QCache<K, V> extends AbstractCache<K, V> {

  final ConcurrentHashMap<K, V> storage;
  private final ConcurrentHashQueue<K> queueIn, queueOut, queueHot;

  @SuppressWarnings("unused")
  public ConcurrentRam2QCache(CacheContext<K, V> context) {
    super(context);
    int maxCapacity = context.getMaxCapacity();
    storage = new ConcurrentHashMap<>(maxCapacity);
    int[] caps = calcQueueCapacities(maxCapacity);
    queueIn = new ConcurrentHashQueue<>(caps[0]);
    queueOut = new ConcurrentHashQueue<>(caps[1]);
    queueHot = new ConcurrentLRUHashQueue<>(caps[2]);
    queueIn.setListener(queueOut::add);
    queueOut.setListener(this::remove);
    queueHot.setListener(key -> evict(key, this.remove(key)));
  }

  protected int[] calcQueueCapacities(int totalCapacity) {
    int in, out, hot, remains = totalCapacity;
    out = remains / 2;
    remains -= out;
    in = remains / 2;
    hot = remains - in;
    return new int[]{in, out, hot};
  }

  @Override
  public Cache<K, V> put(K key, V value) {
    if (!storage.containsKey(key)) {
      queueIn.add(key);
    }
    storage.put(key, value);
    return this;
  }

  @Override
  public Cache<K, V> putAll(Map<K, ? extends V> map) {
    if (map != null && !map.isEmpty()) {
      map.forEach(this::put);
    }
    return this;
  }

  @Override
  public V get(K key) {
    V val = storage.get(key);
    if (val != null) {
      if (queueHot.contains(key)) {
        queueHot.offer(key);
      } else {
        if (queueOut.contains(key)) {
          queueHot.add(key);
          queueOut.remove(key);
        }
      }
      return val;
    }
    V createdValue = createValue(key);
    if (createdValue == null) {
      return null;
    }
    put(key, createdValue);
    return storage.get(key);
  }

  @Override
  public V remove(K key) {
    V val = storage.remove(key);
    if (val != null) {
      queueIn.remove(key);
      queueOut.remove(key);
      queueHot.remove(key);
      return val;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
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

  @SuppressWarnings("unused")
  protected V createValue(K key) {
    return null;
  }
}
