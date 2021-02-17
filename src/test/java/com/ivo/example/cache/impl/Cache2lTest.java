package com.ivo.example.cache.impl;

import com.ivo.example.cache.Algorithm;
import com.ivo.example.cache.Cache;
import com.ivo.example.cache.CacheListener;
import com.ivo.example.cache.CachePool;
import com.ivo.example.cache.exception.CacheException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Cache2lTest extends AbstractCacheTest {

  @TempDir
  static Path cacheDir;

  private Cache2l<Integer, Object> cache2l;

  @Test
  void test2levelCache() {
    Cache<Integer, Object> primary = CachePool.buildRamCache(null, 5);
    Cache<Integer, Object> slave = CachePool.buildFileCache(null, 5, cacheDir.toString());
    cache2l = new TestCache2l(primary, slave);
    cache2l.clear();
    cache2l.setListener((owner, key, value) -> fail("Must not be eldest to remove"));
    for (int i = 1; i < 6; i++) {
      cache2l.put(i, Integer.toString(i));
    }
    cache2l.setListener((owner, key, value) -> assertAll(
        () -> assertSame(primary, owner),
        () -> assertEquals(1, key),
        () -> assertEquals("1", value)));
    cache2l.put(6, "6");
    assertEquals(5, primary.size());
    assertEquals(1, slave.size());
    assertNull(primary.get(1));
    assertEquals("1", slave.get(1));
    cache2l.setListener(null);
    assertEquals("1", cache2l.get(1));// move (1) from slave to primary
    assertEquals("1", primary.get(1));
    assertNull(slave.get(1));
    Map<Integer, Object> pm = new HashMap<>();
    primary.toMap(pm);
    try {
      cache2l.close();
      assertEquals(0, primary.size());
      assertEquals(5, slave.size());
      Map<Integer, Object> sm = new HashMap<>();
      slave.toMap(sm);
      assertAll(
          () -> assertEquals(pm.size(), sm.size()),
          () -> assertTrue(pm.entrySet().containsAll(sm.entrySet())),
          () -> assertTrue(sm.entrySet().containsAll(pm.entrySet()))
      );
    } catch (IOException e) {
      fail(e);
    }
  }

  @Override
  protected Cache<Integer, Object> buildCache() {
    try {
      cache2l = CachePool.build2LRamFileCache(null, Algorithm.LRU, Algorithm.LRU, 5, 5, cacheDir.toString());
      return cache2l;
    } catch (CacheException e) {
      return fail(e);
    }
  }

  @Override
  protected Class<?> getInstanceClass() {
    return Cache2l.class;
  }

  private static class TestCache2l extends Cache2l<Integer, Object> {

    private CacheListener<Integer, Object> listener;

    public TestCache2l(Cache<Integer, Object> primary, Cache<Integer, Object> slave) {
      super(primary, slave);
    }

    @Override
    public void setListener(CacheListener<Integer, Object> listener) {
      this.listener = listener;
    }

    @Override
    public void onEvicted(Object owner, Integer key, Object value) {
      super.onEvicted(owner, key, value);
      if (listener != null) {
        listener.onEvicted(owner, key, value);
      }
    }
  }
}