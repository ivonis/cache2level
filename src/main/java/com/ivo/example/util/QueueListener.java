package com.ivo.example.util;

public interface QueueListener<E> {

  void removeEvicted(E removed);

}
