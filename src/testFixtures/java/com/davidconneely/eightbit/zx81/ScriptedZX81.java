package com.davidconneely.eightbit.zx81;

import com.davidconneely.eightbit.IBus;
import java.io.IOException;
import java.util.function.IntConsumer;
import java.util.function.LongSupplier;

/**
 * Runs the ZX81 emulator with keys typed from a script, and writes text dumps of the screen to a
 * transcript. Useful for trying out programs and keyboard handling without a terminal.
 *
 * <p>Script characters are typed in the native keyboard mode, as from a terminal with a US keyboard
 * (so {@code \n} is NEWLINE, uppercase letters are shifted, {@code P} types {@code "} and Tab makes
 * the next key shifted), one every {@code keyGapMillis} of emulated time. Some characters are
 * commands instead:
 *
 * <ul>
 *   <li>{@code ~} waits 1 second of emulated time.
 *   <li>{@code !} dumps the screen as the next TV frame starts, or after 0.1 seconds if there is no
 *       TV picture; {@code ?} writes only the dump's header line, straight away.
 *   <li>{@code [} and {@code ]} start and stop writing a line for each keyboard read.
 *   <li>{@code {x}} presses Ctrl and the digit or letter x, as at a terminal. So {@code {1}} to
 *       {@code {8}} set the speed to 1 to 8 times real speed, {@code {9}} runs as fast as possible,
 *       and {@code {k}} changes the keyboard mode. {@code {0}} freezes the emulator for good, as
 *       emulated time stops, so the script can't get past a wait.
 * </ul>
 *
 * <p>Typing starts after 2.5 seconds of emulated time, when the ROM is ready. Keys go into the
 * emulator's keyboard queue, so they are pressed when the program asks for them, which can take
 * longer than {@code keyGapMillis}. So {@code ~}, {@code !}, {@code ?} and the end of the script
 * first wait until the keys typed so far have been pressed and released, and the program has asked
 * for another key (so the ROM has finished with a line, for example). They wait for up to 10
 * seconds of emulated time, as a program might never ask. The run ends at the end of the script,
 * with a final screen dump. The script is followed even when the program doesn't read the keyboard.
 *
 * <p>Screen dumps show inverse characters as normal ones, except for the inverse graphics.
 */
public final class ScriptedZX81 {
  private static final long START_MILLIS = 2_500L;
  private static final long WAIT_MILLIS = 1_000L;
  private static final long MAX_KEYS_WAIT_MILLIS = 10_000L;
  private static final long MAX_FRAME_WAIT_MILLIS = 100L;
  private static final char END = '\0'; // the end of the script

  // ZX81 character codes $00-$3F as Unicode; inverse graphics ($80-$8A) are listed separately.
  private static final String CHARS =
      " ▘▝▀▖▌▞▛▒▒▒\"£$:?()><=+-*/;,.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String INVERSE_GRAPHICS = "█▟▙▄▜▐▚▗▒▒▒";

  private final String script;
  private final long keyGapMillis;
  private final StringBuilder transcript = new StringBuilder();
  private final ZX81Machine machine;
  private ScriptedKeyboard keyboard;
  private volatile boolean stopRequested;
  private boolean dumpPending; // whether to dump the screen as the next TV frame starts
  private long dumpDueMillis; // when to dump the screen anyway, if there is no TV picture

  /**
   * @param script keys to type, and commands.
   * @param keyGapMillis emulated time from typing one key to typing the next.
   */
  public ScriptedZX81(final String script, final long keyGapMillis) {
    this.script = script;
    this.keyGapMillis = keyGapMillis;
    this.machine =
        new ZX81Machine(
            new SilentTerminal(),
            (terminal, emulatedNanos, speedKey) ->
                keyboard = new ScriptedKeyboard(terminal, emulatedNanos, speedKey));
    this.machine.onFrameStarted(this::frameStarted);
  }

  /**
   * Run the emulator until the script ends, or {@link #stop} is called.
   *
   * @return the transcript of screen dumps.
   */
  public String run() throws IOException {
    machine.loadRom();
    try {
      machine.run();
    } catch (final ShutdownException ex) {
      // the script has ended, or stop() was called.
    }
    dumpScreen(false);
    return transcript.toString();
  }

  /** Ask the emulator to stop at its next screen refresh. Can be called from any thread. */
  public void stop() {
    stopRequested = true;
  }

  /** Emulated time so far, in nanoseconds. */
  public long emulatedNanos() {
    return machine.emulatedNanos();
  }

  /**
   * Press Ctrl and a digit or letter key, as at a terminal; for example, {@code ctrl('9')} runs as
   * fast as possible. Can be called from any thread.
   */
  public void ctrl(final char key) {
    keyboard.type(ctrlKey(key));
  }

  /** The key typed by Ctrl and a digit or letter key. */
  private static HostKey ctrlKey(final char key) {
    if (key >= '0' && key <= '9') {
      return HostKey.of(HostKey.MODIFIED_DIGIT_0 + key - '0');
    } else if (Character.isLetter(key)) {
      return HostKey.of(Character.toUpperCase(key) & 0x1F);
    }
    throw new IllegalArgumentException("Not a digit or letter: " + key);
  }

  private long emulatedMillis() {
    return machine.emulatedNanos() / 1_000_000L;
  }

