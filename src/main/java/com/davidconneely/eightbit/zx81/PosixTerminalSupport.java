package com.davidconneely.eightbit.zx81;

import static org.fusesource.jansi.internal.CLibrary.TCSANOW;
import static org.fusesource.jansi.internal.CLibrary.tcgetattr;
import static org.fusesource.jansi.internal.CLibrary.tcsetattr;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import org.fusesource.jansi.internal.CLibrary.Termios;
import org.fusesource.jansi.internal.JansiLoader;

class PosixTerminalSupport extends TerminalSupport {
  private static final int STDIN_FILENO = 0;

  private final Termios originalTermios;
  private final Consumer<Termios> cfmakeraw;

  static {
    JansiLoader.initialize();
  }

  static PosixTerminalSupport newInstance(final Consumer<Termios> cfmakeraw) {
    return new PosixTerminalSupport(cfmakeraw);
  }

  // private to prevent other classes instantiating this.
  private PosixTerminalSupport(final Consumer<Termios> cfmakeraw) {
    this.originalTermios = new Termios(); // Struct to store original terminal attributes
    this.cfmakeraw = cfmakeraw;
    tcgetattr(STDIN_FILENO, originalTermios);
  }

  @Override
  void enableRawMode() {
    final Termios rawTermios = new Termios(); // Struct for raw mode attributes
    tcgetattr(STDIN_FILENO, rawTermios);
    this.cfmakeraw.accept(rawTermios); // Modify for raw mode
    final int errcode = tcsetattr(STDIN_FILENO, TCSANOW, rawTermios);
    if (errcode != 0) {
      throw new IllegalStateException("Failed to set terminal attributes to raw mode");
    }
  }

  @Override
  void reset() {
    final int errcode = tcsetattr(STDIN_FILENO, TCSANOW, originalTermios);
    if (errcode != 0) {
      throw new IllegalStateException("Failed to restore terminal attributes to raw mode");
    }
  }

  /** How long to wait for the rest of an escape sequence or UTF-8 character. */
  private static final long FOLLOWING_BYTE_MILLIS = 10L;

  /**
   * Read the next key. Escape sequences are read to their end, so the keys they don't stand for
   * (such as function keys) are ignored, instead of typing the rest of the sequence. Arrow keys
   * read as arrow characters, and Alt with a digit (ESC and the digit) as {@link
   * HostKey#MODIFIED_DIGIT_0} plus the digit.
   */
  @Override
  HostKey readKey() {
    try {
      return readKey(System.in); // raw mode returns each byte as soon as it is typed
    } catch (final IOException e) {
      return null;
    }
  }

  /** Read the next key from the given input, as {@link #readKey()} does from standard input. */
  static HostKey readKey(final InputStream in) throws IOException {
    final int first = in.read();
    if (first < 0) {
      return null;
    } else if (first == 27) {
      return readEscapeSequence(in);
    } else if (first >= 0x80) {
      return HostKey.of(readUtf8(in, first));
    }
    return HostKey.of(first);
  }

  private static HostKey readEscapeSequence(final InputStream in) throws IOException {
    final int next = readFollowingByte(in);
    if (next < 0) {
      return HostKey.of(27); // the Esc key by itself
    } else if (next >= '0' && next <= '9') {
      return HostKey.of(HostKey.MODIFIED_DIGIT_0 + next - '0');
    } else if (next != '[' && next != 'O') {
      return HostKey.of(0); // Alt with another key
    }
    int last; // CSI and SS3 sequences end with a byte from 0x40 to 0x7E
    do {
      last = readFollowingByte(in);
    } while (last >= 0 && (last < 0x40 || last > 0x7E));
    return HostKey.of(
        switch (last) {
          case 'A' -> '↑';
          case 'B' -> '↓';
          case 'C' -> '→';
          case 'D' -> '←';
          default -> 0;
        });
  }

  /** Read the rest of a UTF-8 character, and return its code point (or 0 if it isn't valid). */
  private static int readUtf8(final InputStream in, final int first) throws IOException {
    final int length;
    int codePoint;
    if ((first & 0xE0) == 0xC0) {
      length = 1;
      codePoint = first & 0x1F;
    } else if ((first & 0xF0) == 0xE0) {
      length = 2;
      codePoint = first & 0x0F;
    } else if ((first & 0xF8) == 0xF0) {
      length = 3;
      codePoint = first & 0x07;
    } else {
      return 0;
    }
    for (int i = 0; i < length; ++i) {
      final int next = readFollowingByte(in);
      if (next < 0 || (next & 0xC0) != 0x80) {
        return 0;
      }
      codePoint = (codePoint << 6) | (next & 0x3F);
    }
    return codePoint;
  }

  /** Read a byte that is part of the same key, or return -1 if none arrives soon. */
  private static int readFollowingByte(final InputStream in) throws IOException {
    final long deadline = System.nanoTime() + FOLLOWING_BYTE_MILLIS * 1_000_000L;
    while (in.available() == 0) {
      if (System.nanoTime() - deadline > 0L) {
        return -1;
      }
      try {
        Thread.sleep(1L);
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        return -1;
      }
    }
    return in.read();
  }

  @Override
  void write(final int data) {
    System.out.write(data);
  }

  @Override
  void print(final String text) {
    System.out.print(text);
  }
}
