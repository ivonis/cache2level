package com.ivo.example.cache.impl;

import com.ivo.example.cache.CacheContext;

public class CacheContextImpl<K, V> implements CacheContext<K, V> {
    static final int DEFAULT_CAPACITY = 17;
    static final String DEFAULT_CACHE_PATH = "./cache";
    int maxCapacity;
    String cachePath;

    public CacheContextImpl() {
        maxCapacity = DEFAULT_CAPACITY;
        cachePath = DEFAULT_CACHE_PATH;
    }

    public CacheContextImpl(CacheContextImpl<K, V> ctx) {
        maxCapacity = ctx.maxCapacity > 0 ? ctx.maxCapacity : DEFAULT_CAPACITY;
        cachePath = ctx.cachePath != null ? ctx.cachePath : DEFAULT_CACHE_PATH;
    }

    @Override
    public int getMaxCapacity() {
        return maxCapacity;
    }

    @Override
    public String getCachePath() {
        return cachePath;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public void setCachePath(String cachePath) {
        this.cachePath = cachePath;
    }
}