  private int sysVar(final int address) {
    final IBus bus = machine.bus();
    return bus.rawReadMemByte(address) | (bus.rawReadMemByte(address + 1) << 8);
  }

  private void frameStarted() {
    if (dumpPending) {
      dumpPending = false;
      dumpScreen(false);
    }
  }

  private void dumpScreen(final boolean headerOnly) {
    transcript.append(
        "+---- line %4d  PC=%04x  t=%6dms  %s ----+%n"
            .formatted(
                sysVar(0x4007),
                machine.pc(),
                emulatedMillis(),
                machine.isShowingPicture() ? "picture" : "dimmed"));
    if (headerOnly) {
      return;
    }
    final IBus bus = machine.bus();
    int address = machine.displayFile();
    if (bus.rawReadMemByte(address) == 0x76) {
      ++address; // skip the display file's first NEWLINE
    }
    final StringBuilder line = new StringBuilder();
    for (int row = 0; row < 24; ++address) {
      final int ch = bus.rawReadMemByte(address);
      if (ch == 0x76) {
        transcript.append("|%-32s|%n".formatted(line));
        line.setLength(0);
        ++row;
      } else if ((ch & 0x80) != 0 && (ch & 0x3F) < INVERSE_GRAPHICS.length()) {
        line.append(INVERSE_GRAPHICS.charAt(ch & 0x3F));
      } else {
        line.append(CHARS.charAt(ch & 0x3F)); // other inverse characters are shown as normal
      }
    }
    transcript.append("+--------------------------------+%n".formatted());
  }

  /** A terminal that shows nothing, and has no keys. */
  private static final class SilentTerminal extends TerminalSupport {
    @Override
    void enableRawMode() {}

    @Override
    void reset() {}

    @Override
    HostKey readKey() {
      return null;
    }

    @Override
    void write(final int data) {}

    @Override
    void print(final String text) {}
  }

  /** Types the script's keys, and runs its commands, as the emulator reads the keyboard. */
  private final class ScriptedKeyboard extends TerminalKeyboard {
    private int position;
    private long nextAtMillis = START_MILLIS;
    private long keysDueMillis = -1L; // how long to wait for typed keys to be pressed, if waiting
    private boolean trace;

    ScriptedKeyboard(
        final TerminalSupport terminal,
        final LongSupplier emulatedNanos,
        final IntConsumer speedKey) {
      super(terminal, emulatedNanos, speedKey);
      mode(KeyboardMode.NATIVE);
    }

    @Override
    void start() {} // no terminal to read

    @Override
    boolean isShutdownRequested() {
      followScript(); // also at each screen refresh, in case the program stops reading the keyboard
      return stopRequested;
    }

    @Override
    int readKeyPortByte(final int portNum) {
      if (trace) {
        transcript.append(
            "T %d %04x %d%n".formatted(emulatedMillis(), machine.pc(), sysVar(0x4007)));
      }
      followScript();
      return super.readKeyPortByte(portNum);
    }

    /** Type the script's keys and run its commands, up to the current emulated time. */
    private void followScript() {
      final long now = emulatedMillis();
      if (dumpPending) {
        if (now < dumpDueMillis) {
          return; // wait for the dump, as the next TV frame starts
        }
        frameStarted(); // there is no TV picture, so dump the screen now
      }
      while (now >= nextAtMillis) {
        final char ch = position < script.length() ? script.charAt(position) : END;
        if (waitsForKeys(ch) && !isReadyForKeys()) {
          if (keysDueMillis < 0L) {
            keysDueMillis = now + MAX_KEYS_WAIT_MILLIS;
          }
          if (now < keysDueMillis) {
            return; // wait until the program has finished with the keys typed so far
          }
        }
        keysDueMillis = -1L;
        ++position;
        switch (ch) {
          case END -> throw new ShutdownException("Script finished");
          case '~' -> nextAtMillis = now + WAIT_MILLIS;
          case '!' -> {
            dumpPending = true;
            dumpDueMillis = now + MAX_FRAME_WAIT_MILLIS;
            return;
          }
          case '?' -> dumpScreen(true);
          case '[' -> trace = true;
          case ']' -> trace = false;
          case '{' -> {
            if (position + 1 >= script.length() || script.charAt(position + 1) != '}') {
              throw new IllegalArgumentException("Expected {x} at " + (position - 1));
            }
            type(ctrlKey(script.charAt(position)));
            position += 2; // the key and '}'
          }
          default -> {
            type(ch);
            nextAtMillis = now + keyGapMillis;
          }
        }
      }
    }

    private static boolean waitsForKeys(final char ch) {
      return ch == '~' || ch == '!' || ch == '?' || ch == END;
    }
  }

  /**
   * Run a script and print the transcript.
   *
   * @param args the script ({@code \n} in it is NEWLINE), and optionally the emulated milliseconds
   *     between keys (default 150). For example, with Gradle: {@code ./gradlew -q runScript
   *     --args="'{9}10pP7 x 7 L PX7B7\nr\n~' 150"}.
   */
  public static void main(final String[] args) throws IOException {
    final String script = args[0].replace("\\n", "\n");
    final long keyGapMillis = args.length > 1 ? Long.parseLong(args[1]) : 150L;
    IO.print(new ScriptedZX81(script, keyGapMillis).run());
  }
}
