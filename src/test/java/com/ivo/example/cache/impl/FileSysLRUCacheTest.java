package com.ivo.example.cache.impl;

import com.ivo.example.cache.Cache;
import com.ivo.example.cache.CachePool;
import com.ivo.example.cache.CacheType;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class FileSysLRUCacheTest extends AbstractLRUCacheTest {
    @TempDir
    static Path cacheDir;

    protected Cache<Integer, Object> buildCache() {
        return  CachePool.<Integer, Object>cacheBuilder(CacheType.FileLRU)
                .capacity(10)
                .path(cacheDir.toString())
                .build();
    }
}