package com.davidconneely.eightbit.zx81;

import java.util.function.IntConsumer;
import java.util.function.LongSupplier;

/**
 * The ZX81 keyboard, typed at a terminal. Keys typed are mapped to ZX81 keys by the current {@link
 * KeyboardMode}, except for keys that control the emulator: Ctrl+C closes it, Ctrl+K changes to the
 * next keyboard mode, Ctrl or Alt with a digit sets the speed, and Tab makes the next key shifted.
 */
class TerminalKeyboard {
  private static final int CTRL_C = 0x03;
  private static final int CTRL_K = 0x0B;
  private static final int TAB = 0x09;
  private static final long SHIFT = KeyboardBuffer.key(0, 0);

  /** Makes a keyboard; the parameters are the same as the constructor's. */
  @FunctionalInterface
  interface Factory {
    TerminalKeyboard create(
        TerminalSupport terminal, LongSupplier emulatedNanos, IntConsumer speedKey);
  }

  private final TerminalSupport terminal;
  private final LongSupplier emulatedNanos;
  private final IntConsumer speedKey;
  private final KeyboardBuffer buffer = new KeyboardBuffer();
  private volatile boolean shutdownRequested;
  private volatile KeyboardMode mode = KeyboardMode.CONVENIENCE;
  private volatile boolean stickyShift;

  /**
   * @param terminal where key presses are read from.
   * @param emulatedNanos supplies the machine's emulated time.
   * @param speedKey accepts the digit when Ctrl or Alt is held with a digit key.
   */
  TerminalKeyboard(
      final TerminalSupport terminal,
      final LongSupplier emulatedNanos,
      final IntConsumer speedKey) {
    this.terminal = terminal;
    this.emulatedNanos = emulatedNanos;
    this.speedKey = speedKey;
  }

  /**
   * Start reading the terminal on a separate thread, so keys are queued even while the emulator is
   * busy.
   */
  void start() {
    Thread.ofPlatform().name("terminal-keyboard").daemon().start(this::readTerminal);
  }

  /** Whether Ctrl+C has been pressed, or the terminal can no longer be read. */
  boolean isShutdownRequested() {
    return shutdownRequested;
  }

  /** The current keyboard mode. */
  KeyboardMode mode() {
    return mode;
  }

  /** Set the keyboard mode. Can be called from any thread. */
  void mode(final KeyboardMode mode) {
    this.mode = mode;
    stickyShift = false;
  }

  /** Whether Tab has been pressed, so the next key is typed with SHIFT. */
  boolean isStickyShift() {
    return stickyShift;
  }

  private void readTerminal() {
    try {
      HostKey key;
      while ((key = terminal.readKey()) != null && key.ch() != CTRL_C) {
        type(key);
      }
    } finally {
      shutdownRequested = true;
    }
  }

  /**
   * Queue the ZX81 keys for a typed character, as if typed at a terminal that reports only
   * characters.
   *
   * @param ch the character typed.
   */
  void type(final char ch) {
    type(HostKey.of(ch));
  }

  /**
   * Queue the ZX81 keys for a key typed at the terminal, if it has any, or act on the keys that
   * control the emulator.
   *
   * @param key the key typed.
   */
  void type(final HostKey key) {
    final int ch = key.ch();
    if (ch >= HostKey.MODIFIED_DIGIT_0 && ch <= HostKey.MODIFIED_DIGIT_0 + 9) {
      speedKey.accept(ch - HostKey.MODIFIED_DIGIT_0);
    } else if (ch == CTRL_K) {
      mode(mode.next());
    } else if (ch == TAB) {
      stickyShift = !stickyShift;
    } else {
      final KeyboardMode mode = this.mode;
      long keys = mode.keys(key);
      if (keys != 0L) {
        if (stickyShift) {
          keys |= SHIFT;
          stickyShift = false;
        }
        if (mode == KeyboardMode.GAMING) {
          buffer.typeLatest(keys);
        } else {
          buffer.type(keys);
        }
      }
    }
  }

  /** Read a keyboard port. */
  int readKeyPortByte(final int portNum) {
    return buffer.readPort(portNum, emulatedNanos.getAsLong());
  }

  /** Whether the program has finished with the typed keys, and asked for another. */
  boolean isReadyForKeys() {
    return buffer.isReadyForKeys();
  }

  /** Note that the program is looking at the keyboard, at the next keyboard read. */
  void ask() {
    buffer.ask();
  }

  /** Note that the program waits for a key (in PAUSE). */
  void pause() {
    buffer.pause();
  }

  /** Note that the ROM has finished with the key held down, and waits for the next key. */
  void waitForKey() {
    buffer.waitForKey();
  }

  /** Note that the ROM is checking for BREAK. */
  void breakCheck() {
    buffer.breakCheck();
  }
}
