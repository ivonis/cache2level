package com.ivo.example.util;

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import org.jetbrains.annotations.NotNull;

public class HashQueue<E> extends AbstractQueue<E> implements Queue<E> {

  static final float DEFAULT_LOAD_FACTOR = 0.75f;
  protected final int capacity;
  protected final Map<E, Node<E>> storage;
  protected Node<E> head;
  protected Node<E> tail;
  private QueueListener<E> listener;

  public HashQueue(int capacity) {
    this(capacity, DEFAULT_LOAD_FACTOR);
  }

  public HashQueue(int capacity, float loadFactor) {
    this.capacity = capacity;
    storage = new HashMap<>(capacity, loadFactor);
    head = tail = null;
  }

  public void setListener(QueueListener<E> listener) {
    this.listener = listener;
  }

  @SuppressWarnings("SuspiciousMethodCalls")
  @Override
  public boolean contains(Object e) {
    return storage.containsKey(e);
  }

  @Override
  public boolean remove(Object e) {
    Node<E> removed = storage.remove(e);
    if (removed != null) {
      unlinkNode(removed);
      return true;
    }
    return false;
  }

  @Override
  public boolean offer(E e) {
    Node<E> ne;
    if (storage.isEmpty()) {
      ne = new Node<>(e);
      head = tail = ne;
      storage.put(e, ne);
      return true;
    }
    if ((ne = storage.get(e)) != null) {
      return nodeAlreadyExists(ne);
    }
    while (storage.size() >= capacity) {
      removeHead(true);
    }
    ne = new Node<>(e, tail, null);
    storage.put(e, ne);
    tail.next = ne;
    tail = ne;
    return true;
  }

  protected boolean nodeAlreadyExists(Node<E> ne) {
    return false;
  }

  @Override
  public E poll() {
    return removeHead(false);
  }

  @Override
  public E peek() {
    return head != null ? head.val : null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public @NotNull Iterator<E> iterator() {
    E[] array = (E[]) storage.keySet().toArray();
    return Arrays.stream(array).iterator();
  }

  @Override
  public int size() {
    return storage.size();
  }

  protected E removeHead(boolean isEldest) {
    Node<E> removed = storage.remove(head.val);
    if (removed != null) {
      unlinkHead();
      if (isEldest && listener != null) {
        listener.removeEldest(removed.val);
      }
      return removed.val;
    }
    return null;
  }

  protected void unlinkHead() {
    head = head.next;
    if (head != null) {
      head.prev = null;
    } else {
      tail = null;
    }
  }

  protected void unlinkNode(Node<E> node) {
    if (node == head) {
      unlinkHead();
    } else {
      Node<E> p = node.prev, n = node.next;
      p.next = n;
      if (n != null) {
        n.prev = p;
      } else {
        tail = p;
      }
    }
  }

  @Override
  public void clear() {
    storage.clear();
    while (head != null) {
      unlinkHead();
    }
  }

  static class Node<E> {

    E val;
    Node<E> prev;
    Node<E> next;

    Node(E val) {
      this(val, null, null);
    }

    Node(E val, Node<E> prev, Node<E> next) {
      this.val = val;
      this.prev = prev;
      this.next = next;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Node<?> node = (Node<?>) o;
      return Objects.equals(val, node.val);
    }

    @Override
    public int hashCode() {
      return Objects.hash(val);
    }
  }
}
