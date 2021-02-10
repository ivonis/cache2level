package com.ivo.example.cache;

public enum CacheType {
    RamLRU(false, Algorithm.LRU),
    FileLRU(true, Algorithm.LRU),
    Ram2Q(false, Algorithm.TwoQ);

    public static CacheType typeFor(boolean inFileSys, Algorithm algorithm) {
        for (CacheType ct : values()) {
            if (ct.inFileSys == inFileSys && ct.algorithm == algorithm) {
                return ct;
            }
        }
        return null;
    }

    boolean inFileSys;
    Algorithm algorithm;

    CacheType(boolean inFileSys, Algorithm algorithm) {
        this.inFileSys = inFileSys;
        this.algorithm = algorithm;
    }

}
