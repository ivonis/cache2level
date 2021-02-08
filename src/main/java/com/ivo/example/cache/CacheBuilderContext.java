package com.ivo.example.cache;

public interface CacheBuilderContext<K, V> {
    CacheType getCacheType();

    void setCacheType(CacheType type);

    int getCapacity();

    void setCapacity(int capacity);

    String getPath();

    void setPath(String path);

    CacheContext<K, V> getCacheContext();

}
