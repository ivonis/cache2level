package com.ivo.example.cache.impl;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

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
}
