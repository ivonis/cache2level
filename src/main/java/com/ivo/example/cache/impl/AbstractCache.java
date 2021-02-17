package com.ivo.example.cache.impl;

import com.ivo.example.cache.Cache;
import com.ivo.example.cache.CacheContext;
import com.ivo.example.cache.CacheListener;

public abstract class AbstractCache<K, V> implements Cache<K, V> {

  protected CacheContext<K, V> context;
  protected final int capacity;
  protected CacheListener<K, V> listener;

  protected AbstractCache(CacheContext<K, V> context) {
    this.context = context;
    this.capacity = context.getMaxCapacity();
  }

  @Override
  public void setListener(CacheListener<K, V> listener) {
    this.listener = listener;
  }
}
