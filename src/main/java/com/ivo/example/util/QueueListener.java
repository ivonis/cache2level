package com.ivo.example.util;

public interface QueueListener<E> {

  void removeEldest(E removed);

}
