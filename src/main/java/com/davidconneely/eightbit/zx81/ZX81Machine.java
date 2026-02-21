package com.davidconneely.eightbit.zx81;

import com.davidconneely.eightbit.IBus;
import com.davidconneely.eightbit.z80.Core;
import java.io.IOException;

public class ZX81Machine {
  private final TerminalSupport terminal;
  private final TerminalDisplay display;
  private final TerminalKeyboard keyboard;
  private final IBus bus;
  private final Core core;

  ZX81Machine() {
    this.terminal = TerminalSupport.get();
    this.display = new TerminalDisplay(terminal);
    this.keyboard = new TerminalKeyboard(terminal);
    this.bus = new ZX81Bus(this.keyboard);
    this.core = new Core(this.bus);
  }

  public static void main(final String[] args) throws IOException {
    final ZX81Machine zx81 = new ZX81Machine();
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
  }

  private void reset() {
    display.reset();
    terminal.reset();
  }

  private void run() throws IOException {
    final long startup = System.currentTimeMillis();
    long instructions = 0L;
    long frames = 0L;
    long lastRender = startup;
    core.state().pc(0);
    while (true) {
      core.step();
      ++instructions;
      final int pc = core.state().pc();
      if (pc == 0x0343) {
        if (core.state().cf()) {
          core.state().pc(0x02F4); // LOAD requires a filename in this emulator.
        } else {
          loadProgram(core.state().de());
          core.state().pc(bus.cpuReadMemWord(core.state().spInc2()));
        }
      }
      if (pc > 0x7FFF) { // executing display file, we're probably waiting on user input.
        // effective RET.
        core.state().pc(bus.cpuReadMemWord(core.state().spInc2()));
      }
      final long now = System.currentTimeMillis();
      if (now - lastRender >= 19L) {
        ++frames;
        display.state1(
            "PC=0x%04x  |%6.1f kIPS  |%6.1f fps"
                .formatted(
                    pc, instructions * 1.0 / (now - startup), frames * 1000.0 / (now - startup)));
        display.renderDFile(bus);
        lastRender = now;
      }
      // Z80 at 3.25MHz, assuming 0.145 instr/cycle, gives 0.47125 MIPS.
      // Slow mode spends 80% of time in display code, so adjust 275L below until you get around for
      // 94.25 instr/ms.
      if (instructions % 275L == 0L) {
        try {
          Thread.sleep(1);
        } catch (final InterruptedException ex) {
          /*do nothing.*/
        }
      }
    }
  }

  private void loadRom() throws IOException {
    var rom = getClass().getResourceAsStream("/z80/zx81/zx81.rom").readAllBytes();
    bus.rawWriteMemBytes(0, rom, 0, rom.length);
  }

  private void loadProgram(int addressFilename) throws IOException {
    var program =
        getClass()
            .getResourceAsStream("/z80/zx81/" + getFilename(addressFilename) + ".p")
            .readAllBytes();
    bus.rawWriteMemBytes(16393, program, 0, program.length);
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
