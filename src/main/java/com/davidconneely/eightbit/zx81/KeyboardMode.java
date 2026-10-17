package com.davidconneely.eightbit.zx81;

import java.util.Map;

/**
 * How keys typed at the terminal map to keys on the ZX81 keyboard.
 *
 * <p>In all modes: Enter is NEWLINE, Backspace is RUBOUT (Shift+0), Ctrl+E is EDIT (Shift+1),
 * Ctrl+F is FUNCTION (Shift+NEWLINE) and Ctrl+G is GRAPHICS (Shift+9).
 */
enum KeyboardMode {
  /**
   * Characters are typed as the ZX81 character they look like, where it has one. So '*' is Shift+B,
   * '"' is Shift+P and '^' is ** (Shift+H). Letters are typed unshifted whatever their case, as the
   * ZX81 has only capital letters. The arrow keys are the ZX81's arrows.
   */
  CONVENIENCE,

  /**
   * Keys act as the keys in the same place on the ZX81: the 39 keys 0-9, A-Z, Enter, '.' and Space,
   * with or without Shift. Comma is Shift+'.' (the ZX81's comma), and the arrow keys are the ZX81's
   * arrows (Shift+5 to Shift+8). Where the terminal reports only characters, not keys, this assumes
   * a US keyboard layout, so (for example) '*' is Shift+8.
   */
  NATIVE,

  /** As {@link #NATIVE}, but the arrow keys are 5, 6, 7 and 8 without Shift, as many games use. */
  GAMING;

  private static final long SHIFT = KeyboardBuffer.key(0, 0);

  /** The labels of the keys in each row and column; SHIFT is shown as '^', NEWLINE as '\n'. */
  private static final String[] LABELS = {
    "^ZXCV", "ASDFG", "QWERT", "12345", "09876", "POIUY", "\nLKJH", " .MNB"
  };

  /** Characters typed with Shift and a digit on a US keyboard, for digits 0-9. */
  private static final String US_SHIFTED_DIGITS = ")!@#$%^&*(";

  private static final Map<Character, Long> CONVENIENCE_SYMBOLS =
      Map.ofEntries(
          Map.entry('"', shifted('P')),
          Map.entry('£', shifted(' ')),
          Map.entry('\\', shifted(' ')), // for keyboards with no '£'
          Map.entry('$', shifted('U')),
          Map.entry(':', shifted('Z')),
          Map.entry('?', shifted('C')),
          Map.entry('(', shifted('I')),
          Map.entry('[', shifted('I')),
          Map.entry('{', shifted('I')),
          Map.entry(')', shifted('O')),
          Map.entry(']', shifted('O')),
          Map.entry('}', shifted('O')),
          Map.entry('>', shifted('M')),
          Map.entry('<', shifted('N')),
          Map.entry('=', shifted('L')),
          Map.entry('+', shifted('K')),
          Map.entry('-', shifted('J')),
          Map.entry('*', shifted('B')),
          Map.entry('/', shifted('V')),
          Map.entry(';', shifted('X')),
          Map.entry(',', shifted('.')),
          Map.entry('^', shifted('H'))); // ** (raise to the power)

  // Windows virtual-key codes.
  private static final int VK_RETURN = 0x0D;
  private static final int VK_SPACE = 0x20;
  private static final int VK_NUMPAD0 = 0x60;
  private static final int VK_NUMPAD9 = 0x69;
  private static final int VK_OEM_PERIOD = 0xBE;

  /** The next mode, in a cycle of all the modes. */
  KeyboardMode next() {
    final KeyboardMode[] modes = values();
    return modes[(ordinal() + 1) % modes.length];
  }

  /**
   * The ZX81 keys to hold down for a key typed at the terminal.
   *
   * @return bits from {@link KeyboardBuffer#key(int, int)}, or 0 if the key has no ZX81 keys.
   */
  long keys(final HostKey key) {
    return switch (this) {
      case NATIVE -> nativeKeys(key);
      case CONVENIENCE -> convenienceKeys(key.ch());
      case GAMING ->
          switch (key.ch()) {
            case '←' -> key('5');
            case '↓' -> key('6');
            case '↑' -> key('7');
            case '→' -> key('8');
            default -> nativeKeys(key);
          };
    };
  }

  /** The bit for the ZX81 key with the given label. */
  static long key(final char label) {
    for (int row = 0; row < LABELS.length; ++row) {
      final int column = LABELS[row].indexOf(label);
      if (column >= 0) {
        return KeyboardBuffer.key(row, column);
      }
    }
    throw new IllegalArgumentException("No ZX81 key labelled " + label);
  }

  /** The bits for SHIFT and the ZX81 key with the given label. */
  static long shifted(final char label) {
    return SHIFT | key(label);
  }

  private static long nativeKeys(final HostKey key) {
    final long keys = nativeKeyCodeKeys(key.keyCode());
    if (keys != 0L) {
      return key.shift() ? SHIFT | keys : keys;
    }
    final int ch = key.ch();
    if (ch >= 'a' && ch <= 'z') {
      return key(Character.toUpperCase((char) ch));
    } else if ((ch >= 'A' && ch <= 'Z')) {
      return shifted((char) ch);
    } else if (ch >= '0' && ch <= '9' || ch == '.' || ch == ' ') {
      return key((char) ch);
    } else if (ch == '>' || ch == ',') { // '>' is Shift+'.' on a US keyboard
      return shifted('.');
    }
    final int digit = US_SHIFTED_DIGITS.indexOf(ch);
    if (digit >= 0) {
      return shifted((char) ('0' + digit));
    }
    return commonKeys(ch);
  }

  /** The ZX81 key in the same place as a key on a PC keyboard, or 0 if there isn't one. */
  private static long nativeKeyCodeKeys(final int keyCode) {
    if (keyCode >= 'A' && keyCode <= 'Z' || keyCode >= '0' && keyCode <= '9') {
      return key((char) keyCode);
    } else if (keyCode >= VK_NUMPAD0 && keyCode <= VK_NUMPAD9) {
      return key((char) ('0' + keyCode - VK_NUMPAD0));
    }
    return switch (keyCode) {
      case VK_RETURN -> key('\n');
      case VK_SPACE -> key(' ');
      case VK_OEM_PERIOD -> key('.');
      default -> 0L;
    };
  }

  private static long convenienceKeys(final int ch) {
    if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z') {
      return key(Character.toUpperCase((char) ch));
    } else if (ch >= '0' && ch <= '9' || ch == '.' || ch == ' ') {
      return key((char) ch);
    }
    final Long keys = CONVENIENCE_SYMBOLS.get((char) ch);
    return keys != null ? keys : commonKeys(ch);
  }

  private static long commonKeys(final int ch) {
    return switch (ch) {
      case '\r', '\n' -> key('\n');
      case 0x7F, 0x08 -> shifted('0'); // Backspace for RUBOUT
      case '←' -> shifted('5');
      case '↓' -> shifted('6');
      case '↑' -> shifted('7');
      case '→' -> shifted('8');
      case 0x05 -> shifted('1'); // Ctrl+E for EDIT
      case 0x06 -> shifted('\n'); // Ctrl+F for FUNCTION
      case 0x07 -> shifted('9'); // Ctrl+G for GRAPHICS
      default -> 0L;
    };
  }
}
