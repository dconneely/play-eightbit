package com.davidconneely.eightbit.zx81;

import com.davidconneely.eightbit.IBus;
import com.davidconneely.eightbit.z80.Core;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

public class ZX81Machine {
  // The Z80 runs at 3.25 MHz. The CPU core doesn't count T-states, so instructions are timed on
  // average (0.145 instructions per T-state), except for the NOPs executed in the display file and
  // by HALT, which take 4 T-states.
  private static final long INSTRUCTION_NANOS = 2_122L;
  private static final long NOP_NANOS = 1_231L;

  // The TV scan line is 207 T-states; the NMI generator interrupts once a line while it is on.
  private static final long SCAN_LINE_NANOS = 63_692L;

  // The picture is shown while the CPU has run the display file within this time (2.5 frames).
  private static final long MAX_PICTURE_GAP_NANOS = 50_000_000L;

  private static final long MAX_THROTTLE_LAG_NANOS = 100_000_000L;

  static final String ROM_RESOURCE = "/z80/zx81/zx81.rom";

  /** Speed that stops running instructions. */
  static final int FROZEN = 0;

  /** Speed that runs instructions as fast as possible. */
  static final int UNTHROTTLED = -1;

  private final TerminalSupport terminal;
  private final TerminalDisplay display;
  private final TerminalKeyboard keyboard;
  private final ZX81Bus bus;
  private final Core core;

  private long instructions;
  private long emulatedNanos;
  private long nextNmiNanos;
  private long pictureNanos = -MAX_PICTURE_GAP_NANOS; // when the display file last ran
  private boolean newFrame; // whether the display file hasn't run since the last vertical sync
  private int displayFile = -1; // where the display file ran from in the last frame
  private volatile int speed = 1; // multiple of real speed, FROZEN or UNTHROTTLED
  private Runnable frameStarted = () -> {};

  ZX81Machine() {
    this(TerminalSupport.get(), TerminalKeyboard::new);
  }

  /**
   * @param terminal where the display is shown.
   * @param keyboardFactory makes the keyboard, given this machine's terminal, emulated time and
   *     speed control.
   */
  ZX81Machine(final TerminalSupport terminal, final TerminalKeyboard.Factory keyboardFactory) {
    this.terminal = terminal;
    this.display = new TerminalDisplay(terminal);
    this.keyboard = keyboardFactory.create(terminal, this::emulatedNanos, this::speedKey);
    this.bus = new ZX81Bus(this.keyboard);
    this.core = new Core(this.bus);
  }

  private static final String USAGE =
      "Usage: java -jar eightbit.jar [--keyboard=convenience|native|gaming]";

  public static void main(final String[] args) throws IOException {
    KeyboardMode mode = KeyboardMode.CONVENIENCE;
    for (final String arg : args) {
      try {
        if (!arg.startsWith("--keyboard=")) {
          throw new IllegalArgumentException(arg);
        }
        mode = KeyboardMode.valueOf(arg.substring(11).toUpperCase(Locale.ROOT));
      } catch (final IllegalArgumentException ex) {
        System.err.println(USAGE);
        System.exit(2);
      }
    }
    final ZX81Machine zx81 = new ZX81Machine();
    zx81.keyboard.mode(mode);
    zx81.loadRom();
    try {
      zx81.init();
      zx81.run();
    } catch (final ShutdownException ex) {
      // do nothing.
    } finally {
      zx81.reset();
    }
  }

  private void init() {
    terminal.enableRawMode();
    display.init();
    keyboard.start();
  }

  private void reset() {
    display.reset();
    terminal.reset();
  }

  /** Time on the emulated machine, from the instructions executed. */
  long emulatedNanos() {
    return emulatedNanos;
  }

  /**
   * Whether the ZX81 is producing a TV picture, by running the display file. It always is in SLOW
   * mode; in FAST mode, only while the ROM waits for a key or in PAUSE.
   */
  boolean isShowingPicture() {
    return emulatedNanos - pictureNanos < MAX_PICTURE_GAP_NANOS;
  }

  /**
   * Address of the display file shown on the TV: where the CPU started running it in the last
   * frame, or D_FILE until it has run. Usually these are the same, but some games run their own
   * display file.
   */
  int displayFile() {
    return displayFile >= 0 ? displayFile : bus.cpuReadMemWord(0x400C);
  }

