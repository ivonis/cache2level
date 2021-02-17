package com.ivo.example.util;

import static com.ivo.example.util.AsyncFileLocker.failClose;
import static com.ivo.example.util.AsyncFileLocker.isBlocking;
import static com.ivo.example.util.AsyncFileLocker.sleepOneTick;
import static com.ivo.example.util.AsyncFileLocker.waitFuture;
import static java.nio.file.StandardOpenOption.READ;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class AsyncFileInputStream extends InputStream {
  private static final Logger LOG = Logger.getLogger(AsyncFileInputStream.class.getName());

  public static final int MAX_BUFFER_SIZE = 8192;

  private final AsynchronousFileChannel channel;
  private final ByteBuffer buffer;
  private final AsyncFileLocker locker;
  private long channelPos = 0L;
  private boolean eof = false;

  public AsyncFileInputStream(Path path, boolean lock) throws IOException {
    channel = AsynchronousFileChannel.open(path, READ);
    locker = new AsyncFileLocker(channel, lock, true);
    buffer = ByteBuffer.allocate(Math.min(MAX_BUFFER_SIZE, (int) channel.size()));
    fillBuffer();
  }

  private void fillBuffer() {
    if (eof) {
      return;
    }
    if (buffer.position() > 0 && buffer.hasRemaining()) {
      buffer.compact();
    } else {
      buffer.clear();
    }
    Optional<Integer> amount;
    do {
      amount = waitFuture(
          channel.read(buffer, channelPos), t -> {
            if (isBlocking(t)) {
              LOG.info("Reading from file is blocked");
              sleepOneTick();
            } else {
              failClose(this, t);
            }
          });
    } while (amount.isEmpty());

    int n = amount.get();
    if (n == -1) {
      eof = true;
    } else {
      channelPos += n;
      buffer.flip();
    }
  }

  @Override
  public int read() {
    locker.checkLock(this);
    if (buffer.hasRemaining()) {
      return buffer.get() & 0xff;
    }
    fillBuffer();
    return eof ? -1 : buffer.get() & 0xff;
  }

  @Override
  public int read(byte[] b, int off, int len) {
    Objects.checkFromIndexSize(off, len, b.length);
    locker.checkLock(this);
    if (!buffer.hasRemaining()) {
      if (eof) {
        return -1;
      }
      fillBuffer();
    }
    int rem = buffer.remaining();
    if (rem >= len) {
      buffer.get(b, off, len);
    } else {
      int r = rem, n = len, off0 = off;
      while (n != 0) {
        buffer.get(b, off0, r);
        off0 += r;
        n -= r;
        fillBuffer();
        if (eof) {
          return len - n;
        }
        rem = buffer.remaining();
        r = Math.min(n, rem);
      }
    }
    return len;
  }

  @Override
  public long skip(long n) throws IOException {
    long k = buffer.remaining();
    int pos = buffer.position();
    if (n < k) {
      k = n < 0 ? 0 : n;
      buffer.position(pos + (int) k);
      return k;
    }
    k = channel.size() - channelPos - pos;
    if (n < k) {
      k = n < 0 ? 0 : n;
      channelPos += pos + k;
      fillBuffer();
      buffer.position(pos + (int) k);
    }
    return k;
  }

  @Override
  public int available() {
    return buffer.remaining();
  }

  @Override
  public boolean markSupported() {
    return true;
  }

  @Override
  public void mark(int readAheadLimit) {
    buffer.mark();
  }

  @Override
  public synchronized void reset() {
    buffer.reset();
  }

  /**
   * Closing a {@code ByteArrayInputStream} has no effect. The methods in this class can be called
   * after the stream has been closed without generating an {@code IOException}.
   */
  @Override
  public void close() throws IOException {
    channel.close();
  }


}
