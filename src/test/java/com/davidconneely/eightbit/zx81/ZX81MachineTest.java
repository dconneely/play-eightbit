package com.davidconneely.eightbit.zx81;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Runs the ZX81 ROM, so these tests are skipped if it hasn't been downloaded. */
final class ZX81MachineTest {
  private static final long MILLIS = 1_000_000L;

  @BeforeEach
  void romIsAvailable() {
    assumeTrue(
        ZX81Machine.class.getResource(ZX81Machine.ROM_RESOURCE) != null, "ZX81 ROM not found");
  }

  @Test
  void typedProgramRuns() throws Exception {
    // Typed in the native keyboard mode. At the K cursor, f is FOR, p is PRINT, n is NEXT and r is
    // RUN. With Shift, L is =, $ (Shift+4) is TO, P is ", X is ; and B is *.
    // {9} runs it unthrottled, and each ~ waits a second (of emulated time) for it to finish.
    final String script = "{9}10fiL1$12\n20piXP x 7 L PXiB7\n30ni\nr\n~~~~~~~~~~";
    final String screen = new ScriptedZX81(script, 150L).run();
    for (int i = 1; i <= 12; ++i) {
      assertTrue(screen.contains("|%d X 7 = %d ".formatted(i, i * 7)), screen);
    }
    assertTrue(screen.contains("|0/30 "), screen);
  }

  @Test
  void fastModeShowsNoPictureWhileRunning() throws Exception {
    // F (Shift+F) is FAST and D (Shift+D) is SLOW. Each ? writes a header line, which says whether
    // there is a picture: while running in FAST mode, after it stops, and while running in SLOW.
    final String script = "{9}F\n10fiL1$300\n20ni\nr\n?~~~~~~~~~~?D\nr\n?~~~~~~~~~~";
    final List<String> headers =
        new ScriptedZX81(script, 150L).run().lines().filter(l -> l.startsWith("+---- ")).toList();
    assertTrue(headers.get(0).endsWith(" dimmed ----+"), headers.get(0));
    assertTrue(headers.get(1).endsWith(" picture ----+"), headers.get(1));
    assertTrue(headers.get(2).endsWith(" picture ----+"), headers.get(2));
  }

  @Test
  void spaceBreaksIntoAProgramInEitherMode() throws Exception {
    // At the K cursor, g is GOTO. F (Shift+F) is FAST and D (Shift+D) is SLOW.
    for (final String mode : List.of("F", "D")) {
      final String screen = new ScriptedZX81("{9}" + mode + "\n10g10\nr\n~~ ~~", 150L).run();
      assertTrue(screen.contains("|D/10 "), mode + "\n" + screen);
    }
  }

  @Test
  void dumpWaitsForTheTypedKeys() throws Exception {
    // The ROM takes keys more slowly than they are typed, and is busy after NEWLINE, so ! waits
    // until it has entered the line and listed it.
    final String screen = new ScriptedZX81("{9}10pPabcdefghijklmnopqrstP\n!", 150L).run();
    final String dump = screen.substring(0, screen.indexOf("+----", 1));
    assertTrue(dump.contains("|  10>PRINT \"ABCDEFGHIJKLMNOPQRST|"), dump);
  }

  @Test
  void loadingAMissingProgramReportsBreak() throws Exception {
    // At the K cursor, j is LOAD, and with Shift, P is ".
    final String screen = new ScriptedZX81("{9}jPno such programP\n~", 150L).run();
    assertTrue(screen.contains("|D/0 "), screen);
  }

  @Test
  void speedKeysChangeHowFastTheEmulatorRuns() throws Exception {
    final ScriptedZX81 zx81 = new ScriptedZX81("~".repeat(1_000), 150L);
    zx81.ctrl('0');
    final Thread thread = Thread.ofPlatform().start(this.run(zx81));
    try {
      Thread.sleep(200L);
      assertEquals(0L, zx81.emulatedNanos(), "frozen from the start");

      zx81.ctrl('1');
      final long realSpeed = emulatedNanosIn(zx81, 500L);
      assertTrue(realSpeed > 250 * MILLIS && realSpeed < 1_000 * MILLIS, "x1: " + realSpeed);

      zx81.ctrl('0');
      Thread.sleep(50L);
      assertEquals(0L, emulatedNanosIn(zx81, 200L), "frozen");

      zx81.ctrl('9');
      final long unthrottled = emulatedNanosIn(zx81, 500L);
      assertTrue(unthrottled > 2 * realSpeed, "unthrottled: " + unthrottled);

      zx81.ctrl('1'); // doesn't wait for real time to catch up with the time run unthrottled
      final long realAgain = emulatedNanosIn(zx81, 500L);
      assertTrue(realAgain > 250 * MILLIS && realAgain < 1_000 * MILLIS, "x1 again: " + realAgain);
    } finally {
      zx81.stop();
      thread.join();
    }
  }

  private Runnable run(final ScriptedZX81 zx81) {
    return () -> {
      try {
        zx81.run();
      } catch (final Exception ex) {
        throw new IllegalStateException(ex);
      }
    };
  }

  private static long emulatedNanosIn(final ScriptedZX81 zx81, final long realMillis)
      throws InterruptedException {
    final long start = zx81.emulatedNanos();
    Thread.sleep(realMillis);
    return zx81.emulatedNanos() - start;
  }
}
