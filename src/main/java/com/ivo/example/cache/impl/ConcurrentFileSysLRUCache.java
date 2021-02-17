package com.ivo.example.cache.impl;

import com.ivo.example.cache.CacheContext;
import com.ivo.example.cache.CacheImpl;
import com.ivo.example.cache.Store;
import com.ivo.example.cache.exception.CacheException;
import com.ivo.example.cache.impl.FileSysLRUCache.LRUEvictFileCollector;
import com.ivo.example.util.AsyncFileInputStream;
import com.ivo.example.util.AsyncFileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

@CacheImpl(store = Store.FS, async = true)
public class ConcurrentFileSysLRUCache<K, V> extends FileSysCache<K, V> {

  static final int BUF_SIZE = 1024;

  private final AtomicInteger size;

  @SuppressWarnings("unused")
  public ConcurrentFileSysLRUCache(CacheContext<K, V> context) {
    super(context);
    size = new AtomicInteger();
  }

  @Override
  public int size() {
    return size.get();
  }

  @Override
  protected void saveCacheData(K key, V value, Path path) throws CacheException {
    try (AsyncFileOutputStream fos = new AsyncFileOutputStream(path, BUF_SIZE, true);
        ObjectOutputStream out = new ObjectOutputStream(fos)) {
      out.writeObject(key);
      out.writeObject(value);
    } catch (IOException e) {
      throw new CacheException(e);
    }
  }

  @SuppressWarnings("unchecked")
  protected Entry<K, V> loadCacheData(Path path) throws CacheException {
    try (AsyncFileInputStream fis = new AsyncFileInputStream(path, false);
        ObjectInputStream ois = new ObjectInputStream(fis)) {
      K k = (K) ois.readObject();
      V v = (V) ois.readObject();
      return new Entry<>(k, v, path);
    } catch (IOException | ClassNotFoundException | ClassCastException e) {
      throw new CacheException(e);
    }
  }

  @SuppressWarnings("unchecked")
  protected K loadCacheKey(Path path) throws CacheException {
    try (AsyncFileInputStream fis = new AsyncFileInputStream(path, false);
        ObjectInputStream ois = new ObjectInputStream(fis)) {
      return (K) ois.readObject();
    } catch (IOException | ClassNotFoundException | ClassCastException e) {
      throw new CacheException(e);
    }
  }

  @Override
  protected void resetSize() {
    size.set(0);
  }

  @Override
  protected void incSize() {
    size.incrementAndGet();
  }

  @Override
  protected void decSize() {
    size.decrementAndGet();
  }

  @Override
  protected FileCollector evictFileCollector() {
    return new LRUEvictFileCollector(this);
  }
}
