package com.ivo.example.cache.impl;

public class CacheUtils {

  private CacheUtils() {
  }

  public static <T extends Comparable<T>> T max(T v1, T v2) {
    if (v1 != null) {
      return v1.compareTo(v2) >= 0 ? v1 : v2;
    }
    return v2;
  }

  public static <T extends Comparable<T>> T max(T v1, T v2, T v3) {
    return max(max(v1, v2), v3);
  }

  public static <T extends Comparable<T>> T min(T v1, T v2) {
    if (v1 != null) {
      return v1.compareTo(v2) <= 0 ? v1 : v2;
    }
    return v2;
  }

  public static <T extends Comparable<T>> T min(T v1, T v2, T v3) {
    return min(min(v1, v2), v3);
  }

  /**
   * Optimized form of: key + "=" + val
   */
  public static String mapEntryToString(Object key, Object val) {
    final String k, v;
    final int klen, vlen;
    final char[] chars =
        new char[(klen = (k = objectToString(key)).length()) +
            (vlen = (v = objectToString(val)).length()) + 1];
    k.getChars(0, klen, chars, 0);
    chars[klen] = '=';
    v.getChars(0, vlen, chars, klen + 1);
    return new String(chars);
  }

  private static String objectToString(Object x) {
    // Extreme compatibility with StringBuilder.append(null)
    String s;
    return (x == null || (s = x.toString()) == null) ? "null" : s;
  }
}
