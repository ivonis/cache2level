package com.ivo.example.cache.impl;

import com.ivo.example.cache.Algorithm;
import com.ivo.example.cache.Cache;
import com.ivo.example.cache.CachePool;
import com.ivo.example.cache.Store;

class Ram2QCacheTest extends Abstract2QCacheTest {

  @Override
  protected Cache<Integer, Object> buildCache() {
    return CachePool.<Integer, Object>cacheBuilder(Algorithm.TwoQ).store(Store.RAM)
        .capacity(10).build();
  }

  @Override
  protected Class<?> getInstanceClass() {
    return Ram2QCache.class;
  }
}