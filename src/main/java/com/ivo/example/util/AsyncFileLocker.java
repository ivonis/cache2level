package com.ivo.example.util;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class AsyncFileLocker {

  private final AsynchronousFileChannel channel;
  private Future<FileLock> lockFuture;
  private final boolean shared;

  protected AsyncFileLocker(AsynchronousFileChannel channel, boolean lock, boolean shared) {
    this.channel = channel;
    this.shared = shared;
    if (lock) {
      setLock(shared);
    }
  }

  public static void sleepOneTick() {
    try {
      Thread.sleep(1L);
    } catch (InterruptedException e) {
      //nothing
    }
  }

  public static void failClose(Closeable closeable, Throwable t) {
    try {
      closeable.close();
    } catch (IOException e) {
      //nothing
    }
    throw new RuntimeException("Stream is closed due to an error", t);
  }

  public static <T> Optional<T> waitFuture(Future<T> future, Consumer<? super Throwable> errorConsumer) {
    while (!future.isDone()) {
      sleepOneTick();
    }
    try {
      return Optional.ofNullable(future.get());
    } catch (InterruptedException | ExecutionException e) {
      if (errorConsumer != null) {
        errorConsumer.accept(e);
      }
    }
    return Optional.empty();
  }

  public static boolean isBlocking(Throwable t) {
    return t instanceof ExecutionException && t.getCause() instanceof IOException;
  }

  public void setLock(boolean shared) {
    lockFuture = null;
    while (lockFuture == null) {
      try {
        lockFuture = channel.lock(0L, Long.MAX_VALUE, shared);
      } catch (OverlappingFileLockException e) {
        sleepOneTick();
      }
    }
  }

  public void checkLock(final Closeable closeable) {
    if (lockFuture != null) {
      waitFuture(lockFuture, t -> {
        if (isBlocking(t)) {
          setLock(shared);
        } else {
          failClose(closeable, t);
        }
      }).ifPresent(fileLock -> lockFuture = null);
    }
  }

}
