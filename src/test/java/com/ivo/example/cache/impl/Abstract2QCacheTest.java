package com.ivo.example.cache.impl;

import com.ivo.example.cache.Algorithm;
import com.ivo.example.cache.Cache;
import com.ivo.example.cache.CachePool;
import org.junit.jupiter.api.Test;

public abstract class Abstract2QCacheTest extends AbstractCacheTest {
  static final int capacity = 20;
  static final int capIn = 5;
  static final int capOut = 10;
  static final int capHot = 5;
  protected final Cache<Integer, Object> cache2q = CachePool
      .buildRamCache(null, Algorithm.TwoQ, capacity, async());

  @Test
  void test2Q() {
    // fill In & Out

    for (int i = 1; i <= capacity - capHot; i++) {
      cache2q.put(i, valByKey(i));
    }
    //In: 11,12,13,14,15
    //Out:1,2,3,4,5,6,7,8,9,10
    //Hot:-
    assertEquals(capIn + capOut, cache2q.size());

    putToCache(16);
    //In: 12,13,14,15,16
    //Out:2,3,4,5,6,7,8,9,10,11
    //Hot:-
    assertEquals(capIn + capOut, cache2q.size());
    assertNull(cache2q.get(1));

    //test get from In:
    assertEquals(valByKey(12), cache2q.get(12));
    putToCache(17);
    //In: 13,14,15,16,17
    //Out:3,4,5,6,7,8,9,10,11,12
    //Hot:-
    assertEquals(capIn + capOut, cache2q.size());
    assertNull(cache2q.get(2));

    //test get from Out:
    assertEquals(valByKey(3), cache2q.get(3));
    //In: 13,14,15,16,17
    //Out:4,5,6,7,8,9,10,11,12
    //Hot:3
    putToCache(18);
    //In: 14,15,16,17,18
    //Out:4,5,6,7,8,9,10,11,12,13
    //Hot:3
    assertEquals(capIn + capOut + 1, cache2q.size());
    assertEquals(valByKey(4), cache2q.get(4));
    //In: 14,15,16,17,18
    //Out:5,6,7,8,9,10,11,12,13
    //Hot:3,4
    assertEquals(valByKey(6), cache2q.get(6));
    putToCache(19);
    putToCache(20);
    //In: 16,17,18,19,20
    //Out:5,7,8,9,10,11,12,13,14,15
    //Hot:3,4,6
    assertEquals(capIn + capOut + 3, cache2q.size());
    assertEquals(valByKey(10), cache2q.get(10));
    assertEquals(valByKey(12), cache2q.get(12));
    putToCache(21);
    putToCache(22);
    //In: 18,19,20,21,22
    //Out:5,7,8,9,11,13,14,15,16,17
    //Hot:3,4,6,10,12
    assertEquals(capacity, cache2q.size());

    //Test LRU evict from Hot
    assertEquals(valByKey(5), cache2q.get(5));
    //In: 18,19,20,21,22
    //Out:7,8,9,11,13,14,15,16,17
    //Hot:4,6,10,12,5 (3 - evicted)
    assertEquals(capacity - 1, cache2q.size());
    assertNull(cache2q.get(3));
    assertEquals(valByKey(4), cache2q.get(4));
    //Hot:6,10,12,5,4
    assertEquals(valByKey(8), cache2q.get(8));
    //Out:7,9,11,13,14,15,16,17
    //Hot:10,12,5,4,8 (6 - evicted)
    assertEquals(capacity - 2, cache2q.size());
    assertNull(cache2q.get(6));
    assertEquals(valByKey(12), cache2q.get(12));
    //Hot:10,5,4,8,12
    assertEquals(valByKey(7), cache2q.get(7));
    //Out:9,11,13,14,15,16,17
    //Hot:5,4,8,12,7 (10 - evicted)
    assertNull(cache2q.get(10));
    assertEquals(valByKey(5), cache2q.get(5));
    //Hot:4,8,12,7,5
    assertEquals(valByKey(11), cache2q.get(11));
    //Out:9,13,14,15,16,17
    //Hot:8,12,7,5,11 (4 - evicted)
    assertNull(cache2q.get(4));
    assertEquals(valByKey(11), cache2q.get(11));
  }

  protected void putToCache(int key) {
    cache2q.put(key, valByKey(key));
  }

  protected Object valByKey(int key) {
    return "val" + key;
  }

  protected boolean async() {
    return false;
  }
}
