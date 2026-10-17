package com.davidconneely.eightbit.zx81;

/**
 * A key typed at the terminal.
 *
 * @param ch the character typed. Arrow keys are {@code '←'}, {@code '↑'}, {@code '→'} and {@code
 *     '↓'}, Backspace is DEL (0x7F), and Ctrl or Alt with a digit key is {@link #MODIFIED_DIGIT_0}
 *     plus the digit. 0 if the key doesn't type a character.
 * @param keyCode the Windows virtual-key code of the key, if the terminal reports it and neither
 *     Ctrl nor Alt was held, or 0.
 * @param shift whether Shift was held, if {@code keyCode} isn't 0.
 */
record HostKey(int ch, int keyCode, boolean shift) {
  /**
   * {@link #ch} is this plus the digit, for Ctrl or Alt held with a digit key, from terminals that
   * report it. Beyond the range of Unicode code points, so it can't be mistaken for a character.
   */
  static final int MODIFIED_DIGIT_0 = 0x110000;

  /** A key known only by the character it typed. */
  static HostKey of(final int ch) {
    return new HostKey(ch, 0, false);
  }
}
