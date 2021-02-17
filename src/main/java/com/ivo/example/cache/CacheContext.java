package com.ivo.example.cache;

public interface CacheContext<K, V> {

  int getMaxCapacity();

  String getCachePath();
}
