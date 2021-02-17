package com.ivo.example.cache.impl;

import org.junit.jupiter.api.Test;

public abstract class AbstractLRUCacheTest extends AbstractCacheTest {

  @Test
  void testLRU() {
    assertEquals(cache.size(), 0);
    for (int i = 0; i < 10; i++) {
      cache.put(i, Integer.toString(i));
    }
    // in cache: (0,1,2,3,4,5,6,7,8,9)
    assertEquals(10, cache.size());
    cache.put(10, "10");// in cache: (1,2,3,4,5,6,7,8,9,10), 0 - removed

    assertNull(cache.get(0));
    assertEquals(10, cache.size());
    assertNotNull(cache.get(1)); // usage 1, in cache: (2,3,4,5,6,7,8,9,10,1)

    cache.put(11, "11");// in cache: (3,4,5,6,7,8,9,10,1,11), 2 - removed
    assertEquals(10, cache.size());
    assertNull(cache.get(2));

    cache.put(3, "03"); // overwrite 3, // in cache: (4,5,6,7,8,9,10,1,11,3)
    assertEquals(10, cache.size());

    cache.put(12, "12");// in cache: (5,6,7,8,9,10,1,11,3,12), 4 - removed
    assertEquals(10, cache.size());
    assertNull(cache.get(4));

    cache.put(13, "13");// in cache: (6,7,8,9,10,1,11,3,12,13), 5 - removed
    assertEquals(10, cache.size());
    assertNull(cache.get(5));

    assertNotNull(cache.get(6)); // usage 6, in cache: (7,8,9,10,1,11,3,12,13,6)
    assertNotNull(cache.get(7)); // usage 7, in cache: (8,9,10,1,11,3,12,13,6,7)
    assertNotNull(cache.get(8)); // usage 8, in cache: (9,10,1,11,3,12,13,6,7,8)
    assertNotNull(cache.get(9)); // usage 9, in cache: (10,1,11,3,12,13,6,7,8,9)

    cache.put(14, "14");// in cache: (1,11,3,12,13,6,7,8,9,14), 10 - removed
    assertEquals(10, cache.size());
    assertNull(cache.get(10));

    cache.put(15, "15");// in cache: (11,3,12,13,6,7,8,9,14,15), 1 - removed
    assertEquals(10, cache.size());
    assertNull(cache.get(1));

    cache.clear();
    assertEquals(0, cache.size());
  }
}
