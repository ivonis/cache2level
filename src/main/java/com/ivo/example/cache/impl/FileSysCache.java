package com.ivo.example.cache.impl;

import com.ivo.example.cache.Cache;
import com.ivo.example.cache.CacheContext;
import com.ivo.example.cache.exception.CacheException;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class FileSysCache<K, V> extends AbstractCache<K, V> {

  static final FileSystem FILE_SYSTEM = FileSystems.getDefault();
  static final String F_SEP = FILE_SYSTEM.getSeparator();
  static final int MAX_HASH_SPLIT_LEN = 2;
  static final Pattern HASH_SPLIT = Pattern.compile(".{1," + MAX_HASH_SPLIT_LEN + "}");
  static final String FILE_PFX = "cdata_";
  static final String FILE_SFX = "";
  private static final Logger LOG = Logger.getLogger(FileSysCache.class.getName());
  protected final Path storeDir;

  public FileSysCache(CacheContext<K, V> context) {
    super(context);
    storeDir = FILE_SYSTEM.getPath(context.getCachePath());
    init();
  }

  static int hash(Object key) {
    return (key == null) ? 0 : key.hashCode();
  }

  static void changeLastAccessTime(Path path) {
    try {
      Files.getFileAttributeView(path, BasicFileAttributeView.class)
          .setTimes(null, FileTime.from(Instant.now()), null);
    } catch (IOException e) {
      LOG.log(Level.WARNING, "Cannot change of lastAccessTime for:" + path, e);
    }
  }

  @Override
  public Cache<K, V> clear() {
    if (Files.notExists(storeDir)) {
      try {
        Files.createDirectories(storeDir);
      } catch (IOException e) {
        //nothing
      }
      return this;
    }
    try {
      Files.walkFileTree(storeDir, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Files.deleteIfExists(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
          FileSysLRUCache.LOG.log(Level.WARNING, "visitFileFailed by path:" + file, exc);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          if (!Files.isSameFile(storeDir, dir)) {
            Files.delete(dir);
          }
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException e) {
      //nothing
    }
    init();
    return this;
  }

  protected void removeCacheData(Path path) throws CacheException {
    try {
      Files.delete(path);
      clearEmptyDir(path.getParent());
    } catch (IOException e) {
      throw new CacheException(e);
    }

  }

  protected void clearEmptyDir(Path dir) {
    Path d = dir, p = d.getParent();
    try {
      while (!Files.isSameFile(storeDir, d) && p != null) {
        try {
          Files.delete(d);
          d = p;
          p = d.getParent();
        } catch (DirectoryNotEmptyException e) {
          break;
        }
      }
    } catch (IOException e) {
      //nothing
    }
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  protected Path getPathByKey(K key) {
    int h = hash(key);
    String hs = Integer.toHexString(h);
    Matcher matcher = HASH_SPLIT.matcher(hs);
    Optional<String> optPath = matcher.results().map(MatchResult::group)
        .reduce((s1, s2) -> s1 + F_SEP + s2);
    return storeDir.resolve(optPath.get());
  }

  @Override
  public Cache<K, V> put(K key, V value) {
    try {
      Path dir = getPathByKey(key);
      Entry<K, V> data = find(key, dir);
      if (data != null) {
        saveCacheData(key, value, data.path);
        return this;
      }
      Files.createDirectories(dir);
      Path file = Files.createTempFile(dir, FILE_PFX, FILE_SFX);
      saveCacheData(key, value, file);
      incSize();
      if (size() > capacity) {
        evict();
      }
      return this;
    } catch (CacheException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Cache<K, V> putAll(Map<K, ? extends V> map) {
    for (Map.Entry<K, ? extends V> entry : map.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
    return this;
  }

  @Override
  public V get(K key) {
    Entry<K, V> data = null;
    try {
      Path dir = getPathByKey(key);
      data = find(key, dir);
      if (data != null) {
        changeLastAccessTime(data.path);
      }
    } catch (CacheException e) {
      LOG.log(Level.WARNING, "get by key:" + key + " has failed", e);
    }
    return data != null ? data.value : null;
  }

  @Override
  public V remove(K key) {
    Entry<K, V> data = null;
    try {
      Path dir = getPathByKey(key);
      data = find(key, dir);
      if (data != null) {
        decSize();
        removeCacheData(data.path);
      }
    } catch (CacheException e) {
      LOG.log(Level.WARNING, "remove by key:" + key + " has failed", e);
    }
    return data != null ? data.value : null;
  }

  @Override
  public Iterator<K> keysIt() {
    Collection<K> c = new ArrayList<>();
    loadAllKeys(c);
    return c.iterator();
  }

  @Override
  public Map<K, ? super V> toMap(Map<K, ? super V> map) {
    if (map == null) {
      return null;
    }
    try {
      Files.walkFileTree(storeDir, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
          try {
            Entry<K, V> entry = loadCacheData(file);
            map.put(entry.key, entry.value);
          } catch (CacheException e) {
            LOG.log(Level.WARNING, "loadCacheData by path:" + file + " has failed", e);
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
          LOG.log(Level.WARNING, "visitFileFailed by path:" + file, exc);
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException e) {
      //nothing
    }
    return map;
  }

  protected Entry<K, V> find(K key, Path dir) throws CacheException {
    if (Files.notExists(dir)) {
      return null;
    }
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, (Files::isRegularFile))) {
      for (Path path : ds) {
        Entry<K, V> entry = loadCacheData(path);
        if (Objects.equals(key, entry.key)) {
          return entry;
        }
      }
    } catch (IOException e) {
      throw new CacheException(e);
    }
    return null;
  }

  protected abstract void saveCacheData(K key, V value, Path path) throws CacheException;

  protected abstract Entry<K, V> loadCacheData(Path path) throws CacheException;

  protected void init() {
    resetSize();
    final AtomicInteger restored = new AtomicInteger(0), invalid = new AtomicInteger(0);
    try {
      Files.createDirectories(storeDir);
      Files.walkFileTree(storeDir, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          try {
            loadCacheData(file);
            incSize();
            restored.incrementAndGet();
          } catch (CacheException e1) {
            invalid.incrementAndGet();
            FileSysLRUCache.LOG.warning("Found invalid cache file:" + file);
            Files.delete(file);
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          if (!Files.isSameFile(storeDir, dir)) {
            try {//try to remove empty dir
              Files.delete(dir);
            } catch (IOException e) {
              //nothing
            }
          }
          return FileVisitResult.CONTINUE;
        }
      });
      if (size() > capacity) {
        evict();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      FileSysLRUCache.LOG.info(
          "Cache initialisation results: restored=" + restored + ", invalid(removed)=" + invalid);
    }

  }

  protected abstract void resetSize();

  protected abstract void incSize();

  protected abstract void decSize();

  protected abstract K loadCacheKey(Path path) throws CacheException;

  protected void evict() {
    if (size() - capacity <= 0) {
      return;
    }
    final FileCollector collector = evictFileCollector();
    try {
      Files.walkFileTree(storeDir, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          FileVisitResult result = super.visitFile(file, attrs);
          collector.collect(file, attrs);
          return result;
        }
      });
      collector.processCollected();
    } catch (IOException e) {
      //nothing
    }
  }

  protected abstract FileCollector evictFileCollector();

  protected void loadAllKeys(final Collection<K> keys) {
    try {
      Files.walkFileTree(storeDir, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          K k;
          try {
            k = loadCacheKey(file);
            keys.add(k);
            changeLastAccessTime(file);

          } catch (CacheException e) {
            //nothing
          }
          return super.visitFile(file, attrs);
        }
      });
    } catch (IOException e) {
      //nothing
    }
  }

  interface FileCollector {

    void collect(Path file, BasicFileAttributes attrs);

    void processCollected();
  }

  static class Entry<K, V> {

    K key;
    V value;
    Path path;

    Entry(K key, V value, Path path) {
      this.key = key;
      this.value = value;
      this.path = path;
    }
  }
}
