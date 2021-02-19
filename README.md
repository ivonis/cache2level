# cache2level

cache2level is my experiments with various caching algorithms in java.

The library contains configurable implementations of caching algorithms:
1. LRU algorithms (for RAM and file system)
2. 2Q(RAM)
3. Two-level cache

All caching algorithms have both single-threaded (default) and thread-safe versions.

## Installation

## Usage
### Creating RAM Cache

```java
@Test
void createRAMCache() {
  // Create LRU RAM Cache with capacity = 20
    Cache<Integer, Object> cacheLRU1 = CachePool.buildRamCache(null, 20);
    Cache<Integer, Object> cacheLRU2 = CachePool
      .cacheBuilder(Algorithm.LRU)
      .store(Store.RAM)
      .capacity(20);
    Cache<Integer, Object> cacheLRU3 = new CacheBuilder<>()
      .begin(Algorithm.LRU)
      .store(Store.RAM)
      .capacity(20)
      .build();
    }
    // Create 2Q RAM Cache with capacity = 20
    Cache<Integer, Object> cache2Q1 = CachePool.buildRamCache(null, Algorithm.TwoQ, 20);
    Cache<Integer, Object> cache2Q2 = CachePool
      .cacheBuilder(Algorithm.TwoQ)
      .store(Store.RAM)
      .capacity(20);
    Cache<Integer, Object> cache2Q3 = new CacheBuilder<>()
      .begin(Algorithm.TwoQ)
      .store(Store.RAM)
      .capacity(20)
      .build();
    // use of creating caches ...
}
```
### Creating File System Cache

```java
@TempDir Path cacheDir1;
@TempDir Path cacheDir2;

@Test
void createFSCache() {
  // Create LRU FS Cache with capacity = 20
    Cache<Integer, Object> cache1 = CachePool.buildFileCache(null, 20, cacheDir1.toString());
    Cache<Integer, Object> cache2 = new CacheBuilder<>()
      .begin(Algorithm.TwoQ)
      .store(Store.FS)
      .capacity(20)
      .path(cacheDir2.toString())
      .build();
    // use of creating caches ...
}
```
### Creating Thread-safe Cache

```java
@TempDir Path cacheDir;//Thread-safe file caches can work with one directory

@Test
void createAsyncCache() {
      // in RAM
      Cache<Integer, Object> cache1 = CachePool.buildRamCache(null, Algorithm.TwoQ, 20, true);
      Cache<Integer, Object> cache2 = new CacheBuilder<>()
        .begin(Algorithm.TwoQ)
        .store(Store.RAM)
        .capacity(20)
        .async(true)
        .build();
      // in FS
      Cache<Integer, Object> cache3 = CachePool.buildFileCache(null, 20, cacheDir.toString(), true);
      Cache<Integer, Object> cache4 = new CacheBuilder<>()
        .begin(Algorithm.LRU)
        .store(Store.FS)
        .capacity(20)
        .path(cacheDir.toString())
        .async(true)
        .build();
    // use of creating caches ...
}
```
### Creating Two-Level Cache
```java
@TempDir Path cacheDir;

@Test
void createTwoLevelCache(){
    boolean isAsync = true;
    // Case A
    // Step 1: create any two cache:
    Cache<Integer, Object> primary = CachePool.buildRamCache(null,Algorithm.TwoQ,20, isAsync);
    Cache<Integer, Object> slave = CachePool.buildFileCache(null,100,cacheDir.toString(), isAsync);
    // Step 2: create Two-Level Cache
    Cache2l<Integer, Object> cache2l = new Cache2l<>(primary,slave);

    // Case B (use Cache2lBuilder)
    Cache2l<Integer, Object> cache2l = new Cache2lBuilder<Integer, Object>()
    .begin(Algorithm.TwoQ,Algorithm.LRU)
    .store(Store.RAM,Store.FS)
    .capacity(20,100)
    .slavePath(cacheDir.toString())
    .async(isAsync)
    .build();
    // use of creating caches ...
}
```

### usage of cache
```java
@Test
void usageCache(){
    Cache<Integer, String> cache = CachePool.buildRamCache(null, 3);
    cache.put(1, "str1").put(2, "str2").put(3, "str3");
    Assertions.assertEquals(3, cache.size());
    Assertions.assertEquals("str1", cache.get(1));
    Assertions.assertEquals("str2", cache.get(2));
    Assertions.assertEquals("str2", cache.get(3));

    CacheListener<Integer, String> listener = new CacheListener<>() {
        @Override
        void onEvicted(Object owner, Integer key, String value) {
            Assertions.assertEquals(1, key);
            Assertions.assertEquals("str1", value);
        }
    }
    cache.setListener(listener);
    cache.put(4, "str4");
    Assertions.assertEquals(3, cache.size());
    Assertions.assertNull(cache.get(1));
    cache2l.setListener(null);

    Map<Integer, String> map = Map.of(10, "10", 20, "20", 30, "30");
    cache.putAll(map);// fill cache from Map
    
    Map<Integer, String> map1 = new HashMap<>();
    cache.toMap(map1);// copy all data from cache to Map

    Iterator<Integer> it =  cache.keysIt();
    while (it.hasNext()) {
      Integer k = it.next();
      Assertions.assertEquals(String.valueOf(k), cache.get(k));
    }

    Assertions.assertEquals("30", cache.remove(30));
    Assertions.assertNull(cache.get(30));
    
    cache.clear();
    Assertions.assertEquals(0, cache.size());
}
```

### usage of Two-level cache
```java
@TempDir Path cacheDir;

@Test
void usageCache2l(){
    // After creation 
    Cache2l<Integer, String> cache2l = CachePool.build2LevelCache(
    null, 20, 100, cacheDir.toString(), false
    );
    //You can use the two-level cache like a normal cache:
    Cache<Integer, String> cache = cache2l;
    //cache.put(),cache.get(), etc
    //But the two-level cache has its own catch: Cache2l implements Closeable
    // In method: close() - two-tier cache flushes the cache from the primary cache to the slave
    //and if the slave cache is a cache in the file system, 
    // then when the cache is re-created on the same directory, all cached data will be restored
    cache2l.close();
}
```
### usage of CachePool
```java
@TempDir Path cacheDir;

@Test
void usageCachePool(){
    // If in first argument of all CachePool methods replace null with unique string: 
    Cache<Integer, String> cache = CachePool.buildRamCache("mycache", 20);
    // Then:  
    Cache<Integer, String> cache1 = CachePool.get("mycache");
    Assertions.assertSame(cache, cache1);

    //same result:
    Cache<Integer, Object> cache2 = new CacheBuilder<>()
    .begin(Algorithm.TwoQ)
    .store(Store.RAM)
    .capacity(20)
    .path(cacheDir2.toString())
    .build("myAnotherCache");
    Cache<Integer, String> cache3 = CachePool.get("myAnotherCache");
    Assertions.assertSame(cache2, cache3);
}
```



## Contributing

## License
[MIT](https://choosealicense.com/licenses/mit/)