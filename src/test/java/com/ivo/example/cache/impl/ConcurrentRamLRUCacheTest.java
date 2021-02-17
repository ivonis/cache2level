package com.ivo.example.cache.impl;

import com.ivo.example.cache.Cache;
import com.ivo.example.cache.CachePool;

class ConcurrentRamLRUCacheTest extends AbstractLRUCacheTest {

  @Override
  protected Cache<Integer, Object> buildCache() {
    return CachePool.buildRamCache(null, 10, true);
  }

  @Override
  protected Class<?> getInstanceClass() {
    return ConcurrentRamLRUCache.class;
  }
}