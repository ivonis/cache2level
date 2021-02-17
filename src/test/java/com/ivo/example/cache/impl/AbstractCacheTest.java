package com.ivo.example.cache.impl;

import com.ivo.example.cache.Cache;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public abstract class AbstractCacheTest extends Assertions {

  protected final Cache<Integer, Object> cache = buildCache();

  protected abstract Cache<Integer, Object> buildCache();

  protected abstract Class<?> getInstanceClass();

  @AfterEach
  void tearDown() {
    if (cache != null) {
      cache.clear();
    }
  }

  @Test
  void testInstanceType() {
    assertEquals(getInstanceClass(), cache.getClass());
  }

  @Test
  void testPutAndGet() {
    Cache<Integer, Object> cache1 = cache.put(1, "1");
    assertSame(cache1, cache);
    assertEquals("1", cache.get(1));
  }

  @SuppressWarnings("Java8MapApi")
  @Test
  void testSizeAndPutAllAndKeysIt() {
    cache.put(1, "1");
    int cacheSize = cache.size();
    assertEquals(cacheSize, 1);
    Map<Integer, Object> data = Map.of(2, "2", 3, "3", 4, "4");
    cache.putAll(data);
    cacheSize += data.size();
    assertEquals(cacheSize, cache.size());
    Iterator<Integer> it = cache.keysIt();
    int itCount = 0;
    while (it.hasNext()) {
      Integer k = it.next();
      itCount++;
      Object v = cache.get(k);
      if (data.containsKey(k)) {
        assertEquals(data.get(k), v);
      } else {
        assertEquals("1", v);
      }
    }
    assertEquals(itCount, cache.size());
  }

  @Test
  void testRemove() {
    Map<Integer, Object> data = Map.of(1, "1", 2, "2", 3, "3", 4, "4");
    cache.putAll(data);
    int cacheSize = cache.size();
    assertNull(cache.remove(5));
    assertEquals(cacheSize, cache.size());
    assertEquals("4", cache.remove(4));
    assertEquals(cacheSize - 1, cache.size());
    assertNull(cache.get(4));
    assertNull(cache.remove(4));
  }

  @Test
  void testToMapAndClear() {
    Map<Integer, Object> data = Map.of(1, "1", 2, "2", 3, "3", 4, "4");
    cache.putAll(data);
    Map<Integer, Object> m = new HashMap<>();
    Map<Integer, ? super Object> m1 = cache.toMap(m);
    assertSame(m, m1);
    assertEquals(m.size(), cache.size());

    assertTrue(data.keySet().containsAll(m.keySet()));
    assertTrue(m.keySet().containsAll(data.keySet()));
    assertEquals(m.size(), data.size());
    data.forEach((k, v) -> assertEquals(v, m.get(k)));
    cache.clear();
    assertEquals(0, cache.size());
  }
}
