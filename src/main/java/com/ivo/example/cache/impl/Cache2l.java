package com.ivo.example.cache.impl;

import com.ivo.example.cache.Cache;
import com.ivo.example.cache.CacheListener;
import com.ivo.example.cache.exception.CacheException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class Cache2l<K, V> implements Cache<K, V>, CacheListener<K, V>, Closeable {
    private final Cache<K, V> primaryCache;
    private final Cache<K, V> slaveCache;

    public Cache2l(Cache<K, V> primary, Cache<K, V> slave) {
        primaryCache = primary;
        slaveCache = slave;
        primaryCache.setListener(this);
    }

    @Override
    public Cache<K, V> put(K key, V value) throws CacheException {
        primaryCache.put(key, value);
        return this;
    }

    @Override
    public Cache<K, V> putAll(Map<K, ? extends V> map) throws CacheException {
        primaryCache.putAll(map);
        return this;
    }

    @Override
    public V get(K key) {
        V val = primaryCache.get(key);
        if (val == null) {
            val = slaveCache.remove(key);
            if (val != null) {
                try {
                    primaryCache.put(key, val);
                } catch (CacheException e) {
                    //nothing
                }
            }
        }
        return val;
    }

    @Override
    public V remove(K key) {
        V val = primaryCache.remove(key);
        if (val == null) {
            val = slaveCache.remove(key);
        }
        return val;
    }

    @Override
    public Iterator<K> keysIt() {
        return new Cache2lIterator();
    }

    @Override
    public Map<K, ? super V> toMap(Map<K, ? super V> map) {
        slaveCache.toMap(map);
        primaryCache.toMap(map);
        return map;
    }

    @Override
    public Cache<K, V> clear() {
        primaryCache.clear();
        slaveCache.clear();
        return this;
    }

    @Override
    public int size() {
        return primaryCache.size() + slaveCache.size();
    }

    @Override
    public void setListener(CacheListener<K, V> listener) {
       //nothing
    }

    @Override
    public void removeEldest(Cache<K, V> owner, K key, V value) {
        if (primaryCache == owner) {
            try {
                slaveCache.put(key, value);
            } catch (CacheException e) {
                //nothing
            }
        }
    }

    @Override
    public void close() throws IOException {
        Iterator<K> pkit = primaryCache.keysIt();
        while (pkit.hasNext()) {
            K k = pkit.next();
            try {
                slaveCache.put(k, primaryCache.get(k));
            } catch (CacheException e) {
                throw new IOException(e);
            }
        }
        primaryCache.clear();
    }

    final class Cache2lIterator implements Iterator<K> {
        Iterator<K> primaryIt;
        Iterator<K> slaveIt;
        Iterator<K> currentIt;
        Cache2lIterator() {
            primaryIt = primaryCache.keysIt();
            slaveIt = slaveCache.keysIt();
            currentIt = primaryIt;
        }

        @Override
        public boolean hasNext() {
            return primaryIt.hasNext() || slaveIt.hasNext();
        }

        @Override
        public K next() {
            if (primaryIt.hasNext()) {
                return primaryIt.next();
            }
            if (slaveIt.hasNext()) {
                currentIt = slaveIt;
                return slaveIt.next();
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            currentIt.remove();
        }
    }
}
