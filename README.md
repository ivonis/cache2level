# cache2level

cache2level is my experiments with various caching algorithms in java.

The library contains configurable implementations of caching algorithms:
1. LRU algorithms (for RAM and file system)
2. 2Q(RAM)
3. Two-level cache

## Installation

## Usage

```java
package com.ivo.example.test;

import com.ivo.example.cache.*;
import com.ivo.example.cache.impl.Cache2l;
import java.util.HashMap;
import java.util.Map;

class CacheTest {

  public static void main(String[] args) {
    Cache<Integer, Object> cache = CachePool.<Integer, Object>cacheBuilder(CacheType.RamLRU)
        .capacity(10).build();
    cache.put(1, "a1");
    cache.put(2, "a2");
    System.out.println(cache.get(1));
    Map<Integer, Object> map = new HashMap<>();
    cache.toMap(map);
    map.forEach((key, value) -> System.out.println("[" + key + "]=" + value));

    Cache<Integer, Object> primary = CachePool.buildRamCache(null, 5);
    Cache<Integer, Object> slave = CachePool.buildFileCache(null, 5, "./cache");

    Cache2l<Integer, Object> cache2l = new Cache2l(primary, slave);
    cache2l.put(10, "10");
    cache2l.put(20, "20");
    System.out.println(cache2l.get(10));
    cache.close();// store all cache from primary to slave(in file system)

    Cache<Integer, Object> fileCache = CachePool.<Integer, Object>buildFileCache(null, 10,
        "./cache");
    System.out.println(fileCache.get(10));// 10
    System.out.println(fileCache.get(20));// 10
    //Or:
    cache2l = new Cache2l(primary, fileCache);
    System.out.println(cache2l.get(10));// 10
    System.out.println(cache2l.get(20));// 10

  }
}
```

## Contributing

## License
[MIT](https://choosealicense.com/licenses/mit/)