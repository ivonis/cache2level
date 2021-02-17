package com.ivo.example.cache;

import com.ivo.example.cache.impl.CacheBuilderContextImpl;
import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.reflections.Reflections;

public class CacheBuilder<K, V> {

  static final Logger LOG = Logger.getLogger(CacheBuilder.class.getName());
  private static final String DEF_LOAD_PACKAGE = "com.ivo.example.cache.impl";
  private static final Set<Class<?>> cacheImplClasses = ConcurrentHashMap.newKeySet();

  static {
    loadCacheClasses(DEF_LOAD_PACKAGE);
  }

  private final CacheBuilderContext<K, V> context;

  public CacheBuilder() {
    context = new CacheBuilderContextImpl<>();
  }

  public CacheBuilder(CacheBuilderContext<K, V> context) {
    this.context = context;
  }

  public static void loadCacheClasses(String packageName) {
    Reflections reflections = new Reflections(packageName);
    Set<Class<?>> types = reflections.getTypesAnnotatedWith(CacheImpl.class);
    LOG.info("In package " + packageName + " has found " + types.size() + " cache classes");
    cacheImplClasses.addAll(types);
  }

  public static boolean loadCacheClass(Class<? extends Cache<?, ?>> cacheClass) {
    if (cacheClass != null && cacheClass.isAnnotationPresent(CacheImpl.class)) {
      cacheImplClasses.add(cacheClass);
      return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public static boolean loadCacheClass(String className) throws ClassNotFoundException {
    Class<? extends Cache<?, ?>> cc = (Class<? extends Cache<?, ?>>) Class.forName(className);
    return loadCacheClass(cc);
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends Cache<?, ?>> getCacheClass(CacheBuilderContext<?, ?> context) {
    for (Class<?> cacheClass : cacheImplClasses) {
      Class<? extends Cache<?, ?>> cc = (Class<? extends Cache<?, ?>>) cacheClass;
      if (context.matchClass(cc)) {
        return cc;
      }
    }
    return null;
  }

  public CacheBuilder<K, V> begin(Algorithm alg) {
    context.setAlg(alg);
    return this;
  }

  public CacheBuilder<K, V> store(Store store) {
    context.setStore(store);
    return this;
  }

  public CacheBuilder<K, V> async(boolean async) {
    context.setAsync(async);
    return this;
  }

  public CacheBuilder<K, V> capacity(int capacity) {
    context.setCapacity(capacity);
    return this;
  }


  public CacheBuilder<K, V> path(String path) {
    context.setPath(path);
    return this;
  }

  @SuppressWarnings("unchecked")
  public Cache<K, V> build() {
    Class<? extends Cache<?, ?>> cc = getCacheClass(context);
    try {
      if (cc == null) {
        throw new ClassNotFoundException("The matching cache class was not found");
      }
      Class<? extends Cache<K, V>> cc1 = (Class<? extends Cache<K, V>>) cc;
      Constructor<? extends Cache<K, V>> constructor = cc1.getConstructor(CacheContext.class);
      return constructor.newInstance(context.getCacheContext());
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Cache building is failed", e);
    }
  }

  public Cache<K, V> build(String key) {
    Cache<K, V> cache = build();
    if (key != null) {
      CachePool.put(key, cache);
    }
    return cache;
  }
}
