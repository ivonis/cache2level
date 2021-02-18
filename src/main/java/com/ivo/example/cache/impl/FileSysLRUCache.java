package com.ivo.example.cache.impl;

import com.ivo.example.cache.CacheContext;
import com.ivo.example.cache.CacheImpl;
import com.ivo.example.cache.Store;
import com.ivo.example.cache.exception.CacheException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@CacheImpl(store = Store.FS)
public class FileSysLRUCache<K, V> extends FileSysCache<K, V> {

  static final Logger LOG = Logger.getLogger(FileSysLRUCache.class.getName());

  private int size;

  @SuppressWarnings("unused")
  public FileSysLRUCache(CacheContext<K, V> context) {
    super(context);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  protected void saveCacheData(K key, V value, Path path) throws CacheException {
    try (OutputStream fs = Files.newOutputStream(path)) {
      try (ObjectOutputStream out = new ObjectOutputStream(fs)) {
        out.writeObject(key);
        out.writeObject(value);
      }
    } catch (IOException e) {
      throw new CacheException("saving the object failed", e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Entry<K, V> loadCacheData(Path path) throws CacheException {
    try (InputStream is = Files.newInputStream(path)) {
      try (ObjectInputStream oin = new ObjectInputStream(is)) {
        K k = (K) oin.readObject();
        V v = (V) oin.readObject();
        return new Entry<>(k, v, path);
      }
    } catch (IOException | ClassNotFoundException | ClassCastException e) {
      throw new CacheException("loading the cache entry is failed", e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  protected K loadCacheKey(Path path) throws CacheException {
    try (InputStream is = Files.newInputStream(path)) {
      try (ObjectInputStream oin = new ObjectInputStream(is)) {
        return (K) oin.readObject();
      }
    } catch (IOException | ClassNotFoundException | ClassCastException e) {
      throw new CacheException("loading the cache entry is failed", e);
    }
  }

  @Override
  protected FileCollector evictFileCollector() {
    return new LRUEvictFileCollector(this);
  }

  @Override
  protected void resetSize() {
    size = 0;
  }

  @Override
  protected void incSize() {
    size++;
  }

  @Override
  protected void decSize() {
    size--;
  }

  static class LRUEvictFileCollector implements FileCollector {

    final TreeMap<FileTime, Path> candidates = new TreeMap<>();
    final FileSysCache<?, ?> cache;

    LRUEvictFileCollector(FileSysCache<?, ?> cache) {
      this.cache = cache;
    }

    @Override
    public void collect(Path file, BasicFileAttributes attrs) {
      FileTime time0 = attrs.creationTime();
      FileTime time1 = attrs.lastAccessTime();
      FileTime time2 = attrs.lastModifiedTime();
      FileTime time = CacheUtils.max(time0, time1, time2);
      if (candidates.size() < cache.size() - cache.capacity) {
        candidates.put(time, file);
      } else {
        FileTime lastKey = candidates.lastKey();
        if (time != null && time.compareTo(lastKey) < 0) {
          candidates.remove(lastKey);
          candidates.put(time, file);
        }
      }
    }

    @Override
    public void processCollected() {
      while (!candidates.isEmpty()) {
        FileTime time = candidates.firstKey();
        Path path = candidates.remove(time);
        try {
          cache.removeCacheData(path, true);
          cache.decSize();
        } catch (CacheException e) {
          LOG.log(Level.WARNING, "Couldn't remove eldest:" + path, e);
        }
      }
    }
  }

}
