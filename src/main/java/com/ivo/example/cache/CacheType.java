package com.ivo.example.cache;

public enum CacheType {
  RamLRU(false, Algorithm.LRU, false),
  FileLRU(true, Algorithm.LRU, false),
  Ram2Q(false, Algorithm.TwoQ, false),
  AsyncRamLRU(false, Algorithm.LRU, true);

  public static CacheType typeFor(boolean inFileSys, Algorithm algorithm) {
    return typeFor(inFileSys, algorithm, false);
  }

  public static CacheType typeFor(boolean inFileSys, Algorithm algorithm, boolean threadSafe) {
    for (CacheType ct : values()) {
      if (ct.inFileSys == inFileSys
          && ct.algorithm == algorithm
          && ct.threadSafe == threadSafe) {
        return ct;
      }
    }
    return null;
  }

  boolean inFileSys;
  Algorithm algorithm;
  boolean threadSafe;

  CacheType(boolean inFileSys, Algorithm algorithm, boolean threadSafe) {
    this.inFileSys = inFileSys;
    this.algorithm = algorithm;
    this.threadSafe = threadSafe;
  }

}
