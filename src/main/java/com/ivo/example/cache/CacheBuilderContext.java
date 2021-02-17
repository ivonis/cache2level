package com.ivo.example.cache;

public interface CacheBuilderContext<K, V> {

  Algorithm getAlg();

  void setAlg(Algorithm alg);

  int getCapacity();

  void setCapacity(int capacity);

  String getPath();

  void setPath(String path);

  Store getStore();

  void setStore(Store store);

  boolean isAsync();

  void setAsync(boolean async);

  CacheContext<K, V> getCacheContext();

  default boolean matchClass(Class<? extends Cache<?, ?>> cc) {
    CacheImpl a = cc.getAnnotation(CacheImpl.class);
    return a != null
        && getAlg() == a.alg()
        && getStore() == a.store()
        && isAsync() == a.async();
  }
}
