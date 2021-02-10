package com.ivo.example.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LRUHashQueueTest extends Assertions {

  private final LRUHashQueue<String> queue = new LRUHashQueue<>(5);

  @BeforeEach
  void setUp() {
    queue.clear();
  }

  @Test
  void offerAndPeekAndPollAndContainsAndRemoveTest() {
    String s1 = "s1", s2 = "s2", s3 = "s3", s4 = "s4", s5 = "s5", s6 = "s6";
    assertTrue(queue.offer(s1));
    assertEquals(1, queue.size());
    assertEquals(s1, queue.peek());
    assertEquals(1, queue.size());
    assertEquals(s1, queue.poll());
    assertTrue(queue.isEmpty());
    assertTrue(queue.add(s2));
    assertTrue(queue.contains(s2));
    assertTrue(queue.add(s1));
    assertTrue(queue.add(s2));
    assertTrue(queue.add(s3));
    assertEquals(s1, queue.peek());
    assertTrue(queue.add(s1));
    assertEquals(s2, queue.peek());
    assertEquals(s2, queue.remove());
    assertFalse(queue.contains(s2));
    assertTrue(queue.add(s4));
    assertEquals(s3, queue.peek());
    assertTrue(queue.add(s5));
    assertTrue(queue.add(s6));
    assertTrue(queue.remove(s4));
  }

  @Test
  void iteratorAndEldestListenerTest() {
    queue.setListener(removed -> fail("Removing from an unfilled queue"));
    Set<String> set1 = Set.of("s1", "s2", "s3", "s4", "s5");
    queue.addAll(set1);
    Iterator<String> it = queue.iterator();
    Set<String> set2 = new HashSet<>();
    it.forEachRemaining(set2::add);
    assertTrue(set1.containsAll(set2));
    assertTrue(set2.containsAll(set1));
    final String s = queue.peek();
    queue.setListener(removed -> assertEquals(s, removed));
    queue.add("s6");
    final String s1 = queue.peek();
    queue.setListener(removed -> assertEquals(s1, removed));
    queue.add("s7");
    final String s2 = queue.peek();
    queue.setListener(removed -> assertEquals(s2, removed));
    queue.add("s8");
  }

  @AfterEach
  void tearDown() {
    queue.setListener(null);
  }
}