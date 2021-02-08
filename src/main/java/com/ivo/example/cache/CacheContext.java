package com.ivo.example.cache;

import java.util.function.BiConsumer;

public interface CacheContext<K, V> {
    int getMaxCapacity();
    String getCachePath();
}
