package com.ivo.example.cache.impl;

import com.ivo.example.cache.CacheBuilderContext;
import com.ivo.example.cache.CacheContext;
import com.ivo.example.cache.CacheType;

public class CacheBuilderContextImpl<K, V> implements CacheBuilderContext<K, V> {
    CacheType type;
    CacheContextImpl<K, V> context;

    public CacheBuilderContextImpl() {
        type = CacheType.RamLRU;
        context = new CacheContextImpl<>();
    }

    public CacheBuilderContextImpl(CacheBuilderContextImpl<K, V> ctx) {
        type = ctx.type;
        context = ctx.context;
    }

    @Override
    public CacheType getCacheType() {
        return type;
    }

    @Override
    public void setCacheType(CacheType type) {
        this.type = type;
    }

    @Override
    public int getCapacity() {
        return context.getMaxCapacity();
    }

    @Override
    public void setCapacity(int capacity) {
        context.maxCapacity = capacity;
    }

    @Override
    public String getPath() {
        return context.getCachePath();
    }

    @Override
    public void setPath(String path) {
        context.cachePath = path;
    }

    @Override
    public CacheContext<K, V> getCacheContext() {
        return context;
    }

}
