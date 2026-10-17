package com.davidconneely.eightbit.zx81;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class TerminalKeyboardTest {
  private static final int ROW_SHIFT = 0xFEFE; // SHIFT, Z, X, C, V
  private static final int ROW_1_TO_5 = 0xF7FE;
  private static final int ROW_0_TO_6 = 0xEFFE; // 0, 9, 8, 7, 6

  private long emulated = 0L;
  private int speed = -1;
  private final TerminalKeyboard keyboard =
      new TerminalKeyboard(null, () -> emulated, digit -> speed = digit);

  private void type(final int ch) {
    keyboard.type(HostKey.of(ch));
  }

  /** Ask for a key, and read the given row after the release time, when it is pressed. */
  private int readAfterRelease(final int portNum) {
    keyboard.readKeyPortByte(portNum); // the first keyboard read starts the release time
    emulated += KeyboardBuffer.RELEASE_NANOS;
    keyboard.ask();
    return keyboard.readKeyPortByte(portNum);
  }

  @Test
  void tabShiftsTheNextKey() {
    type('\t');
    assertTrue(keyboard.isStickyShift());
    type('4');
    assertFalse(keyboard.isStickyShift());
    assertEquals(0xFE, readAfterRelease(ROW_SHIFT), "SHIFT down");
    assertEquals(0xF7, keyboard.readKeyPortByte(ROW_1_TO_5), "4 down, for TO");
  }

  @Test
  void ctrlKChangesToTheNextMode() {
    assertEquals(KeyboardMode.CONVENIENCE, keyboard.mode());
    type(0x0B);
    assertEquals(KeyboardMode.NATIVE, keyboard.mode());
  }

  @Test
  void modifiedDigitSetsTheSpeedWithoutTypingIt() {
    type(HostKey.MODIFIED_DIGIT_0 + 2);
    assertEquals(2, speed);
    assertEquals(0xFF, readAfterRelease(ROW_1_TO_5));
  }

  @Test
  void gamingModeKeepsOnlyTheLatestKey() {
    keyboard.mode(KeyboardMode.GAMING);
    type('←'); // 5
    type('↓'); // 6
    type('↑'); // 7
    assertEquals(0xF7, readAfterRelease(ROW_0_TO_6), "7 down");
    assertEquals(0xFF, keyboard.readKeyPortByte(ROW_1_TO_5), "5 dropped");
  }

  @Test
  void otherModesQueueEveryKey() {
    type('5');
    type('7');
    assertEquals(0xEF, readAfterRelease(ROW_1_TO_5), "5 down first");
  }
}
