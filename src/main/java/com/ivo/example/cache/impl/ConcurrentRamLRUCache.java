package com.ivo.example.cache.impl;


import com.ivo.example.cache.Cache;
import com.ivo.example.cache.CacheContext;
import com.ivo.example.cache.CacheImpl;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@CacheImpl(async = true)
public class ConcurrentRamLRUCache<K, V> extends AbstractCache<K, V> {

  private final ConcurrentHashMap<K, Node<K, V>> storage;
  private final AtomicInteger size = new AtomicInteger(0);
  private volatile Node<K, V> head = null;
  private volatile Node<K, V> tail = null;


  @SuppressWarnings("unused")
  public ConcurrentRamLRUCache(CacheContext<K, V> context) {
    super(context);
    storage = new ConcurrentHashMap<>(capacity);
  }

  @Override
  public Cache<K, V> put(K key, V value) {
    Node<K, V> newNode = new Node<>(key, value);
    Node<K, V> oldNode = storage.put(key, newNode);
    if (oldNode != null) {
      newNode.prev = oldNode.prev;
      newNode.next = oldNode.next;
      moveNodeToTail(newNode);
    } else {
      size.incrementAndGet();
      synchronized (this) {
        if (tail == null) {
          head = tail = newNode;
        } else {
          tail.next = newNode;
          newNode.prev = tail;
          newNode.next = null;
          tail = newNode;
        }
      }
      if (size.get() > capacity) {
        evict();
      }
    }
    return this;
  }

  @Override
  public Cache<K, V> putAll(Map<K, ? extends V> map) {
    if (map != null) {
      map.forEach(this::put);
    }
    return this;
  }

  @Override
  public V get(K key) {
    Node<K, V> node = storage.get(key);
    if (node != null) {
      moveNodeToTail(node);
      return node.val;
    }
    return null;
  }

  @Override
  public V remove(K key) {
    Node<K, V> remNode = storage.remove(key);
    if (remNode != null) {
      size.decrementAndGet();
      stitchingGaps(remNode.prev, remNode.next);
      return remNode.val;
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Iterator<K> keysIt() {
    K[] a = (K[]) storage.keySet().toArray();
    return Stream.of(a).iterator();
  }

  @Override
  public Map<K, ? super V> toMap(Map<K, ? super V> map) {
    if (map != null) {
      storage.values().forEach(n -> map.put(n.key, n.val));
    }
    return map;
  }

  @Override
  public Cache<K, V> clear() {
    storage.clear();
    synchronized (this) {
      head = tail = null;
    }
    size.set(0);
    return this;
  }

  @Override
  public int size() {
    return size.get();
  }

  private void evict() {
    while (size.get() > capacity) {
      Node<K, V> removed;
      synchronized (this) {
        removed = storage.remove(head.key);
        head = head.next;
        if (head != null) {
          head.prev = null;
        }
      }
      if (removed != null) {
        size.decrementAndGet();
        if (listener != null) {
          listener.onEvicted(this, removed.key, removed.val);
        }
      }
    }
  }

  //stitching gaps after remove node
  private void stitchingGaps(Node<K, V> left, Node<K, V> right) {
    synchronized (this) {
      if (left == null) {
        head = right;
      } else {
        left.next = right;
      }
      if (right == null) {
        tail = left;
      } else {
        right.prev = left;
      }
    }
  }

  private void moveNodeToTail(Node<K, V> node) {
    if (tail == node) {
      return;
    }
    Node<K, V> p = node.prev, n = node.next;
    synchronized (this) {
      stitchingGaps(p, n);
      if (tail != null) {
        tail.next = node;
        node.prev = tail;
        node.next = null;
        tail = node;
      } else {
        head = tail = node;
      }
    }
  }

  static class Node<K, V> {

    final K key;
    volatile V val;
    volatile Node<K, V> prev;
    volatile Node<K, V> next;

    Node(K key, V val) {
      this.key = key;
      this.val = val;
    }

    public final K getKey() {
      return key;
    }

    public final V getValue() {
      return val;
    }

    public final int hashCode() {
      return key.hashCode() ^ val.hashCode();
    }

    public final String toString() {
      return CacheUtils.mapEntryToString(key, val);
    }

    public final boolean equals(Object o) {
      Object k, v, u;
      Node<?, ?> e;
      return ((o instanceof Map.Entry) &&
          (k = (e = (Node<?, ?>) o).getKey()) != null &&
          (v = e.getValue()) != null &&
          (k == key || k.equals(key)) &&
          (v == (u = val) || v.equals(u)));
    }
  }

}
