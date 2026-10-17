package com.davidconneely.eightbit.zx81;

import static com.davidconneely.eightbit.zx81.KeyboardMode.CONVENIENCE;
import static com.davidconneely.eightbit.zx81.KeyboardMode.GAMING;
import static com.davidconneely.eightbit.zx81.KeyboardMode.NATIVE;
import static com.davidconneely.eightbit.zx81.KeyboardMode.key;
import static com.davidconneely.eightbit.zx81.KeyboardMode.shifted;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class KeyboardModeTest {
  private static final int VK_RETURN = 0x0D;
  private static final int VK_LEFT = 0x25;
  private static final int VK_NUMPAD5 = 0x65;
  private static final int VK_OEM_PERIOD = 0xBE;

  private static long typed(final KeyboardMode mode, final int ch) {
    return mode.keys(HostKey.of(ch));
  }

  @Test
  void keysAreInTheirMatrixPlaces() {
    assertEquals(KeyboardBuffer.key(0, 0) | KeyboardBuffer.key(0, 1), shifted('Z'));
    assertEquals(KeyboardBuffer.key(3, 0), key('1'));
    assertEquals(KeyboardBuffer.key(4, 0), key('0'));
    assertEquals(KeyboardBuffer.key(4, 4), key('6'));
    assertEquals(KeyboardBuffer.key(6, 0), key('\n'));
    assertEquals(KeyboardBuffer.key(7, 0), key(' '));
    assertEquals(KeyboardBuffer.key(7, 4), key('B'));
  }

  @Test
  void nativeCharactersAreKeysOnUsKeyboard() {
    assertEquals(key('A'), typed(NATIVE, 'a'));
    assertEquals(shifted('A'), typed(NATIVE, 'A'));
    assertEquals(key('7'), typed(NATIVE, '7'));
    assertEquals(shifted('8'), typed(NATIVE, '*'), "Shift+8 is the ZX81's right arrow");
    assertEquals(shifted('2'), typed(NATIVE, '@'), "Shift+2 is AND");
    assertEquals(shifted('.'), typed(NATIVE, ','));
    assertEquals(key('\n'), typed(NATIVE, '\r'));
    assertEquals(shifted('0'), typed(NATIVE, 0x7F), "Backspace is RUBOUT");
    assertEquals(shifted('5'), typed(NATIVE, '←'));
    assertEquals(0L, typed(NATIVE, '"'));
  }

  @Test
  void nativeKeyCodesAreKeysInTheSamePlace() {
    // Shift+2 types '"' on a UK keyboard, but is still AND (ZX81 Shift+2).
    assertEquals(shifted('2'), NATIVE.keys(new HostKey('"', '2', true)));
    assertEquals(key('Q'), NATIVE.keys(new HostKey('q', 'Q', false)));
    assertEquals(shifted('.'), NATIVE.keys(new HostKey('>', VK_OEM_PERIOD, true)));
    assertEquals(shifted('\n'), NATIVE.keys(new HostKey('\r', VK_RETURN, true)), "FUNCTION");
    assertEquals(key('5'), NATIVE.keys(new HostKey('5', VK_NUMPAD5, false)));
    assertEquals(shifted('5'), NATIVE.keys(new HostKey('←', VK_LEFT, false)));
  }

  @Test
  void convenienceCharactersAreTheZx81CharactersTheyLookLike() {
    assertEquals(key('P'), typed(CONVENIENCE, 'p'));
    assertEquals(key('P'), typed(CONVENIENCE, 'P'), "only capital letters");
    assertEquals(shifted('B'), typed(CONVENIENCE, '*'));
    assertEquals(shifted('P'), typed(CONVENIENCE, '"'));
    assertEquals(shifted('U'), typed(CONVENIENCE, '$'));
    assertEquals(shifted('H'), typed(CONVENIENCE, '^'), "**");
    assertEquals(shifted(' '), typed(CONVENIENCE, '£'));
    assertEquals(shifted('.'), typed(CONVENIENCE, ','));
    assertEquals(key('.'), typed(CONVENIENCE, '.'));
    assertEquals(shifted('8'), typed(CONVENIENCE, '→'));
    assertEquals(0L, typed(CONVENIENCE, '@'));
  }

  @Test
  void controlKeysAreZx81Modes() {
    for (final KeyboardMode mode : KeyboardMode.values()) {
      assertEquals(shifted('1'), typed(mode, 0x05), "Ctrl+E is EDIT");
      assertEquals(shifted('\n'), typed(mode, 0x06), "Ctrl+F is FUNCTION");
      assertEquals(shifted('9'), typed(mode, 0x07), "Ctrl+G is GRAPHICS");
    }
  }

  @Test
  void gamingArrowsAreUnshiftedDigits() {
    assertEquals(key('5'), typed(GAMING, '←'));
    assertEquals(key('6'), typed(GAMING, '↓'));
    assertEquals(key('7'), typed(GAMING, '↑'));
    assertEquals(key('8'), typed(GAMING, '→'));
    assertEquals(shifted('A'), typed(GAMING, 'A'), "otherwise native");
  }

  @Test
  void modesCycle() {
    assertEquals(NATIVE, CONVENIENCE.next());
    assertEquals(GAMING, NATIVE.next());
    assertEquals(CONVENIENCE, GAMING.next());
  }
}
