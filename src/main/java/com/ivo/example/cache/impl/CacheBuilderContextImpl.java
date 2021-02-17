package com.ivo.example.cache.impl;

import com.ivo.example.cache.Algorithm;
import com.ivo.example.cache.CacheBuilderContext;
import com.ivo.example.cache.CacheContext;
import com.ivo.example.cache.Store;

public class CacheBuilderContextImpl<K, V> implements CacheBuilderContext<K, V> {

  Algorithm alg;
  Store store;
  boolean async;
  CacheContextImpl<K, V> context;

  public CacheBuilderContextImpl() {
    alg = Algorithm.LRU;
    store = Store.RAM;
    async = false;
    context = new CacheContextImpl<>();
  }

  public CacheBuilderContextImpl(CacheBuilderContextImpl<K, V> ctx) {
    alg = ctx.alg;
    store = ctx.store;
    async = ctx.async;
    context = ctx.context;
  }

  @Override
  public Algorithm getAlg() {
    return alg;
  }

  @Override
  public void setAlg(Algorithm alg) {
    this.alg = alg;
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
  public Store getStore() {
    return store;
  }

  @Override
  public void setStore(Store store) {
    this.store = store;
  }

  @Override
  public boolean isAsync() {
    return async;
  }

  @Override
  public void setAsync(boolean async) {
    this.async = async;
  }

  @Override
  public CacheContext<K, V> getCacheContext() {
    return context;
  }

}