  /** Whether the ROM is in SLOW mode (compute and display), from the system variable CDFLAG. */
  private boolean isSlowMode() {
    return (bus.rawReadMemByte(0x403B) & 0x80) != 0;
  }

  /**
   * Set the emulation speed.
   *
   * @param speed multiple of real speed, {@link #FROZEN} or {@link #UNTHROTTLED}.
   */
  void speed(final int speed) {
    this.speed = speed;
  }

  /**
   * Set the emulation speed from a digit key: 1 is real speed, 2 to 8 are that many times faster, 9
   * is unthrottled and 0 is frozen.
   */
  void speedKey(final int digit) {
    speed(
        switch (digit) {
          case 0 -> FROZEN;
          case 9 -> UNTHROTTLED;
          default -> digit;
        });
  }

  /** Run an action each time a TV frame starts, as the CPU starts to run the display file. */
  void onFrameStarted(final Runnable action) {
    frameStarted = action;
  }

  IBus bus() {
    return bus;
  }

  int pc() {
    return core.state().pc();
  }

  private static String describeSpeed(final int speed) {
    return switch (speed) {
      case FROZEN -> "frozen";
      case UNTHROTTLED -> "unthrottled";
      default -> "x" + speed;
    };
  }

  void run() throws IOException {
    long lastRender = System.currentTimeMillis();
    long rateStart = lastRender; // the rates shown are averages over about a second
    long rateInstructions = 0L;
    long rateFrames = 0L;
    double kips = 0.0;
    double fps = 0.0;
    int throttledSpeed = FROZEN; // the speed when the throttle times were taken
    long throttleRealNanos = 0L;
    long throttleEmulatedNanos = 0L;
    core.state().pc(0);
    while (true) {
      final long now = System.currentTimeMillis();
      if (now - lastRender >= 19L) {
        if (keyboard.isShutdownRequested()) {
          throw new ShutdownException("Closing emulator, as Ctrl+C was pressed or input ended");
        }
        ++rateFrames;
        if (now - rateStart >= 1000L) {
          kips = (instructions - rateInstructions) * 1.0 / (now - rateStart);
          fps = rateFrames * 1000.0 / (now - rateStart);
          rateStart = now;
          rateInstructions = instructions;
          rateFrames = 0L;
        }
        display.state1(
            "PC=0x%04x  |%6.1f kIPS  |%6.1f fps  | %s  | %s"
                .formatted(
                    core.state().pc(),
                    kips,
                    fps,
                    isSlowMode() ? "SLOW" : "FAST",
                    describeSpeed(speed)));
        display.state2(
            "%s keyboard (Ctrl+K to change)%s"
                .formatted(
                    keyboard.mode().name().toLowerCase(Locale.ROOT),
                    keyboard.isStickyShift() ? "  |  SHIFT" : ""));
        display.renderDFile(bus, displayFile(), isShowingPicture());
        lastRender = now;
      }
      final int speed = this.speed;
      if (speed == FROZEN) {
        throttledSpeed = FROZEN;
        sleep(1L);
        continue;
      }
      final int pcBefore = core.state().pc();
      trackKeyboardUse(pcBefore);
      core.step();
      ++instructions;
      final boolean nop = pcBefore > 0x7FFF || core.state().halted(); // display file, or HALT
      if (bus.takeVsync()) {
        newFrame = true;
      }
      if (pcBefore > 0x7FFF) {
        pictureNanos = emulatedNanos;
        if (newFrame) {
          newFrame = false;
          displayFile = pcBefore & 0x7FFF;
          frameStarted.run();
        }
      }
      emulatedNanos += nop ? NOP_NANOS : INSTRUCTION_NANOS;
      final int pc = core.state().pc();
      if (pc == 0x0343) {
        if (core.state().cf()) {
          core.state().pc(0x02F4); // LOAD requires a filename in this emulator.
        } else if (loadProgram(core.state().de())) {
          core.state().pc(0x0207); // finish as LOAD does, with SLOW/FAST, which returns from LOAD
        } else {
          core.state().pc(0x03A6); // report D (BREAK), as when a real ZX81 can't find a program
        }
      }
      if (!bus.isNmiGeneratorOn()) {
        nextNmiNanos = emulatedNanos; // the first NMI comes as soon as the generator is on
      }
      if (bus.isNmiGeneratorOn() && emulatedNanos >= nextNmiNanos) {
        core.nmi();
        nextNmiNanos = emulatedNanos + SCAN_LINE_NANOS;
      } else if ((core.state().r() & 0x40) == 0) {
        // The ZX81 interrupts when address line A6 is low as R is put on the address bus, so
        // when bit 6 of R is 0; this ends each scan line of the display file.
        core.irq();
      }
      if (speed == UNTHROTTLED) {
        throttledSpeed = UNTHROTTLED; // so the throttle times are taken again after this
      } else if ((instructions & 0x3FF) == 0L) {
        final long realNanos = System.nanoTime();
        if (speed != throttledSpeed) {
          throttledSpeed = speed;
          throttleRealNanos = realNanos;
          throttleEmulatedNanos = emulatedNanos;
        }
        final long aheadNanos =
            (emulatedNanos - throttleEmulatedNanos) / speed - (realNanos - throttleRealNanos);
        if (aheadNanos >= 1_000_000L) {
          sleep(aheadNanos / 1_000_000L);
        } else if (aheadNanos < -MAX_THROTTLE_LAG_NANOS) { // don't rush to catch up
          throttleRealNanos = realNanos;
          throttleEmulatedNanos = emulatedNanos;
        }
      }
    }
  }

