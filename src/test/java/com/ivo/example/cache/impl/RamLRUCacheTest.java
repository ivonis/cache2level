package com.ivo.example.cache.impl;

import com.ivo.example.cache.Cache;
import com.ivo.example.cache.CachePool;
import com.ivo.example.cache.CacheType;

class RamLRUCacheTest extends AbstractLRUCacheTest {

    protected Cache<Integer, Object> buildCache() {
        return  CachePool.<Integer, Object>cacheBuilder(CacheType.RamLRU)
                .capacity(10).build();
    }
}