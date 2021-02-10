package com.ivo.example.cache.impl;

import com.ivo.example.cache.Cache;
import com.ivo.example.cache.CachePool;
import com.ivo.example.cache.CacheType;

class Ram2QCacheTest extends AbstractCacheTest {

  @Override
  protected Cache<Integer, Object> buildCache() {
    return CachePool.<Integer, Object>cacheBuilder(CacheType.Ram2Q)
        .capacity(10).build();
  }
}