  /**
   * Tell the bus whether the ROM is running, and tell the keyboard when the ROM asks for a key:
   * when it waits for a key (DISPLAY-6), in PAUSE (after FIND-INT, as some programs call it there),
   * when anything but the display routine calls KEYBOARD (for example, INKEY$ and many games), and
   * when it checks for BREAK (BREAK-1).
   */
  private void trackKeyboardUse(final int pc) {
    bus.romRunning(pc < 0x2000);
    switch (pc) {
      case 0x04C1 -> keyboard.waitForKey();
      case 0x0F35 -> keyboard.pause();
      case 0x02BB -> {
        if (bus.cpuReadMemWord(core.state().sp()) != 0x0241) { // not called by the display routine
          keyboard.ask();
        }
      }
      case 0x0F46 -> keyboard.breakCheck();
      default -> {}
    }
  }

  private static void sleep(final long millis) {
    try {
      Thread.sleep(millis);
    } catch (final InterruptedException ex) {
      /*do nothing.*/
    }
  }

  void loadRom() throws IOException {
    try (var in = ZX81Machine.class.getResourceAsStream(ROM_RESOURCE)) {
      if (in == null) {
        throw new FileNotFoundException("ZX81 ROM not found at src/main/resources" + ROM_RESOURCE);
      }
      var rom = in.readAllBytes();
      bus.rawWriteMemBytes(0, rom, 0, rom.length);
    }
  }

  /** Load a program from a resource, and return whether it could be read. */
  private boolean loadProgram(int addressFilename) {
    final String resource = "/z80/zx81/" + getFilename(addressFilename) + ".p";
    final byte[] program;
    try (var in = getClass().getResourceAsStream(resource)) {
      if (in == null) {
        return false;
      }
      program = in.readAllBytes();
    } catch (final IOException ex) {
      return false; // for example, the jar file has been replaced while the emulator runs
    }
    bus.rawWriteMemBytes(16393, program, 0, program.length);
    return true;
  }

  private String getFilename(int addressFilename) {
    StringBuilder sb = new StringBuilder();
    int ch07;
    do {
      ch07 = bus.rawReadMemByte(addressFilename++);
      int ch06 = (ch07 & 0x7F);
      if (ch06 >= 38 && ch06 <= 63) { // a-z
        sb.append((char) (ch06 - 38 + 'a'));
      } else if (ch06 >= 28 && ch06 <= 37) { // 0-9
        sb.append((char) (ch06 - 28 + '0'));
      } else {
        sb.append(
            switch (ch06) { // no '.' or '/' to avoid escaping resources folder
              case 16 -> '(';
              case 17 -> ')';
              case 22 -> '-';
              default -> '_';
            });
      }
    } while ((ch07 & 0x80) == 0);
    return sb.toString();
  }
}
