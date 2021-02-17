package com.ivo.example.cache;

import com.ivo.example.cache.exception.CacheException;
import com.ivo.example.cache.impl.Cache2l;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachePool {

  private static final Map<String, Cache<?, ?>> cacheMap = new ConcurrentHashMap<>();

  private CachePool() {

  }

  @SuppressWarnings("unchecked")
  public static <K, V> Cache<K, V> get(String key) throws ClassCastException {
    Cache<?, ?> cache = cacheMap.get(key);
    if (cache == null) {
      return null;
    }
    return (Cache<K, V>) cache;
  }

  public static Cache<?, ?> put(String key, Cache<?, ?> cache) {
    return cacheMap.put(key, cache);
  }

  public static <K, V> CacheBuilder<K, V> cacheBuilder() {
    return new CacheBuilder<>();
  }

  public static <K, V> CacheBuilder<K, V> cacheBuilder(Algorithm alg) {
    return new CacheBuilder<K, V>().begin(alg);
  }

  public static <K, V> Cache<K, V> buildRamCache(String key, int capacity) {
    return buildRamCache(key, capacity, false);
  }

  public static <K, V> Cache<K, V> buildRamCache(String key, int capacity, boolean async) {
    return buildRamCache(key, Algorithm.LRU, capacity, async);
  }

  public static <K, V> Cache<K, V> buildRamCache(String key, Algorithm alg, int capacity) {
    return buildRamCache(key, alg, capacity, false);
  }

  public static <K, V> Cache<K, V> buildRamCache(String key, Algorithm alg, int capacity,
      boolean async) {
    return new CacheBuilder<K, V>()
        .begin(alg)
        .store(Store.RAM)
        .async(async)
        .capacity(capacity)
        .build(key);
  }

  public static <K, V> Cache<K, V> buildFileCache(String key, int capacity, String path) {
    return buildFileCache(key, capacity, path, false);
  }

  public static <K, V> Cache<K, V> buildFileCache(String key, int capacity, String path,
      boolean async) {
    return buildFileCache(key, Algorithm.LRU, capacity, path, async);
  }

  public static <K, V> Cache<K, V> buildFileCache(String key, Algorithm alg, int capacity,
      String path) {
    return buildFileCache(key, alg, capacity, path, false);
  }

  public static <K, V> Cache<K, V> buildFileCache(String key, Algorithm alg, int capacity,
      String path, boolean async) {
    return new CacheBuilder<K, V>()
        .begin(alg)
        .store(Store.FS)
        .async(async)
        .capacity(capacity)
        .path(path)
        .build(key);
  }

  public static <K, V> Cache2lBuilder<K, V> cache2lBuilder() {
    return new Cache2lBuilder<>();
  }

  public static <K, V> Cache2lBuilder<K, V> cache2lBuilder(Algorithm primaryAlg,
      Algorithm slaveAlg) {
    return new Cache2lBuilder<K, V>().begin(primaryAlg, slaveAlg);
  }

  public static <K, V> Cache2l<K, V> build2LevelCache(String key,
      int primaryCapacity, int slaveCapacity,
      String slavePath, boolean async) {
    return build2LevelCache(key, Algorithm.TwoQ, Algorithm.LRU, Store.RAM, Store.FS,
        primaryCapacity,
        slaveCapacity, null, slavePath, async);
  }

  public static <K, V> Cache2l<K, V> build2LevelCache(String key,
      Algorithm primaryAlg, Algorithm slaveAlg,
      int primaryCapacity, int slaveCapacity,
      String slavePath, boolean async) {
    return build2LevelCache(key, primaryAlg, slaveAlg, Store.RAM, Store.FS, primaryCapacity,
        slaveCapacity, null, slavePath, async);
  }

  public static <K, V> Cache2l<K, V> build2LRamFileCache(String key,
      Algorithm primaryAlg, Algorithm slaveAlg,
      int primaryCapacity, int slaveCapacity,
      String slavePath) throws CacheException {
    return build2LevelCache(key, primaryAlg, slaveAlg, primaryCapacity, slaveCapacity, slavePath,
        false);
  }

  public static <K, V> Cache2l<K, V> build2LevelCache(String key,
      Algorithm primaryAlg, Algorithm slaveAlg,
      Store primaryStore, Store slaveStore,
      int primaryCapacity, int slaveCapacity,
      String primaryPath, String slavePath) {
    return build2LevelCache(key, primaryAlg, slaveAlg, primaryStore, slaveStore, primaryCapacity,
        slaveCapacity, primaryPath, slavePath, false);
  }

  public static <K, V> Cache2l<K, V> build2LevelCache(String key,
      Algorithm primaryAlg, Algorithm slaveAlg,
      Store primaryStore, Store slaveStore,
      int primaryCapacity, int slaveCapacity,
      String primaryPath, String slavePath,
      boolean async) {
    return new Cache2lBuilder<K, V>().begin(primaryAlg, slaveAlg)
        .store(primaryStore, slaveStore)
        .capacity(primaryCapacity, slaveCapacity)
        .primaryPath(primaryPath).slavePath(slavePath)
        .async(async).build(key);
  }
}
