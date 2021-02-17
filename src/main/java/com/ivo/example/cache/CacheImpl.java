package com.ivo.example.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheImpl {

  Algorithm alg() default Algorithm.LRU;

  Store store() default Store.RAM;

  boolean async() default false;
}
