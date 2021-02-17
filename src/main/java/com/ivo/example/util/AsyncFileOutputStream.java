package com.ivo.example.util;

import static com.ivo.example.util.AsyncFileLocker.failClose;
import static com.ivo.example.util.AsyncFileLocker.isBlocking;
import static com.ivo.example.util.AsyncFileLocker.sleepOneTick;
import static com.ivo.example.util.AsyncFileLocker.waitFuture;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class AsyncFileOutputStream extends OutputStream {

  private static final Logger LOG = Logger.getLogger(AsyncFileInputStream.class.getName());
  private final AsynchronousFileChannel channel;
  private final ByteBuffer buffer;
  private final int autoFlushLimit;
  private final AsyncFileLocker locker;
  private long channelPos = 0L;

  public AsyncFileOutputStream(Path path, int bufSize, boolean lock) throws IOException {
    if (bufSize < 0) {
      throw new IllegalArgumentException("Negative initial size: " + bufSize);
    }
    channel = AsynchronousFileChannel.open(path, WRITE);
    locker = new AsyncFileLocker(channel, lock, false);
    buffer = ByteBuffer.allocate(bufSize);
    autoFlushLimit = bufSize / 2;
  }

  private boolean checkBuffer(int size) {
    if (buffer.remaining() < size) {
      try {
        flush();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return buffer.remaining() >= size;
    }
    return true;
  }

  @Override
  public void write(int b) throws IOException {
    locker.checkLock(this);
    if (checkBuffer(1)) {
      buffer.put((byte) b);
      if (buffer.remaining() < autoFlushLimit) {
        flush();
      }
    } else {
      throw new IOException("buffer is full");
    }
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    Objects.checkFromIndexSize(off, len, b.length);
    locker.checkLock(this);
    if (checkBuffer(len)) {
      buffer.put(b, off, len);
      if (buffer.remaining() < autoFlushLimit) {
        flush();
      }
    } else {
      int r = buffer.remaining(), n = len, off0 = off;
      while (n != 0) {
        buffer.put(b, off0, r);
        flush();
        off0 += r;
        n -= r;
        r = buffer.remaining();
      }
    }
  }

  @Override
  public void flush() throws IOException {
    if (buffer.position() == 0) {
      return;
    }
    buffer.flip();
    Optional<Integer> amount;
    do {
      amount = waitFuture(
          channel.write(buffer, channelPos), t -> {
            if (isBlocking(t)) {
              LOG.info("Writing to file is blocked");
              sleepOneTick();
            } else {
              failClose(this, t);
            }
          });
    } while (amount.isEmpty());
    channelPos += amount.get();
    buffer.flip();
  }

  @Override
  public void close() throws IOException {
    try {
      flush();
    } finally {
      channel.close();
    }
    super.close();
  }

  public String toString() {
    return new String(buffer.array(), 0, buffer.limit());
  }

}
