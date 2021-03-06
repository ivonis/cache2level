package com.ivo.example.cache.impl;

import com.ivo.example.cache.Cache;
import com.ivo.example.cache.CachePool;

class RamLRUCacheTest extends AbstractLRUCacheTest {

  protected Cache<Integer, Object> buildCache() {
    return CachePool.buildRamCache(null, 10);
  }

  @Override
  protected Class<?> getInstanceClass() {
    return RamLRUCache.class;
  }
}