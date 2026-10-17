package com.davidconneely.eightbit.zx81;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

final class PosixTerminalSupportTest {
  /** Read all the keys typed in the given input, as their characters. */
  private static String keys(final String typed) throws IOException {
    final InputStream in = new ByteArrayInputStream(typed.getBytes(StandardCharsets.UTF_8));
    final StringBuilder keys = new StringBuilder();
    HostKey key;
    while ((key = PosixTerminalSupport.readKey(in)) != null) {
      keys.appendCodePoint(key.ch() >= HostKey.MODIFIED_DIGIT_0 ? '#' : key.ch());
    }
    return keys.toString();
  }

  @Test
  void charactersAreKeys() throws IOException {
    assertEquals("ab\r", keys("ab\r"));
  }

  @Test
  void utf8CharacterIsOneKey() throws IOException {
    assertEquals("£", keys("£"));
  }

  @Test
  void arrowKeysAreArrows() throws IOException {
    assertEquals("↑↓→←", keys("\u001B[A\u001B[B\u001B[C\u001BOD"));
  }

  @Test
  void modifiedArrowKeysAreArrows() throws IOException {
    assertEquals("→", keys("\u001B[1;5C"));
  }

  @Test
  void otherSequencesAreReadToTheirEnd() throws IOException {
    assertEquals("\0a\0b", keys("\u001B[15~a\u001BOPb")); // F5, then F1
  }

  @Test
  void altWithDigitIsModifiedDigit() throws IOException {
    final InputStream in = new ByteArrayInputStream(new byte[] {27, '3'});
    assertEquals(HostKey.MODIFIED_DIGIT_0 + 3, PosixTerminalSupport.readKey(in).ch());
    assertNull(PosixTerminalSupport.readKey(in));
  }

  @Test
  void escapeByItselfIsEscape() throws IOException {
    assertEquals("\u001B", keys("\u001B"));
  }
}
