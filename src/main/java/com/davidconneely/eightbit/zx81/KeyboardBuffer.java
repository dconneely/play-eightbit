package com.davidconneely.eightbit.zx81;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Turns typed keys into the state of the ZX81 keyboard matrix, as read by the CPU.
 *
 * <p>A terminal reports that a key was typed, but not how long it was held down. So each typed key
 * is held down for {@link #HOLD_NANOS} of emulated time, then released. The ROM and programs read
 * the matrix as they would a real keyboard.
 *
 * <p>Only one typed key is held down at a time, as the ROM reads two keys held down together as no
 * key. Keys typed faster than that are queued, and each waits until no key has been held down for
 * {@link #RELEASE_NANOS}, as the ROM needs to see no key between keys. That time starts with the
 * first keyboard read after the release, as the ROM doesn't watch the keyboard while it is busy in
 * FAST mode.
 *
 * <p>A queued key also waits until the program asks for a key, so keys typed while the program is
 * busy aren't lost. A real ZX81 would lose them, but a person typing at a ZX81 can see when it is
 * ready. A program that looks at the keyboard now and then asks for one keyboard read ({@link
 * #ask}), so the key is down when it looks, even if it looks only a few times a second. A program
 * that waits for a key ({@link #pause}, {@link #waitForKey}) asks until a key is pressed.
 *
 * <p>Keys typed by {@link #typeLatest} replace any queued keys, and typing the key held down again
 * keeps it held down for longer. So a key that repeats while held down at the terminal stays held
 * down.
 *
 * <p>{@link #type} and {@link #typeLatest} can be called from any thread; {@link #readPort} must be
 * called from the thread running the emulator.
 */
final class KeyboardBuffer {
  static final int ROWS = 8;
  static final int COLUMNS = 5;

  static final long HOLD_NANOS = 150_000_000L;
  static final long RELEASE_NANOS = 100_000_000L;
  private static final int MAX_QUEUED_KEYS = 64;
  private static final long SPACE = key(7, 0);

  private record TypedKeys(long keys, boolean latest) {}

  private final BlockingQueue<TypedKeys> queue = new ArrayBlockingQueue<>(MAX_QUEUED_KEYS);
  private TypedKeys held; // keys held down, or null while released
  private boolean timing; // whether the time of this hold or release has started
  private long since; // emulated time that this hold or release started
  private boolean asked; // whether the program asks for a key at the next keyboard read
  private boolean waiting; // whether the program waits for a key, until one is pressed

  /**
   * The bit for one key in the matrix.
   *
   * @param row 0 to 7, the row selected by a 0 in that bit of the high byte of the port number (row
   *     0 is SHIFT, Z, X, C, V and row 7 is SPACE, ., M, N, B).
   * @param column 0 to 4, the bit of the port data that is 0 when the key is down.
   */
  static long key(final int row, final int column) {
    return 1L << (row * COLUMNS + column);
  }

  /**
   * Queue keys to be held down together (for example, SHIFT and another key). Keys are dropped if
   * the queue is full.
   *
   * @param keys bits from {@link #key(int, int)}.
   */
  void type(final long keys) {
    if (keys != 0L) {
      queue.offer(new TypedKeys(keys, false));
    }
  }

  /**
   * Queue keys as {@link #type} does, but drop any keys still queued first, so the latest key wins.
   * If the keys are held down already, they stay held down for longer instead.
   */
  void typeLatest(final long keys) {
    if (keys != 0L) {
      queue.clear();
      queue.offer(new TypedKeys(keys, true));
    }
  }

  /**
   * Whether every typed key has been pressed and released, and the program has asked for another
   * key since, so it has finished with them.
   */
  boolean isReadyForKeys() {
    return held == null && queue.isEmpty() && (asked || waiting);
  }

  /**
   * Note that the program is looking at the keyboard, so the next queued key can be pressed for the
   * next keyboard read (if the release time has passed).
   */
  void ask() {
    asked = true;
  }

  /** Note that the program waits for a key, so the next queued key is pressed when it can be. */
  void pause() {
    waiting = true;
  }

  /**
   * Note that the ROM waits for the next key, so it has finished with any key held down, which is
   * released, and waits for a key.
   */
  void waitForKey() {
    if (held != null) {
      held = null;
      timing = false;
    }
    waiting = true;
  }

  /** Note that the ROM is checking for BREAK, which asks for a key only if SPACE is next. */
  void breakCheck() {
    final TypedKeys next = queue.peek();
    if (next != null && next.keys() == SPACE) {
      asked = true;
    }
  }

  /**
   * Read a keyboard port. Bits 4-0 of the result are 0 for keys held down in the selected rows.
   *
   * @param portNum 16-bit port number; the keyboard is any even port.
   * @param emulatedNanos emulated time.
   */
  int readPort(final int portNum, final long emulatedNanos) {
    if ((portNum & 0x01) != 0) {
      return 0xFF;
    }
    if (!timing) {
      timing = true; // a release starts with the first keyboard read, so the program sees it
      since = emulatedNanos;
    }
    final TypedKeys next = queue.peek();
    if (held != null && next != null && next.latest() && next.keys() == held.keys()) {
      queue.poll();
      since = emulatedNanos; // typed again, so held down for longer
    }
    if (held != null && emulatedNanos - since >= HOLD_NANOS) {
      held = null;
      timing = false;
    }
    if (held == null && timing && (asked || waiting) && emulatedNanos - since >= RELEASE_NANOS) {
      held = queue.poll();
      if (held != null) {
        since = emulatedNanos;
        waiting = false;
      }
    }
    asked = false; // the program has had this read
    return portData(held == null ? 0L : held.keys(), ~(portNum >>> 8) & 0xFF);
  }

  private static int portData(final long pressed, final int rows) {
    int down = 0;
    for (int row = 0; row < ROWS; ++row) {
      if ((rows & (1 << row)) != 0) {
        down |= (int) (pressed >>> (row * COLUMNS)) & 0x1F;
      }
    }
    return ~down & 0xFF;
  }
}
