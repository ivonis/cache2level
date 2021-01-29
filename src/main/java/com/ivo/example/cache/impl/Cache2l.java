package com.ivo.example.cache.impl;

import com.ivo.example.cache.Cache;
import com.ivo.example.cache.EldestCollector;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class Cache2l<K, V> implements Cache<K, V>, EldestCollector<K, V>, Closeable {
    private Cache<K, V> primaryCache;
    private Cache<K, V> slaveCache;

    public Cache2l(Cache<K, V> primaryCache, Cache<K, V> slaveCache) {
        this.primaryCache = primaryCache;
        this.slaveCache = slaveCache;
        this.primaryCache.setEldestCollector(this);
        this.slaveCache.setEldestCollector(this);
    }
    public Cache2l() {
        this(new RamCache<>(), new RamCache<>());
    }

    @Override
    public void put(K key, V value) {
        primaryCache.put(key, value);
    }

    @Override
    public V get(K key) {
        V val = primaryCache.get(key);
        if (val == null) {
            val = slaveCache.get(key);
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
    public void clear() {
        primaryCache.clear();
        slaveCache.clear();
    }

    @Override
    public void setEldestCollector(EldestCollector<K, V> collector) {

    }

    @Override
    public void collect(Cache sender, K key, V value) {
        if (primaryCache == sender) {
            slaveCache.put(key, value);
        }
    }

    @Override
    public void close() throws IOException {

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
