package com.ivo.example.util;

public class LRUHashQueue<E> extends HashQueue<E> {

  public LRUHashQueue(int capacity) {
    super(capacity);
  }

  @Override
  protected boolean nodeAlreadyExists(Node<E> ne) {
    nodeToTail(ne);
    return true;
  }

  private void nodeToTail(Node<E> node) {
    if (node == tail) {
      return;
    }
    Node<E> p = node.prev, n = node.next;
    // node is head
    if (p != null) {
      p.next = n;
    } else {
      head = n;
    }
    if (n != null) {
      n.prev = p;
    }
    node.prev = tail;
    node.next = null;
    tail.next = node;
    tail = node;
  }

}
