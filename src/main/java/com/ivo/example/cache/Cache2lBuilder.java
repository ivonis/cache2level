package com.ivo.example.cache;

import com.ivo.example.cache.impl.Cache2l;

public class Cache2lBuilder<K, V> {

  private final CacheBuilder<K, V> primaryBuilder;
  private final CacheBuilder<K, V> slaveBuilder;

  public Cache2lBuilder() {
    primaryBuilder = new CacheBuilder<>();
    slaveBuilder = new CacheBuilder<>();
  }

  public Cache2lBuilder(CacheBuilderContext<K, V> primaryContext,
      CacheBuilderContext<K, V> slaveContext) {
    this.primaryBuilder = new CacheBuilder<>(primaryContext);
    this.slaveBuilder = new CacheBuilder<>(slaveContext);
  }

  public Cache2lBuilder<K, V> begin(Algorithm primaryAlg, Algorithm slaveAlg) {
    primaryBuilder.begin(primaryAlg);
    slaveBuilder.begin(slaveAlg);
    return this;
  }

  public Cache2lBuilder<K, V> capacity(int primaryCapacity, int slaveCapacity) {
    primaryBuilder.capacity(primaryCapacity);
    slaveBuilder.capacity(slaveCapacity);
    return this;
  }

  public Cache2lBuilder<K, V> store(Store primaryStore, Store slaveStore) {
    primaryBuilder.store(primaryStore);
    slaveBuilder.store(slaveStore);
    return this;
  }

  public Cache2lBuilder<K, V> primaryPath(String path) {
    primaryBuilder.path(path);
    return this;
  }

  public Cache2lBuilder<K, V> slavePath(String path) {
    slaveBuilder.path(path);
    return this;
  }

  public Cache2lBuilder<K, V> async(boolean async) {
    primaryBuilder.async(async);
    slaveBuilder.async(async);
    return this;
  }

  public Cache2l<K, V> build() {
    Cache<K, V> primary = primaryBuilder.build();
    Cache<K, V> slave = slaveBuilder.build();

    return new Cache2l<>(primary, slave);
  }

  public Cache2l<K, V> build(String key) {
    Cache2l<K, V> cache = build();
    if (key != null) {
      CachePool.put(key, cache);
    }
    return cache;
  }
}
