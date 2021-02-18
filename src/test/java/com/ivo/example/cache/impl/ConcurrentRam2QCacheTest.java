package com.ivo.example.cache.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.ivo.example.cache.Algorithm;
import com.ivo.example.cache.Cache;
import com.ivo.example.cache.CachePool;

class ConcurrentRam2QCacheTest extends Abstract2QCacheTest {

  @Override
  protected Cache<Integer, Object> buildCache() {
    return CachePool.buildRamCache(null, Algorithm.TwoQ, 10, true);
  }

  @Override
  protected Class<?> getInstanceClass() {
    return ConcurrentRam2QCache.class;
  }

  @Override
  protected boolean async() {
    return true;
  }
}