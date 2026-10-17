package com.davidconneely.eightbit.zx81;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class KeyboardBufferTest {
  private static final long KEY_Q = KeyboardBuffer.key(2, 0); // row $FB, bit 0
  private static final long KEY_A = KeyboardBuffer.key(1, 0); // row $FD, bit 0
  private static final long KEY_SHIFT = KeyboardBuffer.key(0, 0); // row $FE, bit 0
  private static final long KEY_Z = KeyboardBuffer.key(0, 1); // row $FE, bit 1
  private static final long KEY_SPACE = KeyboardBuffer.key(7, 0); // row $7F, bit 0
  private static final long MILLIS = 1_000_000L;
  private static final int HOLD_MILLIS = (int) (KeyboardBuffer.HOLD_NANOS / MILLIS);
  private static final int RELEASE_MILLIS = (int) (KeyboardBuffer.RELEASE_NANOS / MILLIS);

  private final KeyboardBuffer buffer = new KeyboardBuffer();
  private long emulated = 0L;

  private int read(final int portNum) {
    return buffer.readPort(portNum, emulated);
  }

  /** Let the first release time pass, so the next keyboard read can press a key. */
  private void startReleased() {
    read(0x00FE); // the first keyboard read starts the first release time
    emulated += KeyboardBuffer.RELEASE_NANOS;
  }

  /**
   * Ask for a key and read the given port once a millisecond, and return 'D' (key down) or 'u' for
   * each read.
   */
  private String poll(final int portNum, final int count) {
    final StringBuilder reads = new StringBuilder();
    for (int i = 0; i < count; ++i) {
      buffer.ask();
      reads.append(read(portNum) == 0xFF ? 'u' : 'D');
      emulated += MILLIS;
    }
    return reads.toString();
  }

  @Test
  void eachKeyIsHeldForHoldTimeThenReleasedForReleaseTime() {
    buffer.type(KEY_Q);
    buffer.type(KEY_A);
    final String expected =
        "u".repeat(RELEASE_MILLIS)
            + "D".repeat(HOLD_MILLIS)
            + "u".repeat(RELEASE_MILLIS + 1)
            + "D".repeat(HOLD_MILLIS)
            + "u";
    assertEquals(expected, poll(0xF9FE, expected.length())); // rows $FB and $FD
  }

  @Test
  void keyWaitsUntilTheProgramAsksForOne() {
    startReleased();
    buffer.type(KEY_Q);
    assertEquals(0xFF, read(0xFBFE), "not asked for yet");
    emulated += 10 * KeyboardBuffer.HOLD_NANOS; // busy, and not asking for a key
    buffer.ask();
    assertEquals(0xFE, read(0xFBFE), "Q down when asked for");
  }

  @Test
  void askingIsForTheNextReadOnly() {
    // A game that looks at the keyboard a few times a second: the next key is pressed when it
    // looks, rather than at a read by the ROM in between, which could release it before then.
    startReleased();
    buffer.type(KEY_Q);
    buffer.type(KEY_A);
    buffer.ask();
    assertEquals(0xFE, read(0xFBFE), "Q down when the game looks");
    emulated += KeyboardBuffer.HOLD_NANOS;
    assertEquals(0xFF, read(0xF9FE), "Q released");
    read(0x00FE); // the ROM's next read, a frame later, starts the release time
    emulated += 2 * KeyboardBuffer.RELEASE_NANOS;
    assertEquals(0xFF, read(0xF9FE), "A not pressed for a read by the ROM");
    buffer.ask();
    assertEquals(0xFE, read(0xFDFE), "A down when the game looks again");
  }

  @Test
  void pauseWaitsUntilAKeyIsPressed() {
    startReleased();
    buffer.pause();
    read(0x00FE);
    buffer.type(KEY_Q);
    assertEquals(0xFE, read(0xFBFE), "Q down, as PAUSE still waits for a key");
  }

  @Test
  void keyIsHeldWhetherOrNotItsRowIsRead() {
    startReleased();
    buffer.type(KEY_Q);
    buffer.ask();
    assertEquals(0xFF, read(0xFDFE), "Q pressed, but row $FB not read");
    assertEquals(0xFE, read(0xFBFE), "Q down");
    emulated += KeyboardBuffer.HOLD_NANOS;
    assertEquals(0xFF, read(0xFDFE), "Q released by a read of another row");
    assertEquals(0xFF, read(0xFBFE), "Q up");
  }

  @Test
  void releaseTimeStartsWithFirstReadAfterRelease() {
    startReleased();
    buffer.type(KEY_Q);
    buffer.type(KEY_A);
    buffer.ask();
    assertEquals(0xFE, read(0xFBFE), "Q down");
    emulated += KeyboardBuffer.HOLD_NANOS;
    assertEquals(0xFF, read(0xFBFE), "Q up");
    emulated += 10 * KeyboardBuffer.RELEASE_NANOS; // busy in FAST mode, with no keyboard reads
    buffer.ask();
    assertEquals(0xFF, read(0xFDFE), "A not yet down, as the release time starts now");
    emulated += KeyboardBuffer.RELEASE_NANOS;
    buffer.ask();
    assertEquals(0xFE, read(0xFDFE), "A down");
  }

  @Test
  void waitingForTheNextKeyReleasesTheHeldKey() {
    startReleased();
    buffer.type(KEY_Q);
    buffer.type(KEY_A);
    buffer.waitForKey();
    assertEquals(0xFE, read(0xFBFE), "Q down");
    emulated += 2 * 20 * MILLIS; // the ROM accepts Q after two frames, and waits for the next key
    buffer.waitForKey();
    assertEquals(0xFF, read(0xF9FE), "Q released, and the release time starts");
    emulated += KeyboardBuffer.RELEASE_NANOS;
    assertEquals(0xFE, read(0xFDFE), "A down");
  }

  @Test
  void readyForKeysOnceTheProgramAsksAfterTheLastKey() {
    startReleased();
    buffer.type(KEY_Q);
    buffer.ask();
    assertFalse(buffer.isReadyForKeys(), "Q queued");
    assertEquals(0xFE, read(0xFBFE), "Q down");
    assertFalse(buffer.isReadyForKeys(), "Q held down");
    emulated += KeyboardBuffer.HOLD_NANOS;
    assertEquals(0xFF, read(0xFBFE), "Q up");
    assertFalse(buffer.isReadyForKeys(), "not asked for another key yet");
    buffer.waitForKey();
    assertTrue(buffer.isReadyForKeys());
  }

  @Test
  void breakCheckPressesSpaceOnly() {
    startReleased();
    buffer.type(KEY_Q);
    buffer.breakCheck();
    assertEquals(0xFF, read(0xFBFE), "Q waits to be asked for");
    buffer.typeLatest(KEY_SPACE);
    buffer.breakCheck();
    assertEquals(0xFE, read(0x7FFE), "SPACE down for BREAK");
  }

  @Test
  void typingTheHeldKeyAgainHoldsItLonger() {
    startReleased();
    buffer.typeLatest(KEY_Q);
    buffer.ask();
    assertEquals(0xFE, read(0xFBFE), "Q down");
    emulated += KeyboardBuffer.HOLD_NANOS - MILLIS;
    buffer.typeLatest(KEY_Q); // the terminal repeats the key while it is held down
    assertEquals(0xFE, read(0xFBFE), "Q still down");
    emulated += KeyboardBuffer.HOLD_NANOS - MILLIS;
    assertEquals(0xFE, read(0xFBFE), "Q still down, after more than the hold time");
    emulated += MILLIS;
    assertEquals(0xFF, read(0xFBFE), "Q up");
  }

  @Test
  void typingTheHeldKeyAgainInOtherModesQueuesIt() {
    startReleased();
    buffer.type(KEY_Q);
    buffer.ask();
    assertEquals(0xFE, read(0xFBFE), "Q down");
    buffer.type(KEY_Q);
    emulated += KeyboardBuffer.HOLD_NANOS;
    assertEquals(0xFF, read(0xFBFE), "first Q up");
    emulated += KeyboardBuffer.RELEASE_NANOS;
    buffer.ask();
    assertEquals(0xFF, read(0xFBFE), "release time starts");
    emulated += KeyboardBuffer.RELEASE_NANOS;
    buffer.ask();
    assertEquals(0xFE, read(0xFBFE), "second Q down");
  }

  @Test
  void shiftedKeyHoldsShiftToo() {
    startReleased();
    buffer.type(KEY_SHIFT | KEY_Z);
    buffer.ask();
    assertEquals(0xFC, read(0xFEFE));
  }

  @Test
  void readingSeveralRowsCombinesThem() {
    startReleased();
    buffer.type(KEY_SHIFT | KEY_Q);
    buffer.ask();
    assertEquals(0xFE, read(0x00FE)); // all rows
    assertEquals(0xFF, read(0xFDFE)); // row $FD only
  }

  @Test
  void oddPortIsNotKeyboard() {
    startReleased();
    buffer.type(KEY_Q);
    buffer.ask();
    assertEquals(0xFF, read(0xFBFF));
  }
}
