package com.ivo.example.cache.impl;

import com.ivo.example.cache.Cache;
import com.ivo.example.cache.CachePool;
import java.nio.file.Path;
import org.junit.jupiter.api.io.TempDir;

class FileSysLRUCacheTest extends AbstractLRUCacheTest {

  @TempDir
  static Path cacheDir;

  protected Cache<Integer, Object> buildCache() {
    return CachePool.buildFileCache(null, 10, cacheDir.toString());
  }

  @Override
  protected Class<?> getInstanceClass() {
    return FileSysLRUCache.class;
  }
}