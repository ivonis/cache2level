package com.ivo.example.cache.impl;

import com.ivo.example.cache.Cache;
import com.ivo.example.cache.CacheContext;
import com.ivo.example.cache.exception.CacheException;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileSysLRUCache<K, V> extends AbstractCache<K, V> {
    static final Logger LOG = Logger.getLogger(FileSysLRUCache.class.getName());
    static final FileSystem FILE_SYSTEM = FileSystems.getDefault();
    static final String F_SEP = FILE_SYSTEM.getSeparator();
    static final int MAX_HASH_SPLIT_LEN = 3;
    static final Pattern HASH_SPLIT = Pattern.compile(".{1," + MAX_HASH_SPLIT_LEN + "}");
    static final String FILE_PFX = "cdata_";
    static final String FILE_SFX = "";

    private final Path storeDir;
    private int size;

    public FileSysLRUCache(CacheContext<K, V> context) throws CacheException {
        super(context);
        size = 0;
        storeDir = FILE_SYSTEM.getPath(context.getCachePath());
        init();
    }

    static int hash(Object key) {
        return (key == null) ? 0 : key.hashCode();
    }

    @Override
    public Cache<K, V> put(K key, V value) {
        try {
            Path dir = getPathByKey(key);
            Triple<K, V> data = find(key, dir);
            if (data != null) {
                saveCacheData(key, value, data.path);
                return this;
            }
            Files.createDirectories(dir);
            Path file = Files.createTempFile(dir, FILE_PFX, FILE_SFX);
            saveCacheData(key, value, file);
            size++;
            if (size > capacity) {
                removeEldest();
            }
            return this;
        } catch (CacheException|IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Cache<K, V> putAll(Map<K, ? extends V> map) {
        for(Map.Entry<K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public V get(K key) {
        try {
            Path dir = getPathByKey(key);
            Triple<K, V> data = find(key, dir);
            if (data != null) {
                changeLastAccessTime(data.path);
                return data.value;
            }
            return null;
        } catch (CacheException e) {
            LOG.log(Level.WARNING, "get by key:" + key + " has failed", e);
            return null;
        }
    }

    @Override
    public V remove(K key) {
        try {
            Path dir = getPathByKey(key);
            Triple<K, V> data = find(key, dir);
            if (data != null) {
                removeCacheData(data.path);
                return data.value;
            }
        } catch (CacheException e) {
            LOG.log(Level.WARNING, "remove by key:" + key + " has failed", e);
            e.printStackTrace();
        }
        return null;
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
                        Triple<K, V> triple = loadCacheData(file);
                        map.put(triple.key, triple.value);
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
                    LOG.log(Level.WARNING, "visitFileFailed by path:" + file, exc);
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
        try {
            init();
        } catch (CacheException e) {
            LOG.log(Level.WARNING, "Cache initialisation error", e);
        }
        return this;
    }

    @Override
    public int size() {
        return size;
    }

    private Triple<K, V> find(K key, Path dir) throws CacheException {
        if (Files.notExists(dir)) {
            return null;
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, (Files::isRegularFile))) {
            for (Path path : ds) {
                Triple<K, V> triple = loadCacheData(path);
                if (Objects.equals(key, triple.key)) {
                    return triple;
                }
            }
        } catch (IOException e) {
            throw new CacheException(e);
        }
        return null;
    }

    private Path getPathByKey(K key) throws CacheException {
        int h = hash(key);
        String hs = String.valueOf(h);
        Matcher matcher = HASH_SPLIT.matcher(hs);
        Optional<String> optPath = matcher.results().map(MatchResult::group).reduce((s1, s2) -> s1 + F_SEP + s2);
        return storeDir.resolve(optPath.orElseThrow(CacheException::new));
    }

    private void saveCacheData(K key, V value, Path path) throws CacheException {
        try (OutputStream fs = Files.newOutputStream(path)) {
            try (ObjectOutputStream out = new ObjectOutputStream(fs)) {
                out.writeObject(key);
                out.writeObject(value);
            }
        } catch (IOException e) {
            throw new CacheException("saving the object failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Triple<K, V> loadCacheData(Path path) throws CacheException {
        try (InputStream is = Files.newInputStream(path)) {
            try (ObjectInputStream oin = new ObjectInputStream(is)) {
                K k = (K) oin.readObject();
                V v = (V) oin.readObject();
                return new Triple<>(k, v, path);
            }
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new CacheException("loading the cache entry is failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    private K loadCacheKey(Path path) throws CacheException {
        try (InputStream is = Files.newInputStream(path)) {
            try (ObjectInputStream oin = new ObjectInputStream(is)) {
                return (K) oin.readObject();
            }
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new CacheException("loading the cache entry is failed", e);
        }
    }

    private void removeCacheData(Path path) throws CacheException {
        size--;
        try {
            Files.delete(path);
            clearEmptyDir(path.getParent());
        } catch (IOException e) {
            throw new CacheException(e);
        }

    }

    private void clearEmptyDir(Path dir) {
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

    private void init() throws CacheException {
        size = 0;
        final AtomicInteger restored = new AtomicInteger(0), invalid = new AtomicInteger(0);
        try {
            Files.createDirectories(storeDir);
            Files.walkFileTree(storeDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        loadCacheData(file);
                        FileSysLRUCache.this.size++;
                        restored.incrementAndGet();
                    } catch (CacheException e1) {
                        invalid.incrementAndGet();
                        LOG.warning("Found invalid cache file:" + file);
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
            if (size > capacity) {
                removeEldest();
            }
        } catch (IOException e) {
            throw new CacheException(e);
        } finally {
            LOG.info("Cache initialisation results: restored=" + restored + ", invalid(removed)=" + invalid);
        }

    }

    private void removeEldest() {
        final int candidatesMaxCapacity = size - capacity;
        if (candidatesMaxCapacity <= 0) {
            return;
        }
        final TreeMap<FileTime, Path> candidates = new TreeMap<>();

        try {
            Files.walkFileTree(storeDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    FileTime time0 = attrs.creationTime();
                    FileTime time1 = attrs.lastAccessTime();
                    FileTime time2 = attrs.lastModifiedTime();
                    FileTime time = CacheUtils.max(time0, time1, time2);
                    if (candidates.size() < candidatesMaxCapacity) {
                        candidates.put(time, file);
                    } else {
                        FileTime lastKey = candidates.lastKey();
                        if (time != null && time.compareTo(lastKey) < 0) {
                            candidates.remove(lastKey);
                            candidates.put(time, file);
                        }
                    }
                    return super.visitFile(file, attrs);
                }
            });
            while (!candidates.isEmpty()) {
                FileTime time = candidates.firstKey();
                Path path = candidates.remove(time);
                try {
                    removeCacheData(path);
                    LOG.log(Level.INFO, "Remove eldest:" + path);
                } catch (CacheException e) {
                    LOG.log(Level.WARNING, "Couldn't remove eldest:" + path, e);
                    //nothing
                }
            }
        } catch (IOException e) {
            //nothing
        }
    }

    private void loadAllKeys(final Collection<K> keys) {
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

    private static void changeLastAccessTime(Path path) {
        try {
            Files.getFileAttributeView(path, BasicFileAttributeView.class)
                    .setTimes(null, FileTime.from(Instant.now()), null);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Cannot change of lastAccessTime for:" + path, e);
        }
    }

    private static class Triple<K, V> {
        K key;
        V value;
        Path path;

        Triple(K key, V value, Path path) {
            this.key = key;
            this.value = value;
            this.path = path;
        }
    }
}
